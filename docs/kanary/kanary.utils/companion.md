//[kanary](../../index.md)/[kanary.utils](index.md)/[companion](companion.md)

# companion

[jvm]\
val [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;*&gt;.[companion](companion.md): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?

Circumvents bug in [companionObjectInstance](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect.full/index.html) where an [IllegalStateException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-state-exception/index.html) is thrown for certain Java classes.

#### Return

the companion object of the given class, or null if one does not exist
