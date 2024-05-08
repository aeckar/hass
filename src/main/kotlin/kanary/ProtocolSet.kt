package kanary

import com.github.eckar.ReassignmentException
import kotlin.reflect.KClass

internal typealias ReadOperation<T> = Deserializer.() -> T
internal typealias WriteOperation<T> = Serializer.(T) -> Unit

/**
 * Provides a scope wherein protocols for various classes may be defined.
 * It is acceptable, but not encouraged, to create an empty set. Doing so would provide
 * object read/write functionality to the serializer/deserializer, which will always fail when invoked.
 * This is because objects require their type to have a defined protocol before they can be manipulated.
 * @return a protocol set, which can be passed to a
 * [serializer][java.io.OutputStream.serializer] or [deserializer][java.io.InputStream.deserializer]
 * to provide reference type serialization functionality
 */
inline fun protocolSet(builder: ProtocolSetBuilderScope.() -> Unit): ProtocolSet {
    val builderScope = ProtocolSetBuilderScope()
    builder(builderScope)
    return ProtocolSet(builderScope.protocols, builderScope.supertypes)
}

/**
 * The scope wherein binary I/O [protocols][protocolOf] may be defined.
 */
class ProtocolSetBuilderScope @PublishedApi internal constructor() {
    @PublishedApi
    internal val supertypes = mutableMapOf<String,MutableList<String>>()

    @PublishedApi
    internal val protocols = mutableMapOf<String,Protocol<*>>()

    /**
     * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
     * operations of a top-level class can be defined.
     * @throws MissingProtocolException [T] is not a top-level class
     * @throws ReassignmentException either of the operations are defined twice,
     * or this is called more than once for type [T]
     */
    inline fun <reified T : Any> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit): String {
        val classRef = T::class
        val className = classRef.nameIfExists()
        if (className in Serializer.defaultWriteOperations) {
            throw InvalidProtocolException(classRef, "default protocol already defined")
        }
        if (className in protocols) {
            throw ReassignmentException("Binary I/O protocol for class '$className' defined more than once")
        }
        val builderScope = ProtocolBuilderScope<T>(classRef)
        builder(builderScope)
        protocols[className] = try {
            Protocol(builderScope.read, builderScope.write) // TODO
        } catch (_: NoSuchElementException) {
            throw NoSuchElementException("Read or write operation undefined")
        } catch (_: ReassignmentException) {
            throw ReassignmentException("Read or write operation defined more than once")
        }
        return className
    }

    /**
     * Declares that the class name (receiver) inherits from the supertype of
     * the specified [qualified name][KClass.qualifiedName].
     * Doing so permits polymorphic serialization through the creation of packets.
     * Can be chained to the end of [protocolOf].
     * @see and
     */
    infix fun String.implements(supertype: String): SupertypeSet {
        supertypes.getOrPut(this) { mutableListOf() } += supertype
        return SupertypeSet(this)
    }

    /**
     * Declares that the class name (receiver) inherits from the supertype of
     * the specified [qualified name][KClass.qualifiedName].
     * Can be chained.
     * @see implements
     */
    infix fun SupertypeSet.and(otherSupertype: String): SupertypeSet {
        className implements otherSupertype
        return this
    }
}

class ProtocolSet @PublishedApi internal constructor(
    internal val protocols: Map<String, Protocol<*>>,
    internal val supertypes: Map<String, List<String>>
) {
    /**
     * @return a new protocol set containing the protocols of each
     * @throws ReassignmentException the sets contain conflicting declarations of a given protocol
     */
    operator fun plus(other: ProtocolSet): ProtocolSet {
        val otherClassNames = other.protocols.keys
        for (className in protocols.keys) {
            if (className in otherClassNames) {
                throw ReassignmentException("Conflicting declarations for binary I/O protocol of class '$className'")
            }
        }
        return ProtocolSet(protocols + other.protocols, supertypes + other.supertypes)
    }

    internal companion object {
        val DEFAULT = ProtocolSet(mapOf(), mapOf())
    }
}