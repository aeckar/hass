package kanary.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * The [qualified name][KClass.qualifiedName] of the class reference.
 * Replaces any periods after any enclosing typenames with dollar signs, matching their signature in the JVM.
 * If local or anonymous, this property is null.
 */
val KClass<*>.jvmName: String? get() = qualifiedName?.let { if ('.' in it) java.name else it }

/**
 * Circumvents bug in [companionObjectInstance] where an [IllegalStateException] is thrown for certain Java classes.
 * @return the companion object of the given class, or null if one does not exist
 */
val KClass<*>.companion: Any? get() {
    val isKotlinClass = java.declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
    return takeIf { isKotlinClass }?.companionObjectInstance
}

/**
 * @return the class reference matching the given typename
 */
fun KClass(className: String): KClass<*> = Class.forName(className).kotlin

/**
 * @return the receiver if it is not null and can be casted to [T]
 */
inline fun <reified T> Any?.takeIf() = takeIf { it is T } as T?

/**
 * Thrown when there is an attempt to assign a value to a property that has already been given a value
 * and can only be assigned a value once.
 */
class ReassignmentException(message: String) : Exception(message)