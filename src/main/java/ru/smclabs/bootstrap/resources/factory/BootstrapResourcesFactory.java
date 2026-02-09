package ru.smclabs.bootstrap.resources.factory;

import ru.smclabs.bootstrap.resources.dto.BootstrapResources;
import ru.smclabs.bootstrap.resources.model.LauncherResource;
import ru.smclabs.bootstrap.service.resource.ResourcesBuild;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.compressed.resource.ResourceCompressedRuntime;
import ru.smclabs.slauncher.resources.factory.ResourcesFactory;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BootstrapResourcesFactory implements ResourcesFactory {
    private final DirProvider dirProvider;
    private final Path bootstrapDir;
    private final Path runtimeDir;

    public BootstrapResourcesFactory(DirProvider dirProvider) {
        this.dirProvider = dirProvider;
        bootstrapDir = dirProvider.getPersistenceDir("bootstrap");
        runtimeDir = dirProvider.getPersistenceDir("runtime/bootstrap");
    }

    public List<Resource> build(BootstrapResources dto) {
        List<Resource> resources = new ArrayList<>();
        buildRuntime(dto, resources);
        buildFiles(dto, resources);
        return resources;
    }

    @Override
    public String prepareUrl(ResourceModel model) {
        return model.getUrl()
                .replace("%domain%", "%slauncher-backend%")
                + "?ver=" + model.getHash();
    }

    @Override
    public Path preparePath(ResourceModel model) {
        return Paths.get(model.getPath()
                .replace("%bootstrap-dir%", bootstrapDir.toString())
                .replace("%runtime-dir%", runtimeDir.toString()));
    }

    @Override
    public DirProvider getDirProvider() {
        return dirProvider;
    }

    private void buildRuntime(BootstrapResources dto, List<Resource> resources) {
        for (ResourceModel model : dto.getRuntime()) {
            resources.add(new ResourceCompressedRuntime(this, model, "bootstrap"));
        }
    }

    private void buildFiles(BootstrapResources dto, List<Resource> resources) {
        for (ResourceModel model : dto.getFiles()) {
            if (LauncherResource.matches(model)) {
                resources.add(new LauncherResource(this, model));
            } else {
                resources.add(new Resource(this, model));
            }
        }
    }
}
