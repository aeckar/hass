package io.github.aeckar.kanary.reflect

import kotlin.reflect.KClass

internal typealias Type = KClass<*>

internal val Type.isLocalOrAnonymous inline get() = with (java) { isLocalClass || isAnonymousClass }

@Suppress("NOTHING_TO_INLINE")
internal inline fun Type(className: String): Type = Class.forName(className).kotlin