package io.github.aeckar.kanary.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * The [qualified name][KClass.qualifiedName] of the class reference.
 *
 * Replaces any periods after any enclosing typenames with dollar signs, matching their signature in the JVM.
 * If local or anonymous, this property is null.
 */
internal val KClass<*>.jvmName: String? get() = qualifiedName?.let { if ('.' in it) java.name else it }

/**
 * Circumvents bug in [companionObjectInstance] where an [IllegalStateException] is thrown for certain Java classes.
 * @return the companion object of the given class, or null if one does not exist
 */
@PublishedApi
internal val KClass<*>.companion: Any? get() {
    val isKotlinClass = java.declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
    return takeIf { isKotlinClass }?.companionObjectInstance
}

internal fun KClass(className: String): KClass<*> = Class.forName(className).kotlin

@PublishedApi
internal inline fun <reified T : Any> Any?.takeIf() = takeIf { it is T } as T?

/**
 * Thrown when there is an attempt to assign a value to a property that has already been given a value
 * and can only be assigned a value once.
 */
class ReassignmentException(message: String) : Exception(message)

/**
 * Signifies that the collection iterates through its elements in the same order that they were inserted.
 *
 * Performs no formal checks, and is purely for documentation purposes.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
internal annotation class IteratesInOrder