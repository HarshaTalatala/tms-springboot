package com.harsha.tms.exception;

public class LoadAlreadyBookedException extends RuntimeException {

    public LoadAlreadyBookedException(String message) {
        super(message);
    }

    public LoadAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }
}

