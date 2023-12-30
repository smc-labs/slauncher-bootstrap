package ru.smclabs.bootstrap.util.logger;

import ru.smclabs.http.logger.IHttpLogger;

import java.util.logging.Level;

public class Logger implements IHttpLogger {

    private final java.util.logging.Logger logger;

    public Logger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    public void info(String message, Object... objects) {
        this.logger.log(Level.INFO, message, objects);
    }

    public void error(String message, Throwable e) {
        this.logger.log(Level.SEVERE, message, e);
    }

    public void warn(String message, Object... objects) {
        this.logger.log(Level.WARNING, message, objects);
    }
}
