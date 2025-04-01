package com.example.exception;

public class PaymentException extends RetryableException {
    public PaymentException(String message) {
        super(message, "payment", 3, 1000L);
    }

    public PaymentException(String message, String operation, int maxRetries, long retryDelay) {
        super(message, operation, maxRetries, retryDelay);
    }
} 