package io.github.aeckar.kanary

import io.github.aeckar.kanary.io.TypeFlag
import io.github.aeckar.kanary.io.TypeFlag.*
import io.github.aeckar.kanary.io.Decoder
import io.github.aeckar.kanary.reflect.Callable
import io.github.aeckar.kanary.reflect.Type
import java.io.Closeable
import java.io.InputStream
import kotlin.reflect.full.IllegalCallableAccessException

/**
 * Performs an unchecked cast to [T], throwing [ObjectMismatchException] if the cast fails.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> Any?.matchCast(classRef: Type? = null): T {
    return try {
        this as T
    } catch (e: TypeCastException) {
        val protocolInfo = classRef?.let { " (in protocol of '$classRef')" } ?: ""
        throw ObjectMismatchException("${e.message}$protocolInfo")
    }
}

/**
 * Reads serialized data from a stream in Kanary format.
 *
 * Does not need to be closed so long as the underlying stream is closed.
 * Calling [close] also closes the underlying stream.
 */
class InputDeserializer internal constructor(
    stream: InputStream,
    internal val schema: Schema // Accessed by ObjectDeserializer
) : CollectionDeserializer(), Deserializer, Closeable {
    // Accessed by ObjectDeserializer, SupertypeDeserializer
    internal val decoder = Decoder(stream)

    // -------------------- public API --------------------

    override fun readBoolean(): Boolean {
        decoder.ensureTypeFlag(BOOLEAN)
        return decoder.decodeBoolean()
    }

    override fun readByte(): Byte {
        decoder.ensureTypeFlag(BYTE)
        return decoder.decodeByte()
    }

    override fun readChar(): Char {
        decoder.ensureTypeFlag(CHAR)
        return decoder.decodeChar()
    }

    override fun readShort(): Short {
        decoder.ensureTypeFlag(SHORT)
        return decoder.decodeShort()
    }

    override fun readInt(): Int {
        decoder.ensureTypeFlag(INT)
        return decoder.decodeInt()
    }

    override fun readLong(): Long {
        decoder.ensureTypeFlag(LONG)
        return decoder.decodeLong()
    }

    override fun readFloat(): Float {
        decoder.ensureTypeFlag(FLOAT)
        return decoder.decodeFloat()
    }

    override fun readDouble(): Double {
        decoder.ensureTypeFlag(DOUBLE)
        return decoder.decodeDouble()
    }

    override fun <T> read() = readObject().matchCast<T>()

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     */
    override fun close() = decoder.stream.close()

    // ------------------------------------------------------------------------

    // Accessed by SupertypeDeserializer
    internal fun readObject(flag: TypeFlag = decoder.decodeTypeFlag()): Any? {
        BuiltInReadOperations()[flag]?.let { return it() }
        // flag == OBJECT
        val classRef = decoder.decodeType()    // Serialized as string data
        val totalSupers = decoder.decodeRawByte()
        val supertypes = if (totalSupers != 0) {
            buildMap {
                repeat(totalSupers) {
                    val superFlag = decoder.decodeTypeFlag()
                    val supertype: Type
                    val objects: List<Any?>
                    if (superFlag in BuiltInReadOperations()) {
                        supertype = superFlag.type
                        objects = listOf(readObject(superFlag))
                    } else {
                        // superFlag == OBJECT
                        supertype = decoder.decodeType()
                        objects = buildList {
                            var objectFlag = decoder.decodeTypeFlag()
                            while (objectFlag !== END_OBJECT) {
                                this += readObject(objectFlag)
                                objectFlag = decoder.decodeTypeFlag()
                            }
                        }
                    }
                    this[supertype] = SupertypeDeserializer(classRef, supertype, objects)
                }
            }
        } else {
            emptyMap()
        }
        return ObjectDeserializer(classRef, supertypes, this).resolveObject()
    }

    private object BuiltInReadOperations {
        private val builtInReads: Map<TypeFlag, InputDeserializer.() -> Any?> = hashMapOf(
            builtInReadOf(BOOLEAN) {
                decoder.decodeBoolean()
            },
            builtInReadOf(BYTE) {
                decoder.decodeByte()
            },
            builtInReadOf(CHAR) {
                decoder.decodeChar()
            },
            builtInReadOf(SHORT) {
                decoder.decodeShort()
            },
            builtInReadOf(INT) {
                decoder.decodeInt()
            },
            builtInReadOf(LONG) {
                decoder.decodeLong()
            },
            builtInReadOf(FLOAT) {
                decoder.decodeFloat()
            },
            builtInReadOf(DOUBLE) {
                decoder.decodeDouble()
            },
            builtInReadOf(BOOLEAN_ARRAY) {
                BooleanArray(decoder.decodeInt()) { decoder.decodeBoolean() }
            },
            builtInReadOf(BYTE_ARRAY) {
                ByteArray(decoder.decodeInt()) { decoder.decodeByte() }
            },
            builtInReadOf(CHAR_ARRAY) {
                val buffer = decoder.decodePrimitiveArray(Char.SIZE_BYTES).asCharBuffer()
                CharArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(SHORT_ARRAY) {
                val buffer = decoder.decodePrimitiveArray(Short.SIZE_BYTES).asShortBuffer()
                ShortArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(INT_ARRAY) {
                val buffer = decoder.decodePrimitiveArray(Int.SIZE_BYTES).asIntBuffer()
                IntArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(LONG_ARRAY) {
                val buffer = decoder.decodePrimitiveArray(Long.SIZE_BYTES).asLongBuffer()
                LongArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(FLOAT_ARRAY) {
                val buffer = decoder.decodePrimitiveArray(Float.SIZE_BYTES).asFloatBuffer()
                FloatArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(DOUBLE_ARRAY) {
                val buffer = decoder.decodePrimitiveArray(Double.SIZE_BYTES).asDoubleBuffer()
                DoubleArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(STRING) {
                decoder.decodeString()
            },
            builtInReadOf(OBJECT_ARRAY) {
                Array(decoder.decodeInt()) { readObject() }
            },
            builtInReadOf(LIST) {
                val size = decoder.decodeInt()
                val mutable = ArrayList<Any?>(size)
                repeat(size) { mutable += readObject() }
                DeserializedList(mutable, source = this)
            },
            builtInReadOf(ITERABLE) {
                buildList {
                    var flag = decoder.decodeTypeFlag()
                    while (flag != END_OBJECT) {
                        this += readObject(flag)
                        flag = decoder.decodeTypeFlag()
                    }
                }
            },
            builtInReadOf(PAIR) {
                Pair(readObject(), readObject())
            },
            builtInReadOf(TRIPLE) {
                Triple(readObject(), readObject(), readObject())
            },
            builtInReadOf(MAP_ENTRY) {
                val key = readObject()
                val value = readObject()
                object : Map.Entry<Any?,Any?> {
                    override val key get() = key
                    override val value get() = value
                }
            },
            builtInReadOf(MAP) {
                val size = decoder.decodeInt()
                val mutable = LinkedHashMap<Any?,Any?>()
                repeat(size) { mutable[readObject()] = readObject() }
                DeserializedMap(mutable, source = this)
            },
            builtInReadOf(SET) {
                val size = decoder.decodeInt()
                val mutable = LinkedHashSet<Any?>(size)
                repeat(size) { mutable += readObject() }
                DeserializedSet(mutable, source = this)
            },
            builtInReadOf(SCHEMA) {
                with(decoder) {
                    val threadSafe = decodeBoolean()
                    val totalProtocols = decodeInt()
                    val protocols = if (totalProtocols > 0 ) {
                        val protocolMap = HashMap<Type, Protocol>()
                        repeat(totalProtocols) {
                            val type = decodeType()
                            var hasFallback = false
                            var hasStatic = false
                            val read: ReadOperation? = if (decodeBoolean()) {
                                hasFallback = decodeBoolean()
                                decodeSerializable()
                            } else {
                                null
                            }
                            val write: WriteOperation? = if (decodeBoolean()) {
                                hasStatic = decodeBoolean()
                                decodeSerializable()
                            } else {
                                null
                            }
                            protocolMap[type] = Protocol(read, write, hasFallback, hasStatic)
                        }
                        protocolMap
                    } else {
                        mapOf() // Mutability not necessary
                    }
                    val shared = Schema.Properties(threadSafe)
                    repeat(decodeInt()) { shared.readsOrFallbacks[decodeType()] = decodeSerializable() }
                    repeat(decodeInt()) {   // Each can never be empty
                        val type = decodeType()
                        val writeMap: MutableWriteMap = hashMapOf()
                        repeat(decodeInt()) { writeMap[decodeType()] = decodeSerializable() }
                        shared.writeMaps[type] = writeMap
                    }
                    repeat(decodeInt()) {
                        shared.primaryPropertyArrays[decodeString()] = decodeSerializable<() -> Array<out Callable>>()()
                    }
                    repeat(decodeInt()) {
                        shared.primaryConstructors[decodeString()] = decodeSerializable<() -> Callable>()()
                    }
                    Schema(protocols, shared)
                }
            },
            builtInReadOf(CONTAINER) {
                val className = decoder.decodeString()
                val parameters = Array<Any?>(decoder.decodeByte().toInt()) { read() }
                try {
                    schema.primaryConstructorOf(className).call(*parameters)
                } catch (_: IllegalCallableAccessException) {
                    throw MalformedContainerException(className, "Primary constructor of container is not public")
                }
            },
            builtInReadOf(UNIT) { /* noop */ },
            builtInReadOf(FUNCTION) {
                decoder.decodeSerializable()
            },
            builtInReadOf(NULL) {
                null
            },
            builtInReadOf(END_OBJECT) { // Impossible under normal circumstances
                throw NoSuchElementException("No object serialized in the current position")
            },
        )

        operator fun invoke() = builtInReads

        private fun builtInReadOf(flag: TypeFlag, read: InputDeserializer.() -> Any?) = flag to read
    }
}