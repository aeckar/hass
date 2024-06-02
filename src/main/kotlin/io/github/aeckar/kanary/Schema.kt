package io.github.aeckar.kanary

import io.github.aeckar.kanary.reflect.Callable
import io.github.aeckar.kanary.reflect.Type
import io.github.aeckar.kanary.reflect.primaryProperties
import java.util.Objects.hash
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses

internal typealias WriteMap = Map<Type, WriteOperation<*>>
internal typealias MutableWriteMap = MutableMap<Type, WriteOperation<*>>

/**
 * Provides a scope wherein protocols for various classes may be defined.
 *
 * A schema with no protocols defined is legal, and should be stored as a variable if used more than once.
 * For operations accessing private members, they must be defined outside the builder scope.
 * @return a serialization schema, which can be passed to a
 * [serializer][java.io.OutputStream.serializer] or [deserializer][java.io.InputStream.deserializer]
 * to provide the directions for serializing the specified reference types
 */
inline fun schema(threadSafe: Boolean = true, builder: SchemaBuilder.() -> Unit): Schema {
    val builderScope = SchemaBuilder(threadSafe, definedProtocols = hashMapOf())
    builder(builderScope)
    return Schema(builderScope.definedProtocols, builderScope.shared ?: Schema.Properties(threadSafe))
}

/**
 * Defines a set of protocols corresponding to how certain types should be written to and read from binary.
 *
 * The table below includes the types with pre-defined protocols.
 * Any built-in read operation designated to an open or abstract type within the table
 * given the [fallback][ProtocolBuilder.fallback] modifier.
 *
 * |               |             |           |
 * |---------------|-------------|-----------|
 * | BooleanArray  | DoubleArray | Map.Entry |
 * | ByteArray     | String      | Map       |
 * | CharArray     | Array       | Unit      |
 * | ShortArray    | List        | Schema    |
 * | IntArray      | Iterable    |           |
 * | LongArray     | Pair        |           |
 * | FloatArray    | Triple      |           |
 *
 * The following categories are also serializable:
 *  - Lambda expressions (requires [@JvmSerializableLambda][JvmSerializableLambda])
 *  - [SAM conversions](https://kotlinlang.org/docs/fun-interfaces.html#sam-conversions)
 *  (requires functional interface to implement [Serializable])
 *  - `null`
 */
class Schema @PublishedApi internal constructor(
    internal val protocols: ProtocolMap,
    internal var shared: Properties
) {
    val isThreadSafe get() = shared.readsOrFallbacks is ConcurrentHashMap

    // ------------------------------ public API ------------------------------

    /**
     * Returns a new schema containing the protocols of both.
     *
     * Should be used if the union is used only once.
     * If used more than once, a new [schema] should be defined with both [added][SchemaBuilder.import] to it.
     *
     * If any one schema is thread-safe, the returned schema is also thread-safe.
     *
     * This function is shorthand for:
     * ```kotlin
     * schema(threadSafe = this.isThreadSafe || other.isThreadSafe) {
     *     import from this@Schema
     *     import from other
     * }
     * ```
     * @throws MalformedProtocolException there exist conflicting declarations of a given protocol
     */
    operator fun plus(other: Schema): Schema {
        Protocol.ensureUniqueMaps(protocols, other.protocols)
        return schema(isThreadSafe || other.isThreadSafe) {
            import from this@Schema
            import from other
        }
    }

    /**
     * Generated according to protocols defined in this schema,
     * as well as its internal mutable state.
     *
     * For performance reasons, the result of many reflection operations are cached
     * within the internal state of every schema. To account for this during schema serialization,
     * the result of this function may be used to determine whether re-serialization is necessary.
     * In doing so, future deserialization produces a schema instance with the result of these reflection operations
     * already computed.
     * @return a unique hashcode, including mutable state
     */
    @Suppress("UNUSED")
    fun deepHashCode() = with (shared) {
        hash(protocols, readsOrFallBacks.size, writeMaps.size, primaryPropertyArrays.size, primaryConstructors.size)
    }

    /**
     * Two schemas are structurally equivalent if they define protocols for the same types.
     *
     * The actual [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
     * operations of each protocol (or, whether they exist or not) are not considered.
     * @return true if this equals [other]
     * @see hashCode
     */
    override fun equals(other: Any?) = other is Schema && protocols == other.protocols

    /**
     * Generated according to the protocols defined in this schema.
     * @return a unique hashcode
     * @see equals
     */
    override fun hashCode() = protocols.hashCode()

    // ------------------------------------------------------------------------

    /*
        Immutable views of shared properties
     */

    internal val readsOrFallBacks: Map<Type, ReadOperation<*>> inline get() = shared.readsOrFallbacks
    internal val writeMaps: Map<Type, WriteMap> inline get() = shared.writeMaps
    internal val primaryPropertyArrays: Map<String, Array<out Callable>> inline get() = shared.primaryPropertyArrays
    internal val primaryConstructors: Map<String, Callable> inline get() = shared.primaryConstructors

    internal fun readOrFallbackOf(classRef: Type): ReadOperation<*> = with(shared) {
        return readsOrFallbacks[classRef] ?: resolveReadOrFallback(classRef).also { readsOrFallbacks[classRef] = it }
    }

    internal fun writeMapOf(classRef: Type): WriteMap = with(shared) {
        return writeMaps[classRef] ?: resolveWriteMap(classRef).also { writeMaps[classRef] = it }
    }

    internal fun primaryPropertiesOf(containerRef: Type, containerName: String): Array<out Callable> = with(shared) {
        return shared.primaryPropertyArrays[containerName]
            ?: containerRef.primaryProperties?.also { primaryPropertyArrays[containerName] = it }
            ?: throw MalformedContainerException(containerName, "Container does not have a primary constructor")
    }

    internal fun primaryConstructorOf(containerName: String): Callable = with(shared) {
        return primaryConstructors[containerName]
            ?: Type(containerName).primaryConstructor?.also { primaryConstructors[containerName] = it }
            ?: throw MalformedContainerException(containerName, "Container does not have a public primary constructor")
    }

    /*
        Mappings are resolved in the following order:
            - Subtypes before supertypes
            - For a given type, its supertypes in the order that they are declared in source code
     */

    private fun resolveReadOrFallback(classRef: Type): ReadOperation<*> {
        fun resolveFallback(classRef: Type, superclasses: List<Type>): ReadOperation<*>? {
            for (superclass in superclasses) {
                protocols[superclass]?.takeIf { it.hasFallback }?.read?.let { return it }
            }
            return superclasses.firstNotNullOfOrNull { resolveFallback(classRef, it.superclasses) }
        }
        protocols[classRef]?.read?.let { return it }
        return resolveFallback(classRef, classRef.superclasses) ?: throw MissingOperationException(
            "Read operation or 'fallback' read operation for '${classRef.qualifiedName}' expected, but not found")
    }

    private fun resolveWriteMap(classRef: Type): WriteMap {
        fun MutableWriteMap.appendMappings(
            superclasses: List<Type>,
        ): WriteMap? {
            for (superclass in superclasses) {
                val protocol = protocols[superclass]
                val superWrite = protocol?.write ?: continue
                if (superclass !in this) {
                    this[superclass] = superWrite
                    if (protocol.hasStatic) {
                        if (this.size > 1) {
                            throw MalformedProtocolException(entries.first().key,
                                    "Non-static write defined with static supertype write")
                        }
                        return this // End search; static write overrides all others
                    }
                }
            }
            // ...returns null for Any
            return superclasses.firstNotNullOfOrNull { appendMappings(it.superclasses) }
        }

        val writeMap: MutableWriteMap = mutableMapOf()
        val protocol = protocols[classRef]
        protocol?.write?.let { writeMap[classRef] = it }
        if (protocol?.hasStatic != true) {
            writeMap.apply { appendMappings(classRef.superclasses) }
        }
        return writeMap.ifEmpty {
            throw MissingOperationException("Write operation for '${classRef.qualifiedName}' expected, but not found")
        }
    }

    @PublishedApi
    internal class Properties private constructor(
        val readsOrFallbacks: MutableMap<Type, ReadOperation<*>>,
        val writeMaps: MutableMap<Type, WriteMap>,
        val primaryPropertyArrays: MutableMap<String, Array<out Callable>>,
        val primaryConstructors: MutableMap<String, Callable>
    ) {
        constructor(threadSafe: Boolean) : this(
            propertyMap(threadSafe),
            propertyMap(threadSafe),
            propertyMap(threadSafe),
            propertyMap(threadSafe)
        )

        companion object {
            @Suppress("UNCHECKED_CAST")
             private fun <T : MutableMap<Any?,Any?>> propertyMap(threadSafe: Boolean): T {
                val possiblyThreadSafeMap: MutableMap<Any?,Any?> = if (threadSafe) ConcurrentHashMap() else hashMapOf()
                return possiblyThreadSafeMap as T
            }

            fun threadSafe(copyFrom: Properties): Properties {
                return Properties(
                    ConcurrentHashMap(copyFrom.readsOrFallbacks),
                    ConcurrentHashMap(copyFrom.writeMaps),
                    ConcurrentHashMap(copyFrom.primaryPropertyArrays),
                    ConcurrentHashMap(copyFrom.primaryConstructors)
                )
            }
        }
    }
}