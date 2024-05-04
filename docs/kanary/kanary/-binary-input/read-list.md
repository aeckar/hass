//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readList](read-list.md)

# readList

[jvm]\
inline fun &lt;[T](read-list.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readList](read-list.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-list.md)&gt;

Reads a [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html) from binary with each member deserialized according to its protocol.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as a list |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | a member is not an instance of type [T](read-list.md) |
