package kanary

import java.io.IOException

internal typealias ReadOperation<T> = PolymorphicDeserializer.() -> T
internal typealias WriteOperation<T> = Serializer.(T) -> Unit
internal typealias SimpleReadOperation<T> = Deserializer.() -> T

interface Writable {
    fun Serializer.write()
}

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException @PublishedApi internal constructor(message: String) : IOException(message)