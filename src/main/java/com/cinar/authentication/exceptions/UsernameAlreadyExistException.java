package com.cinar.authentication.exceptions;

public class UsernameAlreadyExistException extends RuntimeException {
    public UsernameAlreadyExistException(String message) {
        super(message);
    }
}
