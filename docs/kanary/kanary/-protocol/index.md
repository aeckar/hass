//[kanary](../../../index.md)/[kanary](../index.md)/[Protocol](index.md)

# Protocol

[jvm]\
interface [Protocol](index.md)

A locally defined binary I/O protocol.

Delegates the protocol of the type whose companion implements this interface to the locally [defined](../define.md) protocol by which this interface is delegated to. Doing so enables serialization using private members.

It is possible for a local protocol to define one operation and a protocol of the same type defined in a schema to define another operation. If an operation is defined in both, a [MalformedProtocolException](../-malformed-protocol-exception/index.md) is thrown.

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [jvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [hasFallback](has-fallback.md) | [jvm]<br>abstract val [hasFallback](has-fallback.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [hasStatic](has-static.md) | [jvm]<br>abstract val [hasStatic](has-static.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [read](read.md) | [jvm]<br>abstract val [read](read.md): ReadOperation? |
| [write](write.md) | [jvm]<br>abstract val [write](write.md): WriteOperation? |
