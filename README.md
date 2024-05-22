# Kanary
**Fast and memory-efficient binary serialization for Kotlin JVM**

[![JitPack: v2.1](https://jitpack.io/v/aeckar/kanary.svg)](https://jitpack.io/#aeckar/kanary) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Maintained?: yes](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

## Reasoning

The goal of this library is to offer a simple, yet efficient API for binary serialization.
For information that does not need to be accessed directly, JSON is memory-inefficient and slow.
Furthermore, libraries such as 
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) require the use of plugins and
code generation, which adds unnecessary complexity for small projects. Kanary aims to avoid these issues.
