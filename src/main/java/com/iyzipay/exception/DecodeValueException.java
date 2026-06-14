package com.iyzipay.exception;

public class DecodeValueException extends RuntimeException {

    public DecodeValueException(String message) {
        super(message);
    }

    public DecodeValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
