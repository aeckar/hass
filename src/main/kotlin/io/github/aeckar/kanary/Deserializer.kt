package io.github.aeckar.kanary

import io.github.aeckar.kanary.TypeFlag.*
import io.github.aeckar.kanary.utils.KClass
import java.io.Closeable
import java.io.EOFException
import java.io.InputStream
import java.io.ObjectInputStream
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.superclasses

private val EMPTY_DESERIALIZER: Deserializer = InputDeserializer(InputStream.nullInputStream(), schema {})

/**
 * See [Schema] for a list of types that can be deserialized by default.
 * @return a new deserializer capable of reading primitives, primitive arrays, strings, and
 * instances of any type with a defined protocol from Kanary format
 */
fun InputStream.deserializer(protocols: Schema) = InputDeserializer(this, protocols)

/**
 * Permits the reading of serialized data in Kanary format.
 */
sealed interface Deserializer {
    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readBoolean(): Boolean

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readByte(): Byte

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readChar(): Char

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readShort(): Short

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readInt(): Int

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readLong(): Long

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readFloat(): Float

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readDouble(): Double

    /**
     * If [T] is a primitive type, is capable of reading a primitive value.
     * Can be null.
     * @return the serialized object of the given type
     * @throws TypeFlagMismatchException the value was not serialized as a singular object or null
     * @throws TypeCastException the object is not an instance of type [T]
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun <T> read(): T
}

/**
 * Deserializer allowing extraction of data from supertypes with
 * a defined [write operation][ProtocolBuilder.write].
 */
class ObjectDeserializer internal constructor( // Each instance used to read a single OBJECT
    private val classRef: KClass<*>,
    private val supertypes: Map<KClass<*>, Deserializer>,
    private val source: InputDeserializer
) : Deserializer by source {
    /**
     * A supertype deserializer corresponding to the data serialized by the immediate superclass.
     * If the superclass does not have a defined write operation, is assigned a deserializer containing no data.
     */
    val superclass: Deserializer by lazy { supertype(classRef.superclasses.first()) }

    /**
     * @return a supertype deserializer corresponding to the data serialized by given supertype.
     * If the supertype does not have a defined write operation, returns a deserializer containing no data.
     * @throws MalformedProtocolException [T] is not a supertype
     */
    inline fun <reified T : Any> supertype() = supertype(T::class)

    @PublishedApi
    internal fun supertype(classRef: KClass<*>): Deserializer {
        return supertypes[classRef] ?: if (classRef.isSuperclassOf(classRef)) {
            EMPTY_DESERIALIZER
        } else {
            throw MalformedProtocolException(classRef,
                    "type '${classRef.qualifiedName ?: "<local or anonymous>"}' is not a supertype")
        }
    }

    internal fun resolveObject(): Any? {
        return try {
            source.schema.readOperationOf(classRef)(this).also { source.readFlag() /* END_OBJECT */ }
        } catch (_: NoSuchElementException) {
            throw MalformedProtocolException(classRef,
                    "attempted read of object after object deserializer was exhausted")
        }
    }
}

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
    private val byteWrapper = ByteArray(1)

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

    // -------------------- utilities --------------------

    private fun readBytesToBuffer(count: Int) = ByteBuffer.wrap(stream.readNBytesChecked(count))

    private fun throwEOFException(): Nothing {
        throw EOFException(
            "Attempted read of object after deserializer was exhausted. " +
                    "Ensure supertype write operations are not overridden by 'static' write")
    }

    // -------------------- checked InputStream extensions --------------------

    /**
     * Allows reading of bytes with possible value of -1.
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    private fun InputStream.readCheckedRaw(): Byte {
        val readSize = read(byteWrapper)
        if (readSize == -1) {
            throwEOFException()
        }
        return byteWrapper.single()
    }

    /**
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    private fun InputStream.readChecked() = read().also { if (it == -1) throwEOFException() }

    /**
     * @return the next [len] bytes in the stream
     * @throws EOFException stream is exhausted
     */
    private fun InputStream.readNBytesChecked(len: Int) = readNBytes(len).also { if (it.isEmpty()) throwEOFException() }

    // -------------------- flag read operations --------------------

    private fun ensureFlag(flag: TypeFlag) {
        val ordinal = stream.readChecked()
        if (flag.ordinal != ordinal) {
            throw TypeFlagMismatchException("Type flag '$flag' expected, but found '${TypeFlag.nameOf(ordinal)}'")
        }
    }

    internal fun readFlag() = TypeFlag.entries[stream.readChecked()]

    // -------------------- non-validating read operations --------------------

    private fun readBooleanNoValidate() = stream.readChecked() == 1
    private fun readByteNoValidate() = stream.readCheckedRaw()
    private fun readCharNoValidate() = readBytesToBuffer(Char.SIZE_BYTES).char
    private fun readShortNoValidate() = readBytesToBuffer(Short.SIZE_BYTES).short
    private fun readIntNoValidate() = readBytesToBuffer(Int.SIZE_BYTES).int
    private fun readLongNoValidate() = readBytesToBuffer(Long.SIZE_BYTES).long
    private fun readFloatNoValidate() = readBytesToBuffer(Float.SIZE_BYTES).float
    private fun readDoubleNoValidate() = readBytesToBuffer(Double.SIZE_BYTES).double

    // Accessed by ObjectDeserializer, SupertypeDeserializer
    internal fun readStringNoValidate(): String {
        val lengthInBytes = readIntNoValidate()
        return String(stream.readNBytesChecked(lengthInBytes))
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
                BooleanArray(readIntNoValidate()) { stream.readChecked() == 1 }
            },
            builtInReadOf(BYTE_ARRAY) {
                ByteArray(readIntNoValidate()) { stream.readCheckedRaw() }
            },
            builtInReadOf(CHAR_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size*Char.SIZE_BYTES).asCharBuffer()
                CharArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(SHORT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size*Short.SIZE_BYTES).asShortBuffer()
                ShortArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(INT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size*Int.SIZE_BYTES).asIntBuffer()
                IntArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(LONG_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size * Long.SIZE_BYTES).asLongBuffer()
                LongArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(FLOAT_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size * Float.SIZE_BYTES).asFloatBuffer()
                FloatArray(buffer.remaining()).apply { buffer.get(this) }
            },
            builtInReadOf(DOUBLE_ARRAY) {
                val size = readIntNoValidate()
                val buffer = readBytesToBuffer(size * Double.SIZE_BYTES).asDoubleBuffer()
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