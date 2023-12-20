package ru.smclabs.bootstrap.service;

import lombok.Getter;
import ru.smclabs.bootstrap.environment.HttpEnvironment;
import ru.smclabs.bootstrap.service.http.exception.HttpClientException;
import ru.smclabs.bootstrap.util.logger.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Getter
public class HttpService {

    private final HttpEnvironment environment;
    private final Logger logger;

    public HttpService(HttpEnvironment environment, Logger logger) {
        this.environment = environment;
        this.logger = logger;
    }

    public URL createUrl(String path) throws HttpClientException {
        try {
            return new URL(this.environment.getProtocol() + path
                    .replace("%slauncher-backend%", this.environment.getHostname())
                    .replace(".ru/", this.environment.getZone() + "/")
            );
        } catch (MalformedURLException e) {
            throw new HttpClientException("Failed to create URL for path" + path, e);
        }
    }

    public HttpURLConnection openConnection(URL url, String method) throws HttpClientException {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", this.environment.getUserAgent());
            return connection;
        } catch (IOException e) {
            throw new HttpClientException("Failed to open connection!", e);
        }
    }

}
