package io.github.aeckar.kanary

import io.github.aeckar.kanary.TypeFlag.*
import io.github.aeckar.kanary.utils.CheckedInputStream
import io.github.aeckar.kanary.utils.KClass
import java.io.Closeable
import java.io.EOFException
import java.io.InputStream
import java.io.ObjectInputStream
import kotlin.reflect.KClass

/**
 * Reads serialized data from a stream in Kanary format.
 *
 * Does not need to be closed so long as the underlying stream is closed.
 * Calling [close] also closes the underlying stream.
 */
class InputDeserializer(
    private val stream: InputStream,
    internal val schema: Schema
) : Deserializer, Closeable {
    private val checked inline get() = CheckedInputStream(stream)

    // -------------------- public API --------------------

    override fun readBoolean(): Boolean {
        ensureFlag(BOOLEAN)
        return readBooleanNoValidate()
    }

    override fun readByte(): Byte {
        ensureFlag(BYTE)
        return readByteNoValidate()
    }

    override fun readChar(): Char {
        ensureFlag(CHAR)
        return readCharNoValidate()
    }

    override fun readShort(): Short {
        ensureFlag(SHORT)
        return readShortNoValidate()
    }

    override fun readInt(): Int {
        ensureFlag(INT)
        return readIntNoValidate()
    }

    override fun readLong(): Long {
        ensureFlag(LONG)
        return readLongNoValidate()
    }

    override fun readFloat(): Float {
        ensureFlag(FLOAT)
        return readFloatNoValidate()
    }

    override fun readDouble(): Double {
        ensureFlag(DOUBLE)
        return readDoubleNoValidate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> read() = readObject() as T

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     */
    override fun close() = stream.close()

    // -------------------- flag read operations --------------------

    private fun ensureFlag(flag: TypeFlag) {
        val ordinal = checked.read()
        if (flag.ordinal != ordinal) {
            throw TypeFlagMismatchException(flag, ordinal)
        }
    }

    internal fun readFlag() = TypeFlag.entries[checked.read()]

    // -------------------- non-validating read operations --------------------

    private fun readBooleanNoValidate() = checked.read() == 1
    private fun readByteNoValidate() = checked.readRaw(schema.rawBuffer)
    private fun readCharNoValidate() = checked.readToBuffer(Char.SIZE_BYTES).char
    private fun readShortNoValidate() = checked.readToBuffer(Short.SIZE_BYTES).short
    private fun readIntNoValidate() = checked.readToBuffer(Int.SIZE_BYTES).int
    private fun readLongNoValidate() = checked.readToBuffer(Long.SIZE_BYTES).long
    private fun readFloatNoValidate() = checked.readToBuffer(Float.SIZE_BYTES).float
    private fun readDoubleNoValidate() = checked.readToBuffer(Double.SIZE_BYTES).double

    // Accessed by ObjectDeserializer, SupertypeDeserializer
    internal fun readStringNoValidate(): String {
        val lengthInBytes = readIntNoValidate()
        return String(checked.readNBytes(lengthInBytes))
    }

    // -------------------- object read operation with erased type --------------------

    // Accessed by SupertypeDeserializer
    internal fun readObject(flag: TypeFlag = readFlag()): Any? {
        BuiltInReadOperations()[flag]?.let { return this.it() }
        assert(flag === OBJECT)
        val classRef = KClass(className = readStringNoValidate())
        val superCount = stream.read()
        val supertypes = if (superCount == 0) {
            emptyMap()
        } else {
            val source = this
            HashMap<KClass<*>, Deserializer>(superCount).apply {
                repeat(superCount) {
                    val superFlag = readFlag()
                    val supertype: KClass<*>
                    val isBuiltIn = superFlag in BuiltInReadOperations()
                    if (isBuiltIn) {
                        supertype = superFlag.kClass
                    } else {
                        assert(superFlag === OBJECT)
                        supertype = KClass(className = readStringNoValidate())
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
            builtInReadOf(UNIT) {},
            builtInReadOf(FUNCTION) {
                ObjectInputStream(stream).readObject()
            },
            builtInReadOf(NULL) {
                null
            },
            builtInReadOf(BOOLEAN) {
                readBooleanNoValidate()
            },
            builtInReadOf(BYTE) {
                readByteNoValidate()
            },
            builtInReadOf(CHAR) {
                readCharNoValidate()
            },
            builtInReadOf(SHORT) {
                readShortNoValidate()
            },
            builtInReadOf(INT) {
                readIntNoValidate()
            },
            builtInReadOf(LONG) {
                readLongNoValidate()
            },
            builtInReadOf(FLOAT) {
                readFloatNoValidate()
            },
            builtInReadOf(DOUBLE) {
                readDoubleNoValidate()
            },
            builtInReadOf(BOOLEAN_ARRAY) {
                BooleanArray(readIntNoValidate()) { checked.read() == 1 }
            },
            builtInReadOf(BYTE_ARRAY) {
                ByteArray(readIntNoValidate()) { checked.readRaw(schema.rawBuffer) }
            },
            builtInReadOf(CHAR_ARRAY) {
                val size = readIntNoValidate()
                val buffer = checked.readToBuffer(size*Char.SIZE_BYTES).asCharBuffer()
                CharArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(SHORT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = checked.readToBuffer(size*Short.SIZE_BYTES).asShortBuffer()
                ShortArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(INT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = checked.readToBuffer(size*Int.SIZE_BYTES).asIntBuffer()
                IntArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(LONG_ARRAY) {
                val size = readIntNoValidate()
                val buffer = checked.readToBuffer(size * Long.SIZE_BYTES).asLongBuffer()
                LongArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(FLOAT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = checked.readToBuffer(size * Float.SIZE_BYTES).asFloatBuffer()
                FloatArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(DOUBLE_ARRAY) {
                val size = readIntNoValidate()
                val buffer = checked.readToBuffer(size * Double.SIZE_BYTES).asDoubleBuffer()
                DoubleArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(STRING) {
                readStringNoValidate()
            },
            builtInReadOf(OBJECT_ARRAY) {
                val size = readIntNoValidate()
                Array(size) { readObject() }
            },
            builtInReadOf(LIST) {
                val size = readIntNoValidate()
                buildList(size) {
                    repeat(size) { this += readObject() }
                }
            },
            builtInReadOf(ITERABLE) {
                buildList {
                    var flag = readFlag()
                    while (flag != END_OBJECT) {
                        this += readObject(flag)
                        flag = readFlag()
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
                val size = readIntNoValidate()
                buildMap(size) {
                    repeat(size) { this[readObject()] = readObject() }
                }
            }
        )

        operator fun invoke() = builtInReads

        private fun builtInReadOf(flag: TypeFlag, read: InputDeserializer.() -> Any?) = flag to read
    }
}

private class SupertypeDeserializer(    // Each instance used to read a single packet of data
    private val classRef: KClass<*>,
    private val supertype: KClass<*>,
    superFlag: TypeFlag,
    source: InputDeserializer,
    isBuiltIn: Boolean
) : Deserializer {
    private var cursor = 0
    private val objects = if (isBuiltIn) {
        listOf(source.readObject(superFlag))
    } else {
        buildList {
            var flag = source.readFlag()
            while (flag !== END_OBJECT) {
                this += source.readObject(flag)
                flag = source.readFlag()
            }
        }
    }

    override fun readBoolean(): Boolean = nextObject()
    override fun readByte(): Byte = nextObject()
    override fun readChar(): Char = nextObject()
    override fun readShort(): Short = nextObject()
    override fun readInt(): Int = nextObject()
    override fun readLong(): Long = nextObject()
    override fun readFloat(): Float = nextObject()
    override fun readDouble(): Double = nextObject()
    override fun <T> read(): T = nextObject()

    @Suppress("UNCHECKED_CAST")
    private fun <T> nextObject(): T {
        return try {
            (objects[cursor] as T).also { ++cursor }
        } catch (_: IndexOutOfBoundsException) {
            throw EOFException(
                    "Attempted read of object in supertype '$supertype' after" +
                    "supertype deserializer was exhausted (in protocol of '$classRef')")
        }
    }
}