package io.github.aeckar.kanary

import io.github.aeckar.kanary.io.TypeFlag
import io.github.aeckar.kanary.reflect.Type
import java.io.EOFException

/**
 * Each instance is used to read a single packet of data
 * Instantiated from within [InputDeserializer.readObject]
 */
internal class SupertypeDeserializer(
    private val classRef: Type,
    private val supertype: Type,
    superFlag: TypeFlag,
    source: InputDeserializer,
    isBuiltIn: Boolean
) : Deserializer {
    private var cursor = 0
    private val objects = if (isBuiltIn) {
        listOf(source.readObject(superFlag))
    } else {
        buildList {
            var flag = source.stream.readTypeFlag()
            while (flag !== TypeFlag.END_OBJECT) {
                this += source.readObject(flag)
                flag = source.stream.readTypeFlag()
            }
        }
    }

    // ------------------------------ public API ------------------------------

    override fun readBoolean(): Boolean = nextObject()
    override fun readByte(): Byte = nextObject()
    override fun readChar(): Char = nextObject()
    override fun readShort(): Short = nextObject()
    override fun readInt(): Int = nextObject()
    override fun readLong(): Long = nextObject()
    override fun readFloat(): Float = nextObject()
    override fun readDouble(): Double = nextObject()
    override fun <T> read(): T = nextObject()

    // ------------------------------------------------------------------------

    private fun <T> nextObject(): T {
        return try {
            objects[cursor].matchCast<T>(classRef).also { ++cursor }
        } catch (_: IndexOutOfBoundsException) {
            throw EOFException(
                "Attempted read of object in supertype '$supertype' after" +
                        "supertype deserializer was exhausted (in protocol of '$classRef')")
        }
    }
}