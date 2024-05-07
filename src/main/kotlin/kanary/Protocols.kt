package kanary

import java.util.concurrent.ConcurrentHashMap
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
    return ProtocolSet(builderScope.protocols)
}

@PublishedApi
internal fun <T : Any> protocolNameOf(classRef: KClass<out T>) : String {
    return classRef.qualifiedName ?: throw InvalidProtocolException(classRef, "local or anonymous")
}

/**
 * A binary I/O protocol defining read and write operations when this object is
 * serialized using [PrimitiveSerializer] or deserialized using [PrimitiveDeserializer].
 */
@PublishedApi
internal class Protocol<T : Any>(val read: ReadOperation<out T>, val write: WriteOperation<in T>)

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilderScope<T> {
    /**
     * The binary read operation when [Deserializer.readObject] is called with an object of class [T].
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: Deserializer.() -> T by AssignOnce()

    /**
     * The binary write operation when [Serializer.write] is called with an object of class [T]
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: Serializer.(T) -> Unit by AssignOnce()
}

class ProtocolSet @PublishedApi internal constructor(
    private val protocols: Map<String,Protocol<*>>
) {
    init {

    }

    internal val superclassProtocolCache = ConcurrentHashMap<String, Pair<String,Protocol<*>>>()

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> resolve(className: String) = protocols[className]?.let { it as Protocol<T> }

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
        val newProtocols = mutableMapOf<String,Protocol<*>>()
        protocols.forEach { (className, protocol) -> newProtocols[className] = protocol }
        other.protocols.forEach { (className, protocol) -> newProtocols[className] = protocol }
        return ProtocolSet(newProtocols)
    }
}

/**
 * The scope wherein binary I/O [protocols][protocolOf] may be defined.
 */
class ProtocolSetBuilderScope @PublishedApi internal constructor() {
    @PublishedApi
    internal val protocols = mutableMapOf<String,Protocol<*>>()

    /**
     * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
     * operations of a top-level class can be defined.
     * @throws MissingProtocolException [T] is not a top-level class
     * @throws ReassignmentException either of the operations are defined twice,
     * or this is called more than once for type [T]
     */
    inline fun <reified T : Any> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit) {
        val t = T::class
        val className = protocolNameOf(t)
        if (className in defaultProtocolNames) {
            throw InvalidProtocolException(t, "default protocol already defined")
        }
        if (className in protocols) {
            throw ReassignmentException("Binary I/O protocol for class '$className' defined more than once")
        }
        val builderScope = ProtocolBuilderScope<T>()
        builder(builderScope)
        protocols[className] = try {
            Protocol(builderScope.read, builderScope.write)
        } catch (_: NoSuchElementException) {
            throw NoSuchElementException("Read or write operation undefined")
        } catch (_: ReassignmentException) {
            throw ReassignmentException("Read or write operation defined more than once")
        }
    }

    @PublishedApi
    internal companion object {
        val defaultProtocolNames = hashSetOf(
            "kotlin.Boolean",
            "kotlin.Byte",
            "kotlin.Char",
            "kotlin.Short",
            "kotlin.Int",
            "kotlin.Long",
            "kotlin.Float",
            "kotlin.Double",

            "kotlin.BooleanArray",
            "kotlin.ByteArray",
            "kotlin.CharArray",
            "kotlin.ShortArray",
            "kotlin.IntArray",
            "kotlin.LongArray",
            "kotlin.FloatArray",
            "kotlin.DoubleArray",

            "kotlin.String",

            "kotlin.Pair",
            "kotlin.Triple",
            "kotlin.collections.Map.Entry"
        )
    }
}