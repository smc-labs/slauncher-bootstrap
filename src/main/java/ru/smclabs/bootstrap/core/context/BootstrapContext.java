package ru.smclabs.bootstrap.core.context;

import org.jetbrains.annotations.NotNullByDefault;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

@NotNullByDefault
public class BootstrapContext {
    private final Path workingDir;
    private final DirProvider dirProvider;

    public BootstrapContext() {
        workingDir = Paths.get(System.getProperty("user.dir"));
        dirProvider = new DirProvider("SIMPLEMINECRAFT");
        initProperties();
    }

    private void initProperties() {
        System.setProperty("slauncher.logDir", dirProvider.getLogsDir().toString());
        System.setProperty("slauncher.logName", "bootstrap");
    }

    public Path getWorkingDir() {
        return workingDir;
    }

    public DirProvider getDirProvider() {
        return dirProvider;
    }
}
