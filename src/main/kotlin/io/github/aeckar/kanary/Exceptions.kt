package io.github.aeckar.kanary

import io.github.aeckar.kanary.reflect.Type
import java.io.IOException
/**
 * Thrown when the definition of a protocol is invalid.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: Type, reason: String)
        : IllegalArgumentException("$reason (in protocol of '${classRef.qualifiedName}')")

/**
 * Thrown when an object is read as a certain type, but was serialized as a different type.
 */
class ObjectMismatchException internal constructor(message: String) : TypeCastException(message)

/**
 * Thrown when an applicable [read][ProtocolBuilder.read] or [write][ProtocolBuilder.write]
 * operation is expected, but not found.
 */
class MissingOperationException internal constructor(message: String) : IOException(message)

/**
 * Thrown when an attempt is made to read serialized data of a certain type flag, but another type flag is encountered.
 *
 * These flags are emitted as bytes throughout serialized data to enforce type-safety
 * and determine relative position during deserialization.
 */
class TypeFlagMismatchException internal constructor(message: String) : IOException(message)