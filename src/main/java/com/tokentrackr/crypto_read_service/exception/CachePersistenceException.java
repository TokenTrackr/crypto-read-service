package com.tokentrackr.crypto_read_service.exception;

public class CachePersistenceException extends RuntimeException {
    public CachePersistenceException(String message) {
        super(message);
    }

    public CachePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
