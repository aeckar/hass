package io.github.aeckar.kanary

/**
 * Ensure immutability of the underlying mutable collection unless the user
 * requests it  from within the same Serializer they were given the object
 */
internal sealed class DeserializedCollection(val source: Deserializer)

internal class DeserializedList<E>(
    val mutable: MutableList<E>,
    source: Deserializer
) : DeserializedCollection(source), List<E> by mutable

internal class DeserializedMap<K,V>(
    val mutable: MutableMap<K,V>,
    source: Deserializer
) : DeserializedCollection(source), Map<K,V> by mutable

internal class DeserializedSet<E>(
    val mutable: MutableSet<E>,
    source: Deserializer
) : DeserializedCollection(source), Set<E> by mutable