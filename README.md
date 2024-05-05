# Kanary
**Fast and memory-efficient binary serialization for Kotlin JVM**

[![JitPack: v2.1](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Maintained?: yes](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
For information that does not need to be accessed directly, JSON is memory-inefficient and slow.
Furthermore, libraries such as 
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) require the use of plugins and
code generation, which adds unnecessary complexity for small projects. Kanary aims to avoid these issues.

## Overview

Kanary supports serialization of all primitive types, as well as any top-level (excluding local and unnamed) classes
with a defined serialization protocol.
The primary entry-point of the API is `protocolSet`.

```kotlin
fun protocolSet(builder: ProtocolSetBuilderScope.() -> Unit): ProtocolSet
```

Within the scope provided, binary I/O protocols can be defined (0 definitions are allowed).
Protocols for primitive types, primitive arrays, or `String` are disallowed
and will throw an exception when definition is attempted. This is by design to promote correct use of the API.
Instead, the user should use the appropriate primitive `read`/`write` function within a
`PrimitiveSerializer`/`PrimitiveDeserializer`. As a result of this restriction, instances of these types within
generic arrays, lists, and iterable cannot be serialized unless the protocol of an
enclosing class does so manually with their respective `write` function.

```kotlin
fun <reified T> ProtocolSetBuilderScope.protocolOf(
  builder: ProtocolBuilderScope<T>.() -> Unit
): ProtocolSpecification<T>
```

Within the nested scope provided, the user can provide the appropriate
read and write operations for objects of the specified type.

```kotlin
class ProtocolBuilderScope<T> {
    var read: Deserializer.() -> T by AssignOnce()
    var write: Serializer.(T) -> Unit by AssignOnce()
}
```

The outer function returns a `ProtocolSet`, which can be passed to a serializer or deserializer to provide
read/write functionality for reference types with the defined protocols. They can also be combined using
the `+` operator. Because such sets are immutable, they are inherently thread-safe.

To actually serialize/deserialize data, invoke the appropriate function from an `InputStream` or `OutputStream`.

```kotlin
fun InputStream.deserializer(): PrimitiveDeserializer
fun InputStream.deserializer(protocols: ProtocolSet): Deserializer  // Implements PrimitiveDeserializer

fun OutputStream.serializer(): PrimitiveSerializer
fun OutputStream.serializer(protocols: ProtocolSet): Serializer     // Implements PrimitiveSerializer
```

If a reference type is serialized and does not have a defined protocol,
Kanary reflection to find a suitable protocol from one of its superclasses or interfaces.
However, for deserialization, there must exist a protocol for the exact class or interface written in binary.
Both are true for members of generic arrays, lists, and iterables as well.

## Kanary Format

The binary I/O specification in [EBNF](https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form) is as follows:

```
primitive : marker value

primitiveArray : marker size value*

objectArray : marker size (typeName object)*

list : objectArray
    // More efficient than serialization as an iterable due to the fact that the size of the buffer is predetermined

iterable : marker (typeName object)* sentinel

object : marker typeName object
    // Serialized/deserialized by defined protocol
    // Type name determined by KClass<*>.qualifiedName
```

## Example

The class:

```kotlin
// Person.kt

import java.io.FileOutputStream

class Person(val name: String, val id: Int)

private val protocol = protocolSet {
    protocolOf<Person> {
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
}

fun main() {
    FileOutputStream("myFile.bin").serializer(protocol).use {
        it.write(Person("Bob", 7))
    }
}
```

produces the following binary:

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

**v2.1**

- Protocols are no longer defined to the global scope, but to a `ProtocolSet`
- Protocols for primitive types, primitive arrays, and `String`s can no longer be defined
- Now restricts reference type read/write to result of qualified `deserializer`/`serializer`
- Reference types can now be written to binary with the protocol of one of their superclasses or interfaces
- Failure to define either `read` or `write` once now throws a more detailed exception
- Renamed `InputStream.binary` to `InputStream.deserializer`
- Renamed `OutputStream.binary` to `OutputStream.serializer`
- Renamed `BinaryInput` to `Deserializer`
- Renamed `BinaryOutput` to `Serializer`
