package com.urlshortener.exception.service;

import java.util.Arrays;

public class UserValidationException extends RuntimeException {
    public UserValidationException(String validationError, String... params) {
        super(validationError + " Parameters: "+ Arrays.toString(params));
    }
}
