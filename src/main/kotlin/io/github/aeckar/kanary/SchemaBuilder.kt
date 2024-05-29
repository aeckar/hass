package io.github.aeckar.kanary

import io.github.aeckar.kanary.io.TypeFlag
import io.github.aeckar.kanary.reflect.Type

/**
 * The scope wherein binary I/O protocols may be [defined][define].
 */
class SchemaBuilder @PublishedApi internal constructor() {  // No intent to add explicit versioning support
    /**
     * Enables the adding of all protocols from another [Schema] to this one.
     */
    val import inline get() = ImportStatement(this)

    @PublishedApi
    internal val definedProtocols: MutableMap<Type, Protocol> = HashMap()

    // ------------------------------ public API ------------------------------

    /**
     * Provides a scope wherein the [read][ProtocolBuilder.read] and [write][ProtocolBuilder.write]
     * operations of a type can be defined.
     *
     * Protocols that do not define either operation are legal.
     * Doing so disallows the serialization of objects of that specific type.
     * @throws MalformedProtocolException [T] is not a top-level or nested class,
     * or has already been defined a protocol,
     * or either of the operations are defined twice,
     * or this is called more than once for the given type
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> define(builder: ProtocolBuilder<T>.() -> Unit) {
        val classRef = T::class
        if (classRef in TypeFlag.TYPES) {
            throw MalformedProtocolException(classRef, "Protocol defined by default")
        }
        if (classRef in definedProtocols) {
            throw MalformedProtocolException(classRef, "Protocol defined more than once")
        }
        val builderScope = ProtocolBuilder<T>(classRef)
        builder(builderScope)
        definedProtocols[classRef] = with (builderScope) {
            Protocol(read, write as WriteOperation?, hasFallback, hasStatic)
        }
    }

    /**
     * An [import][SchemaBuilder.import] statement.
     */
    @JvmInline
    value class ImportStatement @PublishedApi internal constructor(private val parent: SchemaBuilder) {
        /**
         * Adds all protocols from the given schema to this one.
         *
         * If the union of two schemas is used only sparingly, [Schema.plus] should be used instead.
         * @throws MalformedProtocolException there exist conflicting declarations of a given protocol
         */
        infix fun from(other: Schema) {
            val otherClassRefs = other.protocols.keys
            for (classRef in parent.definedProtocols.keys) {
                if (classRef in otherClassRefs) {
                    throw MalformedProtocolException(classRef, "Conflicting protocol declarations")
                }
            }
            parent.definedProtocols += other.protocols
        }
    }

    // ------------------------------------------------------------------------
}