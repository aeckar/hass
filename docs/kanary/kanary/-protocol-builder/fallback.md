//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[fallback](fallback.md)

# fallback

[jvm]\
fun [fallback](fallback.md)(read: [ReadOperation](../-read-operation/index.md)&lt;[T](index.md)&gt;): [ReadOperation](../-read-operation/index.md)&lt;[T](index.md)&gt;

When prepended to a [read operation](fallback.md), declares that subtypes without a read operation can still be instantiated as an instance of [T](index.md) using this read operation. Generally, this should be used for types whose subtypes have the same public API. Any information not deserialized as a result is lost.

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | [T](index.md) is a final class, or called more than once in a single scope |
