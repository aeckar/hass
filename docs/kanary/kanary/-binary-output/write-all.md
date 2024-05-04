//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryOutput](index.md)/[writeAll](write-all.md)

# writeAll

[jvm]\
inline fun &lt;[T](write-all.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [writeAll](write-all.md)(objArr: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](write-all.md)&gt;)

Writes all members in array according to the protocol of each instance.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | the type of any member of [objArr](write-all.md) is not a top-level class or does not have a defined protocol |

[jvm]\
inline fun &lt;[T](write-all.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [writeAll](write-all.md)(list: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](write-all.md)&gt;)

Writes all members in the list according the protocol of each.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | any member of [list](write-all.md) is not a top-level class or does not have a defined protocol |

[jvm]\
inline fun &lt;[T](write-all.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [writeAll](write-all.md)(iter: [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)&lt;[T](write-all.md)&gt;)

Writes all members in the iterable object according the protocol of each as a list. The caller must ensure that the object has a finite number of members.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | any member of [iter](write-all.md) is not a top-level class or does not have a defined protocol |
