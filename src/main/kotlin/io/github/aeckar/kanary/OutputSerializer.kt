package io.github.aeckar.kanary

import io.github.aeckar.kanary.io.TypeFlag
import io.github.aeckar.kanary.io.TypeFlag.*
import io.github.aeckar.kanary.io.Encoder
import io.github.aeckar.kanary.reflect.*
import java.io.*
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmName

private fun interface BuiltInWriteOperation<T> : TypedWriteOperation<T> {
    fun OutputSerializer.writeOperation(obj: T)
    override fun Serializer.writeOperation(obj: T) = (this as OutputSerializer).writeOperation(obj)
}

@Suppress("NOTHING_TO_INLINE")
private inline infix fun Int.incIf(predicate: Boolean) = if (predicate) this + 1 else this

@Suppress("NOTHING_TO_INLINE")
private inline infix fun Int.decIf(predicate: Boolean) = if (predicate) this - 1 else this

/**
 * Writes serialized data to a stream in Kanary format.
 *
 * Does not need to be closed so long as the underlying stream is closed.
 * Calling [close] also closes the underlying stream; [flush] works similarly.
 */
class OutputSerializer internal constructor(
    stream: OutputStream,
    private val schema: Schema
) : Serializer, Closeable, Flushable {
    private val encoder = Encoder(stream)
    private val serializer inline get() = this  // Used to invoke defined write operation

    // ------------------------------ public API ------------------------------

    override fun writeBoolean(cond: Boolean) {
        encoder.encodeTypeFlag(BOOLEAN)
        encoder.encodeBoolean(cond)
    }

    override fun writeByte(b: Byte) {
        encoder.encodeTypeFlag(BYTE)
        encoder.encodeByte(b)
    }

    override fun writeChar(c: Char) {
        encoder.encodeTypeFlag(CHAR)
        encoder.encodeChar(c)
    }

    override fun writeShort(n: Short) {
        encoder.encodeTypeFlag(SHORT)
        encoder.encodeShort(n)
    }

    override fun writeInt(n: Int) {
        encoder.encodeTypeFlag(INT)
        encoder.encodeInt(n)
    }

    override fun writeLong(n: Long) {
        encoder.encodeTypeFlag(LONG)
        encoder.encodeLong(n)
    }

    override fun writeFloat(n: Float) {
        encoder.encodeTypeFlag(FLOAT)
        encoder.encodeFloat(n)
    }

    override fun writeDouble(n: Double) {
        encoder.encodeTypeFlag(DOUBLE)
        encoder.encodeDouble(n)
    }

    override fun write(obj: Any?) {
        if (obj == null) {
            encoder.encodeTypeFlag(NULL)
            return
        }
        writeObject(obj)
    }

    override fun <T : Any> write(array: Array<out T>) = writeObject(array, nonNullElements = true)
    override fun <T : Any> write(list: List<T>) = writeObject(list, nonNullElements = true)
    override fun <T : Any> write(iter: Iterable<T>) = writeObject(iter, nonNullElements = true)
    override fun <T : Any> write(pair: Pair<T, T>) = writeObject(pair, nonNullElements = true)
    override fun <T : Any> write(triple: Triple<T, T, T>) = writeObject(triple, nonNullElements = true)
    override fun <K : Any, V : Any> write(entry: Map.Entry<K, V>) = writeObject(entry, nonNullElements = true)
    override fun <K : Any, V : Any> write(map: Map<K, V>) = writeObject(map, nonNullElements = true)

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     *
     * If the stream is already closed then invoking this method has no effect.
     */
    override fun close() = encoder.stream.close()

    /**
     * Flushes the underlying stream and forces any buffered output bytes to be written out.
     */
    override fun flush() = encoder.stream.flush()

    // ------------------------------------------------------------------------

    /**
     * ```
     * builtIn := (marker: flag)(builtIn: ...?)
     *
     * superData := (begin: flag = OBJECT)(className: noMarkString)(userData: ...)
     * (end: flag = END_OBJECT)
     *
     * object := (begin: flag = OBJECT)(className: noMarkString)(superCount: i8)
     * (supers: superData*)(builtInSuper: builtIn?)(userData: ...)(end: flag = END_OBJECT)
     * ```
     */
    private fun writeObject(obj: Any, nonNullElements: Boolean = false): Unit = with (encoder) {
        fun Type.writeBuiltIn(obj: Any, builtIns: Map<Type, WriteSignature>) {
            builtIns.getValue(this).let { (flag, write) ->
                encodeTypeFlag(flag)
                write.apply { serializer.writeOperation(obj) }
            }
        }

        fun writeFunction(function: Any, notSerializableMessage: String) {
            if (function !is Serializable) {
                throw NotSerializableException(notSerializableMessage)
            }
            encodeTypeFlag(FUNCTION)
            encodeSerializable(function)
        }

        fun writeContainer(obj: Any, containerRef: Type, containerName: String) {
            val properties = schema.primaryPropertiesOf(containerRef, containerName)
            encodeTypeFlag(CONTAINER)
            encodeString(containerName) // Deserialized as type
            encodeByte(properties.size.toByte())
            properties.forEach { write(it.call(obj)) }
        }

        /*
            Necessary because lambdas lack qualified names
            Only recognizes pre-Kotlin-2.0-style lambdas
         */
        if (obj is Function<*>) {
            writeFunction(obj, "Lambdas must be annotated with @JvmSerializableLambda")
            return
        }
        val classRef = obj::class
        if (classRef.isSAMConversion) {
            writeFunction(obj, "SAM conversions must implement Serializable")
            return
        }
        val className = classRef.takeIf { !it.isLocalOrAnonymous }?.jvmName
            ?: throw NotSerializableException("Serialization of local and anonymous class instances not supported")
        if (classRef.hasAnnotation<Container>()) {
            writeContainer(obj, classRef, className)
            return
        }
        val protocol = schema.protocols[classRef]
        val builtIns = BuiltInWriteOperations given nonNullElements
        val builtInType: Type?
        val writeMap: WriteMap
        if (protocol != null && protocol.hasStatic) {
            builtInType = null
            writeMap = schema.writeMapOf(classRef)
        } else {
            builtInType = builtIns.keys.find { classRef.isSubclassOf(it) }
            writeMap = if (protocol == null && builtInType != null) { // Serialize as built-in type
                return builtInType.writeBuiltIn(obj, builtIns)
            } else {
                schema.writeMapOf(classRef)
            }
        }
        val (type, write) = writeMap.entries.first()
        val hasWrite = type == classRef   // If true, first entry in map is class, write operation of 'obj'
        val nonBuiltInSupers = writeMap.size decIf hasWrite
        val totalSupers = nonBuiltInSupers incIf (builtInType != null)
        encodeTypeFlag(OBJECT)
        encodeString(className)
        stream.write(totalSupers)
        if (nonBuiltInSupers != 0) {
            val entries = writeMap.iterator().also { if (hasWrite) it.next() }
            repeat(nonBuiltInSupers) {
                val (supertype, superWrite) = entries.next()
                encodeTypeFlag(OBJECT)
                encodeType(supertype)
                superWrite.apply { serializer.writeOperation(obj) }
                encodeTypeFlag(END_OBJECT)
            }
            builtInType?.writeBuiltIn(obj, builtIns) // Marks stream with appropriate built-in flag
        }
        write.apply { serializer.writeOperation(obj) }
        encodeTypeFlag(END_OBJECT)
    }

    private object BuiltInWriteOperations {
        private val nonNullBuiltInWrites = mapOf(
            builtInWriteOf(OBJECT_ARRAY) { objArray: Array<Any> ->
                encoder.encodeInt(objArray.size)
                objArray.forEach { writeObject(it) }
            },
            builtInWriteOf(LIST) { list: List<Any> ->
                encoder.encodeInt(list.size)
                list.forEach { writeObject(it) }
            },
            builtInWriteOf(ITERABLE) { iter: Iterable<Any> ->
                iter.forEach { writeObject(it) }
                encoder.encodeTypeFlag(END_OBJECT)
            },
            builtInWriteOf(PAIR) { pair: Pair<Any, Any> ->
                writeObject(pair.first)
                writeObject(pair.second)
            },
            builtInWriteOf(TRIPLE) { triple: Triple<Any, Any, Any> ->
                writeObject(triple.first)
                writeObject(triple.second)
                writeObject(triple.third)
            },
            builtInWriteOf(MAP_ENTRY) { entry: Map.Entry<Any, Any> ->
                writeObject(entry.key)
                writeObject(entry.value)
            },
            builtInWriteOf(MAP) { map: Map<Any, Any> ->   // Multi-maps not supported by default
                encoder.encodeInt(map.size)
                map.forEach { (key, value) ->
                    writeObject(key)
                    writeObject(value)
                }
            },
            builtInWriteOf(SET) { set: Set<Any> ->
                encoder.encodeInt(set.size)
                set.forEach { writeObject(it) }
            }
        )

        private val nullableBuiltInWrites = mapOf(
            builtInWriteOf(BOOLEAN) { value: Boolean ->
                encoder.encodeBoolean(value)
            },
            builtInWriteOf(BYTE) { value: Byte ->
                encoder.encodeByte(value)
            },
            builtInWriteOf(CHAR) { value: Char ->
                encoder.encodeChar(value)
            },
            builtInWriteOf(SHORT) { value: Short ->
                encoder.encodeShort(value)
            },
            builtInWriteOf(INT) { value: Int ->
                encoder.encodeInt(value)
            },
            builtInWriteOf(LONG) { value: Long ->
                encoder.encodeLong(value)
            },
            builtInWriteOf(FLOAT) { value: Float ->
                encoder.encodeFloat(value)
            },
            builtInWriteOf(DOUBLE) { value: Double ->
                encoder.encodeDouble(value)
            },
            builtInWriteOf(BOOLEAN_ARRAY) { array: BooleanArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeBoolean(it) }
            },
            builtInWriteOf(BYTE_ARRAY) { array: ByteArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeByte(it) }
            },
            builtInWriteOf(CHAR_ARRAY) { array: CharArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeChar(it) }
            },
            builtInWriteOf(SHORT_ARRAY) { array: ShortArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeShort(it) }
            },
            builtInWriteOf(INT_ARRAY) { array: IntArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeInt(it) }
            },
            builtInWriteOf(LONG_ARRAY) { array: LongArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeLong(it) }
            },
            builtInWriteOf(FLOAT_ARRAY) { array: FloatArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeFloat(it) }
            },
            builtInWriteOf(DOUBLE_ARRAY) { array: DoubleArray ->
                encoder.encodeInt(array.size)
                array.forEach { encoder.encodeDouble(it) }
            },
            builtInWriteOf(STRING) { s: String ->
                encoder.encodeString(s)
            },
            builtInWriteOf(OBJECT_ARRAY) { objArray: Array<*> ->
                encoder.encodeInt(objArray.size)
                objArray.forEach { write(it) }
            },
            builtInWriteOf(LIST) { list: List<*> ->
                encoder.encodeInt(list.size)
                list.forEach { write(it) }
            },
            builtInWriteOf(ITERABLE) { iter: Iterable<*> ->
                iter.forEach { write(it) }
                encoder.encodeTypeFlag(END_OBJECT)
            },
            builtInWriteOf(PAIR) { pair: Pair<*, *> ->
                write(pair.first)
                write(pair.second)
            },
            builtInWriteOf(TRIPLE) { triple: Triple<*, *, *> ->
                write(triple.first)
                write(triple.second)
                write(triple.third)
            },
            builtInWriteOf(MAP_ENTRY) { entry: Map.Entry<*, *> ->
                write(entry.key)
                write(entry.value)
            },
            builtInWriteOf(MAP) { map: Map<*, *> ->
                encoder.encodeInt(map.size)
                map.forEach { (key, value) ->
                    write(key)
                    write(value)
                }
            },
            builtInWriteOf(SET) { set: Set<*> ->
                encoder.encodeInt(set.size)
                set.forEach { write(it) }
            },
            builtInWriteOf<Unit>(UNIT) { /* noop */ },
            builtInWriteOf(SCHEMA) { schema: Schema ->
                with (encoder) {
                    encodeBoolean(schema.isThreadSafe)
                    encodeInt(schema.protocols.size)
                    schema.protocols.forEach { (classRef, protocol) ->
                        val read = protocol.read
                        val write = protocol.write
                        encodeType(classRef)
                        if (read != null) { // Enables smart cast
                            encodeBoolean(true)
                            encodeBoolean(protocol.hasFallback)
                            encodeSerializable(read)
                        } else {
                            encodeBoolean(false)
                        }
                        if (write != null) { // Enables smart cast
                            encodeBoolean(true)
                            encodeBoolean(protocol.hasStatic)
                            encodeSerializable(write)
                        } else {
                            encodeBoolean(false)
                        }
                    }
                    encodeInt(schema.readsOrFallBacks.size)
                    schema.readsOrFallBacks.forEach { (type, readOrFallback) ->
                        encodeType(type)
                        encodeSerializable(readOrFallback)
                    }
                    encodeInt(schema.writeMaps.size)
                    schema.writeMaps.forEach { (type, writeMap) ->
                        encodeType(type)
                        encodeInt(writeMap.size)
                        writeMap.forEach { (type, write) ->
                            encodeType(type)
                            encodeSerializable(write)
                        }
                    }
                    encodeInt(schema.primaryPropertyArrays.size)
                    schema.primaryPropertyArrays.forEach { (containerName, primaryProperties) ->
                        encodeString(containerName)
                        encodeSerializable @JvmSerializableLambda { primaryProperties }
                    }
                    encodeInt(schema.primaryConstructors.size)
                    schema.primaryConstructors.forEach { (containerName, primaryConstructor) ->
                        encodeString(containerName)
                        encodeSerializable @JvmSerializableLambda { primaryConstructor }
                    }
                }

            }
        )

        /**
         * If [nonNullElements] is true, returns the optimized [built-in write handles][WriteSignature]
         * for composite types with non-null members. Otherwise, returns the built-in write handles
         * for all types with pre-defined protocols agreeing with the list in [Schema].
         * The returned map iterates in order of insertion,
         * with the write handle for [List] coming before that of [Iterable].
         * @return map of built-in write handles according to their [flag][TypeFlag] and element nullability
         */
        infix fun given(nonNullElements: Boolean) = if (nonNullElements) nonNullBuiltInWrites else nullableBuiltInWrites

        @Suppress("UNCHECKED_CAST")
        private inline fun <reified T : Any> builtInWriteOf(
            flag: TypeFlag,
            write: BuiltInWriteOperation<T>
        ): Pair<Type, WriteSignature> =
            T::class to WriteSignature(flag, write as WriteOperation)
    }
}

/**
 * Specifies the [flag] from where the given [write operation][lambda] originates from.
 */
private data class WriteSignature(val flag: TypeFlag, val lambda: WriteOperation)