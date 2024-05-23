//[kanary](../../../index.md)/[kanary](../index.md)/[InputDeserializer](index.md)

# InputDeserializer

[jvm]\
class [InputDeserializer](index.md)(stream: [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html), schema: [Schema](../-schema/index.md)) : [Deserializer](../-deserializer/index.md), [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html)

Reads serialized data from a stream in Kanary format.

Does not need to be closed so long as the underlying stream is closed. Calling [close](close.md) also closes the underlying stream.

## Constructors

| | |
|---|---|
| [InputDeserializer](-input-deserializer.md) | [jvm]<br>constructor(stream: [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html), schema: [Schema](../-schema/index.md)) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [jvm]<br>open override fun [close](close.md)() |
| [read](read.md) | [jvm]<br>open override fun &lt;[T](read.md)&gt; [read](read.md)(): [T](read.md)<br>If [T](read.md) is a primitive type, is capable of reading a primitive value. Can be null. |
| [readBoolean](read-boolean.md) | [jvm]<br>open override fun [readBoolean](read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readByte](read-byte.md) | [jvm]<br>open override fun [readByte](read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readChar](read-char.md) | [jvm]<br>open override fun [readChar](read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readDouble](read-double.md) | [jvm]<br>open override fun [readDouble](read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readFloat](read-float.md) | [jvm]<br>open override fun [readFloat](read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readInt](read-int.md) | [jvm]<br>open override fun [readInt](read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readLong](read-long.md) | [jvm]<br>open override fun [readLong](read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Capable of reading the corresponding boxed type as well. |
| [readShort](read-short.md) | [jvm]<br>open override fun [readShort](read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)<br>Capable of reading the corresponding boxed type as well. |
