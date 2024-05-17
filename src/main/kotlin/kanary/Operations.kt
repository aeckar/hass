package kanary

import java.io.IOException

/**
 * Lambda specified by [read][ProtocolBuilder.read].
 */
typealias ReadOperation<T> = PolymorphicDeserializer.() -> T

/**
 * Lambda specified by [read][ProtocolBuilder.read] when [noinherit][ProtocolBuilder.noinherit] is used as a modifier.
 */
typealias SimpleReadOperation<T> = Deserializer.() -> T

/**
 * Lambda specified by write.
 */
typealias WriteOperation<T> = Serializer.(T) -> Unit

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