//[kanary](../../index.md)/[kanary](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [AssignOnce](-assign-once/index.md) | [jvm]<br>class [AssignOnce](-assign-once/index.md)&lt;[T](-assign-once/index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>Alternative to 'lateinit' modifier preventing reassignment after first assignment. |
| [BinaryInput](-binary-input/index.md) | [jvm]<br>@[JvmInline](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-inline/index.html)<br>value class [BinaryInput](-binary-input/index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html)<br>A binary stream with functions for reading primitives or classes with a [protocolOf](protocol-of.md) in Kanary format. Does not support marking. Calling [close](-binary-input/close.md) also closes the underlying stream. |
| [BinaryOutput](-binary-output/index.md) | [jvm]<br>@[JvmInline](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-inline/index.html)<br>value class [BinaryOutput](-binary-output/index.md) : [Closeable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html), [Flushable](https://docs.oracle.com/javase/8/docs/api/java/io/Flushable.html)<br>A binary stream with functions for writing primitives or classes with a [protocolOf](protocol-of.md) in Kanary format. Does not support marking. Calling [close](-binary-output/close.md) also closes the underlying stream. |
| [MissingProtocolException](-missing-protocol-exception/index.md) | [jvm]<br>class [MissingProtocolException](-missing-protocol-exception/index.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [Exception](https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html)<br>Thrown when a class is expected to have a Kanary I/O protocol, |
| [ProtocolBuilderScope](-protocol-builder-scope/index.md) | [jvm]<br>class [ProtocolBuilderScope](-protocol-builder-scope/index.md)&lt;[T](-protocol-builder-scope/index.md)&gt;<br>The scope wherein a protocol's [read](-protocol-builder-scope/read.md) and [write](-protocol-builder-scope/write.md) operations are defined. |
| [ReassignmentException](-reassignment-exception/index.md) | [jvm]<br>class [ReassignmentException](-reassignment-exception/index.md) : [Exception](https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html)<br>Thrown when there is an attempt to assign a value to a property that has already been given a value and can only be assigned a value once. |

## Functions

| Name | Summary |
|---|---|
| [binary](binary.md) | [jvm]<br>fun [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html).[binary](binary.md)(): [BinaryInput](-binary-input/index.md)<br>fun [OutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html).[binary](binary.md)(): [BinaryOutput](-binary-output/index.md) |
| [protocolOf](protocol-of.md) | [jvm]<br>inline fun &lt;[T](protocol-of.md)&gt; [protocolOf](protocol-of.md)(builder: [ProtocolBuilderScope](-protocol-builder-scope/index.md)&lt;[T](protocol-of.md)&gt;.() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Provides a scope wherein a the binary [read](-protocol-builder-scope/read.md) and [write](-protocol-builder-scope/write.md) operations of a top-level class can be defined. |
