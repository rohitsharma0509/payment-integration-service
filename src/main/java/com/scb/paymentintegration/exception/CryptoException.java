package com.scb.paymentintegration.exception;

public class CryptoException extends RuntimeException {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
