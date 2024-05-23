//[kanary](../../../index.md)/[kanary](../index.md)/[Deserializer](index.md)/[readFloat](read-float.md)

# readFloat

[jvm]\
abstract fun [readFloat](read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)

Capable of reading the corresponding boxed type as well.

#### Return

the serialized value, unboxed

#### Throws

| | |
|---|---|
| [TypeFlagMismatchException](../-type-flag-mismatch-exception/index.md) | an object of a different type was serialized in the current stream position |
| [EOFException](https://docs.oracle.com/javase/8/docs/api/java/io/EOFException.html) | the stream is exhausted before a value can be determined |
