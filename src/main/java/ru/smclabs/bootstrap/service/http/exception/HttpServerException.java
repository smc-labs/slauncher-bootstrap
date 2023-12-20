package ru.smclabs.bootstrap.service.http.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import ru.smclabs.bootstrap.service.http.HttpResponse;
import ru.smclabs.bootstrap.service.http.IHasMessageResponse;
import ru.smclabs.bootstrap.service.http.response.MessageResponse;
import ru.smclabs.jacksonpack.Jackson;

import java.time.LocalDateTime;

@Getter
public class HttpServerException extends Exception implements IHasMessageResponse {

    private final int statusCode;

    public HttpServerException(HttpResponse response, Throwable cause) {
        super(response.getResponseBody(), cause);
        this.statusCode = response.getResponseCode();
    }

    public HttpServerException(HttpResponse response) {
        super(response.getResponseBody());
        this.statusCode = response.getResponseCode();
    }

    @Override
    public MessageResponse toMessageResponse() {
        try {
            return Jackson.getMapper().readValue(this.getMessage(), MessageResponse.class);
        } catch (JsonProcessingException e) {
            MessageResponse response = new MessageResponse();
            response.setTime(LocalDateTime.now().toString());
            response.setType(this.getClass().getSimpleName());
            response.setStatus(this.getStatusCode());
            response.setMessage(this.getMessage());
            return response;
        }
    }
}
