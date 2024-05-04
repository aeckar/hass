//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readArray](read-array.md)

# readArray

[jvm]\
inline fun &lt;[T](read-array.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readArray](read-array.md)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](read-array.md)&gt;

Reads an object array with each member deserialized according to its protocol.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as an object array |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | a member is not null or an instance of type [T](read-array.md) |
