package kanary

import java.io.IOException

internal typealias StaticReadOperation<T> = Deserializer.() -> T
internal typealias ReadOperation<T> = PolymorphicDeserializer.() -> T
internal typealias WriteOperation<T> = Serializer.(T) -> Unit

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: JvmClass, reason: String)
        : IOException("Protocol for type '${classRef.qualifiedName}' is malformed ($reason)")

@PublishedApi
internal class Protocol<T : Any>(builder: ProtocolBuilder<T>) {
    val isReadStatic: Boolean   // TODO implement
    val isReadDefault: Boolean   // TODO implement
    val isWriteStatic: Boolean
    val read: ReadOperation<out T>?
    val write: WriteOperation<in T>?

    init {
        with(builder) {
            if (isReadStatic && isReadDefault) {
                throw MalformedProtocolException(classRef, "read operation cannot be assigned more than one modifier")
            }
            if (isReadStatic && !isWriteStatic) {
                throw MalformedProtocolException(classRef,
                    "read operation with 'static' modifier must accompany static write operation")
            }
        }
        isReadStatic = builder.isReadStatic
        isReadDefault = builder.isReadDefault
        isWriteStatic = builder.isWriteStatic
        read = builder.takeIf { it.isReadDefined }?._read
        write = builder.takeIf { it.isWriteDefined }?._write
    }
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
@Suppress("PropertyName")
class ProtocolBuilder<T : Any>(internal val classRef: JvmClass) {
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
            if (classRef.isAbstract && !isReadDefault) {
                throw MalformedProtocolException(classRef,
                        "read operation without a 'default' modifier not supported for abstract classes and interfaces")
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
            isWriteDefined = true
            _write = value
        }

    internal var _read: ReadOperation<T> = @Suppress("CAST_NEVER_SUCCEEDS") { null as T }
    internal var _write: WriteOperation<T> = {}

    internal var isReadStatic = false
    internal var isReadDefault = false
    internal var isWriteStatic = false
    internal var isReadDefined = false
    internal var isWriteDefined = false

    /**
     * When prepended to a [read operation][read], declares that subtypes without a read operation
     * can still be instantiated as an instance of [T] using this read operation.
     * Generally, this should be done for types whose subtypes will not override equals() or hashCode().
     * Any information not deserialized as a result is lost.
     * @throws MalformedProtocolException [T] is a final class, or called more than once in a single scope
     */
    fun default(read: ReadOperation<T>): ReadOperation<T> {
        if (classRef.isFinal) {
            throw MalformedProtocolException(classRef, "default read operation not supported for final classes")
        }
        if (isReadDefault) {
            throw MalformedProtocolException(classRef, "read modifier 'default' used more than once")
        }
        isReadDefault = true
        return read
    }

    /**
     * When prepended to a [write operation][write], declares that supertypes
     * will not write their packets when an instance of [T] is serialized.
     * Generally, this should be done for types whose supertypes lack write operations.
     * Enables certain optimizations.
     * @throws MalformedProtocolException this function is called more than once in a single scope
     */
    fun static(write: WriteOperation<T>): WriteOperation<T> {
        if (isWriteStatic) {
            throw MalformedProtocolException(classRef, "write modifier 'static' used more than once")
        }
        isWriteStatic = true
        return write
    }

    /**
     * When prepended to a [read operation][read], declares that:
     * - Supertype packets are not accessed during the write operation
     * - Version resolution through [exhaustion testing][ExhaustibleDeserializer.isExhausted] is not required
     *
     * If used, the [write operation][write] of the same protocol must also be [static].
     * Enables certain optimizations.
     * @throws MalformedProtocolException this function is called more than once in a single scope
     */
    fun static(read: StaticReadOperation<T>): StaticReadOperation<T> {
        if (isReadStatic) {
            throw MalformedProtocolException(classRef, "read modifier 'static' used more than once")
        }
        isReadStatic = true
        return read
    }
}