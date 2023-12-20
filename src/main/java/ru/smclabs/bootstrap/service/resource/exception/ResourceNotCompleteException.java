package ru.smclabs.bootstrap.service.resource.exception;

import ru.smclabs.resources.type.Resource;

public class ResourceNotCompleteException extends ResourceWriteException {

    public ResourceNotCompleteException(Resource resource, String message) {
        super(resource, message);
    }

    public ResourceNotCompleteException(Resource resource, String message, Throwable cause) {
        super(resource, message, cause);
    }

    public ResourceNotCompleteException(Resource resource, Throwable cause) {
        super(resource, cause);
    }
}
