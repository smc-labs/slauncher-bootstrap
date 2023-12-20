package ru.smclabs.bootstrap.service.resource.exception;

import lombok.Getter;
import ru.smclabs.resources.type.Resource;

@Getter
public class ResourceWriteException extends Exception {

    private final Resource resource;

    public ResourceWriteException(Resource resource, String message) {
        super(message);
        this.resource = resource;
    }

    public ResourceWriteException(Resource resource, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
    }

    public ResourceWriteException(Resource resource, Throwable cause) {
        super(cause);
        this.resource = resource;
    }
}
