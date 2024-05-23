//[kanary](../../../index.md)/[kanary](../index.md)/[InputDeserializer](index.md)/[readBoolean](read-boolean.md)

# readBoolean

[jvm]\
open override fun [readBoolean](read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Capable of reading the corresponding boxed type as well.

#### Return

the serialized value, unboxed

#### Throws

| | |
|---|---|
| [TypeFlagMismatchException](../-type-flag-mismatch-exception/index.md) | an object of a different type was serialized in the current stream position |
| [EOFException](https://docs.oracle.com/javase/8/docs/api/java/io/EOFException.html) | the stream is exhausted before a value can be determined |
