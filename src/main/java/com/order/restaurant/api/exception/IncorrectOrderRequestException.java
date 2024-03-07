package com.order.restaurant.api.exception;

public class IncorrectOrderRequestException extends Exception {
    public IncorrectOrderRequestException(String message) {
        super(message);
    }
}
