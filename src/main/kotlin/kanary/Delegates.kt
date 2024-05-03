package kanary

import kotlin.reflect.KProperty

/**
 * Alternative to 'lateinit' modifier preventing reassignment after first assignment.
 */
class AssignOnce<T : Any> {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw NoSuchElementException("Property has not been initialized yet")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value?.let { throw ReassignmentException("Property has already been initialized") }
        this.value = value
    }
}

/**
 * Thrown when there is an attempt to assign a value to a property that has already been given a value
 * and can only be assigned a value once.
 */
class ReassignmentException(message: String) : Exception(message)
