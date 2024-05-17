//[kanary](../../index.md)/[kanary](index.md)/[schema](schema.md)

# schema

[jvm]\
inline fun [schema](schema.md)(builder: [SchemaBuilder](-schema-builder/index.md).() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Schema](-schema/index.md)

Provides a scope wherein protocols for various classes may be defined. It is acceptable, but not encouraged, to create an empty set. Doing so would provide object read/write functionality to the serializer/deserializer, which will always fail when invoked. This is because objects require their type to have a defined protocol before they can be manipulated.

#### Return

a serialization schema, which can be passed to a [serializer](serializer.md) or [deserializer](deserializer.md) to provide the directions for serializing the specified reference types
