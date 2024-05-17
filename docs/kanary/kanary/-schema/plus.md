//[kanary](../../../index.md)/[kanary](../index.md)/[Schema](index.md)/[plus](plus.md)

# plus

[jvm]\
operator fun [plus](plus.md)(other: [Schema](index.md)): [Schema](index.md)

Useful as a utility, but slower than simply defining all protocols within the same schema.

#### Return

a new schema containing the protocols of each

#### Throws

| | |
|---|---|
| [ReassignmentException](../-reassignment-exception/index.md) | the sets contain conflicting declarations of a given protocol |
