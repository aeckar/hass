package io.github.aeckar.kanary.utils

import java.io.EOFException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Provides checked operations for [InputStream], throwing [EOFException] if stream is exhausted.
 */
@JvmInline
internal value class CheckedInputStream(private val stream: InputStream) {
    /**
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    fun read() = stream.read().also { if (it == -1) throwEOFException() }

    /**
     * Allows reading of bytes with possible value of -1.
     * @return the next byte in the stream
     * @throws EOFException stream is exhausted
     */
    fun readRaw(buffer: SingletonByteArray): Byte {
        val readSize = stream.read(buffer.array)
        if (readSize == -1) {
            throwEOFException()
        }
        return buffer.value
    }

    /**
     * @return the next [len] bytes in the stream
     * @throws EOFException stream is exhausted
     */
    fun readNBytes(len: Int): ByteArray = stream.readNBytes(len).also { if (it.isEmpty()) throwEOFException() }

    /**
     * @return the next [len] bytes in the stream as a [ByteBuffer]
     * @throws EOFException stream is exhausted
     */
    fun readToBuffer(len: Int): ByteBuffer = ByteBuffer.wrap(readNBytes(len))

    private fun throwEOFException(): Nothing {
        throw EOFException(
                "Attempted read of object after deserializer was exhausted. " +
                "Ensure supertype write operations are not overridden by 'static' write")
    }
}