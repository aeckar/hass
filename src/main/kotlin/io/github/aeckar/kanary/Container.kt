package io.github.aeckar.kanary

/**
 * Classes with this annotation can be (de)serialized without a defined protocol.
 *
 * Such classes must have a public primary constructor with all arguments being declared as public properties.
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Container
