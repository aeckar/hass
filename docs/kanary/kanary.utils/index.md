//[kanary](../../index.md)/[kanary.utils](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [ReassignmentException](-reassignment-exception/index.md) | [jvm]<br>class [ReassignmentException](-reassignment-exception/index.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [Exception](https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html)<br>Thrown when there is an attempt to assign a value to a property that has already been given a value and can only be assigned a value once. |

## Properties

| Name | Summary |
|---|---|
| [companion](companion.md) | [jvm]<br>val [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;*&gt;.[companion](companion.md): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?<br>Circumvents bug in [companionObjectInstance](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect.full/index.html) where an [IllegalStateException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-state-exception/index.html) is thrown for certain Java classes. |
| [jvmName](jvm-name.md) | [jvm]<br>val [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;*&gt;.[jvmName](jvm-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>The [qualified name](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/qualified-name.html) of the class reference. Replaces any periods after any enclosing typenames with dollar signs, matching their signature in the JVM. If local or anonymous, this property is null. |

## Functions

| Name | Summary |
|---|---|
| [KClass](-k-class.md) | [jvm]<br>fun [KClass](-k-class.md)(className: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;*&gt; |
| [takeIf](take-if.md) | [jvm]<br>inline fun &lt;[T](take-if.md)&gt; [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?.[takeIf](take-if.md)(): [T](take-if.md)? |
