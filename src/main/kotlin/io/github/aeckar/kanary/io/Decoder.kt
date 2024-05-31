package io.github.aeckar.kanary.io

import io.github.aeckar.kanary.TypeFlagMismatchException
import io.github.aeckar.kanary.reflect.Type
import java.io.EOFException
import java.io.InputStream
import java.io.ObjectInputStream
import java.nio.ByteBuffer

/**
 * Decodes type-specific data from Kanary format, throwing [EOFException] if the underlying stream is exhausted.
 */
internal class Decoder(override val stream: InputStream) : DataStream() {
    /**
     * Equivalent to [stream].read().
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    fun decodeRawByte() = stream.read().also { if (it == -1) throwEOFException() }

    // ------------------------------ flag read/validation ------------------------------

    fun decodeTypeFlag() = TypeFlag.entries[decodeRawByte()]

    fun ensureTypeFlag(expected: TypeFlag) {
        val found = decodeTypeFlag()
        if (found !== expected) {
            throw TypeFlagMismatchException("Type flag '$expected' expected, but found '$found'")
        }
    }

    // ------------------------------ primitive read operations ------------------------------

    fun decodeBoolean() = decodeRawByte() == 1
    fun decodeChar() = decodePrimitive(Char.SIZE_BYTES).char
    fun decodeShort() = decodePrimitive(Short.SIZE_BYTES).short
    fun decodeInt() = decodePrimitive(Int.SIZE_BYTES).int
    fun decodeLong() = decodePrimitive(Long.SIZE_BYTES).long
    fun decodeFloat() = decodePrimitive(Float.SIZE_BYTES).float
    fun decodeDouble() = decodePrimitive(Double.SIZE_BYTES).double

    /**
     * Allows reading of bytes with possible value of -1.
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    fun decodeByte() = decodePrimitive(1)[0]

    // ------------------------------ object read operations ------------------------------

    fun decodePrimitiveArray(sizeBytes: Int) = stream.readNBytes(decodeInt() * sizeBytes).asByteBuffer()

    @Suppress("UNCHECKED_CAST")
    fun <F> decodeSerializable() = ObjectInputStream(stream).readObject() as F

    fun decodeType() = Type(decodeString())

    fun decodeString(): String {
        val lengthInBytes = decodeInt()
        val bytes = ByteArray(lengthInBytes)
        val readSize = stream.read(bytes, /* off = */ 0, lengthInBytes)
        if (readSize == -1) {
            throwEOFException()
        }
        return String(bytes)
    }

    // ------------------------------------------------------------------------

    private fun decodePrimitive(len: Int): ByteBuffer {
        val readSize = stream.read(buffer, 0, len)
        if (readSize == -1) {
            throwEOFException()
        }
        return buffer.asByteBuffer()
    }

    private fun throwEOFException(): Nothing {
        throw EOFException(
            "Attempted read of object after deserializer was exhausted. " +
                    "Ensure supertype write operations are not overridden by 'static' write")
    }
}