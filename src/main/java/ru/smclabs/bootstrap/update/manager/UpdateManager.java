package ru.smclabs.bootstrap.update.manager;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.gui.controller.UpdateViewController;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.bootstrap.process.repository.ProcessRefRepository;
import ru.smclabs.bootstrap.process.starter.LauncherProcessStarter;
import ru.smclabs.bootstrap.update.UpdateTask;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.util.concurrent.atomic.AtomicReference;

public class UpdateManager {
    private static final Logger log = LoggerFactory.getLogger(UpdateManager.class);

    private final DirProvider dirProvider;
    private final BootstrapHttpService httpService;
    private final ProcessRefRepository processRefStorage;
    private final LauncherProcessStarter launcherProcessStarter;
    private final UpdateViewController viewController;
    private final AtomicReference<UpdateTask> currentTaskRef = new AtomicReference<>();

    public UpdateManager(
            DirProvider dirProvider,
            BootstrapHttpService httpService,
            ProcessRefRepository processRefStorage,
            LauncherProcessStarter launcherProcessStarter,
            UpdateViewController viewController
    ) {
        this.dirProvider = dirProvider;
        this.httpService = httpService;
        this.processRefStorage = processRefStorage;
        this.launcherProcessStarter = launcherProcessStarter;
        this.viewController = viewController;
    }

    public UpdateTask runUpdate() throws InterruptedException {
        cancelTaskAndWait();

        UpdateTask newTask = createTask();
        newTask.start();

        currentTaskRef.set(newTask);

        return newTask;
    }

    public @Nullable UpdateTask cancelTask() {
        UpdateTask currentTask = currentTaskRef.getAndSet(null);

        if (currentTask != null && currentTask.isNotCancelled()) {
            currentTask.cancel();
        }

        return currentTask;
    }

    private void cancelTaskAndWait() throws InterruptedException {
        UpdateTask currentTask = cancelTask();

        if (currentTask != null) {
            try {
                currentTask.join();
            } catch (InterruptedException e) {
                log.info("Thread interrupted while waiting for cancel task.");
                throw e;
            }
        }
    }

    private UpdateTask createTask() {
        return new UpdateTask(
                httpService,
                dirProvider,
                processRefStorage,
                viewController,
                launcherProcessStarter
        );
    }
}
