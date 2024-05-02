# Kanary
**Fast and memory-efficient binary serialization for Kotlin**

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
For information that does not need to be accessed directly, JSON is memory-inefficient and slow.
There are alternative binary formats, however their implementations often require reflection and are generally
more complicated than the task they are trying to accomplish.

## Concepts

Kanary supports serialization of all primitive types, as well as any top-level (excluding local and unnamed) classes with a defined serialization protocol. The primary entry-point of the API is `protocolOf`.

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

Protocol declarations **must** be made from within the `init` block of any companion object. The exact object does not matter as long as the
type parameter `T` is correctly assigned. These calls are thread-safe and non-blocking, since they are evaluated during class loading.

`BinaryInput` provides read functionality, while `BinaryOutput` provides write functionality.

```kotlin
@JvmInline
value class BinaryInput internal constructor(private val stream: InputStream) : Closeable {
    fun read/* primitive */()    // primitives
    fun read<T : Any>()          // classes with protocols
}

@JvmInline
value class BinaryOutput internal constructor(private val stream: OutputStream) : Closeable, Flushable {
    fun write(/* primitive */)    // primitives
    fun write(obj: Any)           // classes with protocols
}
```

## Example

```kotlin
// Person.kt

class Person(val name: String, val id: Int) {
    private companion object {
        init { defineProtocol() }
    }
}

private fun defineProtocol() {
    protocolOf<Person> {  // Evaluated only once between constructors
        read = {  // Used for read<Person>(/* Person instance */)
            val name = readString()
            val id = readInt()
            Person(name, id)
        }
        write = { instance ->  // Used for write(/* Person instance */)
            write(instance.name)
            write(instance.id)
        }
    }
}
```

## Benchmarks

TODO

