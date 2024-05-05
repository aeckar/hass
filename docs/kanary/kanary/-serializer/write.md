//[kanary](../../../index.md)/[kanary](../index.md)/[Serializer](index.md)/[write](write.md)

# write

[jvm]\
fun &lt;[T](write.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [write](write.md)(obj: [T](write.md))

Writes the object according to the protocol of its type. If the type of the object does not have a defined protocol, the protocol of its superclass or the first interface declared in source code with a protocol is chosen.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | the type of [obj](write.md) is not a top-level class or does not have a defined protocol |
