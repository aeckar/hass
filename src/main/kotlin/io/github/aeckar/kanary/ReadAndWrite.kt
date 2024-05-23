package io.github.aeckar.kanary

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
 * A [TypedReadOperation] with its type parameter erased.
 */
internal typealias ReadOperation = ObjectDeserializer.() -> Any?

/**
 * A [TypedWriteOperation] with its type parameter erased.
 */
internal typealias WriteOperation = Serializer.(Any?) -> Unit

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException @PublishedApi internal constructor(message: String) : IOException(message)

internal class FallbackReadOperation<T>(read: TypedReadOperation<T>): (ObjectDeserializer) -> T by read

internal class StaticWriteOperation<T>(write: TypedWriteOperation<T>) : (Serializer, T) -> Unit by write