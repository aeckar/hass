//[kanary](../../../index.md)/[kanary](../index.md)/[PrimitiveDeserializer](index.md)

# PrimitiveDeserializer

open class [PrimitiveDeserializer](index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html)

Reads serialized data from a stream in Kanary format. Does not need to be closed so long as the underlying stream is closed. Because no protocols are defined, no instances of any reference types may be read. Calling [close](close.md) also closes the underlying stream.

#### Inheritors

| |
|---|
| [Deserializer](../-deserializer/index.md) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [jvm]<br>open override fun [close](close.md)() |
| [readBoolean](read-boolean.md) | [jvm]<br>fun [readBoolean](read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [readBooleanArray](read-boolean-array.md) | [jvm]<br>fun [readBooleanArray](read-boolean-array.md)(): [BooleanArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean-array/index.html) |
| [readByte](read-byte.md) | [jvm]<br>fun [readByte](read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html) |
| [readChar](read-char.md) | [jvm]<br>fun [readChar](read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html) |
| [readCharArray](read-char-array.md) | [jvm]<br>fun [readCharArray](read-char-array.md)(): [CharArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char-array/index.html) |
| [readDouble](read-double.md) | [jvm]<br>fun [readDouble](read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [readDoubleArray](read-double-array.md) | [jvm]<br>fun [readDoubleArray](read-double-array.md)(): [DoubleArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double-array/index.html) |
| [readFloat](read-float.md) | [jvm]<br>fun [readFloat](read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [readFloatArray](read-float-array.md) | [jvm]<br>fun [readFloatArray](read-float-array.md)(): [FloatArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float-array/index.html) |
| [readInt](read-int.md) | [jvm]<br>fun [readInt](read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [readIntArray](read-int-array.md) | [jvm]<br>fun [readIntArray](read-int-array.md)(): [IntArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int-array/index.html) |
| [readLong](read-long.md) | [jvm]<br>fun [readLong](read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [readLongArray](read-long-array.md) | [jvm]<br>fun [readLongArray](read-long-array.md)(): [LongArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long-array/index.html) |
| [readShort](read-short.md) | [jvm]<br>fun [readShort](read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html) |
| [readShortArray](read-short-array.md) | [jvm]<br>fun [readShortArray](read-short-array.md)(): [ShortArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short-array/index.html) |
| [readString](read-string.md) | [jvm]<br>fun [readString](read-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
