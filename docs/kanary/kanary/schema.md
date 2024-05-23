//[kanary](../../index.md)/[kanary](index.md)/[schema](schema.md)

# schema

[jvm]\
inline fun [schema](schema.md)(builder: [SchemaBuilder](-schema-builder/index.md).() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Schema](-schema/index.md)

Provides a scope wherein protocols for various classes may be defined. A schema with no protocols defined is legal, and should be stored as a variable if used more than once.

#### Return

a serialization schema, which can be passed to a [serializer](serializer.md) or [deserializer](deserializer.md) to provide the directions for serializing the specified reference types
