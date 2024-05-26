@file:JvmMultifileClass
@file:JvmName("UtilsKt")
package io.github.aeckar.kanary.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * The [qualified name][KClass.qualifiedName] of the class reference.
 *
 * Replaces any periods after any enclosing typenames with dollar signs, matching their signature in the JVM.
 * If local or anonymous, this property is null.
 */
internal val KClass<*>.jvmName: String? inline get() = qualifiedName?.let { if ('.' in it) java.name else it }

/**
 * Circumvents bug in [companionObjectInstance] where an [IllegalStateException] is thrown for certain Java classes.
 * @return the companion object of the given class, or null if one does not exist
 */
@PublishedApi
internal val KClass<*>.companion: Any? inline get() {
    val isKotlinClass = java.declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
    return takeIf { isKotlinClass }?.companionObjectInstance
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun KClass(className: String): KClass<*> = Class.forName(className).kotlin

@PublishedApi
internal inline fun <reified T : Any> Any?.takeIf() = takeIf { it is T } as T?

@Suppress("NOTHING_TO_INLINE")
internal inline infix fun Int.incIf(predicate: Boolean) = if (predicate) this + 1 else this

@Suppress("NOTHING_TO_INLINE")
internal inline infix fun Int.decIf(predicate: Boolean) = if (predicate) this - 1 else this