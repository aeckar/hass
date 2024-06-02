package io.github.aeckar.kanary

import java.io.EOFException
import java.io.InputStream

/**
 * See [Schema] for a list of types that can be deserialized by default.
 * @return a new deserializer capable of reading primitives, primitive arrays, strings, and
 * instances of any type with a defined protocol from Kanary format
 */
fun InputStream.deserializer(protocols: Schema) = InputDeserializer(this, protocols)

/**
 * Permits the reading of serialized data in Kanary format.
 */
sealed interface Deserializer {
    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readBoolean(): Boolean

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readByte(): Byte

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readChar(): Char

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readShort(): Short

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readInt(): Int

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readLong(): Long

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readFloat(): Float

    /**
     * Capable of reading the corresponding boxed type as well.
     * @return the serialized value, unboxed
     * @throws
     * @throws TypeFlagMismatchException an object of a different type was serialized in the current stream position
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun readDouble(): Double

    /**
     * If [T] is a primitive type, is capable of reading a primitive value.
     *
     * Can be null. If the serialized object is null and [T] is a nullable type, it cannot be guaranteed
     * that the object passed to [Serializer.write] agrees with the specified type parameter.
     * @return the serialized object of the given type
     * @throws TypeFlagMismatchException the value was not serialized as a singular object or null
     * @throws TypeCastException the object is not an instance of type [T]
     * @throws MalformedContainerException if the serialized object is container and
     * its primary constructor is not public
     * @throws EOFException the stream is exhausted before a value can be determined
     */
    fun <T> read(): T
}