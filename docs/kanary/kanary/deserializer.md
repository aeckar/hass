//[kanary](../../index.md)/[kanary](index.md)/[deserializer](deserializer.md)

# deserializer

[jvm]\
fun [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html).[deserializer](deserializer.md)(): [PrimitiveDeserializer](-primitive-deserializer/index.md)

#### Return

a new deserializer capable of reading primitives, primitive arrays, and strings from Kanary format

[jvm]\
fun [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html).[deserializer](deserializer.md)(protocols: [ProtocolSet](-protocol-set/index.md)): [Deserializer](-deserializer/index.md)

#### Return

a new deserializer capable of reading primitives, primitive arrays, strings, and instances of any type with a defined protocol from Kanary format
