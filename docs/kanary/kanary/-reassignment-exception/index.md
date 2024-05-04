//[kanary](../../../index.md)/[kanary](../index.md)/[ReassignmentException](index.md)

# ReassignmentException

[jvm]\
class [ReassignmentException](index.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [Exception](https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html)

Thrown when there is an attempt to assign a value to a property that has already been given a value and can only be assigned a value once.

## Constructors

| | |
|---|---|
| [ReassignmentException](-reassignment-exception.md) | [jvm]<br>constructor(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [cause](../-missing-protocol-exception/index.md#-654012527%2FProperties%2F-1216412040) | [jvm]<br>open val [cause](../-missing-protocol-exception/index.md#-654012527%2FProperties%2F-1216412040): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)? |
| [message](../-missing-protocol-exception/index.md#1824300659%2FProperties%2F-1216412040) | [jvm]<br>open val [message](../-missing-protocol-exception/index.md#1824300659%2FProperties%2F-1216412040): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |

## Functions

| Name | Summary |
|---|---|
| [addSuppressed](../-missing-protocol-exception/index.md#282858770%2FFunctions%2F-1216412040) | [jvm]<br>fun [addSuppressed](../-missing-protocol-exception/index.md#282858770%2FFunctions%2F-1216412040)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)) |
| [fillInStackTrace](../-missing-protocol-exception/index.md#-1102069925%2FFunctions%2F-1216412040) | [jvm]<br>open fun [fillInStackTrace](../-missing-protocol-exception/index.md#-1102069925%2FFunctions%2F-1216412040)(): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [getLocalizedMessage](../-missing-protocol-exception/index.md#1043865560%2FFunctions%2F-1216412040) | [jvm]<br>open fun [getLocalizedMessage](../-missing-protocol-exception/index.md#1043865560%2FFunctions%2F-1216412040)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getStackTrace](../-missing-protocol-exception/index.md#2050903719%2FFunctions%2F-1216412040) | [jvm]<br>open fun [getStackTrace](../-missing-protocol-exception/index.md#2050903719%2FFunctions%2F-1216412040)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://docs.oracle.com/javase/8/docs/api/java/lang/StackTraceElement.html)&gt; |
| [getSuppressed](../-missing-protocol-exception/index.md#672492560%2FFunctions%2F-1216412040) | [jvm]<br>fun [getSuppressed](../-missing-protocol-exception/index.md#672492560%2FFunctions%2F-1216412040)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)&gt; |
| [initCause](../-missing-protocol-exception/index.md#-418225042%2FFunctions%2F-1216412040) | [jvm]<br>open fun [initCause](../-missing-protocol-exception/index.md#-418225042%2FFunctions%2F-1216412040)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [printStackTrace](../-missing-protocol-exception/index.md#-1769529168%2FFunctions%2F-1216412040) | [jvm]<br>open fun [printStackTrace](../-missing-protocol-exception/index.md#-1769529168%2FFunctions%2F-1216412040)()<br>open fun [printStackTrace](../-missing-protocol-exception/index.md#1841853697%2FFunctions%2F-1216412040)(p0: [PrintStream](https://docs.oracle.com/javase/8/docs/api/java/io/PrintStream.html))<br>open fun [printStackTrace](../-missing-protocol-exception/index.md#1175535278%2FFunctions%2F-1216412040)(p0: [PrintWriter](https://docs.oracle.com/javase/8/docs/api/java/io/PrintWriter.html)) |
| [setStackTrace](../-missing-protocol-exception/index.md#2135801318%2FFunctions%2F-1216412040) | [jvm]<br>open fun [setStackTrace](../-missing-protocol-exception/index.md#2135801318%2FFunctions%2F-1216412040)(p0: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://docs.oracle.com/javase/8/docs/api/java/lang/StackTraceElement.html)&gt;) |
