package ru.smclabs.bootstrap.service;

import lombok.Getter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.resource.ResourcesUpdateTask;
import ru.smclabs.resources.ResourcesFinder;
import ru.smclabs.resources.exception.ResourceException;

import java.util.concurrent.locks.ReentrantLock;

public class ResourcesService extends AbstractService {

    public ResourcesService(Bootstrap bootstrap) {
        super(bootstrap);
        this.lock = new ReentrantLock();
        this.finder = new ResourcesFinder(bootstrap.getDirProvider());
    }

    private final ReentrantLock lock;
    private final @Getter ResourcesFinder finder;

    private volatile ResourcesUpdateTask currentTask;

    public void createTask() {
        this.lock.lock();
        try {
            if (this.currentTask == null || this.currentTask.isCancelled()) {
                this.currentTask = new ResourcesUpdateTask(this,
                        this.getBootstrap().getGuiService().getPanelBackground().getPanelUpdate());
            } else {
                throw new ResourceException("Resource service is busy!");
            }

            this.currentTask.start();
        } finally {
            this.lock.unlock();
        }
    }

    public void cancelTask() {
        this.lock.lock();
        try {
            if (this.currentTask == null) return;
            this.currentTask.interrupt();
            this.currentTask = null;
        } finally {
            this.lock.unlock();
        }
    }
}
