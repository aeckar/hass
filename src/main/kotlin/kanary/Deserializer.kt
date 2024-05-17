package kanary

import kanary.TypeFlag.*
import java.io.*
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.superclasses

/**
 * @return a new deserializer capable of reading primitives, primitive arrays, strings, and
 * instances of any type with a defined protocol from Kanary format
 */
fun InputStream.deserializer(protocols: Schema = Schema.EMPTY) = InputDeserializer(this, protocols)

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
        val EMPTY: ExhaustibleDeserializer = InputDeserializer(InputStream.nullInputStream(), Schema.EMPTY)
    }
}

/**
 * Reads serialized data from a stream in Kanary format.
 * Can be used to determine whether there is more data that can be read from this object.
 */
sealed interface ExhaustibleDeserializer : Deserializer {
    fun isExhausted(): Boolean
    fun isNotExhausted(): Boolean
}

/**
 * Deserializer allowing extraction of data from supertypes with
 * a defined [write operation][ProtocolBuilder.write].
 */
class PolymorphicDeserializer internal constructor( // Each instance used to read a single OBJECT
    private val objStream: InputDeserializer,
    private val packets: Map<KClass<*>, ExhaustibleDeserializer>,
) : ExhaustibleDeserializer by objStream {
    private val classRef = KClass(objStream.readStringNoValidate())

    /**
     * A deserializer corresponding to the data serialized by the immediate superclass.
     * If the superclass does not have a defined write operation, is assigned a deserializer containing no data.
     */
    val superclass: ExhaustibleDeserializer by lazy {
        classRef.superclasses
            .first()
            .takeIf { !it.isAbstract }
            ?.let { supertype(it) } ?: Deserializer.EMPTY
    }

    /**
     * @return a deserializer corresponding to the data serialized by given supertype.
     * If the supertype does not have a defined write operation, returns a deserializer containing no data.
     * @throws MalformedProtocolException [T] is not a supertype
     */
    inline fun <reified T : Any> supertype() = supertype(T::class)

    @PublishedApi
    internal fun supertype(classRef: KClass<*>): ExhaustibleDeserializer {
        return packets[classRef] ?: if (classRef.isSuperclassOf(classRef)) {
            Deserializer.EMPTY
        } else {
            throw MalformedProtocolException(classRef,
                    "type '${classRef.qualifiedName ?: "<local or anonymous>"}' is not a supertype")
        }
    }

    internal fun resolveObject(): Any? {
        return objStream.schema.resolveRead(classRef)(this)
    }
}

/**
 * Reads serialized data from a stream in Kanary format.
 * Can be used to determine whether there is more data that can be read from this object.
 * Does not need to be closed so long as the underlying stream is closed.
 * Calling [close] also closes the underlying stream.
 */
class InputDeserializer(
    private var stream: InputStream,
    internal val schema: Schema
) : ExhaustibleDeserializer, Closeable {
    private val byteWrapper = ByteArray(1)

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
     * @throws TypeFlagMismatchException the value was not serialized as a singular object or null
     * @throws TypeCastException the object is not an instance of type [T]
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> read() = readObject() as T

    override fun close() = stream.close()

    internal fun readStringNoValidate(): String {
        val lengthInBytes = readIntNoValidate()
        return String(stream.readNBytesChecked(lengthInBytes))
    }

    private fun readTypeFlag() = TypeFlag.entries[stream.readChecked()]

    private fun readBytesToBuffer(count: Int) = ByteBuffer.wrap(stream.readNBytesChecked(count))

    private fun readBooleanNoValidate() = stream.readChecked() == 1
    private fun readByteNoValidate() = stream.readCheckedRaw()
    private fun readCharNoValidate() = readBytesToBuffer(Char.SIZE_BYTES).char
    private fun readShortNoValidate() = readBytesToBuffer(Short.SIZE_BYTES).short
    private fun readIntNoValidate() = readBytesToBuffer(Int.SIZE_BYTES).int
    private fun readLongNoValidate() = readBytesToBuffer(Long.SIZE_BYTES).long
    private fun readFloatNoValidate() = readBytesToBuffer(Float.SIZE_BYTES).float
    private fun readDoubleNoValidate() = readBytesToBuffer(Double.SIZE_BYTES).double

    @Suppress("UNCHECKED_CAST")
    private fun readObject(): Any? {
        val flag = readTypeFlag()
        builtInReads[flag]?.let { return it(this) }
        if (flag === SIMPLE_OBJECT) {
            val className = readStringNoValidate()
            return (schema.actualReads[KClass(className)] as? SimpleReadOperation<*>)?.invoke(this)
                ?: throw MissingOperationException("read operation or 'fallback' read operation for '$className' not found")
        }
        if (flag === FUNCTION) {
            return ObjectInputStream(stream).readObject()
        }
        assert(flag === OBJECT)
        val packetCount = stream.read()
        val objStream: ArrayOutputStream
        if (packetCount == 0) {
            val lengthInBytes = readIntNoValidate()
            objStream = ArrayOutputStream(lengthInBytes).apply { acceptNBytes(stream, lengthInBytes) }
            return PolymorphicDeserializer(InputDeserializer(objStream), emptyMap()).resolveObject()
        }
        val packets = HashMap<KClass<*>, ExhaustibleDeserializer>(packetCount)
        repeat(packetCount) {
            val lengthInBytes = readIntNoValidate()
            val packetFlag = readTypeFlag()
            val intermediate: ArrayOutputStream
            val kClass: KClass<*>
            if (packetFlag in builtInReads) {
                intermediate = ArrayOutputStream(lengthInBytes - 1 /* flag */)
                intermediate.write(packetFlag.ordinal)  // Users may read built-in superclass as itself
                kClass = packetFlag.kClass
            } else {
                assert(packetFlag === OBJECT)
                val stringLength = readIntNoValidate()
                val className = String(stream.readNBytesChecked(stringLength))
                intermediate = ArrayOutputStream(lengthInBytes - stringLength - 5 /* flag + strlen */)
                kClass = KClass(className)
            }
            intermediate.acceptNBytes(this.stream, intermediate.capacity)
            packets[kClass] = InputDeserializer(intermediate)
        }
        val lengthInBytes = readIntNoValidate()
        objStream = ArrayOutputStream(lengthInBytes).apply { acceptNBytes(stream, lengthInBytes) }
        return PolymorphicDeserializer(InputDeserializer(objStream), packets).resolveObject()
    }

    private fun readBuiltInList(): List<*> = builtInReads.getValue(LIST)() as List<*>

    private fun InputDeserializer(stream: ArrayOutputStream) = InputDeserializer(stream.asInputStream(), schema)

    // Allows reading of Byte's with possible value of -1
    private fun InputStream.readCheckedRaw(): Byte {
        val readSize = read(byteWrapper)
        if (readSize == -1) {
            throwEOF()
        }
        return byteWrapper.single()
    }

    private fun InputStream.readChecked() = read().also { if (it == -1) throwEOF() }
    private fun InputStream.readNBytesChecked(len: Int) = readNBytes(len).also { if (it.isEmpty()) throwEOF() }

    private fun throwEOF(): Nothing {
        throw EOFException("Attempted read of object after stream was exhausted." +
                "Ensure packets are not overridden by 'static' write operation")
    }

    private companion object {
        val builtInReads = linkedMapOf( // Preserve iteration order
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
                BooleanArray(readIntNoValidate()) { stream.readChecked() == 1 }
            },
            read(BYTE_ARRAY) {
                ByteArray(readIntNoValidate()) { stream.readCheckedRaw() }
            },
            read(CHAR_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size*Char.SIZE_BYTES).asCharBuffer()
                CharArray(buffer.remaining()).apply { buffer.get(this) }
            },
            read(SHORT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size*Short.SIZE_BYTES).asShortBuffer()
                ShortArray(buffer.remaining()).apply { buffer.get(this) }
            },
            read(INT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size*Int.SIZE_BYTES).asIntBuffer()
                IntArray(buffer.remaining()).apply { buffer.get(this) }
            },
            read(LONG_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size * Long.SIZE_BYTES).asLongBuffer()
                LongArray(buffer.remaining()).apply { buffer.get(this) }
            },
            read(FLOAT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size * Float.SIZE_BYTES).asFloatBuffer()
                FloatArray(buffer.remaining()).apply { buffer.get(this) }
            },
            read(DOUBLE_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size * Double.SIZE_BYTES).asDoubleBuffer()
                DoubleArray(buffer.remaining()).apply { buffer.get(this) }
            },
            read(STRING) {
                readStringNoValidate()
            },
            read(OBJECT_ARRAY) {
                val size = readIntNoValidate()
                Array(size) { readObject() }
            },
            read(LIST) {
                val size = readIntNoValidate()
                val list = ArrayList<Any?>(size).apply {
                    repeat(size) { this += readObject() }
                }
                object : List<Any?> by list {}  // Prevent modification by user
            },
            read(ITERABLE) {
                object : Iterable<Any?> by readBuiltInList() {}
            },
            read(PAIR) {
                val first = readObject()   // Ensure proper read order
                val second = readObject()
                Pair(first, second)
            },
            read(TRIPLE) {
                val first = readObject()
                val second = readObject()
                val third = readObject()
                Triple(first, second, third)
            },
            read(MAP_ENTRY) {
                val key = readObject()
                val value = readObject()
                object : Map.Entry<Any?,Any?> {
                    override val key get() = key
                    override val value get() = value
                }
            },
            read(MAP) {
                val size = readIntNoValidate()
                val map = HashMap<Any?,Any?>(size).apply {
                    repeat(size) {
                        val key = readObject()
                        val value = readObject()
                        put(key, value)
                    }
                }
                object : Map<Any?,Any?> by map {}
            }
        )

        fun read(flag: TypeFlag, read: InputDeserializer.() -> Any?) = flag to read
    }
}