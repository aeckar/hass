package kanary

import com.github.eckar.ReassignmentException
import com.github.eckar.once

/**
 * The scope wherein a protocol's [read] and [write] operations are defined.
 */
class ProtocolBuilderScope<T>(private val classRef: JvmType) {
    /**
     * The binary read operation when [Deserializer.readObject] is called with an object of class [T].
     * For types that implement
     * @throws InvalidProtocolException [T] is an abstract class or interface
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var read: PolymorphicDeserializer.() -> T by once {
        if (classRef.isAbstract) {
            throw InvalidProtocolException(classRef, "read is not supported for abstract classes and interfaces")
        }
    }

    /**
     * The binary write operation when [Serializer.writeBoolean] is called with an object of class [T]
     * @throws ReassignmentException this is assigned to more than once in a single scope
     */
    var write: WriteOperation<T> by once()
}

@PublishedApi
internal class Protocol<T : Any>(val read: ReadOperation<out T>, val write: WriteOperation<in T>)