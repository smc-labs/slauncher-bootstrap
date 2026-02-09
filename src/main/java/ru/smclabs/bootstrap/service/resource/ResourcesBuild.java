package ru.smclabs.bootstrap.service.resource;

import ru.smclabs.bootstrap.service.resource.download.ResourceDownloadTask;
import ru.smclabs.slauncher.resources.type.Resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ResourcesBuild {
    private final List<Resource> resources = new ArrayList<>();

    public void sort() {
        resources.sort(
                Comparator.comparingLong(Resource::getSize)
                        .reversed()
        );
    }

    public void compile(Resource resource) {
        resources.add(resource);
    }

    public List<ResourceDownloadTask> findInvalidResources() {
        return resources.stream()
                .filter(Resource::isInvalid)
                .map(ResourceDownloadTask::new)
                .collect(Collectors.toList());
    }

    public List<Resource> getResources() {
        return resources;
    }
}
