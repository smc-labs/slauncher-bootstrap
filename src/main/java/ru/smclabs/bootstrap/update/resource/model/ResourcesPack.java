package ru.smclabs.bootstrap.update.resource.model;

import ru.smclabs.slauncher.resources.compressed.resource.ResourceCompressedRuntime;
import ru.smclabs.slauncher.resources.type.Resource;

import java.util.List;

public class ResourcesPack {
    private final List<Resource> resources;

    private LauncherResource launcher;
    private ResourceCompressedRuntime runtime;

    public ResourcesPack(List<Resource> resources) {
        this.resources = List.copyOf(resources);
        findRequiredResources();
    }

    public LauncherResource getLauncher() {
        return launcher;
    }

    public ResourceCompressedRuntime getRuntime() {
        return runtime;
    }

    public List<Resource> getResources() {
        return resources;
    }

    private void findRequiredResources() {
        for (Resource resource : resources) {
            if (resource instanceof LauncherResource) {
                launcher = (LauncherResource) resource;
            }

            if (resource instanceof ResourceCompressedRuntime) {
                runtime = (ResourceCompressedRuntime) resource;
            }
        }

        if (launcher == null) {
            throw new IllegalStateException("Launcher resource not found");
        }

        if (runtime == null) {
            throw new IllegalStateException("Runtime resource not found");
        }
    }
}
