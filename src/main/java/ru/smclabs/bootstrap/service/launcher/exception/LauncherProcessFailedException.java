package ru.smclabs.bootstrap.service.launcher.exception;

import java.io.IOException;

public class LauncherProcessFailedException extends LauncherServiceException {
    private final String processOutput;

    public LauncherProcessFailedException(String processOutput) {
        super("Launcher process is failed!");
        this.processOutput = processOutput;
    }

    public LauncherProcessFailedException(IOException exception, String processOutput) {
        super(exception);
        this.processOutput = processOutput;
    }

    public String getProcessOutput() {
        return processOutput;
    }
}
