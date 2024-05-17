//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[write](write.md)

# write

[jvm]\
fun [write](write.md)()

Signals that serialization should be handled individually by each instance of [T](index.md). Necessary for serializing private members. If a default protocol is not already defined for the types of these members, one must be defined.

[jvm]\
var [write](write.md): [WriteOperation](../-write-operation/index.md)&lt;[T](index.md)&gt;?

The binary write operation called when [OutputSerializer.write](../-output-serializer/write.md) is called with an object of class [T](index.md) If not declared, then a no-op default write operation is used.

#### Throws

| | |
|---|---|
| [ReassignmentException](../-reassignment-exception/index.md) | this is assigned to more than once in a single scope |
