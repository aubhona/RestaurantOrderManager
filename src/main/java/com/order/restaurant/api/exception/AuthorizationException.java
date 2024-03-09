package com.order.restaurant.api.exception;

public class AuthorizationException extends Exception {
    public AuthorizationException(String message) {
        super(message);
    }
}
