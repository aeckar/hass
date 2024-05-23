//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[write](write.md)

# write

[jvm]\
var [write](write.md): [TypedWriteOperation](../-typed-write-operation/index.md)&lt;[T](index.md)&gt;?

The binary write operation called when [Serializer.write](../-serializer/write.md) is called with an object of class [T](index.md) If not declared, then a no-op default write operation is used.

#### Throws

| | |
|---|---|
| [ReassignmentException](../-reassignment-exception/index.md) | this is assigned to more than once in a single scope |
