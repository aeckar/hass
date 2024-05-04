//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readNullablesArray](read-nullables-array.md)

# readNullablesArray

[jvm]\
inline fun &lt;[T](read-nullables-array.md), [N](read-nullables-array.md) : [T](read-nullables-array.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullablesArray](read-nullables-array.md)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](read-nullables-array.md)?&gt;

Reads an object array from binary with each member deserialized according to its protocol, or null respectively.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as an object array |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | a member is not null or an instance of type [T](read-nullables-array.md) |
