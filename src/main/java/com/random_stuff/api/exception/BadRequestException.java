package com.random_stuff.api.exception;
 
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}