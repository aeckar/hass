//[kanary](../../../index.md)/[kanary](../index.md)/[PrimitiveSerializer](index.md)

# PrimitiveSerializer

open class [PrimitiveSerializer](index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html), [Flushable](https://docs.oracle.com/javase/8/docs/api/java/io/Flushable.html)

Writes serialized data to a stream in Kanary format. Does not need to be closed so long as the underlying stream is closed. Because no protocols are defined, no instances of any reference types may be written. Calling [close](close.md) also closes the underlying stream.

#### Inheritors

| |
|---|
| [Serializer](../-serializer/index.md) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [jvm]<br>open override fun [close](close.md)() |
| [flush](flush.md) | [jvm]<br>open override fun [flush](flush.md)() |
| [write](write.md) | [jvm]<br>fun [write](write.md)(cond: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>fun [write](write.md)(condArr: [BooleanArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean-array/index.html))<br>fun [write](write.md)(b: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html))<br>fun [write](write.md)(bArr: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html))<br>fun [write](write.md)(c: [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html))<br>fun [write](write.md)(cArr: [CharArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char-array/index.html))<br>fun [write](write.md)(fp: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html))<br>fun [write](write.md)(nArr: [DoubleArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double-array/index.html))<br>fun [write](write.md)(fp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html))<br>fun [write](write.md)(nArr: [FloatArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float-array/index.html))<br>fun [write](write.md)(n: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))<br>fun [write](write.md)(nArr: [IntArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int-array/index.html))<br>fun [write](write.md)(n: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))<br>fun [write](write.md)(nArr: [LongArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long-array/index.html))<br>fun [write](write.md)(n: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html))<br>fun [write](write.md)(nArr: [ShortArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short-array/index.html))<br>fun [write](write.md)(s: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |
