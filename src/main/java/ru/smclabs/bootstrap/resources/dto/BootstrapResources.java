package ru.smclabs.bootstrap.resources.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.smclabs.slauncher.model.resource.ResourceModel;
import ru.smclabs.slauncher.resources.provider.DirProvider;

import java.util.ArrayList;
import java.util.List;

public class BootstrapResources {
    private final List<ResourceModel> files;
    private final List<ResourceModel> runtime;

    @JsonCreator
    public BootstrapResources(
            @JsonProperty("files") List<ResourceModel> files,
            @JsonProperty("runtime") List<ResourceModel> runtime
    ) {
        this.files = files;
        this.runtime = runtime;
    }

    public List<ResourceModel> getFiles() {
        return files;
    }

    public List<ResourceModel> getRuntime() {
        return runtime;
    }
}
