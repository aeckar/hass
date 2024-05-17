//[kanary](../../../index.md)/[kanary](../index.md)/[OutputSerializer](index.md)/[write](write.md)

# write

[jvm]\
open override fun [write](write.md)(obj: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?)

Writes the object in binary format according to the protocol of its type, or null. If the object is not null and its type does not have a defined protocol, the protocol of its superclass or the first interface declared in source code with a protocol is chosen.

[jvm]\
open override fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(array: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](write.md)&gt;)

Writes all members in array according to the protocol of each instance. Avoids null check for members, unlike generic `write`. Arrays of primitive types should be passed to the generic overload.

[jvm]\
open override fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(list: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](write.md)&gt;)

Writes all members in the list according the protocol of each. Avoids null check for members, unlike generic `write`.

[jvm]\
open override fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(iter: [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)&lt;[T](write.md)&gt;)

Writes all members in the iterable object according the protocol of each as a list. The caller must ensure that the object has a finite number of members. Avoids null check for members, unlike generic `write`.

[jvm]\
open override fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(pair: [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[T](write.md), [T](write.md)&gt;)

Writes the given pair according to the protocols of its members. Avoids null check for members, unlike generic `write`.

[jvm]\
open override fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(triple: [Triple](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)&lt;[T](write.md), [T](write.md), [T](write.md)&gt;)

Writes the given triple according to the protocols of its members. Avoids null check for members, unlike generic `write`.

[jvm]\
open override fun &lt;[K](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [V](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(entry: [Map.Entry](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/-entry/index.html)&lt;[K](write.md), [V](write.md)&gt;)

Writes the given map entry according to the protocols of its key and value. Avoids null check for members, unlike generic `write`.

[jvm]\
open override fun &lt;[K](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [V](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(map: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[K](write.md), [V](write.md)&gt;)

Writes the given map according to the protocols of its keys and values. Avoids null check for entries, unlike generic `write`.
