package ru.smclabs.bootstrap.service.resource.type;

import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.factory.ResourcesFactory;
import ru.smclabs.slauncher.resources.type.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ResourceLauncher extends Resource {

    public ResourceLauncher(ResourcesFactory factory, ResourceModel model) {
        super(factory, model);
    }

    public void removeOlderVersions() {
        try (Stream<Path> files = Files.find(this.path.getParent(), 1, (path, attributes) -> {
            if (attributes.isRegularFile()) {
                String fileName = path.getFileName().toString();
                return fileName.endsWith(".jar") && fileName.startsWith("slauncher") && !fileName.equals(this.name);
            }

            return false;
        })) {
            files.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    Bootstrap.getInstance().getLogger().error("Failed to remove older slauncher version!", e);
                }
            });
        } catch (IOException e) {
            Bootstrap.getInstance().getLogger().error("Failed to remove older slauncher version!", e);
        }
    }
}
