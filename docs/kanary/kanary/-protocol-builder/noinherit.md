//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[noinherit](noinherit.md)

# noinherit

[jvm]\
fun [noinherit](noinherit.md)(read: [SimpleReadOperation](../-simple-read-operation/index.md)&lt;[T](index.md)&gt;): [SimpleReadOperation](../-simple-read-operation/index.md)&lt;[T](index.md)&gt;

When prepended to a [read operation](noinherit.md), declares that:

- 
   Supertype packets are not accessed during the write operation
- 
   Version resolution through [exhaustion testing](../-exhaustible-deserializer/index.md) is not required

If used, the [write operation](write.md) of the same protocol must have the hasStatic modifier. Additionally, subtypes of this type may not define a protocol within the same schema. Enables certain optimizations.

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | this function is called more than once in a single scope |
