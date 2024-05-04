@file:Suppress("UNUSED")
package kanary

import kanary.TypeCode.*
import java.io.Closeable
import java.io.Flushable
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * @return a new binary input stream associated with this stream
 */
fun InputStream.binary() = BinaryInput(this)

/**
 * @return a new binary output stream associated with this stream
 */
fun OutputStream.binary() = BinaryOutput(this)

/**
 * A binary stream with functions for reading primitives or classes with a [protocolOf] in Kanary format.
 * Does not support marking.
 * Calling [close] also closes the underlying stream.
 * This object does not need to be closed so long as the underlying stream is closed.
 */
@JvmInline
value class BinaryInput internal constructor(@PublishedApi internal val stream: InputStream) : Closeable {
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

    /**
     * Reads an object array from binary with each member deserialized according to its protocol, or null respectively.
     * @throws TypeMismatchException the object was not serialized as an object array
     * @throws TypeCastException a member is not null or an instance of type [T]
     */
    inline fun <T, reified N : T & Any> readNullablesArray(): Array<out T?> {
        shortCircuitValidate(OBJECT_ARRAY, NULLABLE_ARRAY) { return readArrayNoValidate<N>() }
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
        shortCircuitValidate(LIST, NULLABLE_LIST) { return readListNoValidate<N>() }
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
            NULLABLE_LIST.ordinal -> readNullablesListNoValidate<T, N>()
            ITERABLE_BEGIN.ordinal -> readIterableNoValidate<N>()
            NULLABLE_BEGIN.ordinal -> Unit
            else -> throw TypeMismatchException(LIST, NULLABLE_LIST, ITERABLE_BEGIN, NULLABLE_BEGIN, code)
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
        shortCircuitValidate(LIST, ITERABLE_BEGIN) { return readListNoValidate() }
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

    override fun close() = stream.close()

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
    internal fun readIntNoValidate() = readBytesNoValidate(Int.SIZE_BYTES).int

    @PublishedApi
    internal fun readStringNoValidate(): String {
        val size = readIntNoValidate()
        return String(stream.readNBytes(size))
    }

    @PublishedApi
    internal inline fun <reified T : Any> readArrayNoValidate(): Array<T> {
        val size = readIntNoValidate()
        return Array(size) { readObjectNoValidate<T>() }
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
    internal inline fun <reified T : Any> readObjectNoValidate(): T {
        return resolveProtocol(Class.forName(readStringNoValidate()).kotlin).onRead(this) as T
    }

    private fun readBytesNoValidate(count: Int) = ByteBuffer.wrap(stream.readNBytes(count))
}

/**
 * A binary stream with functions for writing primitives or classes with a [protocolOf] in Kanary format.
 * Does not support marking.
 * Calling [close] also closes the underlying stream.
 * This object does not need to be closed so long as the underlying stream is closed.
 */
@JvmInline
value class BinaryOutput internal constructor(@PublishedApi internal val stream: OutputStream) : Closeable, Flushable {
    fun write(cond: Boolean) {
        BOOLEAN.mark(stream)
        stream.write(if (cond) 1 else 0)
    }

    fun write(b: Byte) {
        BYTE.mark(stream)
        stream.write(b.toInt())
    }

    fun write(c: Char) {
        CHAR.mark(stream)
        writeBytesNoMark(Char.SIZE_BYTES) { putChar(c) }
    }

    fun write(n: Short) {
        SHORT.mark(stream)
        writeBytesNoMark(Short.SIZE_BYTES) { putShort(n) }
    }

    fun write(n: Int) {
        INT.mark(stream)
        writeNoMark(n)
    }

    fun write(n: Long) {
        LONG.mark(stream)
        writeBytesNoMark(Long.SIZE_BYTES) { putLong(n) }
    }

    fun write(fp: Float) {
        FLOAT.mark(stream)
        writeBytesNoMark(Float.SIZE_BYTES) { putFloat(fp) }
    }

    fun write(fp: Double) {
        DOUBLE.mark(stream)
        writeBytesNoMark(Double.SIZE_BYTES) { putDouble(fp) }
    }

    fun write(condArr: BooleanArray) {
        BOOLEAN_ARRAY.mark(stream)
        condArr.forEach { stream.write(if (it) 1 else 0) }
    }

    fun write(bArr: ByteArray) {
        BYTE_ARRAY.mark(stream)
        writeBytesNoMark(Int.SIZE_BYTES) { putInt(bArr.size) }
        stream.write(bArr)
    }

    fun write(cArr: CharArray) {
        CHAR_ARRAY.mark(stream)
        writeArrayNoMark(Char.SIZE_BYTES, cArr.size) { cArr.forEach { putChar(it) } }
    }

    fun write(nArr: ShortArray) {
        SHORT_ARRAY.mark(stream)
        writeArrayNoMark(Short.SIZE_BYTES, nArr.size) { nArr.forEach { putShort(it) } }
    }

    fun write(nArr: IntArray) {
        INT_ARRAY.mark(stream)
        writeArrayNoMark(Int.SIZE_BYTES, nArr.size) { nArr.forEach { putInt(it) } }
    }

    fun write(nArr: LongArray) {
        LONG_ARRAY.mark(stream)
        writeArrayNoMark(Long.SIZE_BYTES, nArr.size) { nArr.forEach { putLong(it) } }
    }

    fun write(nArr: FloatArray) {
        FLOAT_ARRAY.mark(stream)
        writeArrayNoMark(Float.SIZE_BYTES, nArr.size) { nArr.forEach { putFloat(it) } }
    }

    fun write(nArr: DoubleArray) {
        DOUBLE_ARRAY.mark(stream)
        writeArrayNoMark(Double.SIZE_BYTES, nArr.size) { nArr.forEach { putDouble(it) } }
    }

    fun write(s: String) {  // marker, size, char...
        STRING.mark(stream)
        writeNoMark(s)
    }

    /**
     * Writes all members in array according to the protocol of each instance.
     * @throws MissingProtocolException the type of any member of [nullablesArr] is not null, and
     * is not a top-level class or does not have a defined protocol
     */
    inline fun <T, reified N : T & Any> writeAllOr(nullablesArr: Array<out T>) {  // marker, type, size, (marker, member)...
        NULLABLE_ARRAY.mark(stream)
        writeNoMark(nullablesArr.size)
        nullablesArr.forEach {
            if (it == null) {
                NULL.mark(stream)
                return@forEach
            }
            write(it)
        }
    }

    /**
     * Writes all members in array according to the protocol of each instance.
     * @throws MissingProtocolException the type of any member of [objArr]
     * is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> writeAll(objArr: Array<out T>) {  // marker, type, size, (marker, member)...
        OBJECT_ARRAY.mark(stream)
        writeNoMark(objArr.size)
        objArr.forEach { write(it) }
    }

    /**
     * Writes all members in the list according the protocol of each.
     * @throws MissingProtocolException any member of [nullablesList] is not null, and
     * its type is not top-level class or does not have a defined protocol
     */
    inline fun <reified T> writeAllOr(nullablesList: List<T>) {   // marker, size, (marker, member)...
        NULLABLE_LIST.mark(stream)
        writeNoMark(nullablesList.size)
        nullablesList.forEach {
            if (it == null) {
                NULL.mark(stream)
                return@forEach
            }
            write(it)
        }
    }

    /**
     * Writes all members in the list according the protocol of each.
     * @throws MissingProtocolException any member of [list] is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> writeAll(list: List<T>) { // marker, size, (marker, member)...
        LIST.mark(stream)
        writeNoMark(list.size)
        list.forEach { write(it) }
    }

    /**
     * Writes all members in the iterable object according the protocol of each instance as a list.
     * The caller must ensure that the object has a finite number of members.
     * @throws MissingProtocolException any member of [nullablesIter] is not null, and
     * its type is not top-level class or does not have a defined protocol
     */
    inline fun <reified T> writeAllOr(nullablesIter: Iterable<T>) {  // begin, (marker, member)..., end
        NULLABLE_BEGIN.mark(stream)
        nullablesIter.forEach {
            if (it == null) {
                NULL.mark(stream)
                return@forEach
            }
            write(it)
        }
        SENTINEL.mark(stream)
    }

    /**
     * Writes all members in the iterable object according the protocol of each as a list.
     * The caller must ensure that the object has a finite number of members.
     * @throws MissingProtocolException any member of [iter] is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> writeAll(iter: Iterable<T>) { // begin, (marker, member)..., end
        ITERABLE_BEGIN.mark(stream)
        iter.forEach { write(it) }
        SENTINEL.mark(stream)
    }

    /**
     * Writes the object in binary format according to the protocol of its type, or null.
     * @throws MissingProtocolException if [nullable] is not null, and
     * its type is not a top-level class or does not have a defined protocol
     */
    fun writeOr(nullable: Any?) {
        if (nullable == null) {
            NULL.mark(stream)
            return
        }
        write(nullable)
    }

    /**
     * Writes the object according to the protocol of its type.
     * @throws MissingProtocolException the type of [obj] is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(obj: T) {
        OBJECT.mark(stream)
        val classRef = obj::class
        writeNoMark(protocolNameOf(classRef))
        resolveProtocol(classRef).onWrite(this, obj)
    }

    override fun flush() = stream.flush()
    override fun close() = stream.close()

    @PublishedApi
    internal fun writeNoMark(n: Int) = writeBytesNoMark(Int.SIZE_BYTES) { putInt(n) }

    @PublishedApi
    internal fun writeNoMark(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeNoMark(bytes.size)
        stream.write(bytes)
    }

    @PublishedApi
    internal inline fun writeBytesNoMark(count: Int, write: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(count)
        write(buffer)
        stream.write(buffer.array())
    }

    // marker, size, member...
    private inline fun writeArrayNoMark(memberBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1*Int.SIZE_BYTES + size*memberBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }
}