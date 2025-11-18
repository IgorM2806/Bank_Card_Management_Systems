package org.example.exception;

public class CustomBusinessException extends  RuntimeException {
    public CustomBusinessException(String message) {
        super(message);
    }
}
