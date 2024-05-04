//[kanary](../../index.md)/[kanary](index.md)/[protocolOf](protocol-of.md)

# protocolOf

[jvm]\
inline fun &lt;[T](protocol-of.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [protocolOf](protocol-of.md)(builder: [ProtocolBuilderScope](-protocol-builder-scope/index.md)&lt;[T](protocol-of.md)&gt;.() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [ProtocolSpecification](-protocol-specification/index.md)&lt;[T](protocol-of.md)&gt;

Provides a scope wherein a the binary [read](-protocol-builder-scope/read.md) and [write](-protocol-builder-scope/write.md) operations of a top-level class can be defined.

#### Throws

| | |
|---|---|
| [MissingProtocolException](-missing-protocol-exception/index.md) | [T](protocol-of.md) is not a top-level class |
| [ReassignmentException](-reassignment-exception/index.md) | either of the operations are defined twice, or this is called more than once for type [T](protocol-of.md) |
