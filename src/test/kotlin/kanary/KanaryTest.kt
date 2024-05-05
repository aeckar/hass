package kanary

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import kotlin.test.assertEquals

val dataProtocol = protocolSet {
    protocolOf<Data> {
        read = {
            Data(
                readBoolean(),
                readByte(),
                readChar(),
                readShort(),
                readInt(),
                readLong(),
                readFloat(),
                readDouble(),
                readString(),
                readObject()
            )
        }
        write = {
            write(it.booleanValue)
            write(it.byteValue)
            write(it.charValue)
            write(it.shortValue)
            write(it.intValue)
            write(it.longValue)
            write(it.floatValue)
            write(it.doubleValue)
            write(it.stringValue)
            write(it.objValue)
        }
    }
}

val personAndMessageProtocols = protocolSet {
    protocolOf<Message> {
        read = {
            Message(readString())
        }
        write = {
            write(it.message)
        }
    }

    protocolOf<Person> {
        read = {
            val name = readString()
            val id = readInt()
            Person(name, id)
        }
        write = { instance ->
            write(instance.name)
            write(instance.id)
        }
    }
}

val charSequenceWrapperProtocol = protocolSet {
    protocolOf<CharSequenceWrapper> {
        read = {
            val chars = buildString {
                val size = readInt()
                repeat(size) {
                    append(readChar())
                }
            }
            StringWrapper(chars)
        }
        write = {
            write(it.chars.length)
            it.chars.forEach { c -> write(c) }
        }
    }
}

data class Data(
    val booleanValue: Boolean,
    val byteValue: Byte,
    val charValue: Char,
    val shortValue: Short,
    val intValue: Int,
    val longValue: Long,
    val floatValue: Float,
    val doubleValue: Double,
    val stringValue: String,
    val objValue: Message
)

data class Message(val message: String)
data class Person(val name: String, val id: Int) : Serializable
abstract class CharSequenceWrapper(open val chars: CharSequence)
class StringWrapper(override val chars: String) : CharSequenceWrapper(chars)

class KanaryTest {
    @Test
    fun inputOutput() {
        val writeData = Data(
            true,
            42,
            'X',
            -17,
            123,
            987654321L,
            -3.14159f,
            2.71828,
            "Random string",
            Message("Hello, Copilot!")
        )
        val writeMessage = Message("Goodbye, world!")
        val combinedProtocol = dataProtocol + personAndMessageProtocols
        FileOutputStream("src/test/resources/inputOutput.bin").serializer(combinedProtocol).use {
            it.write(writeData)
            it.write(writeMessage)
        }
        val readData: Data
        val readMessage: Message
        FileInputStream("src/test/resources/inputOutput.bin").deserializer(combinedProtocol).use {
            readData = it.readObject()
            readMessage = it.readObject()
        }
        assertEquals(writeData, readData)
        assertEquals(writeMessage, readMessage)
    }

    @Test
    fun readmeExample() {
        val original = Person("Bob", 7)
        FileOutputStream("src/test/resources/readmeExample.bin").serializer(personAndMessageProtocols).use {
            it.write(original)
        }
        val deserialized: Person
        FileInputStream("src/test/resources/readmeExample.bin").deserializer(personAndMessageProtocols).use {
            deserialized = it.readObject()
        }
        assertEquals(original, deserialized)
    }

    @Test
    fun polymorphicWrite() {
        val original = StringWrapper("Hello, world!")
        FileOutputStream("src/test/resources/polymorphicWrite.bin").serializer(charSequenceWrapperProtocol).use {
            it.write(original)
        }
        val deserialized: CharSequenceWrapper
        FileInputStream("src/test/resources/polymorphicWrite.bin").deserializer(charSequenceWrapperProtocol).use {
            deserialized = it.readObject()
        }
        assertEquals(original.chars, deserialized.chars)
    }

    @Test
    fun missingProtocol() {
        val original = Person("Jim", 15)
        assertThrows<MissingProtocolException> {
            FileOutputStream("src/test/resources/missingProtocol.bin").serializer(charSequenceWrapperProtocol).use {
                it.write(original)
            }
        }
    }
}