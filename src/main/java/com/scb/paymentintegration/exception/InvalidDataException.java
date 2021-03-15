package com.scb.paymentintegration.exception;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class InvalidDataException extends RuntimeException {
    private final List<String> messages;
    public InvalidDataException(String message) {
        this.messages = Arrays.asList(message);
    }
    public InvalidDataException(List<String> messages) {
        this.messages = messages;
    }
}
