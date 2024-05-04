package kanary

import kotlin.reflect.KClass

internal typealias ReadOperation<T> = BinaryInput.() -> T
internal typealias WriteOperation<T> = BinaryOutput.(T) -> Unit

/**
 * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
 * operations of a top-level class can be defined.
 * @throws MissingProtocolException [T] is not a top-level class
 * @throws ReassignmentException either of the operations are defined twice,
 * or this is called more than once for type [T]
 */
inline fun <reified T : Any> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit): ProtocolSpecification<T> {
    val className = protocolNameOf(T::class)
    if (className in definedProtocols) {
        throw ReassignmentException("Protocol for class '$className' defined twice")
    }
    val builderScope = ProtocolBuilderScope<T>()
    builder(builderScope)
    return ProtocolSpecification(className, builderScope.read, builderScope.write)
}

@PublishedApi
internal val definedProtocols = mutableMapOf<String,Protocol<*>>()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T : Any> resolveProtocol(classRef: KClass<out T>): Protocol<T> {
    return definedProtocols[protocolNameOf(classRef)]?.let { it as Protocol<T> }
            ?: throw MissingProtocolException(classRef)
}

@PublishedApi
internal fun <T : Any> protocolNameOf(classRef: KClass<out T>) : String {
    return classRef.qualifiedName ?: throw InvalidProtocolException(classRef)
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilderScope<T> {
    /**
     * The binary read operation when [BinaryInput.readObject] is called with an object of class [T].
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: BinaryInput.() -> T by AssignOnce()

    /**
     * The binary write operation when [BinaryOutput.writeOr] is called with an object of class [T]
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: BinaryOutput.(T) -> Unit by AssignOnce()
}

/**
 * Specified a [Protocol] without assigning it to its class.
 */
class ProtocolSpecification<T : Any>(
    private val className: String,
    private val onRead: ReadOperation<out T>,
    private val onWrite: WriteOperation<in T>
) {
    /**
     * Assigns the specified protocol to its class.
     * This function should only be invoked ONCE and within the companion of the class the protocol is specific to.
     */
    fun assign() {
        definedProtocols[className] = Protocol(onRead, onWrite)
    }
}

/**
 * A binary I/O protocol defining read and write operations when this object is
 * serialized using [BinaryOutput] or deserialized using [BinaryInput].
 */
internal class Protocol<T : Any>(val onRead: ReadOperation<out T>, val onWrite: WriteOperation<in T>)

