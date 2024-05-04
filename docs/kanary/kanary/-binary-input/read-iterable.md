//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readIterable](read-iterable.md)

# readIterable

[jvm]\
inline fun &lt;[T](read-iterable.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readIterable](read-iterable.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-iterable.md)&gt;

Reads an [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html) from binary with each member deserialized according to its protocol. Although [readList](read-list.md) is more efficient, this function may also parse lists.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as an iterable or list |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | a member is not an instance of type [T](read-iterable.md) |
