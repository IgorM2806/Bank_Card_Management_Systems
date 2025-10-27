package org.example.exception;

import java.io.Serial;

public class CardNotFoundException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public CardNotFoundException(String message) {
        super(message);
    }
}
