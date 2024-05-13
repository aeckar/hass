package kanary

import kanary.TypeCode.*
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

private val EMPTY_ISTREAM = InputStream.nullInputStream()
private val EMPTY_OSTREAM = OutputStream.nullOutputStream()

/**
 * @return a new deserializer capable of reading primitives, primitive arrays, strings, and
 * instances of any type with a defined protocol from Kanary format
 */
fun InputStream.deserializer(protocols: Schema = Schema.EMPTY): ExhaustibleDeserializer = InputDeserializer(this, protocols)

/**
 * Reads serialized data from a stream in Kanary format.
 */
sealed interface Deserializer {
    fun readBoolean(): Boolean
    fun readByte(): Byte
    fun readChar(): Char
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun <T> read(): T

    companion object {
        internal val EMPTY: ExhaustibleDeserializer = InputDeserializer(EMPTY_ISTREAM, Schema.EMPTY)
    }
}

/**
 * Reads serialized data from a stream in Kanary format.
 * Does not need to be closed so long as the underlying stream is closed.
 * Calling [close] also closes the underlying stream.
 * Until closed, instances are blocking.
 */
sealed interface ExhaustibleDeserializer : Deserializer, Closeable {
    fun isExhausted(): Boolean
    fun isNotExhausted(): Boolean
}

// Each instance is used to read a single OBJECT
/**
 * Deserializer allowing extraction of data from supertypes with
 * a defined [write operation][ProtocolBuilder.write].
 */
class PolymorphicDeserializer internal constructor(
    private val obj: InputDeserializer,
    private val packets: Map<JvmClass, ExhaustibleDeserializer>,
) : ExhaustibleDeserializer by obj {
    private val classRef = Class.forName(obj.readStringNoValidate()).kotlin

    /**
     * A deserializer corresponding to the data serialized by the immediate superclass.
     * If the superclass does not have a defined write operation, is assigned a deserializer containing no data.
     */
    val superclass: ExhaustibleDeserializer by lazy {
        classRef.supertypes
            .first().jvmErasure
            .takeIf { !it.isAbstract }?.let { supertype(it) } ?: Deserializer.EMPTY
    }

    /**
     * @return a deserializer corresponding to the data serialized by given supertype.
     * If the supertype does not have a defined write operation, returns a deserializer containing no data.
     * @throws MalformedProtocolException [T] is not a supertype
     */
    inline fun <reified T : Any> supertype() = supertype(T::class)

    @PublishedApi
    internal fun supertype(jvmClass: JvmClass): ExhaustibleDeserializer {
        return packets[jvmClass] ?: if (jvmClass.isSuperclassOf(classRef)) {
            Deserializer.EMPTY
        } else {
            throw MalformedProtocolException(classRef, "type '${jvmClass.qualifiedName}' is not a supertype")
        }
    }

    internal fun readObject(): Any {
        return obj.protocols.allProtocols.getValue(classRef).read?.invoke(this)
            ?: throw MalformedProtocolException(classRef, "read operation not found")
    }
}

internal class InputDeserializer(
    private var stream: InputStream,
    internal val protocols: Schema
) : ExhaustibleDeserializer, Closeable {
    override fun isExhausted() = stream.available() == 0
    override fun isNotExhausted() = stream.available() != 0

    override fun readBoolean(): Boolean {
        BOOLEAN.validate(stream)
        return readBooleanNoValidate()
    }

    override fun readByte(): Byte {
        BYTE.validate(stream)
        return readByteNoValidate()
    }

    override fun readChar(): Char {
        CHAR.validate(stream)
        return readCharNoValidate()
    }

    override fun readShort(): Short {
        SHORT.validate(stream)
        return readShortNoValidate()
    }

    override fun readInt(): Int {
        INT.validate(stream)
        return readIntNoValidate()
    }

    override fun readLong(): Long {
        LONG.validate(stream)
        return readLongNoValidate()
    }

    override fun readFloat(): Float {
        FLOAT.validate(stream)
        return readFloatNoValidate()
    }

    override fun readDouble(): Double {
        DOUBLE.validate(stream)
        return readDoubleNoValidate()
    }

    /**
     * Reads an object of the specified type from binary according to the protocol of its type, or null respectively.
     * @throws TypeMismatchException the value was not serialized as a singular object or null
     * @throws TypeCastException the object is not an instance of type [T]
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> read() = readAny() as T

    override fun close() = stream.close()

    internal fun readStringNoValidate(): String {
        val size = readIntNoValidate()
        return String(stream.readNBytes(size))
    }

    private fun readTypeCode() = TypeCode.entries[stream.read()]

    private fun readBytesNoValidate(count: Int) = ByteBuffer.wrap(stream.readNBytes(count))

    private fun readBooleanNoValidate() = stream.read() == 1
    private fun readByteNoValidate() = stream.read().toByte()
    private fun readCharNoValidate() = readBytesNoValidate(Char.SIZE_BYTES).char
    private fun readShortNoValidate() = readBytesNoValidate(Short.SIZE_BYTES).short
    private fun readIntNoValidate() = readBytesNoValidate(Int.SIZE_BYTES).int
    private fun readLongNoValidate() = readBytesNoValidate(Long.SIZE_BYTES).long
    private fun readFloatNoValidate() = readBytesNoValidate(Float.SIZE_BYTES).float
    private fun readDoubleNoValidate() = readBytesNoValidate(Double.SIZE_BYTES).double

    private fun readAny(): Any? {
        val code = readTypeCode()
        builtInReads[code]?.let { return it(this) }
        assert(code === OBJECT)
        val packetCount = stream.read()
        val obj: ArrayOutputStream
        if (packetCount == 0) {
            obj = ArrayOutputStream(readIntNoValidate()).apply { acceptNBytes(stream, bytes.size) }
            return PolymorphicDeserializer(InputDeserializer(obj.asInputStream(), protocols), emptyMap()).readObject()
        }
        val serializer = OutputSerializer(EMPTY_OSTREAM, protocols)
        val packets = HashMap<JvmClass, ExhaustibleDeserializer>(packetCount)
        repeat(packetCount) {
            val packetSize = readIntNoValidate()
            val packetCode = readTypeCode()
            val packet = ArrayOutputStream(packetSize)
            val jvmClass: JvmClass
            packet.write(packetCode.ordinal)
            if (packetCode in builtInReads) {
                jvmClass = packetCode.jvmClass
            } else {
                assert(packetCode === OBJECT)
                val className = readStringNoValidate()
                jvmClass = Class.forName(className).kotlin
                serializer.wrap(packet).writeStringNoMark(className)  // stateful
            }
            packet.acceptNBytes(stream, packetSize)
            packets[jvmClass] = InputDeserializer(packet.asInputStream(), protocols)
        }
        obj = ArrayOutputStream(readIntNoValidate()).apply { acceptNBytes(stream, bytes.size) }
        return PolymorphicDeserializer(InputDeserializer(obj.asInputStream(), protocols), packets).readObject()
    }

    companion object {
        private val builtInReads = mapOf(
            read(UNIT) {},
            read(NULL) {
                null
            },
            read(BOOLEAN) {
                readBooleanNoValidate()
            },
            read(BYTE) {
                readByteNoValidate()
            },
            read(CHAR) {
                readCharNoValidate()
            },
            read(SHORT) {
                readShortNoValidate()
            },
            read(INT) {
                readIntNoValidate()
            },
            read(LONG) {
                readLongNoValidate()
            },
            read(FLOAT) {
                readFloatNoValidate()
            },
            read(DOUBLE) {
                readDoubleNoValidate()
            },
            read(BOOLEAN_ARRAY) {
                BooleanArray(readIntNoValidate()) { stream.read() == 1 }
            },
            read(BYTE_ARRAY) {
                ByteArray(readIntNoValidate()) { stream.read().toByte() }
            },
            read(CHAR_ARRAY) {
                val size = readIntNoValidate()
                readBytesNoValidate(size*Char.SIZE_BYTES).asCharBuffer().array()
            },
            read(SHORT_ARRAY) {
                val size = readIntNoValidate()
                readBytesNoValidate(size*Short.SIZE_BYTES).asShortBuffer().array()
            },
            read(INT_ARRAY) {
                val size = readIntNoValidate()
                readBytesNoValidate(size*Int.SIZE_BYTES).asIntBuffer().array()
            },
            read(LONG_ARRAY) {
                val size = readIntNoValidate()
                readBytesNoValidate(size*Long.SIZE_BYTES).asLongBuffer().array()
            },
            read(FLOAT_ARRAY) {
                val size = readIntNoValidate()
                readBytesNoValidate(size*Float.SIZE_BYTES).asFloatBuffer().array()
            },
            read(DOUBLE_ARRAY){
                val size = readIntNoValidate()
                readBytesNoValidate(size*Double.SIZE_BYTES).asDoubleBuffer().array()
            },
            read(STRING) {
                readStringNoValidate()
            },
            read(OBJECT_ARRAY) {
                val size = readIntNoValidate()
                Array(size) { readAny() }
            },
            read(LIST) {
                val size = readIntNoValidate()
                val underlying = ArrayList<Any?>(size).apply {
                    repeat(size) { this += readAny() }
                }
                SerializedList(underlying)
            },
            read(ITERABLE) {
                val underlying = mutableListOf<Any?>().apply {
                    do {
                        this += readAny().takeUnless { it === Unit /* sentinel */ } ?: break
                    } while (true)
                }
                SerializedIterable(underlying)
            },
            read(PAIR) {
                val first = readAny()   // Ensure proper read order
                val second = readAny()
                Pair(first, second)
            },
            read(TRIPLE) {
                val first = readAny()
                val second = readAny()
                val third = readAny()
                Triple(first, second, third)
            },
            read(MAP_ENTRY) {
                val key = readAny()
                val value = readAny()
                SerializedMapEntry(key, value)

            },
            read(MAP) {
                val size = readIntNoValidate()
                val underlying = HashMap<Any?,Any?>(size).apply {
                    val key = readAny()
                    val value = readAny()
                    put(key, value)
                }
                SerializedMap(underlying)
            }
        )

        private fun read(code: TypeCode, read: InputDeserializer.() -> Any?) = code to read

        private class SerializedMapEntry(override val key: Any?, override val value: Any?) : Map.Entry<Any?,Any?>
        private class SerializedList(list: List<*>) : List<Any?> by list
        private class SerializedIterable(iter: Iterable<*>) : Iterable<Any?> by iter
        private class SerializedMap(map: Map<Any?,*>) : Map<Any?,Any?> by map
    }
}