package com.github.aeckar.kanary

import java.io.IOException

/**
 * Lambda specified by [read operation][ProtocolBuilder.read].
 */
typealias TypedReadOperation<T> = ObjectDeserializer.() -> T

/**
 * Lambda specified by [write operation][ProtocolBuilder.write].
 */
typealias TypedWriteOperation<T> = Serializer.(T) -> Unit

/**
 * A generic [TypedReadOperation]
 */
internal typealias ReadOperation = ObjectDeserializer.() -> Any?

/**
 * A generic [TypedWriteOperation].
 */
internal typealias WriteOperation = Serializer.(Any?) -> Unit

/**
 * @return a locally defined [protocol][Protocol]
 */
inline fun <reified T> define(
    noinline read: TypedReadOperation<T>? = null,
    noinline write: TypedWriteOperation<T>? = null
) = Protocol(read, write)

/**
 * Applies the 'static' modifier to the given write operation.
 * @return the supplied write operation
 */
fun <T> static(write: TypedWriteOperation<T>): TypedWriteOperation<T> = StaticWriteOperation(write)

/**
 * Applies the '[fallback][ProtocolBuilder.fallback]' modifier to the given read operation.
 * @return the supplied [read operation][ProtocolBuilder.read]
 */
fun <T> fallback(read: TypedReadOperation<T>): TypedReadOperation<T> = FallbackReadOperation(read)

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException @PublishedApi internal constructor(message: String) : IOException(message)

internal class FallbackReadOperation<T>(read: TypedReadOperation<T>): (ObjectDeserializer) -> T by read

internal class StaticWriteOperation<T>(write: TypedWriteOperation<T>) : (Serializer, T) -> Unit by write