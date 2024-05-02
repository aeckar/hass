# Kanary
**Fast and memory-efficient binary serialization for Kotlin**

[![](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary)

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
inline fun <reified T> protocolOf(builder: ProtocolBuilderScope<T>.() -> Unit) { /* ... */ }
```

Within the `ProtocolBuilderScope` provided, the user can provide the appropriate read and write operations.

```kotlin
class ProtocolBuilderScope<T> {
    var read: BinaryInput.() -> T by AssignOnce()
    var write: BinaryOutput.(T) -> Unit by AssignOnce()
}
```

`protocolOf()` **must** be called from within the `init` block of the
companion object of the class that is being defined protocol.
This ensures thread-safety, since the protocol is only defined once the classes is loaded in (used).

`BinaryInput` provides read functionality, while `BinaryOutput` provides write functionality.

```kotlin
import java.io.Closeable
import java.io.Flushable

@JvmInline
value class BinaryInput internal constructor(private val stream: InputStream) : Closeable {
    fun read/* primitive */() { /* ... */ }     // primitives
    fun <T : Any> read() { /* ... */ }          // classes with protocols
    
    // ...
}

@JvmInline
value class BinaryOutput internal constructor(private val stream: OutputStream) : Closeable, Flushable {
    fun write(/* primitive */) { /* ... */ }    // primitives
    fun write(obj: Any) { /* ... */ }           // classes with protocols
    
    // ...
}
```

In the produced binary:

- Primitives and their wrapper types are serialized to their exact value

- Arrays and `Iterable<*>`s are serialized to their length, followed by each member

- Classes are serialized to their [qualified class name](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/qualified-name.html), followed by the binary format specified by their protocol

## Example

The class:

```kotlin
// Person.kt

class Person(val name: String, val id: Int) {
    private companion object {
        init { protocol }
    }
}

private val protocol = protocolOf<Person> { // Evaluated once in companion initializer
    read = {
        val name = readString()
        val id = readInt()
        Person(name, id)
    }
    write = { instance ->
        write(instance.name)
        write(instance.id)
    }
}
```

produces the following binary file:

```
TODO
```

## Benchmarks

TODO

## Changelog

**v1.0**

- Release

**v1.1**

- Download from JitPack fixed

- Changed name of `protocol` to `protocolOf`.

- `read` and `write` are now assigned as properties

**v1.2**

- Serialization by protocol prepends the class name to serialized data

- Superclasses can now be deserialized from a serialized subclass with a defined protocol

- Added I/O protocols for primitive wrapper types, array types, and types implementing `Iterable<*>`