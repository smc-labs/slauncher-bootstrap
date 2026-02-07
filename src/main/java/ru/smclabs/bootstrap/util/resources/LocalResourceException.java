package ru.smclabs.bootstrap.util.resources;

public class LocalResourceException extends RuntimeException {

    public LocalResourceException() {
        super();
    }

    public LocalResourceException(String message) {
        super(message);
    }

    public LocalResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalResourceException(Throwable cause) {
        super(cause);
    }

}
