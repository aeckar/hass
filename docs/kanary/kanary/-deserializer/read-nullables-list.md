//[kanary](../../../index.md)/[kanary](../index.md)/[Deserializer](index.md)/[readNullablesList](read-nullables-list.md)

# readNullablesList

[jvm]\
inline fun &lt;[T](read-nullables-list.md), [N](read-nullables-list.md) : [T](read-nullables-list.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullablesList](read-nullables-list.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-nullables-list.md)?&gt;

Reads a list from binary with each member deserialized according to its protocol, or null respectively.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as a list |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | a member is not null or an instance of type [T](read-nullables-list.md) |
