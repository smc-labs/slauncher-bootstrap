package ru.smclabs.bootstrap.service.http;


import ru.smclabs.bootstrap.service.http.response.MessageResponse;

public interface IHasMessageResponse {

    MessageResponse toMessageResponse();

}
