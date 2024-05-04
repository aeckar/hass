//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryOutput](index.md)/[writeAllOr](write-all-or.md)

# writeAllOr

[jvm]\
inline fun &lt;[T](write-all-or.md), [N](write-all-or.md) : [T](write-all-or.md) &amp; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [writeAllOr](write-all-or.md)(nullablesArr: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](write-all-or.md)&gt;)

Writes all members in array according to the protocol of each instance.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | the type of any member of [nullablesArr](write-all-or.md) is not null, and is not a top-level class or does not have a defined protocol |

[jvm]\
inline fun &lt;[T](write-all-or.md)&gt; [writeAllOr](write-all-or.md)(nullablesList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](write-all-or.md)&gt;)

Writes all members in the list according the protocol of each.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | any member of [nullablesList](write-all-or.md) is not null, and its type is not top-level class or does not have a defined protocol |

[jvm]\
inline fun &lt;[T](write-all-or.md)&gt; [writeAllOr](write-all-or.md)(nullablesIter: [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)&lt;[T](write-all-or.md)&gt;)

Writes all members in the iterable object according the protocol of each instance as a list. The caller must ensure that the object has a finite number of members.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | any member of [nullablesIter](write-all-or.md) is not null, and its type is not top-level class or does not have a defined protocol |
