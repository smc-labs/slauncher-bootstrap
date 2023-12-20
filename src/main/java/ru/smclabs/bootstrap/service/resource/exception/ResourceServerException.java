package ru.smclabs.bootstrap.service.resource.exception;

public class ResourceServerException extends Exception {

    public ResourceServerException(String message) {
        super(message);
    }

    public ResourceServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceServerException(Throwable cause) {
        super(cause);
    }
}
