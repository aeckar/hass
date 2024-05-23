//[kanary](../../../index.md)/[kanary](../index.md)/[Serializer](index.md)/[write](write.md)

# write

[jvm]\
abstract fun [write](write.md)(obj: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?)

Serializes the object or boxed primitive value.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | obj is not an instance of a top-level or nested class, or a suitable write operation cannot be determined |

[jvm]\
abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(array: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;out [T](write.md)&gt;)

Serializes the array without checking for null elements.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | any element is not an instance of a top-level or nested class, or a suitable write operation for it cannot be determined |

[jvm]\
abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(list: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](write.md)&gt;)

Serializes the list without checking for null elements.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | any element is not an instance of a top-level or nested class, or a suitable write operation for it cannot be determined |

[jvm]\
abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(iter: [Iterable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)&lt;[T](write.md)&gt;)

Serializes the iterable without checking for null elements.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | any element is not an instance of a top-level or nested class, or a suitable write operation for it cannot be determined |

[jvm]\
abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(pair: [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[T](write.md), [T](write.md)&gt;)

Serializes the pair without checking for null elements.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | any element is not an instance of a top-level or nested class, or a suitable write operation for it cannot be determined |

[jvm]\
abstract fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(triple: [Triple](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)&lt;[T](write.md), [T](write.md), [T](write.md)&gt;)

Serializes the triple without checking for null elements.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | any element is not an instance of a top-level or nested class, or a suitable write operation for it cannot be determined |

[jvm]\
abstract fun &lt;[K](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [V](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(entry: [Map.Entry](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/-entry/index.html)&lt;[K](write.md), [V](write.md)&gt;)

Serializes the map entry without checking for null elements.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | the key or value is not an instance of a top-level or nested class, or a suitable write operation for either cannot be determined |

[jvm]\
abstract fun &lt;[K](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [V](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(map: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[K](write.md), [V](write.md)&gt;)

Serializes the map without checking for null keys or values.

#### Throws

| | |
|---|---|
| [MissingOperationException](../-missing-operation-exception/index.md) | any key or value is not an instance of a top-level or nested class, or a suitable write operation for any cannot be determined |
