package kanary

import kotlin.reflect.KClass

internal typealias ReadOperation<T> = BinaryInput.() -> T
internal typealias WriteOperation<T> = BinaryOutput.(T) -> Unit

@PublishedApi
internal val definedProtocols = mutableMapOf<String,Protocol<*>>()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T : Any> resolveProtocol(classRef: KClass<out T>): Protocol<T> {
    return definedProtocols.getOrDefault(protocolNameOf(classRef)) {
        throw MissingProtocolException(classRef)
    } as Protocol<T>
}

@PublishedApi
internal fun <T : Any> protocolNameOf(classRef: KClass<out T>) : String {
    if (classRef.isAbstract) {
        throw InvalidProtocolException(classRef)
    }
    return classRef.qualifiedName ?: throw InvalidProtocolException(classRef)
}

/**
 * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
 * operations of a top-level class can be defined.
 * @throws MissingProtocolException [T] is not a top-level class
 * @throws ReassignmentException either of the operations are defined twice,
 * or this is called more than once for type [T]
 */
inline fun <reified T : Any> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit) {
    val name = protocolNameOf(T::class)
    if (name in definedProtocols) {
        throw ReassignmentException("Protocol for class '$name' defined twice")
    }
    val builderScope = ProtocolBuilderScope<T>()
    builder(builderScope)
    definedProtocols[name] = with (builderScope) { Protocol(read, write) }   // thread-safe
}

class Protocol<T : Any>(val onRead: ReadOperation<out T>, val onWrite: WriteOperation<T>)

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilderScope<T> {
    /**
     * The binary read operation when [BinaryInput.read] is called with an object of class [T].
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: BinaryInput.() -> T by AssignOnce()

    /**
     * The binary write operation when [BinaryOutput.write] is called with an object of class [T]
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: BinaryOutput.(T) -> Unit by AssignOnce()
}