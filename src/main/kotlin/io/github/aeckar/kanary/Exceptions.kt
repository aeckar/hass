package io.github.aeckar.kanary

import java.io.IOException
import kotlin.reflect.KClass

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: KClass<*>, reason: String)
        : IOException("Protocol for type '${classRef.qualifiedName}' is malformed ($reason)")

/**
 * Thrown when a [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write] operation is expected, but not found.
 */
class MissingOperationException internal constructor(message: String) : IOException(message)

/**
 * Thrown when an attempt is made to serialize an object that cannot be serialized due to the nature of its type.
 *
 * The type may be local, anonymous, or a lambda not annotated with [JvmSerializableLambda].
 */
class NotSerializableException internal constructor(message: String) : IOException(message)

/**
 * Thrown when an attempt is made to read serialized data of a certain flagged type, but another type is encountered.
 */
class TypeFlagMismatchException internal constructor(expected: TypeFlag, found: Int)
        : IOException("Type flag '$expected' expected, but found '${found.name ?: "UNKNOWN"}'") {
    private companion object {
        val Int.name inline get() = TypeFlag.entries.find { it.ordinal == this }?.name
    }
}