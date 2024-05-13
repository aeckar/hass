package kanary

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

internal typealias JvmClass = KClass<*>
internal typealias JvmType = KType
internal typealias ProtocolSequence = List<ProtocolSpecifier>

private typealias ProtocolSpecifier = Pair<String,Protocol<*>>
private typealias MutableProtocolSequence = MutableList<ProtocolSpecifier>

/**
 * Provides a scope wherein protocols for various classes may be defined.
 * It is acceptable, but not encouraged, to create an empty set. Doing so would provide
 * object read/write functionality to the serializer/deserializer, which will always fail when invoked.
 * This is because objects require their type to have a defined protocol before they can be manipulated.
 * @return a serialization schema, which can be passed to a
 * [serializer][java.io.OutputStream.serializer] or [deserializer][java.io.InputStream.deserializer]
 * to provide the directions for serializing the specified reference types
 */
inline fun schema(builder: SchemaBuilder.() -> Unit): Schema {
    val builderScope = SchemaBuilder()
    builder(builderScope)
    return Schema(builderScope)
}

/**
 * Thrown when there is an attempt to assign a value to a property that has already been given a value
 * and can only be assigned a value once.
 */
class ReassignmentException /* not limited to API usage */(message: String) : Exception(message)

/**
 * Defines a set of protocols corresponding to how certain types should be written to and read from binary.
 */
class Schema {
    // Protocols used during deserialization
    internal val allProtocols: Map<JvmClass, Protocol<*>>

    // Protocols for each type serialized, organized in the order they are written
    // Last member of sequence contains write operation of key
    internal val writeSequences: Map<JvmClass, ProtocolSequence>

    @PublishedApi
    internal constructor(builder: SchemaBuilder) {
        fun MutableProtocolSequence.build(supertypes: List<JvmType>): ProtocolSequence {
            supertypes
                .asSequence()
                .map { it.jvmErasure }
                .filter { jvmType ->
                    builder.definedProtocols[jvmType]?.let { protocol ->    // Has protocol
                        protocol.write?.let {                               // Has write operation
                            none { it.second == protocol }                  // Not already in sequence
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

        allProtocols = builder.definedProtocols
        writeSequences = mutableMapOf<JvmClass, ProtocolSequence>().apply {
            builder.definedProtocols.keys.forEach { jvmType ->
                val hierarchy = mutableListOf<ProtocolSpecifier>()  // Can be empty
                put(jvmType, hierarchy.build(jvmType.supertypes) /* stateful */)
                builder.definedProtocols.getValue(jvmType).write?.let {
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
     * Useful as a utility, but slower than simply defining all protocols within the same schema.
     * @return a new schema containing the protocols of each
     * @throws ReassignmentException the sets contain conflicting declarations of a given protocol
     */
    operator fun plus(other: Schema): Schema {
        val otherTypes = other.allProtocols.keys
        for (jvmType in allProtocols.keys) {
            if (jvmType in otherTypes) {
                throw ReassignmentException("Conflicting declarations for protocol of class '${jvmType.qualifiedName!!}'")
            }
        }
        return Schema(allProtocols + other.allProtocols, writeSequences + other.writeSequences)
    }

    internal companion object {
        val EMPTY = Schema(emptyMap(), emptyMap())
    }
}

/**
 * The scope wherein binary I/O [protocols][define] may be defined.
 */
class SchemaBuilder @PublishedApi internal constructor() {  // No intent to add versioning support
    @PublishedApi
    internal val definedProtocols = mutableMapOf<JvmClass,Protocol<*>>()

    /**
     * Provides a scope wherein the [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
     * operations of a type can be defined.
     * @throws MalformedProtocolException [T] is not a top-level class or has already been defined a protocol
     * @throws ReassignmentException either of the operations are defined twice,
     * or this is called more than once for type [T]
     */
    inline fun <reified T : Any> define(builder: ProtocolBuilder<T>.() -> Unit) {
        val classRef = T::class
        if (classRef in builtInTypes) {
            throw MalformedProtocolException(classRef, "defined by default")
        }
        if (classRef in definedProtocols) {
            throw MalformedProtocolException(classRef, "defined more than once")
        }
        val builderScope = ProtocolBuilder<T>(classRef)
        try {
            builder(builderScope)
        } catch (_: ReassignmentException) {
            throw ReassignmentException("Read or write operation defined more than once")
        }
        definedProtocols[classRef] = Protocol(builderScope)
    }

    internal fun specifierOf(type: JvmClass): ProtocolSpecifier {
        return type.qualifiedName!! to definedProtocols.getValue(type)
    }
}