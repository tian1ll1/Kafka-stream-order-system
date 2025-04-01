package com.example.exception;

public class InventoryException extends RetryableException {
    public InventoryException(String message) {
        super(message, "inventory", 3, 1000L);
    }

    public InventoryException(String message, String operation, int maxRetries, long retryDelay) {
        super(message, operation, maxRetries, retryDelay);
    }
} 