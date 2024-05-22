package kanary

import kanary.utils.jvmName
import java.io.IOException
import kotlin.reflect.KClass

private fun ProtocolBuilder<*>.throwMalformed(reason: String): Nothing {
    throw MalformedProtocolException(classRef, reason)
}

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: KClass<*>?, reason: String)
        : IOException("Protocol for type ${classRef?.let { "'${it.qualifiedName}' " }}is malformed ($reason)")

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal class TypeProtocol(builder: ProtocolBuilder<*>) : Protocol {
    override val hasFallback: Boolean
    override val hasStatic: Boolean
    override val read: ReadOperation?
    override val write: WriteOperation?

    init {
        with(builder) {
            if (hasNoinherit && hasFallback) {
                throwMalformed("read operation cannot be assigned more than one modifier")
            }
            if (hasNoinherit && !hasStatic) {
                throwMalformed("read operation with 'noinherit' modifier must accompany 'static' write operation")
            }
        }
        hasFallback = builder.hasFallback
        hasStatic = builder.hasStatic
        read = builder.access { read }
        write = builder.access { write } as WriteOperation
    }
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 * If the protocol of a nested class is defined and its package contains any uppercase letters,
 * attempting to read it from binary will throw [ClassNotFoundException].
 */
class ProtocolBuilder<T : Any>(internal val classRef: KClass<*>) {
    init {
        if (classRef in builtInTypes) {
            throwMalformed("built-in protocol already exists")
        }
        if (classRef.jvmName == null) {
            throwMalformed("local and anonymous classes cannot be serialized")
        }
    }

    /**
     * The binary read operation called when [Deserializer.read] is called with an object of class [T].
     * Information deserialized from supertypes is converted into a packet,
     * from which the read operation can use the information to create a new instance of [T].
     * @throws MalformedProtocolException [T] is an abstract class or interface
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: TypedReadOperation<T>? = null
        get() {
            checkAccess("read operation")
            return field
        }
        set(value) {
            value ?: throwNullAssignment("read operation")
            access { read }?.let { throwReassignment("read operation") }
            if (classRef.isAbstract && !hasFallback) {
                throwMalformed("read operation without a 'default' modifier not supported for abstract classes and interfaces")
            }
            field = value
        }

    /**
     * The binary write operation called when [Serializer.write] is called with an object of class [T]
     * If not declared, then a no-op default write operation is used.
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: TypedWriteOperation<T>? = null
        get() {
            checkAccess("write operation")
            return field
        }
        set(value) {
            value ?: throwNullAssignment("write operation")
            access { write }?.let { throwReassignment("write operation") }
            field = value
        }

    internal var hasNoinherit = false
    internal var hasFallback = false
    internal var hasStatic = false

    /**
     * When prepended to a [read operation][read], declares that subtypes without a read operation
     * can still be instantiated as an instance of [T] using this read operation.
     * Generally, this should be used for types whose subtypes have the same public API.
     * Any information not deserialized as a result is lost.
     * @throws MalformedProtocolException [T] is a final class, or called more than once in a single scope
     */
    fun fallback(read: TypedReadOperation<T>): TypedReadOperation<T> {
        if (classRef.isFinal) {
            throwMalformed("read modifier 'fallback' not supported for final classes")
        }
        if (hasFallback) {
            throwMalformed("read modifier 'fallback' used more than once")
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
    fun static(write: TypedWriteOperation<T>): TypedWriteOperation<T> {
        if (hasStatic) {
            throwMalformed("write modifier 'static' used more than once")
        }
        hasStatic = true
        return write
    }

    private var readableParams = false

    internal inline fun <R> access(block: ProtocolBuilder<T>.() -> R): R {
        readableParams = true
        val result = block()
        readableParams = false
        return result
    }

    private fun throwNullAssignment(varName: String): Nothing {
        throwMalformed("$varName cannot be null")
    }

    private fun throwReassignment(varName: String): Nothing {
        throwMalformed("$varName assigned a value more than once")
    }

    private fun checkAccess(varName: String) {
        if (!readableParams) {
            throwMalformed("$varName may only be assigned to, not accessed")
        }
    }
}

