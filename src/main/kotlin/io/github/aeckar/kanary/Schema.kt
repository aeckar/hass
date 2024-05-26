package io.github.aeckar.kanary

import io.github.aeckar.kanary.utils.CheckedInputStream
import io.github.aeckar.kanary.utils.SingletonByteArray
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

internal typealias WriteMap = Map<KClass<*>, WriteOperation>

private typealias MutableWriteMap = MutableMap<KClass<*>, WriteOperation>

/**
 * Provides a scope wherein protocols for various classes may be defined.
 *
 * A schema with no protocols defined is legal, and should be stored as a variable if used more than once.
 * For operations accessing private members, they must be defined outside the builder scope.
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
class Schema @PublishedApi internal constructor(builder: SchemaBuilder) {
    internal val protocols: Map<KClass<*>, Protocol> = builder.definedProtocols

    /**
     * @see CheckedInputStream.readRaw
     */
    internal val rawBuffer = SingletonByteArray()

    private val readsOrFallbacks: MutableMap<KClass<*>, ReadOperation> = hashMapOf()
    private val writeSequences: MutableMap<KClass<*>, WriteMap> = hashMapOf()

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

    internal fun writeMapOf(classRef: KClass<*>): WriteMap {
        return writeSequences[classRef] ?: resolveWriteMap(classRef).also { writeSequences[classRef] = it }
    }

    internal fun readOrFallbackOf(classRef: KClass<*>): TypedReadOperation<*> {
        return readsOrFallbacks[classRef] ?: resolveReadOrFallback(classRef).also { readsOrFallbacks[classRef] = it }
    }

    /*
        Properties are resolved in the following order:
            - Subtypes before supertypes
            - For a given type, its supertypes in the order that they are declared in source code
     */

    private fun resolveReadOrFallback(classRef: KClass<*>): TypedReadOperation<*> {
        fun resolveFallback(classRef: KClass<*>, superclasses: List<KClass<*>>): TypedReadOperation<*>? {
            for (kClass in superclasses) {
                protocols[kClass]?.takeIf { it.hasFallback }?.read?.let { return it }
            }
            return superclasses.firstNotNullOfOrNull { resolveFallback(classRef, it.superclasses) }
        }

        protocols[classRef]?.read?.let { return it }
        return resolveFallback(classRef, classRef.superclasses) ?: throw MissingOperationException(
            "Read operation or 'fallback' read operation for '${classRef.qualifiedName}' expected, but not found")
    }

    private fun resolveWriteMap(classRef: KClass<*>): WriteMap {
        fun MutableWriteMap.appendMappings(
            superclasses: List<KClass<*>>,
        ): WriteMap? {
            for (kClass in superclasses) {
                val protocol = protocols[kClass]
                val lambda = protocol?.write ?: continue
                if (kClass !in this) {
                    this[kClass] = lambda
                    if (protocol.hasStatic) {
                        if (this.isNotEmpty()) {
                            throw MalformedProtocolException(entries.first().key,
                                    "non-static write defined with static supertype write")
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
}