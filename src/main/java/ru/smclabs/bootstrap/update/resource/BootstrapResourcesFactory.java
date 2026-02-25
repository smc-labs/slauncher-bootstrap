package ru.smclabs.bootstrap.update.resource;

import ru.smclabs.bootstrap.update.resource.model.BootstrapResources;
import ru.smclabs.bootstrap.update.resource.model.LauncherResource;
import ru.smclabs.bootstrap.update.resource.model.ResourcesPack;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.compressed.resource.ResourceCompressedRuntime;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;
import ru.smclabs.system.info.arch.ArchType;
import ru.smclabs.system.info.os.OsType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BootstrapResourcesFactory implements ru.smclabs.slauncher.resources.factory.ResourcesFactory {
    private final DirProvider dirProvider;
    private final Path bootstrapDir;
    private final Path runtimeDir;

    public BootstrapResourcesFactory(DirProvider dirProvider) {
        this.dirProvider = dirProvider;
        bootstrapDir = dirProvider.getPersistenceDir("bootstrap");
        runtimeDir = dirProvider.getPersistenceDir("runtime/bootstrap");
    }

    public ResourcesPack build(BootstrapResources dto) {
        List<Resource> resources = new ArrayList<>();
        buildRuntime(dto, resources);
        buildFiles(dto, resources);
        return new ResourcesPack(resources);
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
        String os = OsType.current().id();
        String arch = ArchType.current().id();
        ResourceCompressedRuntime runtime = null;

        for (ResourceModel model : dto.runtime()) {
            if (model.getName().contains(os) && model.getName().contains(arch)) {
                runtime = new ResourceCompressedRuntime(this, model, "bootstrap");
                break;
            }
        }

        if (runtime == null) {
            throw new IllegalStateException("No runtime found for " + os + "/" + arch);
        }

        resources.add(runtime);
    }

    private void buildFiles(BootstrapResources dto, List<Resource> resources) {
        for (ResourceModel model : dto.files()) {
            if (LauncherResource.matches(model)) {
                resources.add(new LauncherResource(this, model));
            } else {
                resources.add(new Resource(this, model));
            }
        }
    }
}
