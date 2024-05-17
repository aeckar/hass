package kanary

import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val DEFAULT_SIZE = 32

// Unlike java.io.ByteOutputStream, is not synchronized
internal class ArrayOutputStream(initialCapacity: Int = DEFAULT_SIZE) : OutputStream() {
    var size = 0
    val capacity inline get() = bytes.size
    private var bytes = ByteArray(initialCapacity)

    fun writeTo(stream: OutputStream) {
        stream.write(bytes, 0, size)
    }

    // Does not perform bounds checking
    fun acceptNBytes(from: InputStream, len: Int) {
        ensureCapacity(size + len)
        repeat(len) { writeUnchecked(from.read().toByte()) }
    }

    // After invocation, integrity of state is not guaranteed
    fun asInputStream(): InputStream = ArrayInputStream(size, bytes)

    override fun write(p0: Int) {
        ensureCapacity(size + 1)
        writeUnchecked(p0.toByte())
    }

    override fun write(b: ByteArray) = write(b, 0, b.size)  // Uphold contract

    override fun write(b: ByteArray, off: Int, len: Int) {
        ensureCapacity(size + len)
        repeat(len) { writeUnchecked(b[off + it]) }
    }

    private fun ensureCapacity(required: Int) {
        if (required <= bytes.size) {
            return
        }
        val newSize = bytes.size.toLong() * 2L
        if (newSize > Int.MAX_VALUE) {
            throw IOException("Object is too large to be serialized at one time")
        }
        val new = ByteArray(newSize.toInt())
        System.arraycopy(bytes, 0, new, 0, bytes.size)
        bytes = new
    }

    private fun writeUnchecked(b: Byte) {
        bytes[size] = b
        ++size
    }
}

private class ArrayInputStream(private val size: Int, private val bytes: ByteArray) : InputStream() {
    private var cursor = 0

    override fun read(b: ByteArray) = read(b, 0, b.size)    // Uphold contract
    override fun readAllBytes(): ByteArray = readNBytes(available())
    override fun readNBytes(len: Int): ByteArray = ByteArray(len).apply { readNBytes(this, 0, this@apply.size) }

    override fun read(): Int {
        return try {
            bytes[cursor].toInt().also { ++cursor }
        } catch (_: IndexOutOfBoundsException) {
            -1
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val readSize = readNBytes(b, off, len)
        return if (readSize == 0) -1 else readSize
    }

    override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        val readSize = len.coerceAtMost(size - cursor)
        System.arraycopy(bytes, cursor, b, off, readSize)
        cursor += readSize
        return readSize
    }

    override fun skip(n: Long): Long {
        if (n < 0) {
            return 0L
        }
        val startPos = cursor
        cursor += n.toInt()
        return (cursor.coerceAtMost(size) - startPos).toLong()
    }

    override fun skipNBytes(n: Long) {
        if (n < 0) {
            throw IllegalArgumentException("Negative bytes read (n = $n)")
        }
        val skipped = skip(n)
        if (skipped != n) {
            throw EOFException("End of byte array reached")
        }
    }

    override fun available() = bytes.size - cursor
    override fun markSupported() = false
}
