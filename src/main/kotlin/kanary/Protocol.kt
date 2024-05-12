package kanary

import com.github.eckar.ReassignmentException
import com.github.eckar.once

internal typealias ReadOperation<T> = PolymorphicDeserializer.() -> T
internal typealias WriteOperation<T> = Serializer.(T) -> Unit

@PublishedApi
internal class Protocol<T : Any>(builder: ProtocolBuilder<T>) {
    val isReadDefault = builder.isReadDefault
    val isWriteStatic = builder.isWriteStatic
    val read: ReadOperation<out T>? = builder.takeIf { it.isReadDefined }?.read
    val write: WriteOperation<in T>? = builder.takeIf { it.isWriteDefined }?.write
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 *
 * abstract:
 * default read and write
 *
 * concrete default:
 * write
 *
 * concrete default:
 * <none>
 *
 * concrete:
 * read and write
 */
class ProtocolBuilder<T : Any>(private val classRef: JvmClass) {
    init {
        if (classRef in TypeCode.jvmTypes) {
            throw MalformedProtocolException(classRef, "built-in protocol already exists")
        }
    }

    /**
     * The binary read operation called when [Deserializer.read] is called with an object of class [T].
     * Information deserialized from supertypes is converted into a packet,
     * from which the read operation can use the information to create a new instance of [T].
     * @throws MalformedProtocolException [T] is an abstract class or interface
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: ReadOperation<T> by once {
        if (classRef.isAbstract) {
            throw MalformedProtocolException(classRef, "read not supported for abstract classes and interfaces")
        }
        isReadDefined = true
    }

    /**
     * The binary write operation called when [Serializer.write] is called with an object of class [T]
     * If not declared, then a no-op default write operation is used.
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: WriteOperation<T> by once { isWriteDefined = true }

    internal var isReadDefault = false
    internal var isWriteStatic = false
    internal var isReadDefined = false
    internal var isWriteDefined = false

    /**
     * When prepended to a [read operation][read] declaration, signifies that subtypes without a read operation
     * can still be instantiated as an instance of [T] using this read operation.
     * Generally, this should be done for types whose subtypes will not override equals() or hashCode().
     * Any information not deserialized as a result is lost.
     *
     * Example usage:
     *
     * ```
     * read = default { ... }
     *
     * read = { ... }
     * default(read)    // Also acceptable, but must come after
     * ```
     *
     * @throws MalformedProtocolException [T] is a final class, or called more than once in a single scope
     */
    fun default(read: ReadOperation<T>): ReadOperation<T> {
        if (classRef.isFinal) {
            throw MalformedProtocolException(classRef, "default read not supported for final classes")
        }
        if (isReadDefault) {
            throw MalformedProtocolException(classRef, "modifier 'default' used more than once")
        }
        isReadDefault = true
        return read
    }

    /**
     * When prepended to a [write operation][write] declaration, signifies that supertypes
     * should not write their packets when an instance of [T] is serialized.
     * Generally, this should be done for types whose supertypes lack write operations.
     * This allows for increased performance because the API does not need to check whether
     * any supertypes are capable of creating packets using their write operation.
     *
     * Example usage:
     *
     * ```
     * write = static { ... }
     *
     * write = { ... }
     * static(write)    // Also acceptable, but must come after
     */
    fun static(write: WriteOperation<T>): WriteOperation<T> {
        if (isWriteStatic) {
            throw MalformedProtocolException(classRef, "modifier 'static' used more than once")
        }
        isWriteStatic = true
        return write
    }
}