//[kanary](../../../index.md)/[kanary](../index.md)/[ProtocolSpecification](index.md)

# ProtocolSpecification

[jvm]\
class [ProtocolSpecification](index.md)&lt;[T](index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;(className: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), onRead: ReadOperation&lt;out [T](index.md)&gt;, onWrite: WriteOperation&lt;in [T](index.md)&gt;)

Specified a Protocol without assigning it to its class.

## Constructors

| | |
|---|---|
| [ProtocolSpecification](-protocol-specification.md) | [jvm]<br>constructor(className: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), onRead: ReadOperation&lt;out [T](index.md)&gt;, onWrite: WriteOperation&lt;in [T](index.md)&gt;) |

## Functions

| Name | Summary |
|---|---|
| [assign](assign.md) | [jvm]<br>fun [assign](assign.md)()<br>Assigns the specified protocol to its class. If it is to be used within a program, this function should be invoked **once** from within the `init` block of the companion of the class the protocol is specific to. |
