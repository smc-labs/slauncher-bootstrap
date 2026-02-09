package ru.smclabs.bootstrap.resources;

import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.resources.update.UpdateTask;

public class ResourcesManager {
    private final Bootstrap bootstrap;
    private UpdateTask task;

    public ResourcesManager(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void start() {
        if (task == null) {
            task = new UpdateTask(bootstrap.getLogger(), bootstrap
                    .getHttpService(), bootstrap.getContext().getDirProvider(),
                    bootstrap.getGuiService().getPanelBackground().get
                    );
        }
    }
}
