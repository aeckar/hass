package kanary

import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import kotlin.test.assertEquals

val dataProtocol = protocolOf<Data> {
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

val messageProtocol = protocolOf<Message> {
    read = {
        Message(readString())
    }
    write = {
        write(it.message)
    }
}

val personProtocol = protocolOf<Person> {
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
) {
    private companion object {
        init { dataProtocol.assign() }
    }
}

data class Message(val message: String) {
    private companion object {
        init { messageProtocol.assign() }
    }
}


data class Person(val name: String, val id: Int) : Serializable {
    private companion object {
        init { personProtocol.assign() }
    }
}

class KanaryTest {
    @Test
    fun kanaryTest() {
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
        FileOutputStream("src/test/resources/kanaryTest.bin").binary().use {
            it.write(writeData)
            it.write(writeMessage)
        }
        val readData: Data
        val readMessage: Message
        FileInputStream("src/test/resources/kanaryTest.bin").binary().use {
            readData = it.readObject()
            readMessage = it.readObject()
        }
        assertEquals(writeData, readData)
        assertEquals(writeMessage, readMessage)
    }

    @Test
    fun personTest() {
        val original = Person("Bob", 7)
        FileOutputStream("src/test/resources/person.bin").binary().use {
            it.write(original)
        }
        val deserialized: Person
        FileInputStream("src/test/resources/person.bin").binary().use {
            deserialized = it.readObject()
        }
        assertEquals(original, deserialized)
    }
}