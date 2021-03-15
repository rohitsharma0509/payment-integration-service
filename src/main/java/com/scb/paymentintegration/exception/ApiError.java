package com.scb.paymentintegration.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ApiError {

    private HttpStatus status;
    private String statusCode;
    private List<String> errors;
    private String message;
    Date timestamp = new Date(new Timestamp(System.currentTimeMillis()).getTime());

    public ApiError() {
        super();
    }

    public ApiError(final HttpStatus status, final String message, final List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiError(final String statusCode, final String message, final String error) {
        super();
        this.statusCode = statusCode;
        this.message = message;
        errors = Arrays.asList(error);
    }

    public ApiError(final HttpStatus status, final String message, final String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }

    public void setError(final String error) {
        errors = Arrays.asList(error);
    }

}