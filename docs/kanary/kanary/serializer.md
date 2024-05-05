//[kanary](../../index.md)/[kanary](index.md)/[serializer](serializer.md)

# serializer

[jvm]\
fun [OutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html).[serializer](serializer.md)(): [PrimitiveSerializer](-primitive-serializer/index.md)

#### Return

a new serializer capable of writing primitives, primitive arrays, and strings to Kanary format

[jvm]\
fun [OutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html).[serializer](serializer.md)(protocols: [ProtocolSet](-protocol-set/index.md)): [Serializer](-serializer/index.md)

#### Return

a new serializer capable of writing primitives, primitive arrays, strings and instances of any ype with a defined protocol to Kanary format
