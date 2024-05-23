//[kanary](../../index.md)/[kanary](index.md)/[serializer](serializer.md)

# serializer

[jvm]\
fun [OutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html).[serializer](serializer.md)(protocols: [Schema](-schema/index.md)): [OutputSerializer](-output-serializer/index.md)

See [Schema](-schema/index.md) for a list of types that can be serialized by default.

#### Return

a new serializer capable of writing primitives, primitive arrays, and instances of any type with a defined protocol to Kanary format
