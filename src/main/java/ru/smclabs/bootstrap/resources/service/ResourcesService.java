package ru.smclabs.bootstrap.resources.service;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.smclabs.bootstrap.gui.core.GuiService;
import ru.smclabs.bootstrap.http.BootstrapHttpService;
import ru.smclabs.bootstrap.launcher.ProcessManager;
import ru.smclabs.bootstrap.resources.task.UpdateTask;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.util.concurrent.atomic.AtomicReference;

public class ResourcesService {
    private static final Logger log = LoggerFactory.getLogger(ResourcesService.class);

    private final DirProvider dirProvider;
    private final BootstrapHttpService httpService;
    private final GuiService guiService;
    private final ProcessManager processManager;
    private final AtomicReference<UpdateTask> currentTaskRef = new AtomicReference<>();

    public ResourcesService(
            DirProvider dirProvider,
            BootstrapHttpService httpService,
            GuiService guiService
    ) {
        this.dirProvider = dirProvider;
        this.httpService = httpService;
        this.guiService = guiService;
        this.processManager = new ProcessManager(dirProvider);
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
                processManager,
                guiService.getPanelBackground().getPanelUpdate()
        );
    }
}
