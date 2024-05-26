package io.github.aeckar.kanary

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Special [bytes][ordinal] emitted to serialized data to enforce type-safety
 * and determine relative position during deserialization.
 */
@PublishedApi
internal enum class TypeFlag(val kClass: KClass<*> = Nothing::class) {
    BOOLEAN(Boolean::class),
    BYTE(Byte::class),
    CHAR(Char::class),
    SHORT(Short::class),
    INT(Int::class),
    LONG(Long::class),
    FLOAT(Float::class),
    DOUBLE(Double::class),
    BOOLEAN_ARRAY(BooleanArray::class),
    BYTE_ARRAY(ByteArray::class),
    CHAR_ARRAY(CharArray::class),
    SHORT_ARRAY(ShortArray::class),
    INT_ARRAY(IntArray::class),
    LONG_ARRAY(LongArray::class),
    FLOAT_ARRAY(FloatArray::class),
    DOUBLE_ARRAY(DoubleArray::class),
    STRING(String::class),
    OBJECT_ARRAY(Array<Any>::class),
    LIST(List::class),
    ITERABLE(Iterable::class),
    PAIR(Pair::class),
    TRIPLE(Triple::class),
    MAP_ENTRY(Map.Entry::class),
    MAP(Map::class),
    UNIT(Unit::class),
    OBJECT(Any::class),
    FUNCTION(KFunction::class),
    NULL,
    END_OBJECT;

    @PublishedApi
    internal companion object {
        /**
         * Types specified by [Schema] as having pre-defined protocols.
         */
        val K_CLASSES = TypeFlag.entries.asSequence().map { it.kClass }.toSet()
    }
}
