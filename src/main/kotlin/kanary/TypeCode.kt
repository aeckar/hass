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

    OBJECT_ARRAY,   NULLABLE_ARRAY,
    LIST,           NULLABLE_LIST,
    ITERABLE_BEGIN, NULLABLE_BEGIN,

    SENTINEL,
    OBJECT,
    NULL,
    STRING;

    fun validate(stream: InputStream) {
        val code = stream.read()
        if (ordinal != code) {
            throw TypeMismatchException(this, code)
        }
    }

    fun mark(stream: OutputStream) {
        stream.write(ordinal)
    }

    companion object {
        fun nameOf(code: Int) = entries.find { it.ordinal == code }?.name ?: "UNKNOWN"
    }
}
