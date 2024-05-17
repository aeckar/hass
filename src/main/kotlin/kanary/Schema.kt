package kanary

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.superclasses

/*
    Within the API, classes and interfaces are both referred to as "classes".
    From the user's perspective, "type" will be used to refer to both instead.
    Similarly, KClass's of defined protocols are named 'classRef'.
    All other class references are simply named 'kClass'.
 */

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
 * Defines a set of protocols corresponding to how certain types should be written to and read from binary.
 */
class Schema {
    /*
      Keys contain:
        - Write operations for each type serialized, organized in the order they are written
        - Write of key as last member, if defined
        - Possibly nothing
      Subtypes come before supertypes, the final member being the exception
     */
    internal val writeSequences: Map<KClass<*>, List<WriteSpecifier>>

    // Delegates read to that of defined protocol or 'fallback' read
    internal val actualReads: Map<KClass<*>, ReadOperation<*>>

    internal val definedProtocols: Map<KClass<*>, Protocol<*>>

    @PublishedApi
    internal constructor(builder: SchemaBuilder) {
        fun MutableList<WriteSpecifier>.buildWriteSequence(
            builder: SchemaBuilder,
            superclasses: List<KClass<*>>
        ): List<WriteSpecifier>? {
            for (kClass in superclasses) {
                val protocol = builder.definedProtocols[kClass] ?: continue
                val writeOperation = protocol.write ?: continue
                if (none { it.write === writeOperation }) {
                    this += WriteSpecifier(kClass, writeOperation)
                    if (protocol.hasStatic) {
                        return null // End search; static write overrides all others
                    }
                }
            }
            for (kClass in superclasses) {
                buildWriteSequence(builder, kClass.superclasses) ?: return this
            }
            return this
        }

        fun resolveRead(builder: SchemaBuilder, superclasses: List<KClass<*>>): ReadOperation<*>? {
            for (kClass in superclasses) {
                builder.definedProtocols[kClass]?.let { protocol ->
                    if (protocol.hasFallback) {
                        return protocol.read
                    }
                }
            }
            for (kClass in superclasses) {
                resolveRead(builder, kClass.superclasses)?.let { return it }
            }
            return null
        }

        definedProtocols = builder.definedProtocols
        writeSequences = HashMap<KClass<*>, List<WriteSpecifier>>().apply {
            for (classRef in (definedProtocols as Map).keys) {
                val sequence = mutableListOf<WriteSpecifier>()
                val protocol = definedProtocols.getValue(classRef)
                protocol.write?.let { sequence += WriteSpecifier(classRef, it) }
                this[classRef] = sequence
                    .takeIf { !protocol.hasStatic }
                    ?.buildWriteSequence(builder, classRef.superclasses) ?: sequence
            }
        }
        actualReads = HashMap<KClass<*>, ReadOperation<*>>().apply {
            builder.definedProtocols.forEach { (classRef, protocol) ->
                protocol.takeIf { !it.hasNoinherit }?.write?.let {  // Ensure no supertype has static write
                    if (classRef.allSuperclasses.any { superclass -> definedProtocols[superclass]?.hasStatic == true }) {
                        throw MalformedProtocolException(classRef, "write defined when supertype write is 'static'")
                    }
                }
                if (protocol.read != null) {
                    this[classRef] = protocol.read
                    return@forEach
                }
                resolveRead(builder, classRef.superclasses)?.let {
                    this[classRef] = it
                }
            }
        }
    }

    private constructor(
        writeSequences: Map<KClass<*>, List<WriteSpecifier>>,
        reads: Map<KClass<*>, ReadOperation<*>>,
        definedProtocols: Map<KClass<*>, Protocol<*>>,
    ) {
        this.writeSequences = writeSequences
        this.actualReads = reads
        this.definedProtocols = definedProtocols
    }

    /**
     * Useful as a utility, but slower than simply defining all protocols within the same schema.
     * @return a new schema containing the protocols of each
     * @throws ReassignmentException the sets contain conflicting declarations of a given protocol
     */
    operator fun plus(other: Schema): Schema {
        val otherTypes = other.definedProtocols.keys
        for (kClass in definedProtocols.keys) {
            if (kClass in otherTypes) {
                throw ReassignmentException("Conflicting declarations for protocol of class '${kClass.qualifiedName!!}'")
            }
        }
        return Schema(
            writeSequences + other.writeSequences,
            actualReads + other.actualReads,
            definedProtocols + other.definedProtocols
        )
    }

    internal fun resolveWriteSequence(superclasses: List<KClass<*>>): List<WriteSpecifier>? {
        for (kClass in superclasses) {
            writeSequences[kClass]?.let { return it }
        }
        for (kClass in superclasses) {
            resolveWriteSequence(kClass.superclasses)?.let { return it }
        }
        return null
    }

    internal fun resolveRead(classRef: KClass<*>): ReadOperation<*> {
        actualReads[classRef]?.let { return it }
        resolveFallbackRead(classRef.superclasses)?.let { return it }
        throw MalformedProtocolException(classRef, "read or 'fallback' read for '${classRef.qualifiedName}' not found")
    }

    private fun resolveFallbackRead(superclasses: List<KClass<*>>): ReadOperation<*>? {
        for (kClass in superclasses) {
            definedProtocols[kClass]?.let { if (it.hasFallback) return it.read }
        }
        for (kClass in superclasses) {
            resolveFallbackRead(kClass.superclasses)?.let { return it }
        }
        return null
    }

    internal companion object {
        val EMPTY = Schema(emptyMap(), emptyMap(), emptyMap())
    }
}

/**
 * The scope wherein binary I/O [protocols][define] may be defined.
 */
class SchemaBuilder @PublishedApi internal constructor() {  // No intent to add versioning support
    @PublishedApi
    internal val definedProtocols = HashMap<KClass<*>, Protocol<*>>()

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
}

internal data class WriteSpecifier(val kClass: KClass<*>, val write: WriteOperation<*>)