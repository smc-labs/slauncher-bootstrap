package ru.smclabs.bootstrap.service.launcher.exception;

public class LauncherServiceException extends RuntimeException {
    public LauncherServiceException(String message) {
        super(message);
    }

    public LauncherServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LauncherServiceException(Throwable cause) {
        super(cause);
    }
}
