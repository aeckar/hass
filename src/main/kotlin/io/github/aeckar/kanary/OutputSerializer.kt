package io.github.aeckar.kanary

import io.github.aeckar.kanary.io.TypeFlag
import io.github.aeckar.kanary.io.TypeFlag.*
import io.github.aeckar.kanary.io.OutputDataStream
import io.github.aeckar.kanary.reflect.*
import io.github.aeckar.kanary.reflect.Type
import io.github.aeckar.kanary.reflect.isLocalOrAnonymous
import io.github.aeckar.kanary.reflect.isSAMConversion
import java.io.*
import java.util.concurrent.ConcurrentHashMap
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
    private val stream = OutputDataStream(stream)
    private val serializer inline get() = this  // Used to invoke defined write operation

    // ------------------------------ public API ------------------------------

    override fun writeBoolean(cond: Boolean) {
        stream.writeTypeFlag(BOOLEAN)
        stream.writeBoolean(cond)
    }

    override fun writeByte(b: Byte) {
        stream.writeTypeFlag(BYTE)
        stream.writeByte(b)
    }

    override fun writeChar(c: Char) {
        stream.writeTypeFlag(CHAR)
        stream.writeChar(c)
    }

    override fun writeShort(n: Short) {
        stream.writeTypeFlag(SHORT)
        stream.writeShort(n)
    }

    override fun writeInt(n: Int) {
        stream.writeTypeFlag(INT)
        stream.writeInt(n)
    }

    override fun writeLong(n: Long) {
        stream.writeTypeFlag(LONG)
        stream.writeLong(n)
    }

    override fun writeFloat(n: Float) {
        stream.writeTypeFlag(FLOAT)
        stream.writeFloat(n)
    }

    override fun writeDouble(n: Double) {
        stream.writeTypeFlag(DOUBLE)
        stream.writeDouble(n)
    }

    /**
     * Writes the object in binary format according to the protocol of its type.
     *
     * Null objects are accepted, however their non-nullable type information is erased.
     * If the object is not null and its type does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     * @throws MissingOperationException any object of an anonymous or local class,
     * or an appropriate write operation cannot be found
     */
    override fun write(obj: Any?) {
        if (obj == null) {
            stream.writeTypeFlag(NULL)
            return
        }
        writeObject(obj)
    }

    /**
     * Writes all elements in array according to the protocol of each instance.
     *
     * Avoids null check for elements, unlike generic `write`.
     * Arrays of primitive types should be passed to the generic overload.
     */
    override fun <T : Any> write(array: Array<out T>) = writeObject(array, nonNullElements = true)

    /**
     * Writes all elements in the list according the protocol of each.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(list: List<T>) = writeObject(list, nonNullElements = true)

    /**
     * Writes all elements in the iterable object according the protocol of each as a list.
     *
     * The caller must ensure that the object has a finite number of elements.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(iter: Iterable<T>) = writeObject(iter, nonNullElements = true)

    /**
     * Writes the given pair according to the protocols of its elements.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(pair: Pair<T, T>) = writeObject(pair, nonNullElements = true)

    /**
     * Writes the given triple according to the protocols of its elements.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(triple: Triple<T, T, T>) = writeObject(triple, nonNullElements = true)

    /**
     * Writes the given map entry according to the protocols of its key and value.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(entry: Map.Entry<K, V>) = writeObject(entry, nonNullElements = true)

    /**
     * Writes the given map according to the protocols of its keys and values.
     *
     * Avoids null check for entries, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(map: Map<K, V>) = writeObject(map, nonNullElements = true)

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     *
     * If the stream is already closed then invoking this method has no effect.
     */
    override fun close() = stream.raw.close()

    /**
     * Flushes the underlying stream and forces any buffered output bytes to be written out.
     */
    override fun flush() = stream.raw.flush()

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
    private fun writeObject(obj: Any, nonNullElements: Boolean = false): Unit = with (stream) {
        fun Type.writeBuiltIn(obj: Any, builtIns: Map<Type, WriteHandle>) {
            builtIns.getValue(this).let { (flag, write) ->
                writeTypeFlag(flag)
                write.apply { serializer.writeOperation(obj) }
            }
        }

        fun writeFunction(obj: Any, notSerializableMessage: String) {
            if (obj !is Serializable) {
                throw NotSerializableException(notSerializableMessage)
            }
            writeTypeFlag(FUNCTION)
            writeSerializable(obj)
            return
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
        if (classRef.hasAnnotation<Container>()) {
            val parameters = classRef.containedProperties
                ?: throw NotSerializableException("Containers must declare a public primary constructor")
            stream.writeTypeFlag(CONTAINER)
            stream.writeByte(parameters.size.toByte())
            stream.writeString(classRef.jvmName)
            parameters.forEach { write(it.call(obj)) }
            return
        }
        if (classRef.isSAMConversion) {
            writeFunction(obj, "Functional interfaces of SAM conversions must implement Serializable")
            return
        }
        val className = classRef.takeIf { !it.isLocalOrAnonymous }?.jvmName
            ?: throw NotSerializableException("Serialization of local and anonymous class instances not supported")
        val protocol = schema.protocols[classRef]
        val builtIns = BuiltInWriteOperations given nonNullElements
        val builtInKClass: Type?
        val writeMap: WriteMap
        if (protocol != null && protocol.hasStatic) {
            builtInKClass = null
            writeMap = schema.writeMapOf(classRef)
        } else {
            builtInKClass = builtIns.keys.find { classRef.isSubclassOf(it) }
            writeMap = if (protocol == null && builtInKClass != null) { // Serialize as built-in type
                return builtInKClass.writeBuiltIn(obj, builtIns)
            } else {
                schema.writeMapOf(classRef)
            }
        }
        val (kClass, write) = writeMap.entries.first()
        val hasWrite = kClass == classRef   // If true, first entry in map is class, write operation of 'obj'
        val nonBuiltInSupers = writeMap.size decIf hasWrite
        val totalSupers = nonBuiltInSupers incIf (builtInKClass != null)
        writeTypeFlag(OBJECT)
        writeString(className)
        raw.write(totalSupers)
        if (nonBuiltInSupers != 0) {
            val entries = writeMap.iterator().also { if (hasWrite) it.next() }
            repeat(nonBuiltInSupers) {
                val (superKClass, superWrite) = entries.next()
                writeTypeFlag(OBJECT)
                writeType(superKClass)
                superWrite.apply { serializer.writeOperation(obj) }
                writeTypeFlag(END_OBJECT)
            }
            builtInKClass?.writeBuiltIn(obj, builtIns) // Marks stream with appropriate built-in flag
        }
        write.apply { serializer.writeOperation(obj) }
        writeTypeFlag(END_OBJECT)
    }

    private object BuiltInWriteOperations {
        private val nonNullBuiltInWrites = mapOf(
            builtInWriteOf(OBJECT_ARRAY) { objArray: Array<Any> ->
                stream.writeInt(objArray.size)
                objArray.forEach { writeObject(it) }
            },
            builtInWriteOf(LIST) { list: List<Any> ->
                stream.writeInt(list.size)
                list.forEach { writeObject(it) }
            },
            builtInWriteOf(ITERABLE) { iter: Iterable<Any> ->
                iter.forEach { writeObject(it) }
                stream.writeTypeFlag(END_OBJECT)
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
                stream.writeInt(map.size)
                map.forEach { (key, value) ->
                    writeObject(key)
                    writeObject(value)
                }
            },
            builtInWriteOf(SET) { set: Set<Any> ->
                stream.writeInt(set.size)
                set.forEach { writeObject(it) }
            }
        )

        private val nullableBuiltInWrites = mapOf(
            builtInWriteOf(BOOLEAN) { value: Boolean ->
                stream.writeBoolean(value)
            },
            builtInWriteOf(BYTE) { value: Byte ->
                stream.writeByte(value)
            },
            builtInWriteOf(CHAR) { value: Char ->
                stream.writeChar(value)
            },
            builtInWriteOf(SHORT) { value: Short ->
                stream.writeShort(value)
            },
            builtInWriteOf(INT) { value: Int ->
                stream.writeInt(value)
            },
            builtInWriteOf(LONG) { value: Long ->
                stream.writeLong(value)
            },
            builtInWriteOf(FLOAT) { value: Float ->
                stream.writeFloat(value)
            },
            builtInWriteOf(DOUBLE) { value: Double ->
                stream.writeDouble(value)
            },
            builtInWriteOf(BOOLEAN_ARRAY) { array: BooleanArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeBoolean(it) }
            },
            builtInWriteOf(BYTE_ARRAY) { array: ByteArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeByte(it) }
            },
            builtInWriteOf(CHAR_ARRAY) { array: CharArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeChar(it) }
            },
            builtInWriteOf(SHORT_ARRAY) { array: ShortArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeShort(it) }
            },
            builtInWriteOf(INT_ARRAY) { array: IntArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeInt(it) }
            },
            builtInWriteOf(LONG_ARRAY) { array: LongArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeLong(it) }
            },
            builtInWriteOf(FLOAT_ARRAY) { array: FloatArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeFloat(it) }
            },
            builtInWriteOf(DOUBLE_ARRAY) { array: DoubleArray ->
                stream.writeInt(array.size)
                array.forEach { stream.writeDouble(it) }
            },
            builtInWriteOf(STRING) { s: String ->
                stream.writeString(s)
            },
            builtInWriteOf(OBJECT_ARRAY) { objArray: Array<*> ->
                stream.writeInt(objArray.size)
                objArray.forEach { write(it) }
            },
            builtInWriteOf(LIST) { list: List<*> ->
                stream.writeInt(list.size)
                list.forEach { write(it) }
            },
            builtInWriteOf(ITERABLE) { iter: Iterable<*> ->
                iter.forEach { write(it) }
                stream.writeTypeFlag(END_OBJECT)
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
                stream.writeInt(map.size)
                map.forEach { (key, value) ->
                    write(key)
                    write(value)
                }
            },
            builtInWriteOf(SET) { set: Set<*> ->
                stream.writeInt(set.size)
                set.forEach { write(it) }
            },
            builtInWriteOf<Unit>(UNIT) { /* noop */ },
            builtInWriteOf(SCHEMA) { schema: Schema ->
                with (stream) {
                    val readsOrFallbacks = schema.readsOrFallBacks()
                    val writeMaps = schema.writeMaps()
                    writeBoolean(schema.readsOrFallBacks() is ConcurrentHashMap)
                    writeInt(schema.protocols.size)
                    schema.protocols.forEach { (classRef, protocol) ->
                        val read = protocol.read
                        val write = protocol.write
                        writeType(classRef)
                        if (read != null) { // Enables smart cast
                            writeBoolean(true)
                            writeBoolean(protocol.hasFallback)
                            writeSerializable(read)
                        } else {
                            writeBoolean(false)
                        }
                        if (write != null) { // Enables smart cast
                            writeBoolean(true)
                            writeBoolean(protocol.hasStatic)
                            writeSerializable(write)
                        } else {
                            writeBoolean(false)
                        }
                    }
                    writeInt(readsOrFallbacks.size)
                    writeInt(writeMaps.size)
                    readsOrFallbacks.forEach { (kClass, readOrFallback) ->
                        writeType(kClass)
                        writeSerializable(readOrFallback)
                    }
                    writeMaps.forEach { (kClass, writeMap) ->
                        writeType(kClass)
                        writeInt(writeMap.size)
                        writeMap.forEach { (type, write) ->
                            writeType(type)
                            writeSerializable(write)
                        }
                    }
                }
            }
        )

        /**
         * If [nonNullElements] is true, returns the optimized [built-in write handles][WriteHandle]
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
        ): Pair<Type, WriteHandle> =
            T::class to WriteHandle(flag, write as WriteOperation)
    }
}

/**
 * Specifies the [flag] from where the given [write operation][lambda] originates from.
 */
private data class WriteHandle(val flag: TypeFlag, val lambda: WriteOperation)