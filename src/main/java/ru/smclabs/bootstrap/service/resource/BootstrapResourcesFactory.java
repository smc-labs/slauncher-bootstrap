package ru.smclabs.bootstrap.service.resource;

import lombok.Getter;
import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.bootstrap.resources.dto.BootstrapResources;
import ru.smclabs.bootstrap.resources.model.LauncherResource;
import ru.smclabs.bootstrap.service.resource.dto.BootstrapResourceList;
import ru.smclabs.bootstrap.service.resource.type.ResourceLauncher;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.factory.ResourcesFactory;
import ru.smclabs.slauncher.resources.provider.DirProvider;
import ru.smclabs.slauncher.resources.type.Resource;
import ru.smclabs.slauncher.resources.type.ResourceCompressedRuntime;
import ru.smclabs.system.info.SystemInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BootstrapResourcesFactory implements ResourcesFactory {
    private final @Getter Path bootstrapDir;
    private final @Getter Path runtimeDir;
    private final @Getter DirProvider dirProvider;
    private final ResourcesBuild build;

    public BootstrapResourcesFactory() {
        dirProvider = Bootstrap.getInstance().getDirProvider();
        bootstrapDir = dirProvider.getPersistenceDir("bootstrap");
        runtimeDir = dirProvider.getPersistenceDir("runtime/bootstrap");
        build = new ResourcesBuild();
    }

    public ResourcesBuild buildModels(BootstrapResourceList models) {
        models.getFiles().stream().map(model -> {
            if (IS_LAUNCHER_MODEL.test(model)) {
                return new ResourceLauncher(this, model);
            }

            return new Resource(this, model);
        }).forEach(build::compile);

        models.getRuntime().stream()
                .filter(model -> model.getName().contains(SystemInfo.get().toString()))
                .map(model -> new ResourceCompressedRuntime(this, model, "bootstrap"))
                .forEach(build::compile);

        build.sort();
        return build;
    }

}
