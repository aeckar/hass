//[kanary](../../index.md)/[kanary](index.md)/[deserializer](deserializer.md)

# deserializer

[jvm]\
fun [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html).[deserializer](deserializer.md)(protocols: [Schema](-schema/index.md)): [InputDeserializer](-input-deserializer/index.md)

See [Schema](-schema/index.md) for a list of types that can be deserialized by default.

#### Return

a new deserializer capable of reading primitives, primitive arrays, strings, and instances of any type with a defined protocol from Kanary format
