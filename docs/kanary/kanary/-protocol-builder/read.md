//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilder](index.md)/[read](read.md)

# read

[jvm]\
var [read](read.md): [ReadOperation](../-read-operation/index.md)&lt;[T](index.md)&gt;?

The binary read operation called when ExhaustibleDeserializer.read is called with an object of class [T](index.md). Information deserialized from supertypes is converted into a packet, from which the read operation can use the information to create a new instance of [T](index.md).

#### Throws

| | |
|---|---|
| [MalformedProtocolException](../-malformed-protocol-exception/index.md) | [T](index.md) is an abstract class or interface |
| [ReassignmentException](../-reassignment-exception/index.md) | this is assigned to more than once in a single scope |
