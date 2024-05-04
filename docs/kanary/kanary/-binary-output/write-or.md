//[kanary](../../../index.md)/[kanary](../index.md)/[BinaryOutput](index.md)/[writeOr](write-or.md)

# writeOr

[jvm]\
fun [writeOr](write-or.md)(nullable: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?)

Writes the object in binary format according to the protocol of its type, or null.

#### Throws

| | |
|---|---|
| [MissingProtocolException](../-missing-protocol-exception/index.md) | if [nullable](write-or.md) is not null, and its type is not a top-level class or does not have a defined protocol |
