//[kanary](../../../index.md)/[kanary](../index.md)/[SchemaBuilder](index.md)/[plusAssign](plus-assign.md)

# plusAssign

[jvm]\
operator fun [plusAssign](plus-assign.md)(other: [Schema](../-schema/index.md))

Adds all protocols from the given schema to this one. If the union of two schemas is used only sparingly, [Schema.plus](../-schema/plus.md) should be used instead.

#### Throws

| | |
|---|---|
| [ReassignmentException](../-reassignment-exception/index.md) | there exist conflicting declarations of a given protocol |
