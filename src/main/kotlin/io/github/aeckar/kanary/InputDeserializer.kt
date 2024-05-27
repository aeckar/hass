package io.github.aeckar.kanary

import io.github.aeckar.kanary.io.TypeFlag
import io.github.aeckar.kanary.io.TypeFlag.*
import io.github.aeckar.kanary.io.InputDataStream
import io.github.aeckar.kanary.reflect.Type
import java.io.Closeable
import java.io.InputStream
import java.io.ObjectInputStream

/**
 * Performs an unchecked cast to [T], throwing [ObjectMismatchException] if the cast fails.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> Any?.castTo(classRef: Type? = null): T {
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
    internal val stream = InputDataStream(stream)

    // -------------------- public API --------------------



    override fun readBoolean(): Boolean {
        stream.ensureTypeFlag(BOOLEAN)
        return stream.readBoolean()
    }

    override fun readByte(): Byte {
        stream.ensureTypeFlag(BYTE)
        return stream.readByte()
    }

    override fun readChar(): Char {
        stream.ensureTypeFlag(CHAR)
        return stream.readChar()
    }

    override fun readShort(): Short {
        stream.ensureTypeFlag(SHORT)
        return stream.readShort()
    }

    override fun readInt(): Int {
        stream.ensureTypeFlag(INT)
        return stream.readInt()
    }

    override fun readLong(): Long {
        stream.ensureTypeFlag(LONG)
        return stream.readLong()
    }

    override fun readFloat(): Float {
        stream.ensureTypeFlag(FLOAT)
        return stream.readFloat()
    }

    override fun readDouble(): Double {
        stream.ensureTypeFlag(DOUBLE)
        return stream.readDouble()
    }

    override fun <T> read() = readObject().castTo<T>()

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     */
    override fun close() = stream.raw.close()

    // ------------------------------------------------------------------------

    // Accessed by SupertypeDeserializer
    internal fun readObject(flag: TypeFlag = stream.readTypeFlag()): Any? {
        BuiltInReadOperations()[flag]?.let { return this.it() }
        // flag == OBJECT
        val classRef = Type(className = stream.readString())
        val superCount = stream.readRawByte()
        val supertypes = if (superCount == 0) {
            emptyMap()
        } else {
            val source = this
            HashMap<Type, Deserializer>(superCount).apply {
                repeat(superCount) {
                    val superFlag = stream.readTypeFlag()
                    val isBuiltIn = superFlag in BuiltInReadOperations()
                    val supertype = if (isBuiltIn) {
                        superFlag.kClass
                    } else {
                        // superFlag == OBJECT
                        Type(className = stream.readString())
                    }
                    this[supertype] = SupertypeDeserializer(classRef, supertype, superFlag, source, isBuiltIn)
                }
            }
        }
        return ObjectDeserializer(classRef, supertypes, this).resolveObject()
    }

    private object BuiltInReadOperations {
        private val builtInReads: Map<TypeFlag, InputDeserializer.() -> Any?> = hashMapOf(
            builtInReadOf(END_OBJECT) {
                throw NoSuchElementException("No object serialized in the current position")
            },
            builtInReadOf(UNIT) { /* noop */ },
            builtInReadOf(FUNCTION) {
                ObjectInputStream(stream.raw).readObject()
            },
            builtInReadOf(NULL) {
                null
            },
            builtInReadOf(BOOLEAN) {
                stream.readBoolean()
            },
            builtInReadOf(BYTE) {
                stream.readByte()
            },
            builtInReadOf(CHAR) {
                stream.readChar()
            },
            builtInReadOf(SHORT) {
                stream.readShort()
            },
            builtInReadOf(INT) {
                stream.readInt()
            },
            builtInReadOf(LONG) {
                stream.readLong()
            },
            builtInReadOf(FLOAT) {
                stream.readFloat()
            },
            builtInReadOf(DOUBLE) {
                stream.readDouble()
            },
            builtInReadOf(BOOLEAN_ARRAY) {
                BooleanArray(stream.readInt()) { stream.readBoolean() }
            },
            builtInReadOf(BYTE_ARRAY) {
                ByteArray(stream.readInt()) { stream.readByte() }
            },
            builtInReadOf(CHAR_ARRAY) {
                val buffer = stream.readPrimitiveArray(Char.SIZE_BYTES).asCharBuffer()
                CharArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(SHORT_ARRAY) {
                val buffer = stream.readPrimitiveArray(Short.SIZE_BYTES).asShortBuffer()
                ShortArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(INT_ARRAY) {
                val buffer = stream.readPrimitiveArray(Int.SIZE_BYTES).asIntBuffer()
                IntArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(LONG_ARRAY) {
                val buffer = stream.readPrimitiveArray(Long.SIZE_BYTES).asLongBuffer()
                LongArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(FLOAT_ARRAY) {
                val buffer = stream.readPrimitiveArray(Float.SIZE_BYTES).asFloatBuffer()
                FloatArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(DOUBLE_ARRAY) {
                val buffer = stream.readPrimitiveArray(Double.SIZE_BYTES).asDoubleBuffer()
                DoubleArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(STRING) {
                stream.readString()
            },
            builtInReadOf(OBJECT_ARRAY) {
                Array(stream.readInt()) { readObject() }
            },
            builtInReadOf(LIST) {
                val size = stream.readInt()
                val mutable = ArrayList<Any?>(size)
                repeat(size) { mutable += readObject() }
                DeserializedList(mutable, source = this)
            },
            builtInReadOf(ITERABLE) {
                buildList {
                    var flag = stream.readTypeFlag()
                    while (flag != END_OBJECT) {
                        this += readObject(flag)
                        flag = stream.readTypeFlag()
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
                val key = readObject()  // Closures CANNOT be inlined
                val value = readObject()
                object : Map.Entry<Any?,Any?> {
                    override val key get() = key
                    override val value get() = value
                }
            },
            builtInReadOf(MAP) {
                val size = stream.readInt()
                val mutable = LinkedHashMap<Any?,Any?>(size)
                repeat(size) { mutable[readObject()] = readObject() }
                DeserializedMap(mutable, source = this)
            },
            builtInReadOf(SET) {
                val size = stream.readInt()
                val mutable = LinkedHashSet<Any?>(size)
                repeat(size) { mutable += readObject() }
                DeserializedSet(mutable, source = this)
            }
        )

        operator fun invoke() = builtInReads

        private fun builtInReadOf(flag: TypeFlag, read: InputDeserializer.() -> Any?) = flag to read
    }
}