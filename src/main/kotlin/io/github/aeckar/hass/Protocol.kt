package io.github.aeckar.hass

import io.github.aeckar.hass.reflect.Type

internal typealias ProtocolMap = Map<Type, Protocol>

@PublishedApi
internal class Protocol(
    val read: ReadOperation<*>?,
    val write: WriteOperation<*>?,
    val hasFallback: Boolean,
    val hasStatic: Boolean
) {
    companion object {
        fun ensureUniqueMaps(map: ProtocolMap, otherMap: ProtocolMap) {
            map.keys.find { it in otherMap }?.let {
                throw MalformedProtocolException(it, "Conflicting protocol declarations")
            }
        }
    }
}