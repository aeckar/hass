//[kanary](../../../index.md)/[kanary](../index.md)/[ExhaustibleDeserializer](index.md)

# ExhaustibleDeserializer

sealed interface [ExhaustibleDeserializer](index.md) : [Deserializer](../-deserializer/index.md)

Reads serialized data from a stream in Kanary format. Can be used to determine whether there is more data that can be read from this object.

#### Inheritors

| |
|---|
| [PolymorphicDeserializer](../-polymorphic-deserializer/index.md) |
| [InputDeserializer](../-input-deserializer/index.md) |

## Functions

| Name | Summary |
|---|---|
| [isExhausted](is-exhausted.md) | [jvm]<br>abstract fun [isExhausted](is-exhausted.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isNotExhausted](is-not-exhausted.md) | [jvm]<br>abstract fun [isNotExhausted](is-not-exhausted.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [read](../-deserializer/read.md) | [jvm]<br>abstract fun &lt;[T](../-deserializer/read.md)&gt; [read](../-deserializer/read.md)(): [T](../-deserializer/read.md)<br>If [T](../-deserializer/read.md) is a primitive type, is capable of reading a primitive value. Can be null. |
| [readBoolean](../-deserializer/read-boolean.md) | [jvm]<br>abstract fun [readBoolean](../-deserializer/read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readByte](../-deserializer/read-byte.md) | [jvm]<br>abstract fun [readByte](../-deserializer/read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readChar](../-deserializer/read-char.md) | [jvm]<br>abstract fun [readChar](../-deserializer/read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readDouble](../-deserializer/read-double.md) | [jvm]<br>abstract fun [readDouble](../-deserializer/read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readFloat](../-deserializer/read-float.md) | [jvm]<br>abstract fun [readFloat](../-deserializer/read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readInt](../-deserializer/read-int.md) | [jvm]<br>abstract fun [readInt](../-deserializer/read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readLong](../-deserializer/read-long.md) | [jvm]<br>abstract fun [readLong](../-deserializer/read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readShort](../-deserializer/read-short.md) | [jvm]<br>abstract fun [readShort](../-deserializer/read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)<br>Capable of reading the corresponding boxed type as well. |
