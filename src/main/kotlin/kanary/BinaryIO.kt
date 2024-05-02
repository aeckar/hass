package kanary

import java.io.Closeable
import java.io.Flushable
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
 * A binary stream with functions for reading primitives or classes with a [protocolOf] in Kanary format.
 * Does not support marking.
 * Calling [close] also closes the underlying stream.
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

    /**
     * Reads an object of type [T] from binary according to the protocol of its type.
     * @throws MissingProtocolException [T] is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> read() = resolveProtocol(T::class).onRead(this)

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
 * A binary stream with functions for writing primitives or classes with a [protocolOf] in Kanary format.
 * Does not support marking.
 * Calling [close] also closes the underlying stream.
 */
@JvmInline
value class BinaryOutput internal constructor(private val stream: OutputStream) : Closeable, Flushable {
    fun write(cond: Boolean) = stream.write(if (cond) 1 else 0)
    fun write(b: Byte) = stream.write(b.toInt())
    fun write(c: Char) = c.code.toLong().writeBytes(Char.SIZE_BYTES)
    fun write(n: Short) = n.toLong().writeBytes(Short.SIZE_BYTES)
    fun write(n: Int) = n.toLong().writeBytes(Int.SIZE_BYTES)
    fun write(n: Long) = n.writeBytes(Long.SIZE_BYTES)
    fun write(fp: Float) = write(floatToRawIntBits(fp))
    fun write(fp: Double) = write(doubleToRawLongBits(fp))

    fun write(s: String) {  // size, followed by literal
        val bytes = s.toByteArray(Charsets.UTF_8)
        write(bytes.size)
        stream.write(bytes)
    }

    /**
     * Writes the object in binary format according to the protocol of its type.
     * @throws MissingProtocolException the type of [obj] is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(obj: T) = resolveProtocol(obj::class).onWrite(this, obj)

    override fun flush() {
        stream.flush()
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