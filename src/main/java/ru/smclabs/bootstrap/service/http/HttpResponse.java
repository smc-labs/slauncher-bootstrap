package ru.smclabs.bootstrap.service.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.smclabs.bootstrap.service.http.exception.HttpServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@RequiredArgsConstructor
public class HttpResponse {

    protected final HttpURLConnection connection;

    protected int responseCode;
    protected String responseBody;

    public void readStream() throws HttpServerException {
        try {
            this.responseCode = this.connection.getResponseCode();
            this.readStream(this.selectInputStream());
        } catch (IOException e) {
            if (this.connection.getErrorStream() != null) {
                try {
                    this.readStream(this.connection.getErrorStream());
                } catch (IOException ignored) {
                }
            }

            throw new HttpServerException(this, e);
        }
    }

    protected InputStream selectInputStream() throws IOException {
        return this.connection.getErrorStream() != null
                ? this.connection.getErrorStream()
                : this.connection.getInputStream();
    }

    protected void readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            this.responseBody = result.toString();
        }
    }

}
