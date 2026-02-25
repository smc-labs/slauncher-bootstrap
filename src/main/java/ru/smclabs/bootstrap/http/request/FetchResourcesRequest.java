package ru.smclabs.bootstrap.http.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.smclabs.bootstrap.update.resource.model.BootstrapResources;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.http.request.HttpRequest;

public class FetchResourcesRequest extends HttpRequest<HttpService, BootstrapResources> {
    public FetchResourcesRequest(HttpService httpService) {
        super(
                httpService,
                "GET",
                "application/json",
                "%slauncher-backend%/bootstrap"
        );
    }

    public BootstrapResources execute() throws HttpServiceException, JsonProcessingException {
        return execute(BootstrapResources.class);
    }
}
