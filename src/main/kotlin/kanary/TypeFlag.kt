package kanary

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@PublishedApi
internal val builtInTypes = TypeFlag.entries.asSequence().map { it.jvmClass }.toHashSet()

/**
 * Thrown when an attempt is made to read serialized data of a certain fundamental type, but found another type.
 */
class TypeFlagMismatchException internal constructor(message: String) : IOException(message)

internal enum class TypeFlag(val jvmClass: KClass<*> = Nothing::class) {
    // Primitive types
    BOOLEAN(Boolean::class),
    BYTE(Byte::class),
    CHAR(Char::class),
    SHORT(Short::class),
    INT(Int::class),
    LONG(Long::class),
    FLOAT(Float::class),
    DOUBLE(Double::class),

    // Primitive array types
    BOOLEAN_ARRAY(BooleanArray::class),
    BYTE_ARRAY(ByteArray::class),
    CHAR_ARRAY(CharArray::class),
    SHORT_ARRAY(ShortArray::class),
    INT_ARRAY(IntArray::class),
    LONG_ARRAY(LongArray::class),
    FLOAT_ARRAY(FloatArray::class),
    DOUBLE_ARRAY(DoubleArray::class),

    // Object types
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
    SIMPLE_OBJECT(OBJECT.jvmClass),
    FUNCTION(KFunction::class),
    NULL;

    // Ensures that the correct type is parsed during deserialization
    fun validate(stream: InputStream) {
        val flag = stream.read()
        if (ordinal != flag) {
            throw TypeFlagMismatchException("Type flag '$name' expected, but found '${TypeFlag.nameOf(flag)}'")
        }
    }

    // Marks the beginning of a new type during serialization
    fun mark(stream: OutputStream) {
        stream.write(ordinal)
    }

    companion object {
        fun nameOf(flag: Int) = if (flag == -1) "EOF" else (entries.find { it.ordinal == flag }?.name ?: "UNKNOWN")
    }
}
