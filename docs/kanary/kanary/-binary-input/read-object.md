//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)/[readObject](read-object.md)

# readObject

[jvm]\
inline fun &lt;[T](read-object.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readObject](read-object.md)(): [T](read-object.md)

Reads an object of the specified type from binary according to the protocol of its type.

#### Throws

| | |
|---|---|
| [TypeMismatchException](../-type-mismatch-exception/index.md) | the object was not serialized as a singular object |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | the object is not an instance of type [T](read-object.md) |
