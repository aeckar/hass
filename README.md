# Kanary
**Fast and memory-efficient binary serialization for Kotlin JVM**

[![JitPack: v2.1](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Maintained?: yes](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
For information that does not need to be accessed directly, JSON is memory-inefficient and slow.
Furthermore, libraries such as 
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) require the use of plugins and
code generation, which adds unnecessary complexity for small projects. Kanary aims to avoid these issues.

## Changelog

### v1.0

- Release

### v1.1

- JitPack support fixed
- Changed name of `protocol` to `protocolOf`.
- `read` and `write` are now assigned as properties

### v2.0

- Superclasses can now be deserialized from a serialized subclass with a defined protocol
- Added I/O protocols for primitive array types, object array types, lists, and types implementing `Iterable`
- `protocolOf` now returns a `ProtocolSpecification`, allowing storage of protocol specifications
- Nullable types can now be serialized with their respective functions

*Breaking changes:*
- All serialized values are now prefixed by a unique, 1-byte code, ensuring memory safety
- For object types, serialization by protocol prepends the class name to serialized data

### v2.1

- Protocols are no longer defined to the global scope, but to a `ProtocolSet`
- Protocols for primitive types, primitive arrays, and `String`s can no longer be defined
- Now restricts reference type read/write to result of qualified `deserializer`/`serializer`
- Reference types can now be written to binary with the protocol of one of their superclasses or interfaces
- Failure to define either `read` or `write` once now throws a more detailed exception
- Renamed `InputStream.binary` to `InputStream.deserializer`
- Renamed `OutputStream.binary` to `OutputStream.serializer`
- Renamed `BinaryInput` to `Deserializer`
- Renamed `BinaryOutput` to `Serializer`

### v3.0

- Reworked core library functionality
- Written data by supertypes are now propagated to subtypes as "packets" by default
- Added `noinherit`, `fallback`, and `static` operation modifiers
- Improved type-safety
- Default protocols now exist for certain built-in types
- `KClass<*>` extensions have been made public
- Many name changes to improve semantics
- All reference types are now read from binary using `read<T>()`

*Breaking changes:*
- Types with default protocols are written with their own unique 1-byte code
- For other object types, packets are now emitted during serialization

### v3.1

- Added support for write delegation using the `Writable` interface

### v3.2

- Type aliases for read and write operations made public
- Added documentation for `Writable`
