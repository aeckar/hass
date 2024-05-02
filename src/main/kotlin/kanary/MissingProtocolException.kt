package kanary

/**
 * Thrown when a class is expected to have a Kanary I/O protocol,
 */
class MissingProtocolException(message: String) : Exception(message)