package ru.smclabs.bootstrap.service.resource.dto;

import lombok.Data;
import ru.smclabs.resources.model.ResourceModel;

import java.util.ArrayList;
import java.util.List;

@Data
public class BootstrapResourceList {

    private final List<ResourceModel> files = new ArrayList<>();
    private final List<ResourceModel> runtime = new ArrayList<>();

}
