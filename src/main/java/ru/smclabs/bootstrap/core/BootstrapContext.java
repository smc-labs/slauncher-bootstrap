package ru.smclabs.bootstrap.core;

import org.jetbrains.annotations.NotNull;
import ru.smclabs.slauncher.resources.provider.DirProvider;

public class BootstrapContext {
    private final @NotNull DirProvider dirProvider;

    public BootstrapContext() {
        dirProvider = new DirProvider("SIMPLEMINECRAFT");
        initProperties();
    }

    private void initProperties() {
        System.setProperty("slauncher.logDir", dirProvider.getLogsDir().toString());
        System.setProperty("slauncher.logName", "bootstrap");
    }

    public @NotNull DirProvider getDirProvider() {
        return dirProvider;
    }
}
