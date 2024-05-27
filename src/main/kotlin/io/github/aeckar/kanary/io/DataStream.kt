package io.github.aeckar.kanary.io

import java.io.Closeable
import java.nio.ByteBuffer

private const val MAX_PRIMITIVE_BYTES = Double.SIZE_BYTES

/**
 * Provides type-specific operations for the underlying stream.
 */
internal sealed class DataStream {
    abstract val raw: Closeable

    /**
     * Temporarily stores primitives during reading and writing.
     */
    protected val buffer: ByteArray = ByteArray(MAX_PRIMITIVE_BYTES)

    protected fun ByteArray.asByteBuffer(): ByteBuffer = ByteBuffer.wrap(this)
}