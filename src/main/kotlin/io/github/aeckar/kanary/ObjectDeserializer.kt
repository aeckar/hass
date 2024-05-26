@file:JvmMultifileClass
@file:JvmName("KanaryKt")
package io.github.aeckar.kanary

import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.superclasses

private val EMPTY_DESERIALIZER: Deserializer = InputDeserializer(InputStream.nullInputStream(), schema {})

/**
 * Deserializer allowing extraction of data from supertypes with
 * a defined [write operation][ProtocolBuilder.write].
 */
class ObjectDeserializer internal constructor( // Each instance used to read a single OBJECT
    private val classRef: KClass<*>,
    private val supertypes: Map<KClass<*>, Deserializer>,
    private val source: InputDeserializer
) : Deserializer by source {
    /**
     * A supertype deserializer corresponding to the data serialized by the immediate superclass.
     * If the superclass does not have a defined write operation, is assigned a deserializer containing no data.
     */
    val superclass: Deserializer by lazy { supertype(classRef.superclasses.first()) }

    /**
     * @return a supertype deserializer corresponding to the data serialized by given supertype.
     * If the supertype does not have a defined write operation, returns a deserializer containing no data.
     * @throws MalformedProtocolException [T] is not a supertype
     */
    inline fun <reified T : Any> supertype() = supertype(T::class)

    @PublishedApi
    internal fun supertype(classRef: KClass<*>): Deserializer {
        return supertypes[classRef] ?: if (classRef.isSuperclassOf(classRef)) {
            EMPTY_DESERIALIZER
        } else {
            throw MalformedProtocolException(classRef,
                "type '${classRef.qualifiedName ?: "<local or anonymous>"}' is not a supertype")
        }
    }

    internal fun resolveObject(): Any? {
        return try {
            source.schema.readOrFallbackOf(classRef)(this).also { source.readFlag() /* END_OBJECT */ }
        } catch (_: NoSuchElementException) {
            throw MalformedProtocolException(classRef,
                "attempted read of object after object deserializer was exhausted")
        }
    }
}