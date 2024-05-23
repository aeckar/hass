//[kanary](../../../index.md)/[kanary](../index.md)/[Schema](index.md)

# Schema

[jvm]\
class [Schema](index.md)

Defines a set of protocols corresponding to how certain types should be written to and read from binary.

The following types have pre-defined protocols:

|  |  |  |
|---|---|---|
| BooleanArray | DoubleArray | Map.Entry |
| ByteArray | String | Map |
| CharArray | Array | Unit |
| ShortArray | List | (lambda) |
| IntArray | Iterable | (null) |
| LongArray | Pair |  |
| FloatArray | Triple |  |

Any built-in read operation designated to an open or abstract type is given the '[fallback](../-protocol-builder/fallback.md)' modifier.

## Functions

| Name | Summary |
|---|---|
| [plus](plus.md) | [jvm]<br>operator fun [plus](plus.md)(other: [Schema](index.md)): [Schema](index.md)<br>Returns a new schema containing the protocols of both. Should be used if the union is used only once. If used more than once, a new [schema](../schema.md) should be defined with both [added](../-schema-builder/plus-assign.md) to it. |
