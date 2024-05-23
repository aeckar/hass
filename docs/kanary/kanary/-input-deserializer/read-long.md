//[kanary](../../../index.md)/[kanary](../index.md)/[InputDeserializer](index.md)/[readLong](read-long.md)

# readLong

[jvm]\
open override fun [readLong](read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)

Capable of reading the corresponding boxed type as well.

#### Return

the serialized value, unboxed

#### Throws

| | |
|---|---|
| [TypeFlagMismatchException](../-type-flag-mismatch-exception/index.md) | an object of a different type was serialized in the current stream position |
| [EOFException](https://docs.oracle.com/javase/8/docs/api/java/io/EOFException.html) | the stream is exhausted before a value can be determined |
