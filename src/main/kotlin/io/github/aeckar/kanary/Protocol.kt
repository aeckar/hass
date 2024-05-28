package io.github.aeckar.kanary

@PublishedApi
internal class Protocol(
    val read: ReadOperation?,
    val write: WriteOperation?,
    val hasFallback: Boolean,
    val hasStatic: Boolean
)