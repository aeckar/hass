//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryInput](index.md)

# BinaryInput

[jvm]\
@[JvmInline](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-inline/index.html)

value class [BinaryInput](index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html)

A binary stream with functions for reading primitives or classes with a [protocolOf](../protocol-of.md) in Kanary format. Does not support marking. Calling [close](close.md) also closes the underlying stream. This object does not need to be closed so long as the underlying stream is closed.

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [jvm]<br>open override fun [close](close.md)() |
| [readArray](read-array.md) | [jvm]<br>inline fun &lt;[T](read-array.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readArray](read-array.md)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](read-array.md)&gt;<br>Reads an object array with each member deserialized according to its protocol. |
| [readBoolean](read-boolean.md) | [jvm]<br>fun [readBoolean](read-boolean.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [readBooleanArray](read-boolean-array.md) | [jvm]<br>fun [readBooleanArray](read-boolean-array.md)(): [BooleanArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean-array/index.html) |
| [readByte](read-byte.md) | [jvm]<br>fun [readByte](read-byte.md)(): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html) |
| [readChar](read-char.md) | [jvm]<br>fun [readChar](read-char.md)(): [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html) |
| [readCharArray](read-char-array.md) | [jvm]<br>fun [readCharArray](read-char-array.md)(): [CharArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char-array/index.html) |
| [readDouble](read-double.md) | [jvm]<br>fun [readDouble](read-double.md)(): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [readDoubleArray](read-double-array.md) | [jvm]<br>fun [readDoubleArray](read-double-array.md)(): [DoubleArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double-array/index.html) |
| [readFloat](read-float.md) | [jvm]<br>fun [readFloat](read-float.md)(): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [readFloatArray](read-float-array.md) | [jvm]<br>fun [readFloatArray](read-float-array.md)(): [FloatArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float-array/index.html) |
| [readInt](read-int.md) | [jvm]<br>fun [readInt](read-int.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [readIntArray](read-int-array.md) | [jvm]<br>fun [readIntArray](read-int-array.md)(): [IntArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int-array/index.html) |
| [readIterable](read-iterable.md) | [jvm]<br>inline fun &lt;[T](read-iterable.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readIterable](read-iterable.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-iterable.md)&gt;<br>Reads an [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html) from binary with each member deserialized according to its protocol. Although [readList](read-list.md) is more efficient, this function may also parse lists. |
| [readList](read-list.md) | [jvm]<br>inline fun &lt;[T](read-list.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readList](read-list.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-list.md)&gt;<br>Reads a [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html) from binary with each member deserialized according to its protocol. |
| [readLong](read-long.md) | [jvm]<br>fun [readLong](read-long.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [readLongArray](read-long-array.md) | [jvm]<br>fun [readLongArray](read-long-array.md)(): [LongArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long-array/index.html) |
| [readNullable](read-nullable.md) | [jvm]<br>inline fun &lt;[T](read-nullable.md), [N](read-nullable.md) : [T](read-nullable.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullable](read-nullable.md)(): [T](read-nullable.md)?<br>Reads an object of the specified type from binary according to the protocol of its type, or null respectively. |
| [readNullablesArray](read-nullables-array.md) | [jvm]<br>inline fun &lt;[T](read-nullables-array.md), [N](read-nullables-array.md) : [T](read-nullables-array.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullablesArray](read-nullables-array.md)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](read-nullables-array.md)?&gt;<br>Reads an object array from binary with each member deserialized according to its protocol, or null respectively. |
| [readNullablesIterable](read-nullables-iterable.md) | [jvm]<br>inline fun &lt;[T](read-nullables-iterable.md), [N](read-nullables-iterable.md) : [T](read-nullables-iterable.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullablesIterable](read-nullables-iterable.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-nullables-iterable.md)?&gt;<br>Reads an [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html) from binary with each member deserialized according to its protocol, or null respectively. Although [readNullablesList](read-nullables-list.md) is more efficient, this function may also parse lists. |
| [readNullablesList](read-nullables-list.md) | [jvm]<br>inline fun &lt;[T](read-nullables-list.md), [N](read-nullables-list.md) : [T](read-nullables-list.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readNullablesList](read-nullables-list.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](read-nullables-list.md)?&gt;<br>Reads a list from binary with each member deserialized according to its protocol, or null respectively. |
| [readObject](read-object.md) | [jvm]<br>inline fun &lt;[T](read-object.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [readObject](read-object.md)(): [T](read-object.md)<br>Reads an object of the specified type from binary according to the protocol of its type. |
| [readShort](read-short.md) | [jvm]<br>fun [readShort](read-short.md)(): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html) |
| [readShortArray](read-short-array.md) | [jvm]<br>fun [readShortArray](read-short-array.md)(): [ShortArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short-array/index.html) |
| [readString](read-string.md) | [jvm]<br>fun [readString](read-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
