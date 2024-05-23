//[kanary](../../../index.md)/[kanary](../index.md)/[Deserializer](index.md)/[readShort](read-short.md)

# readShort

[jvm]\
abstract fun [readShort](read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)

Capable of reading the corresponding boxed type as well.

#### Return

the serialized value, unboxed

#### Throws

| | |
|---|---|
| [TypeFlagMismatchException](../-type-flag-mismatch-exception/index.md) | an object of a different type was serialized in the current stream position |
| [EOFException](https://docs.oracle.com/javase/8/docs/api/java/io/EOFException.html) | the stream is exhausted before a value can be determined |
