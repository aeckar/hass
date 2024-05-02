package kanary

/**
 * Thrown when there is an attempt to assign a value to a property that has already been given a value
 * and can only be assigned a value once.
 */
class ReassignmentException internal constructor(message: String) : Exception(message)