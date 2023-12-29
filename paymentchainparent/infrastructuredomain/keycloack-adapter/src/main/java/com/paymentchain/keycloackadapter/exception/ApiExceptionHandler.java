package com.paymentchain.keycloackadapter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.UnknownHostException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<StandardizedApiExceptionResponse> handleUnknownHostException(UnknownHostException ex) {
        StandardizedApiExceptionResponse response = new StandardizedApiExceptionResponse("Connection error","erorr-1024",ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandardizedApiExceptionResponse> handleBussinesRuleException(BusinessRuleException ex) {
        StandardizedApiExceptionResponse response = new StandardizedApiExceptionResponse("Validation error", ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }
}
