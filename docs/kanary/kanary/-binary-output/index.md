//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryOutput](index.md)

# BinaryOutput

[jvm]\
@[JvmInline](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-inline/index.html)

value class [BinaryOutput](index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html), [Flushable](https://docs.oracle.com/javase/8/docs/api/java/io/Flushable.html)

A binary stream with functions for writing primitives or classes with a [protocolOf](../protocol-of.md) in Kanary format. Does not support marking. Calling [close](close.md) also closes the underlying stream.

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [jvm]<br>open override fun [close](close.md)() |
| [flush](flush.md) | [jvm]<br>open override fun [flush](flush.md)() |
| [write](write.md) | [jvm]<br>fun [write](write.md)(obj: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html))<br>Writes the object in binary format according to the protocol of its type.<br>[jvm]<br>fun [write](write.md)(cond: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>fun [write](write.md)(b: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html))<br>fun [write](write.md)(c: [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html))<br>fun [write](write.md)(fp: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html))<br>fun [write](write.md)(fp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html))<br>fun [write](write.md)(n: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))<br>fun [write](write.md)(n: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))<br>fun [write](write.md)(n: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html))<br>fun [write](write.md)(s: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |
