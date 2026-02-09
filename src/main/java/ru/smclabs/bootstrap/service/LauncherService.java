package ru.smclabs.bootstrap.service;

import lombok.Getter;
import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.service.launcher.ProcessManager;

@Getter
public class LauncherService extends AbstractService {

    private final ProcessManager processManager;

    public LauncherService(Bootstrap bootstrap) {
        super(bootstrap);
        processManager = new ProcessManager(bootstrap.getLogger());
    }
}
