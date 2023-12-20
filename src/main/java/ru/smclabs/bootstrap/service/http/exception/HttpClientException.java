package ru.smclabs.bootstrap.service.http.exception;

import ru.smclabs.bootstrap.service.http.IHasMessageResponse;
import ru.smclabs.bootstrap.service.http.response.MessageResponse;

import java.time.LocalDateTime;

public class HttpClientException extends Exception implements IHasMessageResponse {

    public HttpClientException() {
        super();
    }

    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpClientException(Throwable cause) {
        super(cause);
    }

    public MessageResponse toMessageResponse() {
        MessageResponse response = new MessageResponse();
        response.setTime(LocalDateTime.now().toString());
        response.setType(this.getClass().getSimpleName());
        response.setMessage(this.getMessage());
        return response;
    }
}
