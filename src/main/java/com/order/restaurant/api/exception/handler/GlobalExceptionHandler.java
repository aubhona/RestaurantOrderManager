package com.order.restaurant.api.exception.handler;

import com.order.restaurant.api.exception.AuthorizationException;
import com.order.restaurant.api.exception.CookingTimeException;
import com.order.restaurant.api.exception.IncorrectOrderRequestException;
import com.order.restaurant.api.exception.RegistrationException;
import com.order.restaurant.api.model.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.rmi.AccessException;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_MESSAGE = "An error has occurred";

    private ApiResponse getExceptionResponse(Exception exception, HttpStatus httpStatus) {
        ApiResponse response = new ApiResponse();
        response.setDescription(ERROR_MESSAGE);
        response.setExceptionMessage(exception.getMessage());
        response.setCode(httpStatus.toString());
        response.setExceptionName(exception.getClass().getName());

        return response;
    }

    @ExceptionHandler(org.springframework.expression.AccessException.class)
    public ResponseEntity<ApiResponse> handleAccessException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.UNAUTHORIZED),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse> handleAuthorizationException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.FORBIDDEN),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ApiResponse> handleRegistrationException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.FORBIDDEN),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(IncorrectOrderRequestException.class)
    public ResponseEntity<ApiResponse> handleOrderException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.NOT_ACCEPTABLE),
                HttpStatus.NOT_ACCEPTABLE
        );
    }

    @ExceptionHandler(CookingTimeException.class)
    public ResponseEntity<ApiResponse> handleCookingException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.REQUEST_TIMEOUT),
                HttpStatus.REQUEST_TIMEOUT
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleParamsException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception exception) {
        return new ResponseEntity<>(
                getExceptionResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
