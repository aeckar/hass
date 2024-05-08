package kanary

import com.github.eckar.ReassignmentException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

internal typealias ReadOperation<T> = Deserializer.() -> T
internal typealias WriteOperation<T> = Serializer.(T) -> Unit
internal typealias JvmType = KClass<*>

@Suppress("UNCHECKED_CAST")
internal fun ReadOperation<*>.eraseType() = this as ReadOperation<Any>

@Suppress("UNCHECKED_CAST")
internal fun WriteOperation<*>.generic(stream: Serializer, obj: Any) = (this as WriteOperation<Any>)(stream, obj)

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

/**
 * The scope wherein binary I/O [protocols][protocolOf] may be defined.
 */
class ProtocolSetBuilderScope @PublishedApi internal constructor() {
    @PublishedApi
    internal val protocols = mutableMapOf<JvmType,Protocol<*>>()

    /**
     * Provides a scope wherein a the binary [read][ProtocolBuilderScope.read] and [write][ProtocolBuilderScope.write]
     * operations of a top-level class can be defined.
     * @throws MissingProtocolException [T] is not a top-level class
     * @throws ReassignmentException either of the operations are defined twice,
     * or this is called more than once for type [T]
     */
    inline fun <reified T : Any> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit) {
        val classRef = T::class
        when (classRef) {
            Any::class, Nothing::class, Unit::class -> throw InvalidProtocolException(classRef, "fundamental type");
        }
        val className = classRef.nameIfExists() // Ensures eligible protocol
        if (className in Serializer.defaultWriteOperations) {
            throw InvalidProtocolException(classRef, "default protocol already defined")
        }
        if (classRef in protocols) {
            throw ReassignmentException("Binary I/O protocol for class '$className' defined more than once")
        }
        val builderScope = ProtocolBuilderScope<T>(classRef)
        builder(builderScope)
        protocols[classRef] = try {
            Protocol(builderScope.read, builderScope.write) // TODO
        } catch (_: NoSuchElementException) {
            throw NoSuchElementException("Read or write operation undefined")
        } catch (_: ReassignmentException) {
            throw ReassignmentException("Read or write operation defined more than once")
        }
    }
}

class ProtocolSet {
    internal val definedProtocols: Map<JvmType, Protocol<*>>
    internal val supertypes: Map<JvmType, List<JvmType>>

    @PublishedApi
    internal constructor(definedProtocols: Map<JvmType, Protocol<*>>) {
        this.definedProtocols = definedProtocols
        this.supertypes = mutableMapOf<JvmType, MutableList<JvmType>>().apply {
            definedProtocols.keys.forEach { it.supertypes.findSupertypes(subClass = it, this) }
        }
    }

    internal constructor(definedProtocols: Map<JvmType, Protocol<*>>, supertypes: Map<JvmType, List<JvmType>>) {
        this.definedProtocols = definedProtocols
        this.supertypes = supertypes
    }

    /**
     * @return a new protocol set containing the protocols of each
     * @throws ReassignmentException the sets contain conflicting declarations of a given protocol
     */
    operator fun plus(other: ProtocolSet): ProtocolSet {
        val otherClassNames = other.definedProtocols.keys
        for (className in definedProtocols.keys) {
            if (className in otherClassNames) {
                throw ReassignmentException("Conflicting declarations for binary I/O protocol of class '$className'")
            }
        }
        return ProtocolSet(definedProtocols + other.definedProtocols, supertypes + other.supertypes)
    }

    private fun List<KType>.findSupertypes(subClass: JvmType, supertypes: MutableMap<JvmType,MutableList<JvmType>>) {
        asSequence().map { it.jvmErasure }.forEach {
            if (it != Any::class) {
                if (it in definedProtocols && it !in supertypes.getOrPut(subClass) { mutableListOf() }) {
                    supertypes.getValue(subClass).apply { this += it }
                }
                it.supertypes.findSupertypes(subClass, supertypes)
            }
        }
    }

    internal companion object {
        val DEFAULT = ProtocolSet(mapOf(), mapOf())
    }
}