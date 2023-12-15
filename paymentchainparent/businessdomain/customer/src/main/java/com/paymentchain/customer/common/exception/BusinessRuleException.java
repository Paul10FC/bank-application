package com.paymentchain.customer.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BusinessRuleException extends Exception{

    private long id;
    private String code;
    private HttpStatus httpStatus;

    public BusinessRuleException(long id, String code, String message, HttpStatus httpStatus){
        super(message);
        this.id = id;
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessRuleException(String code, String message, HttpStatus httpStatus){
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessRuleException(String message, Throwable cause){
        super(message, cause);
    }
}
