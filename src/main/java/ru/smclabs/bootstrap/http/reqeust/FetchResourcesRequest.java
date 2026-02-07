package ru.smclabs.bootstrap.http.reqeust;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.smclabs.bootstrap.Bootstrap;
import ru.smclabs.bootstrap.service.resource.dto.BootstrapResourceList;
import ru.smclabs.slauncher.http.HttpService;
import ru.smclabs.slauncher.http.exception.HttpServiceException;
import ru.smclabs.slauncher.http.request.HttpRequest;

public class FetchResourcesRequest extends HttpRequest<HttpService, BootstrapResourceList> {
    public FetchResourcesRequest() {
        super(
                Bootstrap.getInstance().getHttpService(),
                "GET",
                "application/json",
                "%slauncher-backend%/bootstrap"
        );
    }

    public BootstrapResourceList execute() throws HttpServiceException, JsonProcessingException {
        return execute(BootstrapResourceList.class);
    }
}
