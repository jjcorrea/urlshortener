package com.urlshortener.exception.dal;

public class UserCreationException extends RuntimeException {
    public UserCreationException(String userId) {
        super("Unable to create user: "+userId);
    }
}
