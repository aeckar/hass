@file:JvmName("Kanary")
@file:JvmMultifileClass
package io.github.aeckar.kanary

import java.io.OutputStream

/**
 * See [Schema] for a list of types that can be serialized by default.
 * @return a new serializer capable of writing primitives, primitive arrays,
 * and instances of any type with a defined protocol to Kanary format
 */
fun OutputStream.serializer(protocols: Schema) = OutputSerializer(this, protocols)

/**
 * Writes the objects in binary format according to the protocol of each type.
 *
 * Null objects are accepted, however their non-nullable type information is erased.
 * If an object is not null and its type does not have a defined protocol, the protocol of its superclass or
 * the first interface declared in source code with a protocol is chosen.
 * If no objects are supplied, nothing is serialized.
 * @throws MissingOperationException any object of an anonymous or local class,
 * or an appropriate write operation cannot be found
 */
fun Serializer.write(vararg objs: Any?) {
    objs.forEach { write(it) }
}

/**
 * Permits the serialization of data in Kanary format.
 */
sealed interface Serializer {
    /**
     * Serializes the value without autoboxing.
     */
    fun writeBoolean(cond: Boolean)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeByte(b: Byte)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeChar(c: Char)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeShort(n: Short)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeInt(n: Int)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeLong(n: Long)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeFloat(n: Float)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeDouble(n: Double)

    /**
     * Serializes the object or boxed primitive value.
     *
     * [SAM conversions](https://kotlinlang.org/docs/fun-interfaces.html#sam-conversions)
     * may be deserialized as the functional interface they implement or their exact function type (e.g. () -> Unit).
     *
     * Iterables, unless they have another applicable protocol, are deserialized as lists.
     *
     * Schemas may, optionally, be serialized to reduce
     * any overhead caused by their initialization.
     *
     * See [Schema] for the full list of types with pre-defined protocols.
     * @throws MissingOperationException obj is not an instance of a top-level or nested class,
     * or a suitable write operation cannot be determined
     * @throws MalformedContainerException a [container][Container] is passed whose primary constructor
     * is not public, or whose primary constructor arguments are not all public properties
     * @throws java.io.NotSerializableException
     * A lambda or SAM conversion is passed that does not implement [Serializable]
     * (either by direct implementation or by annotating with [JvmSerializableLambda])
     */
    fun write(obj: Any?)

    /**
     * Serializes the array without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(array: Array<out T>)

    /**
     * Serializes the list without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(list: List<T>)

    /**
     * Serializes the iterable without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(iter: Iterable<T>)

    /**
     * Serializes the pair without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(pair: Pair<T, T>)

    /**
     * Serializes the triple without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(triple: Triple<T, T, T>)

    /**
     * Serializes the map entry without checking for null elements.
     * @throws MissingOperationException the key or value is not an instance of a top-level or nested class,
     * or a suitable write operation for either cannot be determined
     */
    fun <K : Any, V : Any> write(entry: Map.Entry<K, V>)

    /**
     * Serializes the map without checking for null keys or values.
     * @throws MissingOperationException any key or value is not an instance of a top-level or nested class,
     * or a suitable write operation for any cannot be determined
     */
    fun <K : Any, V : Any> write(map: Map<K, V>)
}