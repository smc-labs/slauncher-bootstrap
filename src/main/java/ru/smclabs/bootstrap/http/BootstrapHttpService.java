package ru.smclabs.bootstrap.http;

import ru.smclabs.bootstrap.core.app.Bootstrap;
import ru.smclabs.slauncher.http.HttpService;

public class BootstrapHttpService extends HttpService {
    public BootstrapHttpService(Bootstrap bootstrap) {
        super(bootstrap.getEnvironment().getHttp(), bootstrap.getLogger());
    }
}
