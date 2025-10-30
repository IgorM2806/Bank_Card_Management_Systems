package org.example.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private String message;
    private Integer code;

    public  MessageResponse(String message, Integer code) {
        this.message = message;
        this.code = code;
    }
}
