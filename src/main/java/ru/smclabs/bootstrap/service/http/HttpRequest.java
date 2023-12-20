package ru.smclabs.bootstrap.service.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import ru.smclabs.bootstrap.environment.HttpEnvironment;
import ru.smclabs.bootstrap.service.HttpService;
import ru.smclabs.bootstrap.service.http.exception.HttpClientException;
import ru.smclabs.bootstrap.service.http.exception.HttpServerException;
import ru.smclabs.bootstrap.util.logger.Logger;
import ru.smclabs.jacksonpack.Jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class HttpRequest<T> {

    protected final HttpService httpService;
    protected final String method;
    protected final String contentType;
    protected final String path;
    protected final Map<String, String> headers = new HashMap<>();

    protected String requestBody;
    protected URL url;

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setRequestBody(Object requestBody) throws JsonProcessingException {
        this.requestBody = Jackson.getMapper().writeValueAsString(requestBody);
    }

    public T execute(Class<T> responseClass) throws HttpClientException, HttpServerException, JsonProcessingException {
        HttpResponse response = this.sendRequestWithRetry();
        return responseClass == String.class
                ? responseClass.cast(response.getResponseBody())
                : Jackson.getMapper().readValue(response.getResponseBody(), responseClass);
    }

    protected HttpResponse sendRequestWithRetry() throws HttpClientException, HttpServerException {
        HttpEnvironment environment = this.httpService.getEnvironment();
        Logger logger = this.httpService.getLogger();

        try {
            return this.sendRequest(logger);
        } catch (HttpServerException | HttpClientException e) {
            logger.error("Failed to send request to " + this.url + " " + this.method, e);

            if (environment.isProtocolChanged() && environment.isZoneChanged()) {
                logger.info("Reset domain and protocol to default...");
                environment.changeProtocol("https");
                environment.changeZone("ru");
                throw e;
            } else if (!environment.isProtocolChanged()) {
                logger.info("Change working protocol to: HTTP");
                environment.changeProtocol("http");
                return this.sendRequest(logger);
            }

            logger.info("Change working protocol to: HTTPS");
            logger.info("Change working domain to: NET");
            environment.changeProtocol("https");
            environment.changeZone("net");

            return this.sendRequest(logger);
        }
    }

    protected HttpResponse sendRequest(Logger logger) throws HttpClientException, HttpServerException {
        HttpURLConnection connection = this.openConnection(this.prepareURL());
        this.writeRequestBody(connection);

        HttpResponse response = new HttpResponse(connection);
        response.readStream();

        if (response.getResponseCode() == 200) {
            return response;
        }

        throw new HttpServerException(response);
    }

    protected URL prepareURL() throws HttpClientException {
        this.url = this.httpService.createUrl(this.path);
        return this.url;
    }

    protected HttpURLConnection openConnection(URL url) throws HttpClientException {
        HttpURLConnection connection = this.httpService.openConnection(url, this.method);
        this.headers.forEach(connection::setRequestProperty);
        return connection;
    }

    protected void writeRequestBody(HttpURLConnection connection) throws HttpClientException {
        if (this.requestBody == null) return;
        if (!(this.method.equals("POST") || this.method.equals("PUT"))) {
            throw new HttpClientException("Post request body allowed only with POST method!");
        }

        byte[] bytes = this.requestBody.getBytes(StandardCharsets.UTF_8);

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", this.contentType + "; charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new HttpClientException("Failed to write request body!", e);
        }
    }
}
