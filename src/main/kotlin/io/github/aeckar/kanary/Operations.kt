@file:JvmMultifileClass
@file:JvmName("KanaryKt")
package io.github.aeckar.kanary

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
 * Instantiates a read operation.
 * @return the given [read operation][ProtocolBuilder.read]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T> readOf(noinline read: TypedReadOperation<T>) = read

/**
 * Instantiates a write operation.
 * @return the given [write operation][ProtocolBuilder.write]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T> writeOf(noinline write: TypedWriteOperation<T>) = write

internal class FallbackReadOperation<T>(read: TypedReadOperation<T>): (ObjectDeserializer) -> T by read

internal class StaticWriteOperation<T>(write: TypedWriteOperation<T>) : (Serializer, T) -> Unit by write