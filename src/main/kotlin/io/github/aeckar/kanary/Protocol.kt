package io.github.aeckar.kanary

@PublishedApi
internal class Protocol(
    val read: ReadOperation?,
    val write: WriteOperation?
) {
    val hasFallback inline get() = read is FallbackReadOperation
    val hasStatic inline get() = write is StaticWriteOperation
}