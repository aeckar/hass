package io.github.aeckar.kanary

import io.github.aeckar.kanary.reflect.Type
import io.github.aeckar.kanary.reflect.isLocalOrAnonymous
import java.io.Serializable

/**
 * A [read operation][ProtocolBuilder.read].
 *
 * SAM conversions of this interface may be defined locally to allow for deserialization of private members.
 * The common convention for this is to assign the object to a constant named `Read` in the corresponding companion.
 * For example:
 *
 * ```kotlin
 * class MyClass {
 *     companion object {
 *         val Read = ReadOperation { /* read behavior with inferred type */ }
 *     }
 * }
 * ```
 */
fun interface ReadOperation<out T> : Serializable {
    fun ObjectDeserializer.readOperation(): T
}

/**
 * A [write operation][ProtocolBuilder.write].
 *
 * SAM conversions of this interface may be defined locally to allow for serialization of private members.
 * The common convention for this is to assign the object to a constant named `Write` in the corresponding companion.
 * For example:
 *
 * ```kotlin
 * class MyClass {
 *     companion object {
 *         val Write = WriteOperation<MyClass> { /* write behavior */ }
 *     }
 * }
 * ```
 */
fun interface WriteOperation<in T> : Serializable {
    fun Serializer.writeOperation(obj: T)
}

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilder<T : Any>(internal val classRef: Type) {
    /**
     * Pseudo-keyword which, when prepended to [read], declares that subtypes without a read operation
     * can still be instantiated as an instance of [T] using this read operation.
     *
     * Generally, this should be used for types whose subtypes have the same public API.
     * Any information not deserialized as a result is lost.
     */
    val fallback inline get() = FallbackModifier(this)

    /**
     * Pseudo-keyword which, when prepended to a [write], declares that the only information serialized
     * from an instance of [T] is that which is specifically written here.
     *
     * If used, subtypes of this type may not define a protocol with a write operation.
     * Enables certain optimizations.
     */
    val static inline get() = StaticModifier(this)

    /**
     * Pseudo-keyword which, when prepended to [read] or [write],
     * declares that the operation does not have any modifiers.
     *
     * Provides a concise API for declaring a read or write operation
     * by using a lambda from outside the current scope without modifiers.
     */
    val default inline get() = DefaultOperation(this)

    @PublishedApi internal var read: ReadOperation<T>? = null
    @PublishedApi internal var write: WriteOperation<T>? = null
    @PublishedApi internal var hasFallback = false
    @PublishedApi internal var hasStatic = false

    init {
        if (classRef.isLocalOrAnonymous) {
            throw MalformedProtocolException(classRef,
                    "Local and anonymous classes cannot have a defined protocol")
        }
    }

    // ------------------------------ public API ------------------------------

    /**
     * Defines the binary read operation called when [Deserializer.read] is called with an object of class [T].
     * Information deserialized from supertypes is converted into packets of information,
     * from which the read operation can use the information to create a new instance of [T].
     * @throws MalformedProtocolException [T] is abstract, or this function is called more than once in a single scope
     */
    fun read(readOperation: ReadOperation<T>) {
        if (classRef.isAbstract) {
            throw MalformedProtocolException(classRef,
                "Read operation without 'fallback' modifier not supported for abstract classes and interfaces")
        }
        read?.let {
            throw MalformedProtocolException(classRef, "Read operation assigned a value more than once")
        }
        read = readOperation
    }

    /**
     * Defines the binary write operation called when [Serializer.write] is called with an object of class [T].
     * @throws MalformedProtocolException this function is called more than once in a single scope
     */
    fun write(writeOperation: WriteOperation<T>) {
        write?.let {
            throw MalformedProtocolException(classRef, "Write operation assigned a value more than once")
        }
        write = writeOperation
    }

    /**
     * [fallback][ProtocolBuilder.fallback] read operation modifier.
     */
    @JvmInline
    value class FallbackModifier<T : Any> @PublishedApi internal constructor(private val parent: ProtocolBuilder<T>) {
        /**
         * Defines a 'fallback' read operation.
         * @throws MalformedProtocolException [T] is a final class,
         * or this function is called more than once in a single scope
         * @see ProtocolBuilder.read
         */
        infix fun read(readOperation: ReadOperation<T>) {
            if (parent.classRef.isFinal) {
                throw MalformedProtocolException(parent.classRef, "'fallback' modifier not supported for final classes")
            }
            parent.read = readOperation
            parent.hasFallback = true
        }
    }

    /**
     * [static][ProtocolBuilder.static] write operation modifier.
     */
    @JvmInline
    value class StaticModifier<T : Any> @PublishedApi internal constructor(private val parent: ProtocolBuilder<T>) {
        /**
         * Defines a 'static' write operation.
         * @throws MalformedProtocolException this function is called more than once in a single scope
         * @see ProtocolBuilder.write
         */
        infix fun write(writeOperation: WriteOperation<T>) {
            parent.write = writeOperation
            parent.hasStatic = true
        }
    }

    /**
     * The default modifier for read and write operations.
     */
    @JvmInline
    value class DefaultOperation<T : Any> @PublishedApi internal constructor(private val parent: ProtocolBuilder<T>) {
        /**
         * Defines a 'fallback' read operation with no modifier.
         * @throws MalformedProtocolException [T] is a final class,
         * or this function is called more than once in a single scope
         * @see ProtocolBuilder.read
         */
        infix fun read(readOperation: ReadOperation<T>) = parent.read(readOperation)

        /**
         * Defines a 'static' write operation with no modifier.
         * @throws MalformedProtocolException this function is called more than once in a single scope
         * @see ProtocolBuilder.write
         */
        infix fun write(writeOperation: WriteOperation<T>) = parent.write(writeOperation)
    }

    // ------------------------------------------------------------------------
}