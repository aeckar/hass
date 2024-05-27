package io.github.aeckar.kanary

import io.github.aeckar.kanary.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.superclasses

internal typealias WriteMap = Map<Type, WriteOperation>
private typealias MutableWriteMap = MutableMap<Type, WriteOperation>

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
    val builderScope = SchemaBuilder()
    builder(builderScope)
    val protocols = builderScope.definedProtocols
    return if (threadSafe) {
        Schema(protocols, ConcurrentHashMap(), ConcurrentHashMap())
    } else {
        Schema(protocols, hashMapOf(), hashMapOf())
    }
}

/**
 * Defines a set of protocols corresponding to how certain types should be written to and read from binary.
 *
 * The following types have pre-defined protocols:
 *
 * |               |             |           |
 * |---------------|-------------|-----------|
 * | BooleanArray  | DoubleArray | Map.Entry |
 * | ByteArray     | String      | Map       |
 * | CharArray     | Array       | Unit      |
 * | ShortArray    | List        | (lambda)  |
 * | IntArray      | Iterable    | (null)    |
 * | LongArray     | Pair        |           |
 * | FloatArray    | Triple      |           |
 *
 * Any built-in read operation designated to an open or abstract type is
 * given the '[fallback][ProtocolBuilder.fallback]' modifier.
 * Serialized lambdas must be annotated with [JvmSerializableLambda].
 */
class Schema @PublishedApi internal constructor(
    internal val protocols: Map<Type, Protocol>,
    private val readsOrFallbacks: MutableMap<Type, ReadOperation>,
    private val writeSequences: MutableMap<Type, WriteMap>
) {
    // ------------------------------ public API ------------------------------

    /**
     * Returns a new schema containing the protocols of both.
     *
     * Should be used if the union is used only once.
     * If used more than once, a new [schema] should be defined with both [added][SchemaBuilder.plusAssign] to it.
     * @throws MalformedProtocolException there exist conflicting declarations of a given protocol
     */
    operator fun plus(other: Schema): Schema {
        return schema {
            this += this@Schema
            this += other
        }
    }

    // ------------------------------------------------------------------------

    internal fun writeMapOf(classRef: Type): WriteMap {
        return writeSequences[classRef] ?: resolveWriteMap(classRef).also { writeSequences[classRef] = it }
    }

    internal fun readOrFallbackOf(classRef: Type): TypedReadOperation<*> {
        return readsOrFallbacks[classRef] ?: resolveReadOrFallback(classRef).also { readsOrFallbacks[classRef] = it }
    }

    /*
        Properties are resolved in the following order:
            - Subtypes before supertypes
            - For a given type, its supertypes in the order that they are declared in source code
     */

    private fun resolveReadOrFallback(classRef: Type): TypedReadOperation<*> {
        fun resolveFallback(classRef: Type, superclasses: List<Type>): TypedReadOperation<*>? {
            for (kClass in superclasses) {
                protocols[kClass]?.takeIf { it.hasFallback }?.read?.let { return it }
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
            for (kClass in superclasses) {
                val protocol = protocols[kClass]
                val lambda = protocol?.write ?: continue
                if (kClass !in this) {
                    this[kClass] = lambda
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
    
    companion object {
        val READ = readOf {
            val threadSafe = readBoolean()
            if (threadSafe) {
                Schema(read(), read<Map<Type, ReadOperation>>().asMutableMap(), read<Map<Type, WriteMap>>().asMutableMap())
            } else {
                Schema(read(), ConcurrentHashMap(read<Map<Type, ReadOperation>>()), ConcurrentHashMap(read<Map<Type, WriteMap>>()))
            }
        }
        val WRITE = writeOf<Schema> {
            writeBoolean(it.writeSequences is ConcurrentHashMap)
            write(it.protocols)
            write(it.readsOrFallbacks)
            write(it.writeSequences)
        }
    }
}

