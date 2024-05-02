package kanary

import kotlin.reflect.KClass

/**
 * An exception originating from the Kanary serialization API.
 */
sealed class KanaryException(message: String) : Exception(message)

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class InvalidProtocolException @PublishedApi internal constructor(classRef: KClass<*>) : KanaryException(
    "Class '${classRef.qualifiedName}' is not eligible to be defined a binary I/O protocol (local or anonymous)")

/**
 * Thrown when a class is expected to have a Kanary I/O protocol, but one has not been defined.
 */
class MissingProtocolException @PublishedApi internal constructor(classRef: KClass<*>) : KanaryException(
    "Binary I/O protocol for class '${classRef.qualifiedName}' expected but not found")

