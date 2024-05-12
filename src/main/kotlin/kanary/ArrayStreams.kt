package kanary

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

private const val DEFAULT_SIZE = 32

internal sealed interface ArrayStream {
    val size: Int
    val bytes: ByteArray

    companion object {
        val EMPTY_BYTES = ByteArray(0)
    }
}

internal class ArrayOutputStream(initialCapacity: Int = DEFAULT_SIZE) : OutputStream(), ArrayStream {
    override var size = 0
    override var bytes = ByteArray(initialCapacity)

    // Does not perform bounds checking
    fun acceptNBytes(from: InputStream, len: Int) {
        repeat(len) { write(from.read().toByte()) }
    }

    fun asInputStream() = ArrayInputStream(size, bytes)
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

    object EMPTY : OutputStream(), ArrayStream {
        override val size = 0
        override val bytes get() = ArrayStream.EMPTY_BYTES

        override fun write(p0: Int) = throw UnsupportedOperationException("Cannot write to empty array stream")
    }
}

internal open class ArrayInputStream(
    override var size: Int,
    override var bytes: ByteArray
) : InputStream(), ArrayStream {

    override fun read(): Int {
        return try {
            bytes[size - 1].toInt()
            --size
        } catch (_: IndexOutOfBoundsException) {
            -1
        }
    }

    override fun read(b: ByteArray) = read(b, 0, b.size)    // Uphold contract
    override fun read(b: ByteArray, off: Int, len: Int) = readNBytes(b, off, len)
    override fun readAllBytes(): ByteArray = readNBytes(available())
    override fun readNBytes(len: Int): ByteArray = ByteArray(len).apply { readNBytes(this, 0, size) }

    override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        repeat(len) { b[off + it] = read().toByte() }
        return len
    }

    override fun skip(n: Long): Long {
        if (n < 0) {
            return 0L
        }
        val start = size
        size = (size.toLong() - n).coerceAtLeast(0).toInt()
        return (start - size).toLong()
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

    override fun available() = bytes.size - size
    override fun markSupported() = false

    object EMPTY : InputStream(), ArrayStream {
        override val size = 0
        override val bytes get() = ArrayStream.EMPTY_BYTES

        override fun read() = throw UnsupportedOperationException("Cannot read from empty array stream")
    }
}