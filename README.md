# Kanary
**Fast and memory-efficient binary serialization for Kotlin JVM**

[![JitPack: v2.1](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Maintained?: yes](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
It offers a custom, type-safe binary serialization format optimized for
speed--no plugins or code generation required!
What's more is that schemas can be extended and stored as objects, providing extensibility for already-compiled code.

To learn more, [documentation](https://github.com/aeckar/kanary/tree/master/docs) is provided.

## Getting Started

```kotlin
import kanary.Schema
import kanary.deserializer
import kanary.schema
import kanary.serializer
import java.io.FileInputStream
import java.io.FileOutputStream

data class Person(val name: String, val age: Int)

val schema: Schema = schema {
    define<Person> {
        read = {
            Person(read(), readInt())
        }
        write = {
            write(it.name)
            write(it.age)
        }
    }
}

fun main() {
    FileOutputStream("myKanaryFile.bin").serializer(schema).use { it.write(Person("John Doe", 34)) }
    val myPerson = FileInputStream("myKanaryFile.bin").deserializer(schema).use { it.read<Person>() }
    println(myPerson)
}
```
