package com.example.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry registry;

    public void recordOrderCreated() {
        registry.counter("orders.created").increment();
    }

    public void recordOrderCompleted() {
        registry.counter("orders.completed").increment();
    }

    public void recordOrderCancelled() {
        registry.counter("orders.cancelled").increment();
    }

    public void recordInventoryCheck() {
        registry.counter("inventory.checks").increment();
    }

    public void recordPaymentProcessed() {
        registry.counter("payments.processed").increment();
    }

    public void recordPaymentFailed() {
        registry.counter("payments.failed").increment();
    }

    public void recordRetry(String operation) {
        registry.counter("retries", "operation", operation).increment();
    }

    public void recordDlqMessage(String topic) {
        registry.counter("dlq.messages", "topic", topic).increment();
    }
} 