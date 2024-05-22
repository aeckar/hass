package kanary

import kanary.utils.takeIf
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.superclasses

/*
    Within the private API, classes and interfaces are both referred to as "classes".
    From the user's perspective, "type" will be used to refer to both instead.
    Similarly, KClass's of defined protocols are named 'classRef'.
    All other class references are simply named 'kClass'.
 */

typealias ReassignmentException = kanary.utils.ReassignmentException

/**
 * Provides a scope wherein protocols for various classes may be defined.
 * A schema with no protocols defined is legal, and should be stored as a variable if used more than once.
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
class Schema @PublishedApi internal constructor(builder: SchemaBuilder) {
    private val protocols: MutableMap<KClass<*>, Protocol>

    // Delegates read to that of defined protocol or 'fallback' read
    private val readOperations: MutableMap<KClass<*>, ReadOperation>

    /*
        Keys contain:
            - Write operations for each type serialized, organized in the order they are written
            - Write of key as last member, if defined
            - Possibly nothing
        Subtypes come before supertypes, the final member being the exception.
        Does not include write operations of supertypes with built-in protocols.
        Iteration order is preserved.
    */
    private val writeSequences: MutableMap<KClass<*>, Set<WriteHandle>>

    init {
        fun MutableSet<WriteHandle>.appendSuperclasses(
            builder: SchemaBuilder,
            superclasses: List<KClass<*>>
        ): Set<WriteHandle>? {
            for (kClass in superclasses) {
                val protocol = builder.definedProtocols[kClass] ?: continue
                val writeOperation = protocol.write ?: continue
                val handle = WriteHandle.wrap(writeOperation)
                if (handle !in this) {
                    this += WriteHandle(kClass, writeOperation)
                    if (protocol.hasStatic) {
                        return null // End search; static write overrides all others
                    }
                }
            }
            for (kClass in superclasses) {
                appendSuperclasses(builder, kClass.superclasses) ?: return this
            }
            return this
        }

        fun resolveRead(builder: SchemaBuilder, superclasses: List<KClass<*>>): TypedReadOperation<*>? {
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
        protocols = builder.definedProtocols
        writeSequences = hashMapOf<KClass<*>, Set<WriteHandle>>().apply {
            for (classRef in protocols.keys) {
                val sequence = mutableSetOf<WriteHandle>()
                val protocol = protocols.getValue(classRef)
                protocol.write?.let { sequence += WriteHandle(classRef, it) }
                this[classRef] = sequence
                    .takeIf { !protocol.hasStatic }
                    ?.appendSuperclasses(builder, classRef.superclasses) ?: sequence
            }
        }
        readOperations = hashMapOf<KClass<*>, ReadOperation>().apply {
            protocols.forEach { (classRef, protocol) ->
                /*
                    Within this scope, since must be checked for every defined protocol.
                    Ensures no supertype has static write.
                 */
                protocol.write.takeIf { !protocol.hasStatic }?.let {
                    if (classRef.allSuperclasses.any { superclass -> protocols[superclass]?.hasStatic == true }) {
                        throw MalformedProtocolException(classRef, "non-static write defined with static supertype write")
                    }
                }

                val read = protocol.read
                if (read != null) {
                    this[classRef] = read
                    return@forEach
                }
                resolveRead(builder, classRef.superclasses)?.let {
                    this[classRef] = it
                }
            }
        }
    }

    /**
     * Returns a new schema containing the protocols of both.
     * Should be used if the union is used only once.
     * If used more than once, a new [schema] should be defined with both [added][SchemaBuilder.plusAssign] to it.
     * @throws ReassignmentException there exist conflicting declarations of a given protocol
     */
    operator fun plus(other: Schema): Schema {
        return schema {
            this += this@Schema
            this += other
        }
    }

    internal fun protocols(): Map<KClass<*>, Protocol> = protocols

    // Returns null if protocol does not exist
    internal fun protocolOf(classRef: KClass<*>): Protocol? {
        return protocols[classRef]
            ?: localProtocolOf(classRef)
    }

    internal fun writeSequenceOf(classRef: KClass<*>): Set<WriteHandle> {
        return writeSequences[classRef]
            ?: resolveWriteSequence(classRef)
            ?: throw MissingOperationException("Write operation for '${classRef.qualifiedName}' expected, but not found")
    }

    internal fun readOperationOf(classRef: KClass<*>): TypedReadOperation<*> {
        return readOperations[classRef]
            ?: resolveReadOperation(classRef)
            ?: throw MalformedProtocolException(classRef,
                    "read or 'fallback' read for '${classRef.qualifiedName}'expected, but not found")
    }

    private fun localProtocolOf(classRef: KClass<*>): Protocol? {
        return classRef.companionObjectInstance.takeIf<Protocol>()?.also { protocols[classRef] = it }
    }

    private fun resolveReadOperation(classRef: KClass<*>, curKClass: KClass<*> = classRef): TypedReadOperation<*>? {
        val superclasses = curKClass.superclasses
        localProtocolOf(classRef)?.read?.let {
            readOperations[classRef] = it
            return it
        }
        for (kClass in superclasses) {
            protocolOf(kClass)?.takeIf { it.hasFallback }?.read?.let {
                readOperations[classRef] = it
                return it
            }
        }
        for (kClass in superclasses) {
            resolveReadOperation(classRef, kClass)?.let { return it }
        }
        return null
    }

    private fun resolveWriteSequence(
        classRef: KClass<*>,
        curKClass: KClass<*> = classRef,
        localWriteSequence: MutableSet<WriteHandle> = mutableSetOf()
    ): Set<WriteHandle>? {
        val superclasses = curKClass.superclasses
        localProtocolOf(curKClass)?.write?.let { localWriteSequence += WriteHandle(curKClass, it) }
        for (kClass in superclasses) {
            writeSequences[kClass]?.let {
                writeSequences[classRef] = it
                return it
            }
            localProtocolOf(kClass)?.write?.let { localWriteSequence += WriteHandle(kClass, it) }
        }
        for (kClass in superclasses) {
            resolveWriteSequence(classRef, kClass, localWriteSequence)?.let { return it }
        }
        return localWriteSequence
            .takeIf { classRef == curKClass && it.isNotEmpty() }
            ?.also { writeSequences[classRef] = it }
    }
}

/**
 * The scope wherein binary I/O protocols may be [defined][define].
 */
class SchemaBuilder @PublishedApi internal constructor() {  // No intent to add explicit versioning support
    @PublishedApi
    internal val definedProtocols = HashMap<KClass<*>, Protocol>()

    /**
     * Provides a scope wherein the [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
     * operations of a type can be defined.
     * @throws MalformedProtocolException [T] is not a top-level or nested class, or has already been defined a protocol
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
        definedProtocols[classRef] = TypeProtocol(builderScope)
    }

    /**
     * Adds all protocols from the given schema to this one.
     * If the union of two schemas is used only sparingly, [Schema.plus] should be used instead.
     * @throws ReassignmentException there exist conflicting declarations of a given protocol
     */
    operator fun plusAssign(other: Schema) {
        val otherClassRefs = other.protocols().keys
        for (classRef in definedProtocols.keys) {
            if (classRef in otherClassRefs) {
                throw ReassignmentException(
                        "Conflicting declarations for protocol of class '${classRef.qualifiedName!!}'")
            }
        }
        definedProtocols += other.protocols()
    }
}

// Equal to another handle iff lambdas are equal
internal data class WriteHandle(val kClass: KClass<*>, val lambda: WriteOperation) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WriteHandle
        return lambda == other.lambda
    }

    override fun hashCode() = lambda.hashCode()

    companion object {
        fun wrap(lambda: WriteOperation) = WriteHandle(Nothing::class, lambda)
    }
}