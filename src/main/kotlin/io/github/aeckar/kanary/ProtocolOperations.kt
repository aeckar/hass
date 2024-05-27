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
// TODO make return api-specific
// TODO read = fallback {} => fallback read {}; write = static {} => static write {}
inline fun <T> readOf(crossinline readObject: TypedReadOperation<T>): TypedReadOperation<T> {
    return (@JvmSerializableLambda { readObject() })
}

/**
 * Instantiates a write operation.
 * @return the given [write operation][ProtocolBuilder.write]
 */
inline fun <T> writeOf(crossinline writeObject: TypedWriteOperation<T>): TypedWriteOperation<T> {
    return (@JvmSerializableLambda { writeObject(it) })
}

fun interface IWriteOperation {
    operator fun Serializer.invoke(obj: Any?)
}

internal class FallbackReadOperation<T>(read: TypedReadOperation<T>): (ObjectDeserializer) -> T by read

internal class StaticWriteOperation<T>(write: TypedWriteOperation<T>) : (Serializer, T) -> Unit by write