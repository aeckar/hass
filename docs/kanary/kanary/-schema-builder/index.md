//[kanary](../../../index.md)/[kanary](../index.md)/[SchemaBuilder](index.md)

# SchemaBuilder

[jvm]\
class [SchemaBuilder](index.md)

The scope wherein binary I/O protocols may be [defined](define.md).

## Functions

| Name | Summary |
|---|---|
| [define](define.md) | [jvm]<br>inline fun &lt;[T](define.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [define](define.md)(builder: [ProtocolBuilder](../-protocol-builder/index.md)&lt;[T](define.md)&gt;.() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Provides a scope wherein the [read](../-protocol-builder/read.md) and [write](../-protocol-builder/write.md) operations of a type can be defined. |
| [plusAssign](plus-assign.md) | [jvm]<br>operator fun [plusAssign](plus-assign.md)(other: [Schema](../-schema/index.md))<br>Adds all protocols from the given schema to this one. If the union of two schemas is used only sparingly, [Schema.plus](../-schema/plus.md) should be used instead. |
