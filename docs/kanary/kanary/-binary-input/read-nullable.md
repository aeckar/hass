//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readNullable](read-nullable.md)

# readNullable

[jvm]\
inline fun &lt;[T](read-nullable.md), [N](read-nullable.md) : [T](read-nullable.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullable](read-nullable.md)(): [T](read-nullable.md)?

Reads an object of the specified type from binary according to the protocol of its type, or null respectively.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the value was not serialized as a singular object or null |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | the object is not an instance of type [T](read-nullable.md) |
