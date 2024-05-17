//[kanary](../../../index.md)/[kanary](../index.md)/[Serializer](index.md)

# Serializer

sealed interface [Serializer](index.md)

#### Inheritors

| |
|---|
| [OutputSerializer](../-output-serializer/index.md) |

## Functions

| Name | Summary |
|---|---|
| [write](write.md) | [jvm]<br>abstract fun [write](write.md)(obj: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?)<br>abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(array: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](write.md)&gt;)<br>abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(pair: [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[T](write.md), [T](write.md)&gt;)<br>abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(triple: [Triple](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)&lt;[T](write.md), [T](write.md), [T](write.md)&gt;)<br>abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(iter: [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)&lt;[T](write.md)&gt;)<br>abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(list: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](write.md)&gt;)<br>abstract fun &lt;[K](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [V](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(entry: [Map.Entry](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/-entry/index.html)&lt;[K](write.md), [V](write.md)&gt;)<br>abstract fun &lt;[K](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [V](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(map: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[K](write.md), [V](write.md)&gt;) |
| [write](../write.md) | [jvm]<br>fun [Serializer](index.md).[write](../write.md)(vararg objs: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?) |
| [writeBoolean](write-boolean.md) | [jvm]<br>abstract fun [writeBoolean](write-boolean.md)(cond: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |
| [writeByte](write-byte.md) | [jvm]<br>abstract fun [writeByte](write-byte.md)(b: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html)) |
| [writeChar](write-char.md) | [jvm]<br>abstract fun [writeChar](write-char.md)(c: [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)) |
| [writeDouble](write-double.md) | [jvm]<br>abstract fun [writeDouble](write-double.md)(fp: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)) |
| [writeFloat](write-float.md) | [jvm]<br>abstract fun [writeFloat](write-float.md)(fp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)) |
| [writeInt](write-int.md) | [jvm]<br>abstract fun [writeInt](write-int.md)(n: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |
| [writeLong](write-long.md) | [jvm]<br>abstract fun [writeLong](write-long.md)(n: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)) |
| [writeShort](write-short.md) | [jvm]<br>abstract fun [writeShort](write-short.md)(n: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)) |
