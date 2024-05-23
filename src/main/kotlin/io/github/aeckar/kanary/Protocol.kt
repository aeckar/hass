package io.github.aeckar.kanary

import io.github.aeckar.kanary.utils.jvmName
import java.io.IOException
import kotlin.reflect.KClass

/**
 * Provides a delegate by which the [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
 * operations of [T] may be locally defined.
 *
 * For every instance of T, these operations will be defined.
 * This is in contrast to a protocol [definition][SchemaBuilder.define] within a [schema], which only apply
 * when that schema is passed to a [serializer] or [deserializer].
 *
 * The protocol returned *must* be delegated to the companion of T implementing [Protocol].
 * Otherwise, the protocol will be unable to be resolved during reading/writing.
 * @return a local protocol
 */
inline fun <reified T> define(
    noinline read: TypedReadOperation<T>? = null,
    noinline write: TypedWriteOperation<T>? = null
) = Protocol(read, write)

/**
 * Applies the '[static][ProtocolBuilder.static]' modifier to the given write operation.
 * @return the supplied [write operation][ProtocolBuilder.write]
 */
fun <T> static(write: TypedWriteOperation<T>): TypedWriteOperation<T> = StaticWriteOperation(write)

/**
 * Applies the '[fallback][ProtocolBuilder.fallback]' modifier to the given read operation.
 * @return the supplied [read operation][ProtocolBuilder.read]
 */
fun <T> fallback(read: TypedReadOperation<T>): TypedReadOperation<T> = FallbackReadOperation(read)

@PublishedApi
internal fun Protocol(read: ReadOperation?, write: WriteOperation?): Protocol = TypeProtocol(read, write)

@JvmName("ProtocolWithCast")
@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun Protocol(read: ReadOperation?, write: TypedWriteOperation<*>?): Protocol {
    return TypeProtocol(read, write as WriteOperation?)
}

@PublishedApi
internal fun mergeProtocols(builder: ProtocolBuilder<*>, localProtocol: Protocol): Protocol {
    val read = if (builder.read != null) {
        if (localProtocol.read != null) {
            throw MalformedProtocolException(builder.classRef, "conflicting definitions of read operation")
        }
        builder.read
    } else {
        localProtocol.read
    }
    val write = if (builder.write != null) {
        if (localProtocol.write != null) {
            throw MalformedProtocolException(builder.classRef, "conflicting definitions of write operation")
        }
        builder.write
    } else {
        localProtocol.write
    }
    return Protocol(read, write)
}

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: KClass<*>?, reason: String)
        : IOException("Protocol for type ${classRef?.let { "'${it.qualifiedName}' " }}is malformed ($reason)")

/**
 * A locally defined binary I/O protocol.
 *
 * Delegates the protocol of the type whose companion implements this interface
 * to the locally [defined][define] protocol by which this interface is delegated to.
 * Doing so enables serialization using private members.
 *
 * It is possible for a local protocol to define one operation and
 * a protocol of the same type defined in a schema to define another operation.
 * If an operation is defined in both, a [MalformedProtocolException] is thrown.
 */
interface Protocol {
    /**
     * @return true if [read] is defined and has the '[fallback][ProtocolBuilder.fallback]' modifier
     */
    val hasFallback: Boolean

    /**
     * @return true if [write] is defined and has the '[static][ProtocolBuilder.static]' modifier
     */
    val hasStatic: Boolean

    /**
     * The [read operation][ProtocolBuilder.read] of this protocol.
     */
    val read: ReadOperation?

    /**
     * The [write operation][ProtocolBuilder.write] of this protocol.
     */
    val write: WriteOperation?
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilder<T : Any>(internal val classRef: KClass<*>) {
    init {
        if (classRef in TYPES_WITH_BUILTIN_PROTOCOLS) {
            throw MalformedProtocolException(classRef, "built-in protocol already exists")
        }
        if (classRef.jvmName == null) {
            throw MalformedProtocolException(classRef, "local and anonymous classes cannot be serialized")
        }
    }

    /**
     * The binary read operation called when [Deserializer.read] is called with an object of class [T].
     * Information deserialized from supertypes is converted into packets of information,
     * from which the read operation can use the information to create a new instance of [T].
     * @throws MalformedProtocolException [T] is an abstract class or interface
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: TypedReadOperation<T>? = null
        set(value) {
            value ?: throw MalformedProtocolException(classRef, "${"read operation"} cannot be null")
            field?.let {
                throw MalformedProtocolException(classRef, "${"read operation"} assigned a value more than once")
            }
            if (classRef.isAbstract && value !is FallbackReadOperation) {
                throw MalformedProtocolException(classRef,
                        "read operation without a 'default' modifier not supported for abstract classes and interfaces")
            }
            field = value
        }

    /**
     * The binary write operation called when [Serializer.write] is called with an object of class [T].
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: TypedWriteOperation<T>? = null
        set(value) {
            value ?: throw MalformedProtocolException(classRef, "${"write operation"} cannot be null")
            field?.let {
                throw MalformedProtocolException(classRef, "${"write operation"} assigned a value more than once")
            }
            field = value
        }

    /**
     * When prepended to a [read operation][read], declares that subtypes without a read operation
     * can still be instantiated as an instance of [T] using this read operation.
     * Generally, this should be used for types whose subtypes have the same public API.
     * Any information not deserialized as a result is lost.
     * @throws MalformedProtocolException [T] is a final class, or called more than once in a single scope
     */
    fun fallback(read: TypedReadOperation<T>): TypedReadOperation<T> {
        if (classRef.isFinal) {
            throw MalformedProtocolException(classRef, "read modifier 'fallback' not supported for final classes")
        }
        return FallbackReadOperation(read)
    }

    /**
     * When prepended to a [write operation][write], declares that the only information serialized
     * from an instance of [T] is that which is specifically written here.
     * If used, subtypes of this type may not define a protocol with a write operation.
     * Enables certain optimizations.
     * @throws MalformedProtocolException this function is called more than once in a single scope
     */
    fun static(write: TypedWriteOperation<T>): TypedWriteOperation<T> {
        return StaticWriteOperation(write)
    }
}

/**
 * Internal representation of a binary I/O protocol.
 */
private class TypeProtocol(
    override val read: ReadOperation?,
    override val write: WriteOperation?
) : Protocol {
    override val hasFallback = read is FallbackReadOperation
    override val hasStatic = write is StaticWriteOperation
}