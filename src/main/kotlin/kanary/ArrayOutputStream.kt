package kanary

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

private const val DEFAULT_SIZE = 32

internal class ArrayOutputStream(initialCapacity: Int = DEFAULT_SIZE) : OutputStream() {
    var size = 0
    var bytes = ByteArray(initialCapacity)

    // Does not perform bounds checking
    fun acceptNBytes(from: InputStream, len: Int) {
        repeat(len) { write(from.read().toByte()) }
    }

    fun asInputStream(): InputStream = ArrayInputStream(this.bytes)

    override fun write(p0: Int) {
        ensureCapacity(size + 1)
        write(p0.toByte())
    }

    override fun write(b: ByteArray) = super.write(b, 0, b.size) // Uphold contract

    override fun write(b: ByteArray, off: Int, len: Int) {
        ensureCapacity(size + len)
        repeat(len) { write(b[off + it]) }
    }

    private fun ensureCapacity(required: Int) {
        if (required <= bytes.size) {
            return
        }
        val new = ByteArray(size * 2)
        System.arraycopy(bytes, 0, new, 0, size)
        bytes = new
    }

    private fun write(b: Byte) {
        bytes[size] = b
        ++size
    }
}

private class ArrayInputStream(private val bytes: ByteArray) : InputStream() {
    private var cursor = 0

    override fun read(b: ByteArray) = read(b, 0, b.size)    // Uphold contract
    override fun read(b: ByteArray, off: Int, len: Int) = readNBytes(b, off, len)
    override fun readAllBytes(): ByteArray = readNBytes(available())
    override fun readNBytes(len: Int): ByteArray = ByteArray(len).apply { readNBytes(this, 0, size) }

    override fun read(): Int {
        return try {
            bytes[cursor].toInt().also { ++cursor }
        } catch (_: IndexOutOfBoundsException) {
            -1
        }
    }

    override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        repeat(len) { b[off + it] = read().toByte() }
        return len
    }

    override fun skip(n: Long): Long {
        if (n < 0) {
            return 0L
        }
        val startPosition = cursor
        cursor = (cursor.toLong() - n).coerceAtLeast(0).toInt()
        return (startPosition - cursor).toLong()
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
