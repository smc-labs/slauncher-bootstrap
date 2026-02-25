package ru.smclabs.bootstrap.process.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.gui.controller.UpdateViewController;
import ru.smclabs.bootstrap.process.ProcessRef;
import ru.smclabs.bootstrap.process.repository.ProcessRefRepository;
import ru.smclabs.bootstrap.update.resource.model.ResourcesPack;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class LauncherProcessStarter {
    private static final Logger log = LoggerFactory.getLogger(LauncherProcessStarter.class);

    private final UpdateViewController viewController;
    private final ProcessRefRepository processRefStorage;
    private final DirProvider dirProvider;

    private ResourcesPack pack;

    public LauncherProcessStarter(
            UpdateViewController viewController,
            ProcessRefRepository processRefStorage,
            DirProvider dirProvider
    ) {
        this.viewController = viewController;
        this.processRefStorage = processRefStorage;
        this.dirProvider = dirProvider;
    }

    public void setPack(ResourcesPack pack) {
        this.pack = pack;
    }

    public void start() throws IOException, InterruptedException {
        log.info("Starting SLauncher...");

        viewController.setTitles("Запуск лаунчера", "Пожалуйста подождите...");

        ProcessRef processRef = startProcess(
                pack.getRuntime().getExecutableBinary(),
                pack.getLauncher().getPath()
        );

        log.info("SLauncher started with pid: {}.", processRef.getPid());
    }

    private ProcessRef startProcess(Path runtime, Path launcher) throws IOException, InterruptedException {
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
            processRefStorage.save(processRef);
            validateStartup(process);
        } catch (IOException e) {
            log.error("Failed to start launcher process:", e);

            process.destroy();

            if (process.waitFor() != 0) {
                process.destroyForcibly();
            }

            throw e;
        }

        return processRef;
    }

    private void validateStartup(Process process) throws IOException {
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
