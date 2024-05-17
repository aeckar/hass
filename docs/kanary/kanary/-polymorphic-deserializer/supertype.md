//[kanary](../../../index.md)/[kanary](../index.md)/[PolymorphicDeserializer](index.md)/[supertype](supertype.md)

# supertype

[jvm]\
inline fun &lt;[T](supertype.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [supertype](supertype.md)(): [ExhaustibleDeserializer](../-exhaustible-deserializer/index.md)

#### Return

a *packet* deserializer corresponding to the data serialized by given supertype. If the supertype does not have a defined write operation, returns a deserializer containing no data.

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | [T](supertype.md) is not a supertype |
