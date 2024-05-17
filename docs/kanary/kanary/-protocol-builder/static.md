//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[static](static.md)

# static

[jvm]\
fun [static](static.md)(write: [WriteOperation](../-write-operation/index.md)&lt;[T](index.md)&gt;): [WriteOperation](../-write-operation/index.md)&lt;[T](index.md)&gt;

When prepended to a [write operation](static.md), declares that the only information serialized from an instance of [T](index.md) is that which is specifically written here. If used, subtypes of this type may not define a protocol with a write operation. Enables certain optimizations.

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | this function is called more than once in a single scope |

[jvm]\
fun [static](static.md)(): [Serializer](../-serializer/index.md).([T](index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

When assigned to [write](write.md), signals that serialization should be handled individually by each instance of [T](index.md), without also serializing information held by each superclass. Necessary for serializing private members. If a default protocol is not already defined for the types of these members, one must be defined.
