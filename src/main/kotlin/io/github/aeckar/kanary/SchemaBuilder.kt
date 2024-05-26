@file:JvmMultifileClass
@file:JvmName("KanaryKt")
package io.github.aeckar.kanary

import io.github.aeckar.kanary.utils.companion
import io.github.aeckar.kanary.utils.takeIf
import kotlin.reflect.KClass

/**
 * The scope wherein binary I/O protocols may be [defined][define].
 */
class SchemaBuilder @PublishedApi internal constructor() {  // No intent to add explicit versioning support
    @PublishedApi
    internal val definedProtocols: MutableMap<KClass<*>, Protocol> = HashMap()

    /**
     * Provides a scope wherein the [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
     * operations of a type can be defined.
     * @throws MalformedProtocolException [T] is not a top-level or nested class,
     * or has already been defined a protocol,
     * or either of the operations are defined twice,
     * or this is called more than once for type [T]
     */
    inline fun <reified T : Any> define(builder: ProtocolBuilder<T>.() -> Unit) {
        val classRef = T::class
        if (classRef in TypeFlag.K_CLASSES) {
            throw MalformedProtocolException(classRef, "defined by default")
        }
        if (classRef in definedProtocols) {
            throw MalformedProtocolException(classRef, "defined more than once")
        }
        val builderScope = ProtocolBuilder<T>(classRef)
        builder(builderScope)
        definedProtocols[classRef] = classRef.companion?.takeIf<Protocol>()?.let { mergeProtocols(builderScope, it) }
            ?: Protocol(builderScope.readOrNull(), builderScope.writeOrNull())

    }

    /**
     * Adds all protocols from the given schema to this one.
     *
     * If the union of two schemas is used only sparingly, [Schema.plus] should be used instead.
     * @throws MalformedProtocolException there exist conflicting declarations of a given protocol
     */
    operator fun plusAssign(other: Schema) {
        val otherClassRefs = other.protocols.keys
        for (classRef in definedProtocols.keys) {
            if (classRef in otherClassRefs) {
                throw MalformedProtocolException(classRef,
                        "Conflicting declarations for protocol of class '${classRef.qualifiedName!!}'")
            }
        }
        definedProtocols += other.protocols
    }

    @PublishedApi
    internal fun mergeProtocols(builder: ProtocolBuilder<*>, localProtocol: Protocol): Protocol {
        val read = if (builder.readOrNull() != null) {
            localProtocol.read?.let {
                throw MalformedProtocolException(builder.classRef, "conflicting definitions of read operation")
            }
            builder.readOrNull()
        } else {
            localProtocol.read
        }
        val write = if (builder.writeOrNull() != null) {
            localProtocol.write?.let {
                throw MalformedProtocolException(builder.classRef, "conflicting definitions of write operation")
            }
            builder.writeOrNull()
        } else {
            localProtocol.write
        }
        return Protocol(read, write)
    }
}