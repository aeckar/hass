package kanary

internal typealias ReadOperation<T> = BinaryInput.() -> T
internal typealias WriteOperation<T> = BinaryOutput.(T) -> Unit

@PublishedApi
internal val definedProtocols = mutableMapOf<String,Pair<ReadOperation<*>,WriteOperation<*>>>()

/**
 * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
 * operations of a top-level class can be defined.
 * @throws ReassignmentException either of the operations are defined twice
 */
inline fun <reified T> T.protocol(builder: ProtocolBuilderScope<T>.() -> Unit) {
    val className = T::class.qualifiedName ?: throw IllegalArgumentException("Only top-level classes can be assigned a protocol")
    if (className in definedProtocols) {
        return
    }
    val builderScope = ProtocolBuilderScope<T>()
    builder(builderScope)
    try {
        definedProtocols[className] = with (builderScope) { onRead to onWrite }   // thread-safe
    } catch (_: NullPointerException) {
        throw IllegalArgumentException("Binary I/O only supported for top-level classes")
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