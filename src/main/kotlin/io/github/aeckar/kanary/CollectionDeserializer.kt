package io.github.aeckar.kanary

/**
 * Permits the reading of serialized, built-in collections in Kanary format.
 *
 * Provides operations to efficiently converted deserialized collections to their mutable counterparts.
 */
sealed class CollectionDeserializer : Deserializer {
    private val parent: Deserializer
    private val instance inline get() = this    // Easy fix for 'leaking this'

    constructor() {
        parent = instance
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
    fun <E> List<E>.asMutableList(): MutableList<E> {
        require(this is DeserializedList && source === parent) {
            "List was not instantiated through deserialization, and thus has no backing mutable list"
        }
        return this.mutable
    }

    /**
     * If the map was instantiated through deserialization, its backing [MutableMap] is returned.
     * @return the receiver as a mutable map
     * @throws IllegalArgumentException the map was not instantiated through deserialization
     */
    fun <K, V> Map<K, V>.asMutableMap(): MutableMap<K,V> {
        require(this is DeserializedMap && source === parent) {
            "Map was not instantiated through deserialization, and thus has no backing mutable map"
        }
        return this.mutable
    }

    /**
     * If the set was instantiated through deserialization, its backing [MutableSet] is returned.
     * @return the receiver as a mutable set
     * @throws IllegalArgumentException the set was not instantiated through deserialization
     */
    fun <E> Set<E>.asMutableSet(): MutableSet<E> {
        require(this is DeserializedSet && source === parent) {
            "Set was not instantiated through deserialization, and thus has no backing mutable set"
        }
        return this.mutable
    }

    // ------------------------------------------------------------------------
}