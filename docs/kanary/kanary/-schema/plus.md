//[kanary](../../../index.md)/[kanary](../index.md)/[Schema](index.md)/[plus](plus.md)

# plus

[jvm]\
operator fun [plus](plus.md)(other: [Schema](index.md)): [Schema](index.md)

Returns a new schema containing the protocols of both. Should be used if the union is used only once. If used more than once, a new [schema](../schema.md) should be defined with both [added](../-schema-builder/plus-assign.md) to it.

#### Throws

| | |
|---|---|
| [ReassignmentException](../-reassignment-exception/index.md) | there exist conflicting declarations of a given protocol |
