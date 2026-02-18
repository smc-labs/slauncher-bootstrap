package ru.smclabs.bootstrap.resources.factory;

import ru.smclabs.bootstrap.resources.dto.BootstrapResources;
import ru.smclabs.bootstrap.resources.model.LauncherResource;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.compressed.resource.ResourceCompressedRuntime;
import ru.smclabs.slauncher.resources.factory.ResourcesFactory;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;
import ru.smclabs.system.info.arch.ArchType;
import ru.smclabs.system.info.os.OsType;

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

        for (ResourceModel model : dto.getRuntime()) {
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
        for (ResourceModel model : dto.getFiles()) {
            if (LauncherResource.matches(model)) {
                resources.add(new LauncherResource(this, model));
            } else {
                resources.add(new Resource(this, model));
            }
        }
    }
}
