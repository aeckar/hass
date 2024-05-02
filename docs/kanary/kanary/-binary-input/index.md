//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)

# BinaryInput

[jvm]\
@[JvmInline](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-inline/index.html)

value class [BinaryInput](index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html)

A binary stream with functions for reading primitives or classes with a [protocolOf](../protocol-of.md) in Kanary format. Does not support marking. Calling [close](close.md) also closes the underlying stream.

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [jvm]<br>open override fun [close](close.md)() |
| [read](read.md) | [jvm]<br>inline fun &lt;[T](read.md)&gt; [read](read.md)(): [T](read.md)<br>Reads an object of type [T](read.md) from binary according to the protocol of its type. |
| [readBoolean](read-boolean.md) | [jvm]<br>fun [readBoolean](read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [readByte](read-byte.md) | [jvm]<br>fun [readByte](read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html) |
| [readChar](read-char.md) | [jvm]<br>fun [readChar](read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html) |
| [readDouble](read-double.md) | [jvm]<br>fun [readDouble](read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [readFloat](read-float.md) | [jvm]<br>fun [readFloat](read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [readInt](read-int.md) | [jvm]<br>fun [readInt](read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [readLong](read-long.md) | [jvm]<br>fun [readLong](read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [readShort](read-short.md) | [jvm]<br>fun [readShort](read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html) |
| [readString](read-string.md) | [jvm]<br>fun [readString](read-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
