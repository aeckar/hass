@file:Suppress("UNUSED")
package io.github.aeckar.kanary

/**
 * Permits the reading of serialized collections in Kanary format.
 *
 * Provides operations to efficiently convert deserialized collections to their mutable counterparts.
 */
sealed class CollectionDeserializer : Deserializer {
    private val parent: Deserializer
    private val self inline get() = this    // Easy fix for 'leaking this'

    constructor() {
        parent = self
    }

    constructor(parent: Deserializer) {
        this.parent = parent
    }

    // ------------------------------ public API ------------------------------

    /**
     * If the list was instantiated through deserialization, its backing [MutableList] is returned.
     * @return the receiver as a mutable list
     * @throws IllegalArgumentException the list was not instantiated through deserialization
     */
    fun <E> List<E>.asMutableList() = ensureCast<DeserializedList<E>>().mutable

    /**
     * If the map was instantiated through deserialization, its backing [MutableMap] is returned.
     * @return the receiver as a mutable map
     * @throws IllegalArgumentException the map was not instantiated through deserialization
     */
    fun <K, V> Map<K, V>.asMutableMap() = ensureCast<DeserializedMap<K,V>>().mutable

    /**
     * If the set was instantiated through deserialization, its backing [MutableSet] is returned.
     * @return the receiver as a mutable set
     * @throws IllegalArgumentException the set was not instantiated through deserialization
     */
    fun <E> Set<E>.asMutableSet(): MutableSet<E> = ensureCast<DeserializedSet<E>>().mutable

    // ------------------------------------------------------------------------

    private inline fun <reified T : DeserializedCollection> Any.ensureCast(): T {
        require(this is T) {
            "Collection was not instantiated through deserialization, and thus has no backing mutable counterpart"
        }
        require(source ===  parent) { "Collection does not belong this deserializer" }
        return this
    }
}