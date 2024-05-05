//[kanary](../../index.md)/[kanary](index.md)/[protocolSet](protocol-set.md)

# protocolSet

[jvm]\
inline fun [protocolSet](protocol-set.md)(builder: [ProtocolSetBuilderScope](-protocol-set-builder-scope/index.md).() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [ProtocolSet](-protocol-set/index.md)

Provides a scope wherein protocols for various classes may be defined. It is acceptable, but not encouraged, to create an empty set. Doing so would provide object read/write functionality to the serializer/deserializer, which will always fail when invoked. This is because objects require their type to have a defined protocol before they can be manipulated.

#### Return

a protocol set, which can be passed to a [serializer](serializer.md) or [deserializer](deserializer.md) to provide reference type serialization functionality
