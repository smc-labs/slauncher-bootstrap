package ru.smclabs.bootstrap.service.launcher;

import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.service.launcher.exception.LauncherServiceException;
import ru.smclabs.bootstrap.service.launcher.process.LauncherProcess;
import ru.smclabs.bootstrap.service.resource.type.ResourceLauncher;
import ru.smclabs.slauncher.logger.Logger;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.ResourceCompressedRuntime;
import ru.smclabs.slauncher.util.logger.ILogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class ProcessManager {

    private static final BiPredicate<Path, BasicFileAttributes> PROCESS_FILE_FILTER = (path, attributes)
            -> attributes.isRegularFile() && path.getFileName().toString().endsWith(".json");

    private final Logger logger;
    private final List<LauncherProcess> processes = new ArrayList<>();

    public ProcessManager(Logger logger) {
        this.logger = logger;
    }

    public LauncherProcess create(Path launcherPath, Path executableBinary) throws LauncherServiceException {
        DirProvider dirProvider = Bootstrap.getInstance().getDirProvider();

        LauncherProcess launcherProcess = new LauncherProcess(dirProvider, executableBinary);
        launcherProcess.addParam("-cp")
                .addParam(launcherPath.toUri().getPath())
                .addParam("ru.smclabs.slauncher.SLauncherMain");

        launcherProcess.start(dirProvider);

        processes.add(launcherProcess);
        return launcherProcess;
    }

    private void destroyGameProcesses(ResourceCompressedRuntime runtime, ResourceLauncher launcher) {
        if (!Files.exists(runtime.getPath()) || !Files.exists(launcher.getPath())) {
            return;
        }

        List<String> params = new ArrayList<>();
        params.add(runtime.getPath().toString());
        params.add("-cp");
        params.add(runtime.getPath().toUri().getPath());
        params.add("ru.smclabs.slauncher.SLauncherMain");
        params.add("--destroy-processes");

        ProcessBuilder builder = new ProcessBuilder(params);
        builder.directory(Bootstrap.getInstance().getDirProvider().getPersistenceDir().toFile());
        builder.redirectErrorStream(true);
        builder.environment().put("_JAVA_OPTIONS", "");

        try {
            Process process = builder.start();

            try (InputStreamReader isr = new InputStreamReader(process.getInputStream());
                 BufferedReader reader = new BufferedReader(isr)) {

                String line = reader.readLine();
                while (line != null) {
                    if (line.contains("Game processes destroyed")) return;
                    line = reader.readLine();
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to destroy game processes!", e);
        }
    }

    public void destroyLauncherProcesses(ResourceCompressedRuntime runtime, ResourceLauncher launcher) {
        destroyGameProcesses(runtime, launcher);

        if (processes.isEmpty()) {
            return;
        }

        processes.forEach(process -> {
            try {
                process.destroy();
            } catch (LauncherServiceException e) {
                logger.error("Failed to destroy process", e);
            }
        });
        processes.clear();
    }

    public void readProcessesFromDisk() throws LauncherServiceException {
        processes.clear();
        DirProvider dirProvider = Bootstrap.getInstance().getDirProvider();
        try (Stream<Path> files = Files.find(dirProvider.getPersistenceDir("data/process/launcher"), 1, PROCESS_FILE_FILTER)) {
            files.forEach(file -> {
                try {
                    processes.add(new LauncherProcess(file));
                } catch (LauncherServiceException e) {
                    logger.error("Failed to read launcher process data!", e);
                }
            });
        } catch (IOException e) {
            throw new LauncherServiceException(e);
        }
    }
}
