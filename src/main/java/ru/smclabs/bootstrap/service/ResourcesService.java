package ru.smclabs.bootstrap.service;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.resource.ResourcesUpdateTask;
import ru.smclabs.slauncher.resources.exception.ResourceException;

import java.util.concurrent.locks.ReentrantLock;

public class ResourcesService extends AbstractService {

    public ResourcesService(Bootstrap bootstrap) {
        super(bootstrap);
        lock = new ReentrantLock();
    }

    private final ReentrantLock lock;

    private volatile ResourcesUpdateTask currentTask;

    public void createTask() {
        lock.lock();
        try {
            if (currentTask == null || currentTask.isCancelled()) {
                currentTask = new ResourcesUpdateTask(this,
                        getBootstrap().getGuiService().getPanelBackground().getPanelUpdate());
            } else {
                throw new ResourceException("Resource service is busy!");
            }

            currentTask.start();
        } finally {
            lock.unlock();
        }
    }

    public void cancelTask() {
        lock.lock();
        try {
            if (currentTask == null) return;
            currentTask.interrupt();
            currentTask = null;
        } finally {
            lock.unlock();
        }
    }
}
