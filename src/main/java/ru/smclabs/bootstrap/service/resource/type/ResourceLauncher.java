package ru.smclabs.bootstrap.service.resource.type;

import ru.smclabs.resources.factory.IResourcesFactory;
import ru.smclabs.resources.model.ResourceModel;
import ru.smclabs.resources.type.Resource;

public class ResourceLauncher extends Resource {

    public ResourceLauncher(IResourcesFactory factory, ResourceModel model) {
        super(factory, model);
    }
}
