package kanary

import kotlin.reflect.KClass

internal fun KClass(className: String): KClass<*> = Class.forName(className).kotlin

inline fun <T> T.isNotNullAnd(predicate: (T & Any).() -> Boolean) = if (this == null) false else predicate(this)