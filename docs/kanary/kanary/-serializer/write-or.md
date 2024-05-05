//[kanary](../../../index.md)/[kanary](../index.md)/[Serializer](index.md)/[writeOr](write-or.md)

# writeOr

[jvm]\
fun [writeOr](write-or.md)(nullable: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?)

Writes the object in binary format according to the protocol of its type, or null. If the object is not null and its type does not have a defined protocol, the protocol of its superclass or the first interface declared in source code with a protocol is chosen.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | if [nullable](write-or.md) is not null, and its type is not a top-level class or does not have a defined protocol |
