package io.github.aeckar.kanary.reflect

import io.github.aeckar.kanary.MalformedContainerException
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

internal typealias Type = KClass<*>
internal typealias Callable = KCallable<*>

internal val Type.isLocalOrAnonymous
    inline get() = with (java) { isLocalClass || isAnonymousClass }

internal val Type.isSAMConversion
    inline get() = java.interfaces.singleOrNull()?.kotlin?.isFun == true && declaredMembers.isEmpty()

/**
 * The properties declared in the primary constructor, or null if this class does not have a primary constructor
 * @see io.github.aeckar.kanary.Container
 */
internal val Type.primaryProperties: Array<out Callable>?
    inline get() {
        val parameters = primaryConstructor?.valueParameters?.map { it.name } ?: return null
        val allProperties = declaredMemberProperties
        val properties = Array<Callable?>(parameters.size) { null }
        for (index in properties.indices) {
            properties[index] = allProperties.find { it.name == parameters[index] }
                ?: throw MalformedContainerException(
                    qualifiedName,
                    "All arguments in the primary constructor of a container must be public properties"
                )
        }
        @Suppress("UNCHECKED_CAST")
        return properties as Array<out Callable>
    }

fun Type(forName: String): Type = Class.forName(forName).kotlin