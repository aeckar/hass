package io.github.aeckar.kanary.utils

/**
 * A [ByteArray] with a single element.
 */
@JvmInline
internal value class SingletonByteArray private constructor(val array: ByteArray) {
    /**
     * The value of the single element in the underlying array.
     */
    val value get() = array.single()

    constructor() : this(ByteArray(1))
}