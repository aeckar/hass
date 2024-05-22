package kanary

import java.io.IOException

// TODO test static and fallback

/**
 * Lambda specified by [read operation][ProtocolBuilder.read].
 */
typealias TypedReadOperation<T> = ObjectDeserializer.() -> T

/**
 * Lambda specified by write operation.
 */
typealias TypedWriteOperation<T> = Serializer.(T) -> Unit

internal typealias ReadOperation = ObjectDeserializer.() -> Any?
internal typealias WriteOperation = Serializer.(Any?) -> Unit

/**
 * @return a locally defined [protocol][Protocol]
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> define(
    noinline read: TypedReadOperation<out T>? = null,
    noinline write: TypedWriteOperation<in T>? = null
): Protocol {
    return LocalTypeProtocol(read, write as WriteOperation)
}

/**
 * Applies the 'static' modifier to the given write operation.
 * @return the supplied write operation
 */
fun <T> static(write: TypedWriteOperation<in T>): TypedWriteOperation<in T> = StaticWriteOperation(write)

/**
 * Applies the 'fallback' modifier to the given read operation.
 * @return the supplied [read operation][ProtocolBuilder.read]
 */
fun <T> fallback(read: TypedReadOperation<out T>): TypedReadOperation<out T> = FallbackReadOperation(read)

/**
 * A locally defined protocol.
 *
 * Delegates the protocol of the type whose companion implements this interface
 * to the locally [defined][define] protocol by which this interface is delegated to.
 * Doing so enables serialization using private members.
 *
 * If a schema explicitly defines the protocol of a type, that definition is used instead.
 * It is possible for a local protocol to define one operation and
 * a protocol of the same type defined in a schema to define another operation
 */
interface Protocol {
    val hasFallback: Boolean
    val hasStatic: Boolean
    val read: ReadOperation?
    val write: WriteOperation?
}

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException @PublishedApi internal constructor(message: String) : IOException(message)

@PublishedApi
internal class LocalTypeProtocol(
    override val read: ReadOperation?,
    override val write: WriteOperation?
) : Protocol {
    override val hasFallback = read is FallbackReadOperation
    override val hasStatic = write is StaticWriteOperation
}

private class FallbackReadOperation<T>(read: TypedReadOperation<out T>): (ObjectDeserializer) -> T by read
private class StaticWriteOperation<T>(write: TypedWriteOperation<in T>) : (Serializer, T) -> Unit by write