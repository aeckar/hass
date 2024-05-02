package kanary

internal typealias ReadOperation<T> = BinaryInput.() -> T
internal typealias WriteOperation<T> = BinaryOutput.(T) -> Unit

@PublishedApi
internal val definedProtocols = mutableMapOf<String,Pair<ReadOperation<*>,WriteOperation<*>>>()

/**
 * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
 * operations of a top-level class can be defined.
 * @throws MissingProtocolException [T] is not a top-level class
 * @throws ReassignmentException either of the operations are defined twice,
 * or this is called more than once for type [T]
 */
inline fun <reified T> protocol(builder: ProtocolBuilderScope<T>.() -> Unit) {
    val className = T::class.qualifiedName ?: throw MissingProtocolException("Only top-level classes can be assigned a protocol")
    if (className in definedProtocols) {
        throw ReassignmentException("Protocol for class '$className' defined twice")
    }
    val builderScope = ProtocolBuilderScope<T>()
    builder(builderScope)
    try {
        definedProtocols[className] = with (builderScope) { onRead to onWrite }   // thread-safe
    } catch (_: NullPointerException) {
        throw MissingProtocolException("Binary I/O only supported for top-level classes")
    }
}


/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilderScope<T> {
    var onRead: ReadOperation<T> by AssignOnce()
    var onWrite: WriteOperation<T> by AssignOnce()

    /**
     * Defines the binary read operation when [BinaryInput.read] is called with an object of class [T]
     * @throws ReassignmentException this is called more than once in a single scope
     */
    fun read(onRead: ReadOperation<T>) {
        this.onRead = onRead
    }

    /**
     * Defines the binary write operation when [BinaryOutput.write] is called with an object of class [T]
     * @throws ReassignmentException this is called more than once in a single scope
     */
    fun write(onWrite: WriteOperation<T>) {
        this.onWrite = onWrite
    }
}