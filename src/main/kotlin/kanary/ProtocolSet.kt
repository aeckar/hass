package kanary

import com.github.eckar.ReassignmentException
import kotlin.reflect.jvm.jvmErasure

internal typealias JvmClass = kotlin.reflect.KClass<*>
internal typealias JvmType = kotlin.reflect.KType
internal typealias ProtocolSequence = List<ProtocolSpecifier>

private typealias ProtocolSpecifier = Pair<String,Protocol<*>>
private typealias MutableProtocolSequence = MutableList<ProtocolSpecifier>

// TODO look into caching already instantiated protocol sets

/**
 * Provides a scope wherein protocols for various classes may be defined.
 * It is acceptable, but not encouraged, to create an empty set. Doing so would provide
 * object read/write functionality to the serializer/deserializer, which will always fail when invoked.
 * This is because objects require their type to have a defined protocol before they can be manipulated.
 * @return a protocol set, which can be passed to a
 * [serializer][java.io.OutputStream.serializer] or [deserializer][java.io.InputStream.deserializer]
 * to provide reference type serialization functionality
 */
inline fun protocolSet(builder: ProtocolSetBuilder.() -> Unit): ProtocolSet {
    val builderScope = ProtocolSetBuilder()
    builder(builderScope)
    return ProtocolSet(builderScope)
}

// No intent to add versioning support
/**
 * The scope wherein binary I/O [protocols][protocolOf] may be defined.
 */
class ProtocolSetBuilder @PublishedApi internal constructor() {
    @PublishedApi
    internal val protocols = mutableMapOf<JvmClass,Protocol<*>>()

    /**
     * Provides a scope wherein a the binary [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
     * operations of a top-level class can be defined.
     * @throws MalformedProtocolException [T] is not a top-level class or has already been defined a protocol
     * @throws ReassignmentException either of the operations are defined twice,
     * or this is called more than once for type [T]
     */
    inline fun <reified T : Any> protocolOf(builder: ProtocolBuilder<T>.() -> Unit) {
        val classRef = T::class
        if (classRef in TypeCode.jvmTypes) {
            throw MalformedProtocolException(classRef, "defined by default")
        }
        if (classRef in protocols) {
            throw MalformedProtocolException(classRef, "defined more than once")
        }
        val builderScope = ProtocolBuilder<T>(classRef)
        try {
            builder(builderScope)
        } catch (_: ReassignmentException) {
            throw ReassignmentException("Read or write operation defined more than once")
        }
        protocols[classRef] = Protocol(builderScope)
    }

    internal fun specifierOf(type: JvmClass): ProtocolSpecifier {
        return type.qualifiedName!! to protocols.getValue(type)
    }
}

/**
 * TODO document
 */
class ProtocolSet {
    // Protocols used during deserialization
    internal val allProtocols: Map<JvmClass, Protocol<*>>

    /* Protocols for each type serialized, organized in the order they are written
     * Last member of protocol hierarchy is protocol of key
     */
    internal val writeSequences: Map<JvmClass, ProtocolSequence>

    @PublishedApi
    internal constructor(builder: ProtocolSetBuilder) {
        fun MutableProtocolSequence.build(supertypes: List<JvmType>): ProtocolSequence {
            supertypes
                .asSequence()
                .map { it.jvmErasure }
                .filter { jvmType ->
                    builder.protocols[jvmType]?.let { protocol ->   // Has protocol
                        protocol.write?.let {                       // Has write operation
                            none { it.second == protocol }          // Not already in sequence
                        }
                    } ?: false
                }
                .forEach { this += builder.specifierOf(it) }
            supertypes
                .asSequence()
                .map { it.jvmErasure.supertypes }
                .forEach { build(it) }
            return this
        }

        allProtocols = builder.protocols
        writeSequences = mutableMapOf<JvmClass, ProtocolSequence>().apply {
            builder.protocols.keys.forEach { jvmType ->
                val hierarchy = mutableListOf<ProtocolSpecifier>()  // Can be empty
                put(jvmType, hierarchy.build(jvmType.supertypes) /* stateful */)
                builder.protocols.getValue(jvmType).write?.let {
                    hierarchy += builder.specifierOf(jvmType)
                }
            }
        }
    }

    private constructor(allProtocols: Map<JvmClass, Protocol<*>>, writeProtocols: Map<JvmClass, ProtocolSequence>) {
        this.allProtocols = allProtocols
        this.writeSequences = writeProtocols
    }

    /**
     * Useful as a utility, but slower than simply declaring all protocols within the same set.
     * @return a new protocol set containing the protocols of each
     * @throws ReassignmentException the sets contain conflicting declarations of a given protocol
     */
    operator fun plus(other: ProtocolSet): ProtocolSet {
        val otherTypes = other.allProtocols.keys
        for (jvmType in allProtocols.keys) {
            if (jvmType in otherTypes) {
                throw ReassignmentException("Conflicting declarations for protocol of class '${jvmType.qualifiedName!!}'")
            }
        }
        return ProtocolSet(allProtocols + other.allProtocols, writeSequences + other.writeSequences)
    }

    internal companion object {
        val EMPTY = ProtocolSet(emptyMap(), emptyMap())
    }
}