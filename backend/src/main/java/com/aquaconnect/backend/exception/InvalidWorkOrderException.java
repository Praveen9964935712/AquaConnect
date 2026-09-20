package com.aquaconnect.backend.exception;

public class InvalidWorkOrderException extends RuntimeException {

    public InvalidWorkOrderException(String message) {
        super(message);
    }
}
