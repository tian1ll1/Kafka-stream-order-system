package com.example.exception;

public class RetryableException extends RuntimeException {
    private final String operation;
    private final int maxRetries;
    private final long retryDelay;

    public RetryableException(String message, String operation, int maxRetries, long retryDelay) {
        super(message);
        this.operation = operation;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    public String getOperation() {
        return operation;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelay() {
        return retryDelay;
    }
} 