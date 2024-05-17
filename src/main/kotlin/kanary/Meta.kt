package kanary

import kotlin.reflect.KClass

/*
    These API are not related to the library, but are useful nonetheless and should be available to users.
 */

/**
 * The [qualified name][KClass.qualifiedName] of the class reference.
 * Replaces any periods after any enclosing typenames with dollar signs, matching their signature in the JVM.
 * If local or anonymous, this property is null.
 */
val KClass<*>.className: String? get() {
    val name = qualifiedName ?: return null
    val classPos = name.indexOfFirst { it.isUpperCase() } + 1
    if (classPos == 0) {
        return name
    }
    return buildString {
        append(name, 0, classPos)
        append(name.replace(".", "$"), classPos, name.length)
    }
}

/**
 * @return the class reference matching the given typename
 */
fun KClass(className: String): KClass<*> = Class.forName(className).kotlin

/**
 * Thrown when there is an attempt to assign a value to a property that has already been given a value
 * and can only be assigned a value once.
 */
class ReassignmentException(message: String) : Exception(message)