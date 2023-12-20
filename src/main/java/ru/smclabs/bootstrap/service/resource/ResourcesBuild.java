package ru.smclabs.bootstrap.service.resource;

import lombok.Getter;
import ru.smclabs.bootstrap.service.resource.download.ResourceDownloadTask;
import ru.smclabs.resources.collection.ResourcesList;
import ru.smclabs.resources.type.Resource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ResourcesBuild {

    private final ResourcesList<Resource> resources = new ResourcesList<>();

    public void sort() {
        this.resources.sort(Comparator.comparingLong(Resource::getSize).reversed());
    }

    public void compile(Resource resource) {
        this.resources.add(resource);
    }

    public boolean isEmpty() {
        return this.resources.isEmpty();
    }

    public List<ResourceDownloadTask> findInvalidResources() {
        return this.resources.stream()
                .filter(Resource::isInvalid)
                .map(ResourceDownloadTask::new)
                .collect(Collectors.toList());
    }
}
