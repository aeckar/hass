package io.github.aeckar.kanary.reflect

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers

internal typealias Type = KClass<*>

internal val Type.isLocalOrAnonymous
    inline get() = with (java) { isLocalClass || isAnonymousClass }

internal val Type.isSAMConversion
    inline get() = java.interfaces.singleOrNull()?.kotlin?.isFun == true && declaredMembers.isEmpty()