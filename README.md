# Kanary
**Effortless binary serialization for Kotlin JVM**

[![JitPack: v1.0.0](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary) [![pages-build-deployment](https://github.com/aeckar/kanary/actions/workflows/pages/pages-build-deployment/badge.svg?branch=master)](https://github.com/aeckar/kanary/actions/workflows/pages/pages-build-deployment) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Maintained?: yes](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

## Overview

The goal of this library is to offer a simple, yet efficient API for binary (de)serialization.
It offers a compact, type-safe binary encoding optimized for ease-of-use... no plugins or code generation required!

The API offers a high-level DSL capable of creating, consuming, extending, and (de)serializing schemas at runtime.
Polymorphic serialization is fully supported, with supertypes sharing their serialized data with all subclasses
in the form of sub-deserializers that can be read from as needed.

Read protocols of abstract classes are prefixed with `fallback`, allowing them to return a default implementation upon deserialization.
This is especially useful for collections, whose fundamental structure does not change with each subclass that implements them.

Write protocols can be prefixed with `static` to override all write operations higher-up in their class hierarchy,
saving the need to serialize data that will ultimately be left unused.

During building, schemas may extend their functionality with protocols defined in other schemas using an `import from` statement.
For API designers, this allows for modularity of serialization logic and a more organized codebase overall.

The beauty of the library is that it's *unopinionated*. You can serialize your objects in however way you want.
There are no contraints that your classes have a primary constructor with property arguments or derive from a common interface
(an exception being SAM conversions and lambdas). However, with this flexibility, the user must also explicitly define
how every serializable class without a built-in protocol should behave. Despite being a powerful tool, it can be a nuisance
for trivial classes whose sole purpose is to contain data. With this, Kanary provides the `Container` annotation that automatically
makes any class annotated with it serializable and deserializable, so long as it abides by the same rules set by
`kotlinx.serialization`'s `@Serializable` annotation. These are, that the class:

- Must have a public primary constructor
- All arguments in the primary constructor (if any) must be public properties

This library is *not* a replacement for [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).
It does not reap the performance benefits of compile-time code generation or the guarantee of multiplatform support.
It *does* however, provide an API that lets you serialize things easily and without much thought with *reasonable* performance
and *excellent* memory-efficiency. One drawback, though, is that because the library makes use of JVM-specific reflection,
it is unlikely that it'll be ported to other platforms. You win some, you lose some. 🤷‍♂️

To view the online documenation, visit [https://aeckar.github.io/kanary/](https://aeckar.github.io/kanary/).

Happy serializing!

## Getting Started

```kotlin
import io.github.aeckar.kanary.Container
import io.github.aeckar.kanary.Schema
import io.github.aeckar.kanary.deserializer
import io.github.aeckar.kanary.schema
import io.github.aeckar.kanary.serializer
import java.io.FileInputStream
import java.io.FileOutputStream

@Container
data class Person(val name: String, val age: Int)

fun main() {
    val schema: Schema = schema {}
    FileOutputStream("myKanaryFile.bin").serializer(schema).use { it.write(Person("John Doe", 34)) }
    val myPerson: Person = FileInputStream("myKanaryFile.bin").deserializer(schema).use { it.read() }
    println(myPerson)    // { name: "John Doe", age: 34 }
}
```

## Planned Features

**API:**
- Java interoperability
- Ability to track changes in schema state

**Implementation:**
- Container caching
- Dependency injection of cached data between different schemas
- Automatic update of serialized schemas upon mutation
