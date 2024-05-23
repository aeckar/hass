//[kanary](../../../index.md)/[kanary](../index.md)/[Deserializer](index.md)/[read](read.md)

# read

[jvm]\
abstract fun &lt;[T](read.md)&gt; [read](read.md)(): [T](read.md)

If [T](read.md) is a primitive type, is capable of reading a primitive value. Can be null.

#### Return

the serialized object of the given type

#### Throws

| | |
|---|---|
| [TypeFlagMismatchException](../-type-flag-mismatch-exception/index.md) | the value was not serialized as a singular object or null |
| [TypeCastException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-type-cast-exception/index.html) | the object is not an instance of type [T](read.md) |
| [EOFException](https://docs.oracle.com/javase/8/docs/api/java/io/EOFException.html) | the stream is exhausted before a value can be determined |
