package ru.smclabs.bootstrap.service.http.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    @JsonProperty
    private String message;
    @JsonProperty
    private String type;
    @JsonProperty
    private int status;
    @JsonProperty
    private String time;

}
