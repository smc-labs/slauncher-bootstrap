package ru.smclabs.bootstrap.launcher.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.launcher.ProcessManager;
import ru.smclabs.bootstrap.launcher.reference.ProcessRef;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ProcessWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessWrapper.class);

    private final DirProvider dirProvider;
    private final ProcessManager processManager;
    private final Path runtime;
    private final Path launcher;

    public ProcessWrapper(
            DirProvider dirProvider,
            ProcessManager processManager,
            Path runtime,
            Path launcher
    ) {
        this.dirProvider = dirProvider;
        this.processManager = processManager;
        this.runtime = runtime;
        this.launcher = launcher;
    }

    public void start() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                runtime.toString(),
                "-jar",
                launcher.toString()
        );
        processBuilder.directory(dirProvider.getPersistenceDir().toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("_JAVA_OPTIONS", "");

        Process process = processBuilder.start();

        ProcessRef processRef = ProcessRef.from(process);

        try {
            processManager.save(processRef);
            waitResponse(process);
        } catch (IOException e) {
            LOGGER.error("Failed to start launcher process:", e);

            process.destroy();

            if (process.waitFor() != 0) {
                process.destroyForcibly();
            }
        }
    }

    private void waitResponse(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("Starting SLauncher")) {
                    break;
                }

                line = reader.readLine();
            }
        }
    }
}
