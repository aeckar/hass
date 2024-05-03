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
 */
@JvmInline
value class BinaryInput internal constructor(@PublishedApi internal val stream: InputStream) : Closeable {
    fun readBoolean(): Boolean {
        TypeCode.BOOLEAN.validate(stream)
        return stream.read() == 1
    }

    fun readByte(): Byte {
        BYTE.validate(stream)
        return stream.read().toByte()
    }

    fun readChar(): Char {
        CHAR.validate(stream)
        return readBytesUnmarked(Char.SIZE_BYTES).char
    }

    fun readShort(): Short {
        SHORT.validate(stream)
        return readBytesUnmarked(Short.SIZE_BYTES).short
    }

    fun readInt(): Int {
        INT.validate(stream)
        return readIntUnmarked()
    }

    fun readLong(): Long {
        LONG.validate(stream)
        return readBytesUnmarked(Long.SIZE_BYTES).long
    }

    fun readFloat(): Float {
        FLOAT.validate(stream)
        return readBytesUnmarked(Float.SIZE_BYTES).float
    }

    fun readDouble(): Double {
        DOUBLE.validate(stream)
        return readBytesUnmarked(Double.SIZE_BYTES).double
    }

    fun readBooleanArray(): BooleanArray {
        BOOLEAN_ARRAY.validate(stream)
        return BooleanArray(readIntUnmarked()) { stream.read() == 1 }
    }

    fun readCharArray(): CharArray {
        CHAR_ARRAY.validate(stream)
        val size = readIntUnmarked()
        return readBytesUnmarked(size*Char.SIZE_BYTES).asCharBuffer().array()
    }

    fun readShortArray(): ShortArray {
        SHORT_ARRAY.validate(stream)
        val size = readIntUnmarked()
        return readBytesUnmarked(size*Short.SIZE_BYTES).asShortBuffer().array()
    }

    fun readIntArray(): IntArray {
        INT_ARRAY.validate(stream)
        val size = readIntUnmarked()
        return readBytesUnmarked(size*Int.SIZE_BYTES).asIntBuffer().array()
    }

    fun readLongArray(): LongArray {
        LONG_ARRAY.validate(stream)
        val size = readIntUnmarked()
        return readBytesUnmarked(size*Long.SIZE_BYTES).asLongBuffer().array()
    }

    fun readFloatArray(): FloatArray {
        FLOAT_ARRAY.validate(stream)
        val size = readIntUnmarked()
        return readBytesUnmarked(size*Float.SIZE_BYTES).asFloatBuffer().array()
    }

    fun readDoubleArray(): DoubleArray {
        DOUBLE_ARRAY.validate(stream)
        val size = readIntUnmarked()
        return readBytesUnmarked(size*Double.SIZE_BYTES).asDoubleBuffer().array()
    }

    fun readString(): String {  // size, followed by literal
        STRING.validate(stream)
        val size = readInt()
        return String(stream.readNBytes(size))
    }

    inline fun <T, reified N : T & Any> readNullableArray(): Array<out T?> {
        shortCircuit(OBJECT_ARRAY, NULLABLE_ARRAY) { return readArrayUnmarked<N>() }
        val size = readIntUnmarked()
        return Array(size) {
            val code = stream.read()
            if (code == NULL.ordinal) {
                return@Array null
            }
            if (code != OBJECT.ordinal) {
                throw TypeMismatchException(NULL, OBJECT, code)
            }
            readObject<N>()
        }
    }

    inline fun <reified T : Any> readArray(): Array<out T> {
        OBJECT_ARRAY.validate(stream)
        return readArrayUnmarked()
    }

    inline fun <reified T, reified N : T & Any> readNullableList(): List<T?> {
        shortCircuit(LIST, NULLABLE_LIST) { return readListUnmarked<N>() }
        return readNullableListUnmarked()
    }

    inline fun <reified T : Any> readList(): List<T> {
        LIST.validate(stream)
        return readListUnmarked()
    }

    inline fun <T, reified N : T & Any> readNullableIterable(): List<T?> {
        when (val code = stream.read()) {
            LIST.ordinal -> readListUnmarked<N>()
            NULLABLE_LIST.ordinal -> readNullableListUnmarked<T, N>()
            ITERABLE_BEGIN.ordinal -> readIterableUnmarked<N>()
            NULLABLE_BEGIN.ordinal -> Unit
            else -> throw TypeMismatchException(LIST, NULLABLE_LIST, ITERABLE_BEGIN, NULLABLE_BEGIN, code)
        }
        val list = mutableListOf<T?>()
        do {
            list += when (val code = stream.read()) {
                SENTINEL.ordinal -> break
                NULL.ordinal -> null
                OBJECT.ordinal -> readObjectUnmarked<N>()
                else -> throw TypeMismatchException(NULL, OBJECT, code)
            }
        } while (true)
        return list
    }

    inline fun <reified T : Any> readIterable(): List<T> {
        shortCircuit(LIST, ITERABLE_BEGIN) { return readListUnmarked() }
        return readIterableUnmarked()
    }


    inline fun <T, reified N : T & Any> readNullable(): T? {
        val code = stream.read()
        if (code == NULL.ordinal) {
            return null
        }
        if (code != OBJECT.ordinal) {
            throw TypeMismatchException(OBJECT, NULL, code)
        }
        return readObjectUnmarked<N>()
    }

    /**
     * Reads an object of type [T] from binary according to the protocol of its type.
     * @throws MissingProtocolException [T] is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> readObject(): T {
        OBJECT.validate(stream)
        return readObjectUnmarked()
    }

    override fun close() = stream.close()

    @PublishedApi
    internal inline fun shortCircuit(
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
    internal fun readIntUnmarked() = readBytesUnmarked(Int.SIZE_BYTES).int

    @PublishedApi
    internal inline fun <reified T : Any> readArrayUnmarked(): Array<T> {
        val size = readIntUnmarked()
        return Array(size) { readObject<T>() }
    }

    @PublishedApi
    internal inline fun <reified T : Any> readListUnmarked(): List<T> {
        val size = readIntUnmarked()
        val list = ArrayList<T>(size)
        repeat (size) {
            list += readObject<T>()
        }
        return list
    }

    @PublishedApi
    internal inline fun <T, reified N : T & Any> readNullableListUnmarked(): List<T?> {
        val size = readIntUnmarked()
        val list = ArrayList<T?>(size)
        repeat(size) {
            list += when (val memberCode = stream.read()) {
                NULL.ordinal -> null
                OBJECT.ordinal -> readObjectUnmarked<N>()
                else -> throw TypeMismatchException(NULL, OBJECT, memberCode)
            }
        }
        return list
    }

    @PublishedApi
    internal inline fun <reified T : Any> readIterableUnmarked(): List<T> {
        val size = readIntUnmarked()
        val list = ArrayList<T>(size)
        repeat (size) {
            list += readObject<T>()
        }
        return list
    }

    @PublishedApi
    internal inline fun <reified T : Any> readObjectUnmarked(): T {
        return resolveProtocol(Class.forName(readString()).kotlin).onRead(this) as T
    }

    private fun readBytesUnmarked(count: Int) = ByteBuffer.wrap(stream.readNBytes(count))
}

/**
 * A binary stream with functions for writing primitives or classes with a [protocolOf] in Kanary format.
 * Does not support marking.
 * Calling [close] also closes the underlying stream.
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
        writeBytesUnmarked(Char.SIZE_BYTES) { putChar(c) }
    }

    fun write(n: Short) {
        SHORT.mark(stream)
        writeBytesUnmarked(Short.SIZE_BYTES) { putShort(n) }
    }

    fun write(n: Int) {
        INT.mark(stream)
        writeUnmarked(n)
    }

    fun write(n: Long) {
        LONG.mark(stream)
        writeBytesUnmarked(Long.SIZE_BYTES) { putLong(n) }
    }

    fun write(fp: Float) {
        FLOAT.mark(stream)
        writeBytesUnmarked(Float.SIZE_BYTES) { putFloat(fp) }
    }

    fun write(fp: Double) {
        DOUBLE.mark(stream)
        writeBytesUnmarked(Double.SIZE_BYTES) { putDouble(fp) }
    }

    fun write(condArr: BooleanArray) {
        BOOLEAN_ARRAY.mark(stream)
        condArr.forEach { stream.write(if (it) 1 else 0) }
    }

    fun write(bArr: ByteArray) {
        BYTE_ARRAY.mark(stream)
        writeBytesUnmarked(Int.SIZE_BYTES) { putInt(bArr.size) }
        stream.write(bArr)
    }

    fun write(cArr: CharArray) {
        CHAR_ARRAY.mark(stream)
        writeArrayUnmarked(Char.SIZE_BYTES, cArr.size) { cArr.forEach { putChar(it) } }
    }

    fun write(nArr: ShortArray) {
        SHORT_ARRAY.mark(stream)
        writeArrayUnmarked(Short.SIZE_BYTES, nArr.size) { nArr.forEach { putShort(it) } }
    }

    fun write(nArr: IntArray) {
        INT_ARRAY.mark(stream)
        writeArrayUnmarked(Int.SIZE_BYTES, nArr.size) { nArr.forEach { putInt(it) } }
    }

    fun write(nArr: LongArray) {
        LONG_ARRAY.mark(stream)
        writeArrayUnmarked(Long.SIZE_BYTES, nArr.size) { nArr.forEach { putLong(it) } }
    }

    fun write(nArr: FloatArray) {
        FLOAT_ARRAY.mark(stream)
        writeArrayUnmarked(Float.SIZE_BYTES, nArr.size) { nArr.forEach { putFloat(it) } }
    }

    fun write(nArr: DoubleArray) {
        DOUBLE_ARRAY.mark(stream)
        writeArrayUnmarked(Double.SIZE_BYTES, nArr.size) { nArr.forEach { putDouble(it) } }
    }

    fun write(s: String) {  // marker, size, char...
        STRING.mark(stream)
        writeUnmarked(s)
    }

    /**
     * Writes all members in array according to the protocol of each instance.
     * @throws MissingProtocolException the type of any member of [nullableArr] is not null, and
     * is not a top-level class or does not have a defined protocol
     */
    inline fun <T, reified N : T & Any> write(nullableArr: Array<out T>) {  // marker, size, (marker, member)...
        NULLABLE_ARRAY.mark(stream)
        val classRef = N::class
        writeUnmarked(protocolNameOf(classRef))
        writeUnmarked(nullableArr.size)
        nullableArr.forEach {
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
    inline fun <reified T : Any> write(objArr: Array<out T>) {  // marker, size, (marker, member)...
        OBJECT_ARRAY.mark(stream)
        val classRef = T::class
        writeUnmarked(protocolNameOf(classRef))
        writeUnmarked(objArr.size)
        objArr.forEach { write(it) }
    }

    /**
     * Writes all members in the list according the protocol of each.
     * @throws MissingProtocolException any member of [nullableList] is not null, and
     * its type is not top-level class or does not have a defined protocol
     */
    inline fun <reified T> write(nullableList: List<T>) {   // marker, size, (marker, member)...
        NULLABLE_LIST.mark(stream)
        writeUnmarked(nullableList.size)
        nullableList.forEach {
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
    inline fun <reified T : Any> write(list: List<T>) { // marker, size, (marker, member)...
        LIST.mark(stream)
        writeUnmarked(list.size)
        list.forEach { write(it) }
    }

    /**
     * Writes all members in the iterable object according the protocol of each instance as a list.
     * The caller must ensure that the object has a finite number of members.
     * @throws MissingProtocolException any member of [nullableIter] is not null, and
     * its type is not top-level class or does not have a defined protocol
     */
    inline fun <reified T> write(nullableIter: Iterable<T>) {  // begin, (marker, member)..., end
        NULLABLE_BEGIN.mark(stream)
        nullableIter.forEach {
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
    inline fun <reified T : Any> write(iter: Iterable<T>) { // begin, (marker, member)..., end
        ITERABLE_BEGIN.mark(stream)
        iter.forEach { write(it) }
        SENTINEL.mark(stream)
    }

    /**
     * Writes the object in binary format according to the protocol of its type, or null.
     * @throws MissingProtocolException if [nullable] is not null, and
     * its type is not a top-level class or does not have a defined protocol
     */
    fun write(nullable: Any?) {
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
    fun write(obj: Any) {
        OBJECT.mark(stream)
        val classRef = obj::class
        writeUnmarked(protocolNameOf(classRef))
        resolveProtocol(classRef).onWrite(this, obj)
    }

    override fun flush() = stream.flush()
    override fun close() = stream.close()

    @PublishedApi
    internal fun writeUnmarked(n: Int) = writeBytesUnmarked(Int.SIZE_BYTES) { putInt(n) }

    @PublishedApi
    internal fun writeUnmarked(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        write(bytes.size)
        stream.write(bytes)
    }

    @PublishedApi
    internal inline fun writeBytesUnmarked(count: Int, write: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(count)
        write(buffer)
        stream.write(buffer.array())
    }

    // marker, size, member...
    private inline fun writeArrayUnmarked(memberBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1*Int.SIZE_BYTES + size*memberBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }
}