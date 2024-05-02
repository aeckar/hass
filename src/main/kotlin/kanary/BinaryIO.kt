package kanary

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.lang.Float.intBitsToFloat
import java.lang.Double.longBitsToDouble
import java.lang.Double.doubleToRawLongBits
import java.lang.Float.floatToRawIntBits

/**
 * @return a new binary input stream associated with this stream
 */
fun InputStream.binary() = BinaryInput(this)

/**
 * @return a new binary output stream associated with this stream
 */
fun OutputStream.binary() = BinaryOutput(this)

/**
 * A binary stream with functions for reading primitives or classes with a [protocol] in Kanary format.
 * Does not support marking.
 */
@JvmInline
value class BinaryInput internal constructor(private val stream: InputStream) : Closeable {
    fun readBoolean() = stream.read() == 1
    fun readByte() = stream.read().toByte()
    fun readChar() = readBytes(Char.SIZE_BYTES).toInt().toChar()
    fun readShort() = readBytes(Short.SIZE_BYTES).toShort()
    fun readInt() = readBytes(Int.SIZE_BYTES).toInt()
    fun readLong() = readBytes(Long.SIZE_BYTES)
    fun readFloat() = intBitsToFloat(readInt())
    fun readDouble() = longBitsToDouble(readLong())
    fun readString() = String(stream.readNBytes(readInt())) // size, followed by literal

    inline fun <reified T> read(): T {
        val className = T::class.qualifiedName ?: throw IllegalArgumentException("Type is not a top-level class")
        return try {
            definedProtocols.getValue(className).first(this) as T
        } catch (e: NoSuchElementException) {
            throw IllegalArgumentException("Class '$className' does not have a binary I/O protocol", e)
        }
    }

    override fun close() {
        stream.close()
    }

    private fun readBytes(count: Int): Long {
        var result = 0L
        val lastIndex = count - 1
        repeat(count) {
            result = result or (readByte().toUByte().toLong() shl (lastIndex - it)*Byte.SIZE_BITS)
        }
        return result
    }
}

/**
 * A binary stream with functions for writing primitives or classes with a [protocol] in Kanary format.
 * Does not support marking.
 */
@JvmInline
value class BinaryOutput internal constructor(private val stream: OutputStream) : Closeable {
    fun write(cond: Boolean) = stream.write(if (cond) 1 else 0)
    fun write(b: Byte) = stream.write(b.toInt())
    fun write(c: Char) = c.code.toLong().writeBytes(Char.SIZE_BYTES)
    fun write(n: Short) = n.toLong().writeBytes(Short.SIZE_BYTES)
    fun write(n: Int) = n.toLong().writeBytes(Int.SIZE_BYTES)
    fun write(n: Long) = n.writeBytes(Long.SIZE_BYTES)
    fun write(fp: Float) = write(floatToRawIntBits(fp))
    fun write(fp: Double) = write(doubleToRawLongBits(fp))

    fun write(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        write(bytes.size)
        stream.write(bytes)
    }

    @Suppress("UNCHECKED_CAST")
    fun write(obj: Any) {
        val className = obj::class.qualifiedName ?: throw IllegalArgumentException("Type is not a top-level class")
        try {
            (definedProtocols.getValue(className).second as WriteOperation<Any>)(this, obj)
        } catch (e: NoSuchElementException) {
            throw IllegalArgumentException("Class '$className' does not have a binary I/O protocol", e)
        }
    }

    override fun close() {
        stream.close()
    }

    private fun Long.writeBytes(count: Int) {
        val lastIndex = count - 1
        repeat(count) {
            stream.write((this ushr (lastIndex - it)*Byte.SIZE_BITS).toUByte().toInt())
        }
    }
}