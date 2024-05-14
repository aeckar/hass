package kanary

import java.io.IOException
import kotlin.reflect.KClass

internal typealias ReadOperation<T> = PolymorphicDeserializer.() -> T
internal typealias WriteOperation<T> = Serializer.(T) -> Unit
internal typealias SimpleReadOperation<T> = Deserializer.() -> T

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: KClass<*>, reason: String)
        : IOException("Protocol for type '${classRef.qualifiedName}' is malformed ($reason)")

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException @PublishedApi internal constructor(message: String) : IOException(message)

@PublishedApi
internal class Protocol<T : Any>(builder: ProtocolBuilder<T>) {
    val hasNoinherit: Boolean   // TODO implement
    val hasFallback: Boolean   // TODO implement
    val hasStatic: Boolean
    val read: ReadOperation<out T>?
    val write: WriteOperation<in T>?

    init {
        with(builder) {
            if (hasNoinherit && hasFallback) {
                throw MalformedProtocolException(classRef, "read operation cannot be assigned more than one modifier")
            }
            if (hasNoinherit && !hasStatic) {
                throw MalformedProtocolException(classRef,
                    "read operation with 'static' modifier must accompany static write operation")
            }
        }
        hasNoinherit = builder.hasNoinherit
        hasFallback = builder.hasFallback
        hasStatic = builder.hasStatic
        read = builder.takeIf { it.isReadDefined }?._read
        write = builder.takeIf { it.isWriteDefined }?._write
    }
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
@Suppress("PropertyName")
class ProtocolBuilder<T : Any>(internal val classRef: KClass<*>) {
    init {
        if (classRef in builtInTypes) {
            throw MalformedProtocolException(classRef, "built-in protocol already exists")
        }
    }

    /**
     * The binary read operation called when [ExhaustibleDeserializer.read] is called with an object of class [T].
     * Information deserialized from supertypes is converted into a packet,
     * from which the read operation can use the information to create a new instance of [T].
     * @throws MalformedProtocolException [T] is an abstract class or interface
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: ReadOperation<T>
        get() = throw MalformedProtocolException(classRef, "read operation may only be assigned to, not accessed")
        set(value) {
            if (classRef.isAbstract && !hasFallback) {
                throw MalformedProtocolException(classRef,
                        "read operation without a 'default' modifier not supported for abstract classes and interfaces")
            }
            if (isReadDefined) {
                throw ReassignmentException("read operation assigned more than once")
            }
            isReadDefined = true
            _read = value
        }

    /**
     * The binary write operation called when [OutputSerializer.write] is called with an object of class [T]
     * If not declared, then a no-op default write operation is used.
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: WriteOperation<T>
        get() = throw MalformedProtocolException(classRef, "write operation may only be assigned to, not accessed")
        set(value) {
            if (isWriteDefined) {
                throw ReassignmentException("write operation assigned more than once")
            }
            isWriteDefined = true
            _write = value
        }

    internal var _read: ReadOperation<T> = @Suppress("CAST_NEVER_SUCCEEDS") { null as T }
    internal var _write: WriteOperation<T> = {}

    internal var hasNoinherit = false
    internal var hasFallback = false
    internal var hasStatic = false

    internal var isReadDefined = false
    internal var isWriteDefined = false

    /**
     * When prepended to a [read operation][read], declares that subtypes without a read operation
     * can still be instantiated as an instance of [T] using this read operation.
     * Generally, this should be used for types whose subtypes have the same public API.
     * Any information not deserialized as a result is lost.
     * @throws MalformedProtocolException [T] is a final class, or called more than once in a single scope
     */
    fun fallback(read: ReadOperation<T>): ReadOperation<T> {
        if (classRef.isFinal) {
            throw MalformedProtocolException(classRef, "read modifier 'fallback' not supported for final classes")
        }
        if (hasFallback) {
            throw MalformedProtocolException(classRef, "read modifier 'fallback' used more than once")
        }
        hasFallback = true
        return read
    }

    /**
     * When prepended to a [write operation][write], declares that the only information serialized
     * from an instance of [T] is that which is specifically written here.
     * If used, subtypes of this type may not define a protocol with a write operation.
     * Enables certain optimizations.
     * @throws MalformedProtocolException this function is called more than once in a single scope
     */
    fun static(write: WriteOperation<T>): WriteOperation<T> {
        if (hasStatic) {
            throw MalformedProtocolException(classRef, "write modifier 'static' used more than once")
        }
        hasStatic = true
        return write
    }

    /**
     * When prepended to a [read operation][read], declares that:
     * - Supertype packets are not accessed during the write operation
     * - Version resolution through [exhaustion testing][ExhaustibleDeserializer] is not required
     *
     * If used, the [write operation][write] of the same protocol must have the [hasStatic] modifier.
     * Additionally, subtypes of this type may not define a protocol within the same schema.
     * Enables certain optimizations.
     * @throws MalformedProtocolException this function is called more than once in a single scope
     */
    fun noinherit(read: SimpleReadOperation<T>): SimpleReadOperation<T> {
        if (hasNoinherit) {
            throw MalformedProtocolException(classRef, "read modifier 'static' used more than once")
        }
        hasNoinherit = true
        return read
    }
}