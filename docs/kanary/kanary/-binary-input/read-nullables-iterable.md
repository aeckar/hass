//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readNullablesIterable](read-nullables-iterable.md)

# readNullablesIterable

[jvm]\
inline fun &lt;[T](read-nullables-iterable.md), [N](read-nullables-iterable.md) : [T](read-nullables-iterable.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullablesIterable](read-nullables-iterable.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-nullables-iterable.md)?&gt;

Reads an [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html) from binary with each member deserialized according to its protocol, or null respectively. Although [readNullablesList](read-nullables-list.md) is more efficient, this function may also parse lists.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as an iterable or list |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | a member is not null or an instance of type [T](read-nullables-iterable.md) |
