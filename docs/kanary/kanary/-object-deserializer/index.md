//[kanary](../../../index.md)/[kanary](../index.md)/[ObjectDeserializer](index.md)

# ObjectDeserializer

[jvm]\
class [ObjectDeserializer](index.md) : [Deserializer](../-deserializer/index.md)

Deserializer allowing extraction of data from supertypes with a defined [write operation](../-protocol-builder/write.md).

## Properties

| Name | Summary |
|---|---|
| [superclass](superclass.md) | [jvm]<br>val [superclass](superclass.md): [Deserializer](../-deserializer/index.md)<br>A supertype deserializer corresponding to the data serialized by the immediate superclass. If the superclass does not have a defined write operation, is assigned a deserializer containing no data. |

## Functions

| Name | Summary |
|---|---|
| [read](../-deserializer/read.md) | [jvm]<br>open override fun &lt;T&gt; [read](../-deserializer/read.md)(): T<br>If T is a primitive type, is capable of reading a primitive value. Can be null. |
| [readBoolean](../-deserializer/read-boolean.md) | [jvm]<br>open override fun [readBoolean](../-deserializer/read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readByte](../-deserializer/read-byte.md) | [jvm]<br>open override fun [readByte](../-deserializer/read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readChar](../-deserializer/read-char.md) | [jvm]<br>open override fun [readChar](../-deserializer/read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readDouble](../-deserializer/read-double.md) | [jvm]<br>open override fun [readDouble](../-deserializer/read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readFloat](../-deserializer/read-float.md) | [jvm]<br>open override fun [readFloat](../-deserializer/read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readInt](../-deserializer/read-int.md) | [jvm]<br>open override fun [readInt](../-deserializer/read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readLong](../-deserializer/read-long.md) | [jvm]<br>open override fun [readLong](../-deserializer/read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readShort](../-deserializer/read-short.md) | [jvm]<br>open override fun [readShort](../-deserializer/read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [supertype](supertype.md) | [jvm]<br>inline fun &lt;[T](supertype.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [supertype](supertype.md)(): [Deserializer](../-deserializer/index.md) |
