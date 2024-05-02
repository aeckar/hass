//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryOutput](index.md)/[write](write.md)

# write

[jvm]\
fun [write](write.md)(cond: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))

fun [write](write.md)(b: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html))

fun [write](write.md)(c: [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html))

fun [write](write.md)(n: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html))

fun [write](write.md)(n: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

fun [write](write.md)(n: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))

fun [write](write.md)(fp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html))

fun [write](write.md)(fp: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html))

fun [write](write.md)(s: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

[jvm]\
fun [write](write.md)(obj: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html))

Writes the object in binary format according to the protocol of its type.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | the type of [obj](write.md) is not a top-level class or does not have a defined protocol |
