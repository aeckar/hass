package io.github.aeckar.kanary

@PublishedApi
internal class Protocol(
    val read: ReadOperation?,
    val write: WriteOperation?
) {
    val hasFallback = read is FallbackReadOperation
    val hasStatic = write is StaticWriteOperation
}