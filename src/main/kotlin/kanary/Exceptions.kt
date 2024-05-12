package kanary

// Throws MalformedProtocolException if class is local or anonymous
@PublishedApi
internal fun JvmClass.nameIfExists(): String {
    return qualifiedName ?: throw MalformedProtocolException(this, "local or anonymous")
}

/**
 * An exception originating from the Kanary serialization API.
 */
sealed class KanaryException(message: String) : Exception(message)

class TypeMismatchException @PublishedApi internal constructor(message: String) : KanaryException(message)

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class MalformedProtocolException @PublishedApi internal constructor(classRef: JvmClass, reason: String)
        : KanaryException("Protocol for type '${classRef.qualifiedName}' is malformed ($reason)")

/**
 * Thrown during serialization or deserialization when a type is expected
 * to have been defined a protocol, but one was not found.
 */
class MissingProtocolException @PublishedApi internal constructor(classRef: JvmClass)
        : KanaryException("Protocol for type '${classRef.qualifiedName}' expected but not found")