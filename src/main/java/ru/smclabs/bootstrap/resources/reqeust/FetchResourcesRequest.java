package ru.smclabs.bootstrap.resources.reqeust;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.smclabs.bootstrap.resources.dto.BootstrapResources;
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
