package ru.ifmo.server;

/**
 * Server exception implementation.
 */
public class ServerException extends RuntimeException {
    /**
     * Constructs server exception with message.
     *
     * @param message Exception message.
     */
    public ServerException(String message) {
        super(message);
    }

    /**
     * Constructs server exception with message and cause.
     *
     * @param message Exception message.
     * @param cause Cause exception.
     */
    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
