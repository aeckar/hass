package io.github.aeckar.kanary.io

import io.github.aeckar.kanary.TypeFlagMismatchException
import java.io.EOFException
import java.io.InputStream
import java.io.ObjectInputStream
import java.nio.ByteBuffer

/**
 * Provides checked, type-specific operations for [InputStream], throwing [EOFException] if stream is exhausted.
 *
 * Operations return the appropriate primitive value, primitive array, string, or [TypeFlag].
 * This class is analogous to a decoder.
 */
internal class InputDataStream(override val raw: InputStream) : DataStream() {
    /**
     * Equivalent to [raw].read().
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    fun readRawByte() = raw.read().also { if (it == -1) throwEOFException() }

    // ------------------------------ flag read/validation ------------------------------

    fun readTypeFlag() = TypeFlag.entries[readRawByte()]

    fun ensureTypeFlag(expected: TypeFlag) {
        val found = readTypeFlag()
        if (found !== expected) {
            throw TypeFlagMismatchException("Type flag '$expected' expected, but found '$found'")
        }
    }

    // ------------------------------ primitive read operations ------------------------------

    fun readBoolean() = readRawByte() == 1
    fun readChar() = readPrimitive(Char.SIZE_BYTES).char
    fun readShort() = readPrimitive(Short.SIZE_BYTES).short
    fun readInt() = readPrimitive(Int.SIZE_BYTES).int
    fun readLong() = readPrimitive(Long.SIZE_BYTES).long
    fun readFloat() = readPrimitive(Float.SIZE_BYTES).float
    fun readDouble() = readPrimitive(Double.SIZE_BYTES).double

    /**
     * Allows reading of bytes with possible value of -1.
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    fun readByte() = readPrimitive(1)[0]

    // ------------------------------ object read operations ------------------------------

    fun readPrimitiveArray(sizeBytes: Int) = raw.readNBytes(readInt() * sizeBytes).asByteBuffer()

    @Suppress("UNCHECKED_CAST")
    fun <F> readSerializable() = ObjectInputStream(raw).readObject() as F

    fun readType() = Class.forName(readString()).kotlin

    fun readString(): String {
        val lengthInBytes = readInt()
        val bytes = ByteArray(lengthInBytes)
        val readSize = raw.read(bytes, /* off = */ 0, lengthInBytes)
        if (readSize == -1) {
            throwEOFException()
        }
        return String(bytes)
    }

    // ------------------------------------------------------------------------

    private fun readPrimitive(len: Int): ByteBuffer {
        val readSize = raw.read(buffer, 0, len)
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