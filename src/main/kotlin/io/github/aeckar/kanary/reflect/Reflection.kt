package io.github.aeckar.kanary.reflect

import io.github.aeckar.kanary.MalformedContainerException
import java.io.NotSerializableException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

internal typealias Type = KClass<*>

internal val Type.isLocalOrAnonymous
    inline get() = with (java) { isLocalClass || isAnonymousClass }

internal val Type.isSAMConversion
    inline get() = java.interfaces.singleOrNull()?.kotlin?.isFun == true && declaredMembers.isEmpty()

/**
 * The properties declared in the primary constructor, or null if this class does not have a primary constructor
 * @see io.github.aeckar.kanary.Container
 */
// TODO look into caching after we get this working
internal val Type.containedProperties: Array<KProperty<*>>?
    inline get() {
        val parameters = primaryConstructor?.valueParameters?.map { it.name } ?: return null
        val allProperties = declaredMemberProperties
        val properties = Array<KProperty<*>?>(parameters.size) { null }
        for (index in properties.indices) {
            properties[index] = allProperties.find { it.name == parameters[index] }
                ?: throw MalformedContainerException(
                    qualifiedName,
                    "All arguments in the primary constructor of a container must be public properties"
                )
        }
        @Suppress("UNCHECKED_CAST")
        return properties as Array<KProperty<*>>
    }