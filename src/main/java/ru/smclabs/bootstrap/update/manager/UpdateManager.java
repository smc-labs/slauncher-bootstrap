package ru.smclabs.bootstrap.update.manager;

import ru.smclabs.bootstrap.gui.controller.UpdateViewController;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.bootstrap.process.repository.ProcessRefRepository;
import ru.smclabs.bootstrap.process.starter.LauncherStarter;
import ru.smclabs.bootstrap.update.UpdateTask;
import ru.smclabs.slauncher.resources.provider.DirProvider;

public class UpdateManager {
    private final DirProvider dirProvider;
    private final BootstrapHttpService httpService;
    private final ProcessRefRepository processRefStorage;
    private final LauncherStarter launcherStarter;
    private final UpdateViewController viewController;

    private UpdateTask currentTask;

    public UpdateManager(
            DirProvider dirProvider,
            BootstrapHttpService httpService,
            ProcessRefRepository processRefStorage,
            LauncherStarter launcherStarter,
            UpdateViewController viewController
    ) {
        this.dirProvider = dirProvider;
        this.httpService = httpService;
        this.processRefStorage = processRefStorage;
        this.launcherStarter = launcherStarter;
        this.viewController = viewController;
    }

    public void runUpdate() throws InterruptedException {
        currentTask = createTask();
        currentTask.start();
        currentTask.join();
    }

    public void cancelTask() {
        if (currentTask != null && currentTask.isNotCancelled()) {
            currentTask.cancel();
        }
    }

    private UpdateTask createTask() {
        return new UpdateTask(
                httpService,
                dirProvider,
                processRefStorage,
                viewController,
                launcherStarter
        );
    }
}
