package ru.smclabs.bootstrap.update.resource.model;

import ru.smclabs.slauncher.model.resource.ResourceModel;

import java.util.List;

public record BootstrapResources(
        List<ResourceModel> files,
        List<ResourceModel> runtime
) {
}
