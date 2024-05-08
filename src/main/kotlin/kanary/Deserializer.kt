@file:Suppress("UNUSED")
package kanary

import kanary.TypeCode.*
import java.io.Closeable
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * @return a new deserializer capable of reading primitives, primitive arrays, and strings from Kanary format
 */
fun InputStream.deserializer() = PrimitiveDeserializer(this)

/**
 * @return a new deserializer capable of reading primitives, primitive arrays, strings, and
 * instances of any type with a defined protocol from Kanary format
 */
fun InputStream.deserializer(protocols: ProtocolSet) = Deserializer(this, protocols)

/**
 * Reads serialized data from a stream in Kanary format.
 * Does not need to be closed so long as the underlying stream is closed.
 * Because no protocols are defined, no instances of any reference types may be read.
 * Calling [close] also closes the underlying stream.
 */
open class PrimitiveDeserializer internal constructor(@PublishedApi internal val stream: InputStream) : Closeable {
    /**
     * @throws TypeMismatchException the object was not serialized as a boolean
     */
    fun readBoolean(): Boolean {
        BOOLEAN.validate(stream)
        return stream.read() == 1
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a byte
     */
    fun readByte(): Byte {
        BYTE.validate(stream)
        return stream.read().toByte()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a character
     */
    fun readChar(): Char {
        CHAR.validate(stream)
        return readBytesNoValidate(Char.SIZE_BYTES).char
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a short
     */
    fun readShort(): Short {
        SHORT.validate(stream)
        return readBytesNoValidate(Short.SIZE_BYTES).short
    }

    /**
     * @throws TypeMismatchException the object was not serialized as an integer
     */
    fun readInt(): Int {
        INT.validate(stream)
        return readIntNoValidate()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a long
     */
    fun readLong(): Long {
        LONG.validate(stream)
        return readBytesNoValidate(Long.SIZE_BYTES).long
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a float
     */
    fun readFloat(): Float {
        FLOAT.validate(stream)
        return readBytesNoValidate(Float.SIZE_BYTES).float
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a double
     */
    fun readDouble(): Double {
        DOUBLE.validate(stream)
        return readBytesNoValidate(Double.SIZE_BYTES).double
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a boolean array
     */
    fun readBooleanArray(): BooleanArray {
        BOOLEAN_ARRAY.validate(stream)
        return BooleanArray(readIntNoValidate()) { stream.read() == 1 }
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a character array
     */
    fun readCharArray(): CharArray {
        CHAR_ARRAY.validate(stream)
        val size = readIntNoValidate()
        return readBytesNoValidate(size*Char.SIZE_BYTES).asCharBuffer().array()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a short array
     */
    fun readShortArray(): ShortArray {
        SHORT_ARRAY.validate(stream)
        val size = readIntNoValidate()
        return readBytesNoValidate(size*Short.SIZE_BYTES).asShortBuffer().array()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as an integer array
     */
    fun readIntArray(): IntArray {
        INT_ARRAY.validate(stream)
        val size = readIntNoValidate()
        return readBytesNoValidate(size*Int.SIZE_BYTES).asIntBuffer().array()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a long array
     */
    fun readLongArray(): LongArray {
        LONG_ARRAY.validate(stream)
        val size = readIntNoValidate()
        return readBytesNoValidate(size*Long.SIZE_BYTES).asLongBuffer().array()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a float array
     */
    fun readFloatArray(): FloatArray {
        FLOAT_ARRAY.validate(stream)
        val size = readIntNoValidate()
        return readBytesNoValidate(size*Float.SIZE_BYTES).asFloatBuffer().array()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a double array
     */
    fun readDoubleArray(): DoubleArray {
        DOUBLE_ARRAY.validate(stream)
        val size = readIntNoValidate()
        return readBytesNoValidate(size*Double.SIZE_BYTES).asDoubleBuffer().array()
    }

    /**
     * @throws TypeMismatchException the object was not serialized as a string
     */
    fun readString(): String {  // size, followed by literal
        STRING.validate(stream)
        return readStringNoValidate()
    }

    override fun close() = stream.close()

    @PublishedApi
    internal fun readIntNoValidate() = readBytesNoValidate(Int.SIZE_BYTES).int

    @PublishedApi
    internal fun readStringNoValidate(): String {
        val size = readIntNoValidate()
        return String(stream.readNBytes(size))
    }

    private fun readBytesNoValidate(count: Int) = ByteBuffer.wrap(stream.readNBytes(count))
}

/**
 * A [PrimitiveDeserializer] that allows the reading of objects whose types have a defined protocol.
 */
class Deserializer internal constructor(
    stream: InputStream,
    private val protocols: ProtocolSet
) : PrimitiveDeserializer(stream) {
    /**
     * Reads an object array from binary with each member deserialized according to its protocol, or null respectively.
     * @throws TypeMismatchException the object was not serialized as an object array
     * @throws TypeCastException a member is not null or an instance of type [T]
     */
    inline fun <T, reified N : T & Any> readNullablesArray(): Array<out T?> {
        shortCircuitValidate(OBJECT_ARRAY, NULLABLES_ARRAY) { return readArrayNoValidate<N>() }
        val size = readIntNoValidate()
        return Array(size) {
            val code = stream.read()
            if (code == NULL.ordinal) {
                return@Array null
            }
            if (code != OBJECT.ordinal) {
                throw TypeMismatchException(NULL, OBJECT, code)
            }
            readObjectNoValidate<N>()
        }
    }

    /**
     * Reads an object array with each member deserialized according to its protocol.
     * @throws TypeMismatchException the object was not serialized as an object array
     * @throws TypeCastException a member is not null or an instance of type [T]
     */
    inline fun <reified T : Any> readArray(): Array<out T> {
        OBJECT_ARRAY.validate(stream)
        return readArrayNoValidate()
    }

    /**
     * Reads a list from binary with each member deserialized according to its protocol, or null respectively.
     * @throws TypeMismatchException the object was not serialized as a list
     * @throws TypeCastException a member is not null or an instance of type [T]
     */
    inline fun <reified T, reified N : T & Any> readNullablesList(): List<T?> {
        shortCircuitValidate(LIST, NULLABLES_LIST) { return readListNoValidate<N>() }
        return readNullablesListNoValidate()
    }

    /**
     * Reads a [List] from binary with each member deserialized according to its protocol.
     * @throws TypeMismatchException the object was not serialized as a list
     * @throws TypeCastException a member is not an instance of type [T]
     */
    inline fun <reified T : Any> readList(): List<T> {
        LIST.validate(stream)
        return readListNoValidate()
    }

    /**
     * Reads an [Iterable] from binary with each member deserialized according to its protocol, or null respectively.
     * Although [readNullablesList] is more efficient, this function may also parse lists.
     * @throws TypeMismatchException the object was not serialized as an iterable or list
     * @throws TypeCastException a member is not null or an instance of type [T]
     */
    inline fun <T, reified N : T & Any> readNullablesIterable(): List<T?> {
        when (val code = stream.read()) {
            LIST.ordinal -> readListNoValidate<N>()
            NULLABLES_LIST.ordinal -> readNullablesListNoValidate<T, N>()
            ITERABLE.ordinal -> readIterableNoValidate<N>()
            NULLABLES_ITERABLE.ordinal -> Unit
            else -> throw TypeMismatchException("Types 'LIST' or 'NULLABLES_LIST' or 'ITERABLE' or " +
                    "'NULLABLES_ITERABLE' expected, found '${TypeCode.nameOf(code)}'")
        }
        val list = mutableListOf<T?>()
        do {
            list += when (val code = stream.read()) {
                SENTINEL.ordinal -> break
                NULL.ordinal -> null
                OBJECT.ordinal -> readObjectNoValidate<N>()
                else -> throw TypeMismatchException(NULL, OBJECT, code)
            }
        } while (true)
        return list
    }

    /**
     * Reads an [Iterable] from binary with each member deserialized according to its protocol.
     * Although [readList] is more efficient, this function may also parse lists.
     * @throws TypeMismatchException the object was not serialized as an iterable or list
     * @throws TypeCastException a member is not an instance of type [T]
     */
    inline fun <reified T : Any> readIterable(): List<T> {
        shortCircuitValidate(LIST, ITERABLE) { return readListNoValidate() }
        return readIterableNoValidate()
    }

    /**
     * Reads an object of the specified type from binary according to the protocol of its type, or null respectively.
     * @throws TypeMismatchException the value was not serialized as a singular object or null
     * @throws TypeCastException the object is not an instance of type [T]
     */
    inline fun <T, reified N : T & Any> readNullable(): T? {
        val code = stream.read()
        if (code == NULL.ordinal) {
            return null
        }
        if (code != OBJECT.ordinal) {
            throw TypeMismatchException(OBJECT, NULL, code)
        }
        return readObjectNoValidate<N>()
    }

    /**
     * Reads an object of the specified type from binary according to the protocol of its type.
     * @throws TypeMismatchException the object was not serialized as a singular object
     * @throws TypeCastException the object is not an instance of type [T]
     */
    inline fun <reified T : Any> readObject(): T {
        OBJECT.validate(stream)
        return readObjectNoValidate()
    }

    @PublishedApi
    internal inline fun shortCircuitValidate(
        short: TypeCode,
        expect: TypeCode,
        onNotNullable: () -> Unit
    ) {
        val code = stream.read()
        if (code == short.ordinal) {
            onNotNullable()
        }
        if (code != expect.ordinal) {
            throw TypeMismatchException(short, expect, code)
        }
    }

    @PublishedApi
    internal inline fun <reified T : Any> readArrayNoValidate(): Array<T> {
        val size = readIntNoValidate()
        return Array(size) { readObjectNoValidate() }
    }

    @PublishedApi
    internal inline fun <reified T : Any> readListNoValidate(): List<T> {
        val size = readIntNoValidate()
        val list = ArrayList<T>(size)
        repeat (size) {
            list += readObject<T>()
        }
        return list
    }

    @PublishedApi
    internal inline fun <T, reified N : T & Any> readNullablesListNoValidate(): List<T?> {
        val size = readIntNoValidate()
        val list = ArrayList<T?>(size)
        repeat(size) {
            list += when (val memberCode = stream.read()) {
                NULL.ordinal -> null
                OBJECT.ordinal -> readObject<N>()
                else -> throw TypeMismatchException(NULL, OBJECT, memberCode)
            }
        }
        return list
    }

    @PublishedApi
    internal inline fun <reified T : Any> readIterableNoValidate(): List<T> {
        val size = readIntNoValidate()
        val list = ArrayList<T>(size)
        repeat (size) {
            list += readObject<T>()
        }
        return list
    }

    @PublishedApi
    internal fun <T : Any> readObjectNoValidate(): T {
        val className = readStringNoValidate()
        println(protocols)
        return protocols.resolve<T>(className)?.read?.invoke(this)
            ?: throw MissingProtocolException("Binary I/O protocol for class '$className' expected but not found")
    }
}

@JvmInline
value class PolymorphicDeserializer() // wrap