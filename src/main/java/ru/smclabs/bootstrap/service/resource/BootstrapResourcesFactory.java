package ru.smclabs.bootstrap.service.resource;

import lombok.Getter;
import ru.smclabs.bootstrap.Bootstrap;
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
import java.util.function.Predicate;

public class BootstrapResourcesFactory implements ResourcesFactory {

    private static final Predicate<ResourceModel> IS_LAUNCHER_MODEL = model ->
            model.getName().contains("slauncher-") && model.getName().endsWith(".jar");

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

    @Override
    public String prepareUrl(ResourceModel model) {
        return model.getUrl().replace("%domain%", "%slauncher-backend%") + "?ver=" + model.getHash();
    }

    @Override
    public Path preparePath(ResourceModel model) {
        return Paths.get(model.getPath()
                .replace("%bootstrap-dir%", bootstrapDir.toString())
                .replace("%runtime-dir%", runtimeDir.toString()));
    }
}
