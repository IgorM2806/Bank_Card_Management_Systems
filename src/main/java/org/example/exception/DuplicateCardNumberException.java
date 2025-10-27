package org.example.exception;

public class DuplicateCardNumberException extends RuntimeException{
    public DuplicateCardNumberException(String message) {
        super(message);
    }
}
