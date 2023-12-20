package ru.smclabs.bootstrap.service.resource;

import lombok.Getter;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.resource.dto.BootstrapResourceList;
import ru.smclabs.bootstrap.service.resource.type.ResourceLauncher;
import ru.smclabs.bootstrap.util.SystemUtils;
import ru.smclabs.resources.factory.IResourcesFactory;
import ru.smclabs.resources.model.ResourceModel;
import ru.smclabs.resources.provider.DirProvider;
import ru.smclabs.resources.type.Resource;
import ru.smclabs.resources.type.ResourceCompressedRuntime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class ResourcesFactory implements IResourcesFactory {

    private final Predicate<ResourceModel> IS_LAUNCHER_MODEL = model ->
            model.getName().contains("slauncher-") && model.getName().endsWith(".jar");

    private final @Getter Path bootstrapDir;
    private final @Getter Path runtimeDir;
    private final @Getter DirProvider dirProvider;
    private final ResourcesBuild build;

    public ResourcesFactory() {
        this.dirProvider = Bootstrap.getInstance().getDirProvider();
        this.bootstrapDir = this.dirProvider.getPersistenceDir("bootstrap");
        this.runtimeDir = this.dirProvider.getPersistenceDir("runtime/bootstrap");
        this.build = new ResourcesBuild();
    }

    public ResourcesBuild buildModels(BootstrapResourceList models) {
        models.getFiles().stream().map(model -> {
            if (IS_LAUNCHER_MODEL.test(model)) {
                return new ResourceLauncher(this, model);
            }

            return new Resource(this, model);
        }).forEach(this.build::compile);

        models.getRuntime().stream()
                .filter(model -> model.getName().contains(SystemUtils.getId()))
                .map(model -> new ResourceCompressedRuntime(this, model, "bootstrap"))
                .forEach(this.build::compile);

        this.build.sort();
        return this.build;
    }

    @Override
    public String prepareUrl(ResourceModel model) {
        return model.getUrl().replace("%domain%", "%slauncher-backend%") + "?ver=" + model.getHash();
    }

    @Override
    public Path preparePath(ResourceModel model) {
        String filePath = model.getPath()
                .replace("%bootstrap-dir%", this.bootstrapDir.toString())
                .replace("%runtime-dir%", this.runtimeDir.toString());

        Path path = Paths.get(filePath);

        if (IS_LAUNCHER_MODEL.test(model)) {
            path = Paths.get(path.toString().replace(path.getFileName().toString(), "slauncher.jar"));
        }

        return path;
    }
}
