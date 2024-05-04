//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolBuilderScope](index.md)

# ProtocolBuilderScope

[jvm]\
class [ProtocolBuilderScope](index.md)&lt;[T](index.md)&gt;

The scope wherein a protocol's [read](read.md) and [write](write.md) operations are defined.

## Constructors

| | |
|---|---|
| [ProtocolBuilderScope](-protocol-builder-scope.md) | [jvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [read](read.md) | [jvm]<br>var [read](read.md): [BinaryInput](../-binary-input/index.md).() -&gt; [T](index.md)<br>The binary read operation when [BinaryInput.readObject](../-binary-input/read-object.md) is called with an object of class [T](index.md). |
| [write](write.md) | [jvm]<br>var [write](write.md): [BinaryOutput](../-binary-output/index.md).([T](index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>The binary write operation when [BinaryOutput.writeOr](../-binary-output/write-or.md) is called with an object of class [T](index.md) |
