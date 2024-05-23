# Kanary
**Fast and memory-efficient binary serialization for Kotlin JVM**

[![JitPack: v1.0.0](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary) [![pages-build-deployment](https://github.com/aeckar/kanary/actions/workflows/pages/pages-build-deployment/badge.svg?branch=master)](https://github.com/aeckar/kanary/actions/workflows/pages/pages-build-deployment) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Maintained?: yes](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
It offers a custom, type-safe binary serialization format optimized for
speed--no plugins or code generation required!
What's more is that schemas can be extended and stored as objects, providing extensibility for already-compiled code.

To learn more, online [documentation](https://aeckar.github.io/kanary/) is provided.

## Getting Started

```kotlin
import com.github.aeckar.kanary.Schema
import com.github.aeckar.kanary.deserializer
import com.github.aeckar.kanary.schema
import com.github.aeckar.kanary.serializer
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
