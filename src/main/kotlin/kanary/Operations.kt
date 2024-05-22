package kanary

import java.io.IOException

/**
 * Lambda specified by [read][ProtocolBuilder.read].
 */
typealias TypedReadOperation<T> = ObjectDeserializer.() -> T

internal typealias ReadOperation = ObjectDeserializer.() -> Any?
internal typealias WriteOperation = Serializer.(Any?) -> Unit

/**
 * Lambda specified by write.
 */
typealias TypedWriteOperation<T> = Serializer.(T) -> Unit

/**
 * Allows the protocol of the implementing type to delegate its write operation to each specific instance of that type.
 * Necessary for serializing private members.
 */
interface Writable {
    fun Serializer.write()
}

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException @PublishedApi internal constructor(message: String) : IOException(message)