//[kanary](../../../index.md)/[kanary](../index.md)/[SchemaBuilder](index.md)/[define](define.md)

# define

[jvm]\
inline fun &lt;[T](define.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [define](define.md)(builder: [ProtocolBuilder](../-protocol-builder/index.md)&lt;[T](define.md)&gt;.() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Provides a scope wherein the [read](../-protocol-builder/read.md) and [write](../-protocol-builder/write.md) operations of a type can be defined.

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | [T](define.md) is not a top-level class or has already been defined a protocol |
| [ReassignmentException](../-reassignment-exception/index.md) | either of the operations are defined twice, or this is called more than once for type [T](define.md) |
