package io.github.aeckar.kanary

import io.github.aeckar.kanary.reflect.Type
import kotlin.reflect.full.superclasses

/**
 * Deserializer allowing extraction of data from supertypes with
 * a defined [write operation][ProtocolBuilder.write].
 */
class ObjectDeserializer internal constructor( // Each instance used to read a single OBJECT
    private val classRef: Type,
    private val supertypes: Map<Type, Deserializer /* is SupertypeDeserializer */>,
    private val parent: InputDeserializer
) : CollectionDeserializer(parent), Deserializer by parent {
    private val deserializer inline get() = this    // Used to invoke defined read operation
    /**
     * A supertype deserializer corresponding to the data serialized by the immediate superclass.
     * If the superclass does not have a defined write operation, is assigned a deserializer containing no data.
     */
    val superclass: Deserializer by lazy { supertype(classRef.superclasses.first()) }

    // ------------------------------ public API ------------------------------

    /**
     * @return a supertype deserializer corresponding to the data serialized by given supertype.
     * If the supertype does not have a defined write operation, returns a deserializer containing no data.
     * @throws MalformedProtocolException [T] is not a supertype
     */
    inline fun <reified T : Any> supertype() = supertype(T::class)

    // ------------------------------------------------------------------------

    @PublishedApi
    internal fun supertype(classRef: Type): Deserializer {
        return supertypes[classRef]
            ?: throw MalformedProtocolException(this.classRef,
                    "Type '${classRef.qualifiedName ?: "<local or anonymous>"}' is not a supertype " +
                    "or does not have a defined write operation")
    }

    internal fun resolveObject(): Any? {
        return try {
            val read = parent.schema.readOrFallbackOf(classRef)
            with(read) { deserializer.readOperation() }.also { parent.stream.readTypeFlag() /* = END_OBJECT */ }
        } catch (_: NoSuchElementException) {
            throw MalformedProtocolException(classRef,
                    "Attempted read of object after object deserializer was exhausted")
        }
    }
}