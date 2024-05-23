//[kanary](../../../index.md)/[kanary](../index.md)/[TypeFlagMismatchException](index.md)

# TypeFlagMismatchException

[jvm]\
class [TypeFlagMismatchException](index.md) : [IOException](https://docs.oracle.com/javase/8/docs/api/java/io/IOException.html)

Thrown when an attempt is made to read serialized data of a certain flagged type, but another type is encountered.

## Properties

| Name | Summary |
|---|---|
| [cause](../../kanary.utils/-reassignment-exception/index.md#-654012527%2FProperties%2F-1216412040) | [jvm]<br>open val [cause](../../kanary.utils/-reassignment-exception/index.md#-654012527%2FProperties%2F-1216412040): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)? |
| [message](../../kanary.utils/-reassignment-exception/index.md#1824300659%2FProperties%2F-1216412040) | [jvm]<br>open val [message](../../kanary.utils/-reassignment-exception/index.md#1824300659%2FProperties%2F-1216412040): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |

## Functions

| Name | Summary |
|---|---|
| [addSuppressed](../../kanary.utils/-reassignment-exception/index.md#282858770%2FFunctions%2F-1216412040) | [jvm]<br>fun [addSuppressed](../../kanary.utils/-reassignment-exception/index.md#282858770%2FFunctions%2F-1216412040)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)) |
| [fillInStackTrace](../../kanary.utils/-reassignment-exception/index.md#-1102069925%2FFunctions%2F-1216412040) | [jvm]<br>open fun [fillInStackTrace](../../kanary.utils/-reassignment-exception/index.md#-1102069925%2FFunctions%2F-1216412040)(): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [getLocalizedMessage](../../kanary.utils/-reassignment-exception/index.md#1043865560%2FFunctions%2F-1216412040) | [jvm]<br>open fun [getLocalizedMessage](../../kanary.utils/-reassignment-exception/index.md#1043865560%2FFunctions%2F-1216412040)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getStackTrace](../../kanary.utils/-reassignment-exception/index.md#2050903719%2FFunctions%2F-1216412040) | [jvm]<br>open fun [getStackTrace](../../kanary.utils/-reassignment-exception/index.md#2050903719%2FFunctions%2F-1216412040)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://docs.oracle.com/javase/8/docs/api/java/lang/StackTraceElement.html)&gt; |
| [getSuppressed](../../kanary.utils/-reassignment-exception/index.md#672492560%2FFunctions%2F-1216412040) | [jvm]<br>fun [getSuppressed](../../kanary.utils/-reassignment-exception/index.md#672492560%2FFunctions%2F-1216412040)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)&gt; |
| [initCause](../../kanary.utils/-reassignment-exception/index.md#-418225042%2FFunctions%2F-1216412040) | [jvm]<br>open fun [initCause](../../kanary.utils/-reassignment-exception/index.md#-418225042%2FFunctions%2F-1216412040)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [printStackTrace](../../kanary.utils/-reassignment-exception/index.md#-1769529168%2FFunctions%2F-1216412040) | [jvm]<br>open fun [printStackTrace](../../kanary.utils/-reassignment-exception/index.md#-1769529168%2FFunctions%2F-1216412040)()<br>open fun [printStackTrace](../../kanary.utils/-reassignment-exception/index.md#1841853697%2FFunctions%2F-1216412040)(p0: [PrintStream](https://docs.oracle.com/javase/8/docs/api/java/io/PrintStream.html))<br>open fun [printStackTrace](../../kanary.utils/-reassignment-exception/index.md#1175535278%2FFunctions%2F-1216412040)(p0: [PrintWriter](https://docs.oracle.com/javase/8/docs/api/java/io/PrintWriter.html)) |
| [setStackTrace](../../kanary.utils/-reassignment-exception/index.md#2135801318%2FFunctions%2F-1216412040) | [jvm]<br>open fun [setStackTrace](../../kanary.utils/-reassignment-exception/index.md#2135801318%2FFunctions%2F-1216412040)(p0: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://docs.oracle.com/javase/8/docs/api/java/lang/StackTraceElement.html)&gt;) |
