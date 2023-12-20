package ru.smclabs.bootstrap.util.resource;

public class ResourceManagerException extends RuntimeException {

    public ResourceManagerException() {
        super();
    }

    public ResourceManagerException(String message) {
        super(message);
    }

    public ResourceManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceManagerException(Throwable cause) {
        super(cause);
    }

}
