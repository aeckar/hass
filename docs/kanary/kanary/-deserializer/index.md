//[kanary](../../../index.md)/[kanary](../index.md)/[Deserializer](index.md)

# Deserializer

sealed interface [Deserializer](index.md)

Reads serialized data from a stream in Kanary format.

#### Inheritors

| |
|---|
| [ExhaustibleDeserializer](../-exhaustible-deserializer/index.md) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [jvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [read](read.md) | [jvm]<br>abstract fun &lt;[T](read.md)&gt; [read](read.md)(): [T](read.md)<br>If [T](read.md) is a primitive type, is capable of reading a primitive value. Can be null. |
| [readBoolean](read-boolean.md) | [jvm]<br>abstract fun [readBoolean](read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readByte](read-byte.md) | [jvm]<br>abstract fun [readByte](read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readChar](read-char.md) | [jvm]<br>abstract fun [readChar](read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readDouble](read-double.md) | [jvm]<br>abstract fun [readDouble](read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readFloat](read-float.md) | [jvm]<br>abstract fun [readFloat](read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readInt](read-int.md) | [jvm]<br>abstract fun [readInt](read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readLong](read-long.md) | [jvm]<br>abstract fun [readLong](read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readShort](read-short.md) | [jvm]<br>abstract fun [readShort](read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)<br>Capable of reading the corresponding boxed type as well. |
