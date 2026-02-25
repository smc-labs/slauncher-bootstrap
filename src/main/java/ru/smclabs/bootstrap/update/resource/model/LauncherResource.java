package ru.smclabs.bootstrap.update.resource.model;

import ru.smclabs.bootstrap.update.resource.BootstrapResourcesFactory;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.type.Resource;

public class LauncherResource extends Resource {
    public LauncherResource(BootstrapResourcesFactory factory, ResourceModel model) {
        super(factory, model);
    }

    public static boolean matches(ResourceModel model) {
        return model.getName().contains("slauncher-") && model.getName().endsWith(".jar");
    }
}
