//[kanary](../../index.md)/[kanary](index.md)/[write](write.md)

# write

[jvm]\
fun [Serializer](-serializer/index.md).[write](write.md)(vararg objs: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?)

Writes the objects in binary format according to the protocol of each type. Null objects are accepted, however their non-nullable type information is erased. If an object is not null and its type does not have a defined protocol, the protocol of its superclass or the first interface declared in source code with a protocol is chosen. If no objects are supplied, nothing is serialized.

#### Throws

| | |
|---|---|
| [MissingOperationException](-missing-operation-exception/index.md) | any object of an anonymous or local class, or an appropriate write operation cannot be found |
