package com.tokentrackr.crypto_read_service.exception;

public class CryptoDataProcessingException extends RuntimeException {
    public CryptoDataProcessingException(String message) {
        super(message);
    }

    public CryptoDataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
