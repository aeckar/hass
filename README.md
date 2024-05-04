# Kanary
**Fast and memory-efficient binary serialization for Kotlin**

[![](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
For information that does not need to be accessed directly, JSON is memory-inefficient and slow.
There are alternative binary formats, however their implementations often require reflection and
(for simple projects) are generally more complicated than the task they are trying to accomplish.

## Overview

Kanary supports serialization of all primitive types, as well as any top-level (excluding local and unnamed) classes
with a defined serialization protocol.
The primary entry-point of the API is `protocolOf`.

```kotlin
inline fun <reified T> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit): ProtocolSpecification<T> { /* ... */ }
```

Within the `ProtocolBuilderScope` provided, the user can provide the appropriate read and write operations.

```kotlin
class ProtocolBuilderScope<T> {
    var read: BinaryInput.() -> T by AssignOnce()
    var write: BinaryOutput.(T) -> Unit by AssignOnce()
}
```

The function returns a `ProtocolSpecification`, which can be used to assign the defined protocol to the class
wherever it is used within a program. The `assign()` function must be called **once** from within the `init` block of the
companion object of the class that is being defined the protocol.
This ensures thread-safety, since the protocol is assigned lazily whenever the class is used for the first time.

`BinaryInput` provides read functionality, while `BinaryOutput` provides write functionality.

```kotlin
import java.io.Closeable
import java.io.Flushable

@JvmInline
value class BinaryInput internal constructor(internal val stream: InputStream) : Closeable {
    fun read/* primitive */()
    fun read/* primitive */Array()
    fun readString()
    fun read/* Nullables? */Array()
    fun read/* Nullables? */List()
    fun read/* Nullables? */Iterable()
    fun readNullable()
    fun readObject()
    
    // ...
}

@JvmInline
value class BinaryOutput internal constructor(internal val stream: OutputStream) : Closeable, Flushable {
    fun write(/* primitive | primitive array | string */) { /* ... */ }
    fun writeAll/* Or */(/* array | list | iterable */)
    fun writeNullable(obj: Any)
    fun writeObject(obj: Any)
    
    // ...
}
```

## Kanary Format

The binary I/O specification is as follows:

- Primitives
```
[code][value]
```

- Primitive arrays

```
[code][size][value1][value2]...[valueN]
```

- Object arrays & lists
  - More efficient than serialization as an iterable due to the fact that the size of the buffer is predetermined

```
[code][size][typeName1][object1][typeName2][object2]...[typeNameN][objectN]
```

- Iterables

```
[code][typeName1][object1][typeName2][object2]...[typeNameN][objectN][sentinel]
```

- Objects
  - Serialized/deserialized by their protocol
  - Type determined by [qualified class name](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/qualified-name.html)

```
[code][typeName][object]
```

## Example

The class:

```kotlin
// Person.kt

class Person(val name: String, val id: Int) {
    private companion object {
        init { protocol.assign() }
    }
}

private val protocol = protocolOf<Person> {
    read = {
        val name = readString() // Ensure reads are in-order
        val id = readInt()
        Person(name, id)
    }
    write = { instance ->
        write(instance.name)
        write(instance.id)
    }
}
```

produces the following binary file when serialized (name = "Bob", id = 7):

```
    | 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F  |
----+--------------------------------------------------+--------------------
 00 | 17 00 00 00 OD 6B 61 6E 61 72 79 2E 50 65 72 73  |    /0  kanary.Pers
 10 | 6F 6E 15 00 00 00 03 42 6F 62 02 00 00 00 07     | on Bobo oO

```

## Benchmarks

TODO

## Changelog

**v1.0**

- Release

**v1.1**

- JitPack support fixed

- Changed name of `protocol` to `protocolOf`.

- `read` and `write` are now assigned as properties

**v2.0**

- All serialized values are now prefixed by a unique, 1-byte code, ensuring memory safety

- For object types, serialization by protocol prepends the class name to serialized data

- Superclasses can now be deserialized from a serialized subclass with a defined protocol

- Added I/O protocols for primitive array types, object array types, lists, and types implementing `Iterable`

- `protocolOf` now returns a `ProtocolSpecification`, allowing storage of protocol specifications

- Nullable types can now be serialized with their respective functions