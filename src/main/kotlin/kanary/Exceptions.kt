package kanary

import kotlin.reflect.KClass

/**
 * @throws InvalidProtocolException the class is local or anonymous
 */
@PublishedApi
internal fun <T : Any> KClass<out T>.nameIfExists(): String {
    return qualifiedName ?: throw InvalidProtocolException(this, "local or anonymous")
}

/**
 * An exception originating from the Kanary serialization API.
 */
sealed class KanaryException(message: String) : Exception(message)

class TypeMismatchException @PublishedApi internal constructor(message: String) : KanaryException(message) {
    @PublishedApi
    internal constructor(expected: TypeCode, or: TypeCode, actual: Int) : this(
            "Types '${expected.name}' or '${or.name}' expected, but found '${TypeCode.nameOf(actual)}'")
}

/**
 * Thrown when an attempt is made to define a protocol for a class that is not top-level.
 */
class InvalidProtocolException @PublishedApi internal constructor(classRef: KClass<*>, reason: String) : KanaryException(
        "Class '${classRef.qualifiedName}' cannot be defined a binary I/O protocol ($reason)")

/**
 * Thrown when a class is expected to have a Kanary I/O protocol, but one has not been defined.
 * Can be thrown during serialization or deserialization.
 */
class MissingProtocolException @PublishedApi internal constructor(message: String) : KanaryException(message)

