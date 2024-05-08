package kanary

import java.io.InputStream
import java.io.OutputStream

@PublishedApi
internal enum class TypeCode {
    BOOLEAN,    BOOLEAN_ARRAY,
    BYTE,       BYTE_ARRAY,
    CHAR,       CHAR_ARRAY,
    SHORT,      SHORT_ARRAY,
    INT,        INT_ARRAY,
    LONG,       LONG_ARRAY,
    FLOAT,      FLOAT_ARRAY,
    DOUBLE,     DOUBLE_ARRAY,

    STRING,

    OBJECT_ARRAY,   NULLABLES_ARRAY,
    LIST,           NULLABLES_LIST,
    ITERABLE,       NULLABLES_ITERABLE,
    MAP_ENTRY,      NULLABLES_MAP_ENTRY,
    PAIR,           NULLABLES_PAIR,
    TRIPLE,         NULLABLES_TRIPLE,

    SENTINEL,
    OBJECT,
    NULL,
    PACKET;

    fun validate(stream: InputStream) {
        val code = stream.read()
        if (ordinal != code) {
            throw TypeMismatchException("Type '$name' expected, but found '${TypeCode.nameOf(code)}'")
        }
    }

    fun mark(stream: OutputStream) {
        stream.write(ordinal)
    }

    companion object {
        fun nameOf(code: Int) = entries.find { it.ordinal == code }?.name ?: "UNKNOWN"
    }
}
