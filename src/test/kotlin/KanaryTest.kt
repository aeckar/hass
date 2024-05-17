import kanary.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertIs

private inline fun useSerializer(testName: String, schema: Schema, use: (OutputSerializer) -> Unit) {
    val serializer = FileOutputStream("src/test/resources/$testName.bin").serializer(schema)
    use(serializer)
}

private inline fun <R> useDeserializer(testName: String, schema: Schema, use: (InputDeserializer) -> R): R {
    val deserializer = FileInputStream("src/test/resources/$testName.bin").deserializer(schema)
    return use(deserializer)
}

data class SerializableData(
    val boolean: Boolean,
    val byte: Byte,
    val char: Char,
    val short: Short,
    val int: Int,
    val long: Long,
    val float: Float,
    val double: Double,

    val booleanArray: BooleanArray,
    val byteArray: ByteArray,
    val charArray: CharArray,
    val shortArray: ShortArray,
    val intArray: IntArray,
    val longArray: LongArray,
    val floatArray: FloatArray,
    val doubleArray: DoubleArray,

    val string: String,
    val objectArray: Array<Any>,
    val list: List<*>,
    val iterable: Iterable<*>,
    val pair: Pair<*, *>,
    val triple: Triple<*, *, *>,
    val mapEntry: Map.Entry<*, *>,
    val map: Map<*, *>,
    val unit: Unit,
    val anyObject: Any,
    val simpleObject: Any,
    val function: () -> String, // Can be any type of function
    val nullValue: Nothing? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializableData

        if (boolean != other.boolean) return false
        if (byte != other.byte) return false
        if (char != other.char) return false
        if (short != other.short) return false
        if (int != other.int) return false
        if (long != other.long) return false
        if (float != other.float) return false
        if (double != other.double) return false
        if (!booleanArray.contentEquals(other.booleanArray)) return false
        if (!byteArray.contentEquals(other.byteArray)) return false
        if (!charArray.contentEquals(other.charArray)) return false
        if (!shortArray.contentEquals(other.shortArray)) return false
        if (!intArray.contentEquals(other.intArray)) return false
        if (!longArray.contentEquals(other.longArray)) return false
        if (!floatArray.contentEquals(other.floatArray)) return false
        if (!doubleArray.contentEquals(other.doubleArray)) return false
        if (string != other.string) return false
        if (!objectArray.contentEquals(other.objectArray)) return false
        if (list != other.list) return false
        if (iterable.toList() != other.iterable.toList()) return false
        if (pair != other.pair) return false
        if (triple != other.triple) return false
        if (mapEntry.key != other.mapEntry.key || mapEntry.value != other.mapEntry.value) return false
        if (map != other.map) return false
        if (anyObject != other.anyObject) return false
        if (simpleObject != other.simpleObject) return false
        /* if (function != other.function) return false */  // Equality not supported

        return true
    }

    override fun hashCode(): Int {
        var result = boolean.hashCode()
        result = 31 * result + byte
        result = 31 * result + char.hashCode()
        result = 31 * result + short
        result = 31 * result + int
        result = 31 * result + long.hashCode()
        result = 31 * result + float.hashCode()
        result = 31 * result + double.hashCode()
        result = 31 * result + booleanArray.contentHashCode()
        result = 31 * result + byteArray.contentHashCode()
        result = 31 * result + charArray.contentHashCode()
        result = 31 * result + shortArray.contentHashCode()
        result = 31 * result + intArray.contentHashCode()
        result = 31 * result + longArray.contentHashCode()
        result = 31 * result + floatArray.contentHashCode()
        result = 31 * result + doubleArray.contentHashCode()
        result = 31 * result + string.hashCode()
        result = 31 * result + objectArray.contentHashCode()
        result = 31 * result + list.hashCode()
        result = 31 * result + iterable.hashCode()
        result = 31 * result + pair.hashCode()
        result = 31 * result + triple.hashCode()
        result = 31 * result + mapEntry.hashCode()
        result = 31 * result + map.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + anyObject.hashCode()
        result = 31 * result + simpleObject.hashCode()
        result = 31 * result + function.hashCode()
        return result
    }
}

class Phonebook(val map: Map<String,Int>) : Map<String,Int> by map

open class ParentClass
open class SubClass : ParentClass()
class SubSubClass : SubClass()

class KanaryTest {
    interface Person {
        val name: String
        val id: Int
    }

    class UniquePerson(override val name: String, override val id: Int) : Person

    @Test
    fun deserialized_data_is_same_as_serialized_data() {
        val schema = schema {
            define<SerializableData> {
                read = {
                    SerializableData(
                        readBoolean(),
                        readByte(),
                        readChar(),
                        readShort(),
                        readInt(),
                        readLong(),
                        readFloat(),
                        readDouble(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read(),
                        read()
                    )
                }
                write = {
                    writeBoolean(it.boolean)    // Thanks copilot :)
                    writeByte(it.byte)
                    writeChar(it.char)
                    writeShort(it.short)
                    writeInt(it.int)
                    writeLong(it.long)
                    writeFloat(it.float)
                    writeDouble(it.double)
                    write(it.booleanArray)
                    write(it.byteArray)
                    write(it.charArray)
                    write(it.shortArray)
                    write(it.intArray)
                    write(it.longArray)
                    write(it.floatArray)
                    write(it.doubleArray)
                    write(it.string)
                    write(it.objectArray)
                    write(it.list)
                    write(it.iterable)
                    write(it.pair)
                    write(it.triple)
                    write(it.mapEntry)
                    write(it.map)
                    write(it.unit)
                    write(it.anyObject)
                    write(it.simpleObject)
                    write(it.function)
                    write(it.nullValue)
                }
            }
        }

        val serialized = SerializableData(
            boolean = true,
            byte = 42,
            char = 'X',
            short = 1000,
            int = 12345,
            long = 9876543210L,
            float = 3.14f,
            double = 2.71828,

            booleanArray = booleanArrayOf(true, false, true),
            byteArray = byteArrayOf(10, 20, 30),
            charArray = charArrayOf('A', 'B', 'C'),
            shortArray = shortArrayOf(100, 200, 300),
            intArray = intArrayOf(1000, 2000, 3000),
            longArray = longArrayOf(100000L, 200000L, 300000L),
            floatArray = floatArrayOf(1.23f, 4.56f, 7.89f),
            doubleArray = doubleArrayOf(0.1, 0.2, 0.3),

            string = "Hello, World!",
            objectArray = arrayOf("Apple", 42, true),
            list = listOf(1, 2, 3),
            iterable = setOf('a', 'b', 'c'),
            pair = Pair("Key", 123),
            triple = Triple(1, 2, 3),
            mapEntry = mapOf("Name" to "Alice").entries.first(),
            map = mapOf("A" to 1, "B" to 2),
            unit = Unit,
            anyObject = "Custom Object",
            simpleObject = 42,
            function = { "Generated Function Result" },
            nullValue = null
        )

        useSerializer("deserialized_data_is_same_as_serialized_data", schema) {
            it.write(serialized)
        }
        val deserialized: SerializableData = useDeserializer("deserialized_data_is_same_as_serialized_data", schema) {
            it.read()
        }
        assertEquals(serialized, deserialized)
    }

    @Test
    fun malformed_protocol_throws() {
        assertThrows<MalformedProtocolException> {
            schema {
                define<String> {}
            }
        }
    }

    @Test
    fun fallback_read() {
        val schema = schema {
            define<Person> {
                read = fallback {
                    object : Person {
                        override val name = "Joe Schmoe"
                        override val id = 1969
                    }
                }
                write = {
                    write(it.name)
                    write(it.id)
                }
            }
        }

        val serialized = UniquePerson("Charlie Brown", 17)
        useSerializer("fallback_read", schema) {    // It's recommended, but not necessary to define a protocol for a type with a fallback read
            it.write(serialized)
        }
        val deserialized: Person = useDeserializer("fallback_read", schema) {
            it.read()
        }
        assertEquals(deserialized.name, "Joe Schmoe")
    }

    @Test
    fun noinherit_read() = noinherit_and_static_reads("noinherit_read")

    @Test
    fun static_write() = noinherit_and_static_reads("static_write")

    @Test
    fun polymorphic_read() {
        val names = mutableListOf<String>()

        val schema = schema {
            define<ParentClass> {
                write = {
                    write("parent")
                }
            }
            define<SubClass> {
                read = {
                    SubClass()
                }
                write = {
                    write("subclass")
                }
            }
            define<SubSubClass> {
                read = {
                    names += supertype<ParentClass>().read<String>()
                    names += superclass.read<String>()
                    names += read<String>()
                    assertIs<SubClass>(read())
                    SubSubClass()
                }
                write = {
                    write("subclass of subclass")
                    write(SubClass())
                }
            }
        }

        val serialized = SubSubClass()
        useSerializer("polymorphic_read", schema) {
            it.write(serialized)
        }
        useDeserializer("polymorphic_read", schema) {
            it.read<SubSubClass>()
        }
        assertEquals(names, listOf("parent", "subclass", "subclass of subclass"))
    }

    class MyClass : Writable {
        override fun Serializer.write() {
            write("Your data here")
        }
    }

    @Test
    fun default_write() {
        val schema = schema {
            define<MyClass> {
                read = noinherit {
                    assertEquals(read(), "Your data here")
                    MyClass()
                }
                write = static()
            }
        }

        val serialized = MyClass()
        useSerializer("default_write", schema) {
            it.write(serialized)
        }
        useDeserializer("default_write", schema) {
            it.read<MyClass>()
        }
    }

    private fun noinherit_and_static_reads(testName: String) {
        val schema = schema {
            define<Phonebook> {
                read = if (testName == "noinherit_read") {
                    noinherit { // Faster and more memory-efficient!
                        Phonebook(
                            mapOf(
                                read<String>() to readInt(),
                                read<String>() to readInt()
                            )
                        )
                    }
                } else {
                    {
                        Phonebook(
                            mapOf(
                                read<String>() to readInt(),
                                read<String>() to readInt()
                            )
                        )
                    }
                }

                write = static {
                    it.entries.forEach { (name, number) ->
                        write(name)
                        write(number)
                    }
                }
            }
        }

        val serialized = Phonebook(mapOf(
            "Caroll" to 717892111,
            "John" to 2131241232
        ))
        useSerializer(testName, schema) {
            it.write(serialized)
        }
        val deserialized: Phonebook = useDeserializer(testName, schema) {
            it.read()

        }
        assertEquals(serialized.map, deserialized.map)
    }
}