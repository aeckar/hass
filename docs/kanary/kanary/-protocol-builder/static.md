//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[static](static.md)

# static

[jvm]\
fun [static](static.md)(write: [TypedWriteOperation](../-typed-write-operation/index.md)&lt;[T](index.md)&gt;): [TypedWriteOperation](../-typed-write-operation/index.md)&lt;[T](index.md)&gt;

When prepended to a [write operation](static.md), declares that the only information serialized from an instance of [T](index.md) is that which is specifically written here. If used, subtypes of this type may not define a protocol with a write operation. Enables certain optimizations.

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | this function is called more than once in a single scope |
