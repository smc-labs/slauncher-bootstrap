package ru.smclabs.bootstrap.http;

import ru.smclabs.bootstrap.core.Bootstrap;

public class BootstrapHttpService extends ru.smclabs.slauncher.http.HttpService {
    public BootstrapHttpService(Bootstrap bootstrap) {
        super(bootstrap.getEnvironment().getHttp());
    }
}
