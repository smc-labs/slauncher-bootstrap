package ru.smclabs.bootstrap.core.context;

import org.jetbrains.annotations.NotNullByDefault;
import ru.smclabs.slauncher.logger.Logger;
import ru.smclabs.slauncher.logger.LoggerFactory;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

@NotNullByDefault
public class BootstrapContext {
    private final Path workingDir;
    private final DirProvider dirProvider;
    private final Logger logger;

    public BootstrapContext() {
        workingDir = Paths.get(System.getProperty("user.dir"));
        dirProvider = new DirProvider("SIMPLEMINECRAFT");
        logger = LoggerFactory.create(dirProvider.getLogsDir(), "bootstrap");
    }

    public Path getWorkingDir() {
        return workingDir;
    }

    public DirProvider getDirProvider() {
        return dirProvider;
    }

    public Logger getLogger() {
        return logger;
    }
}
