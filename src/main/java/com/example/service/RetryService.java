package com.example.service;

import com.example.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RetryService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MetricsService metricsService;

    public void scheduleRetry(OrderCreatedEvent event, String operation) {
        String retryTopic = "retry-" + operation;
        kafkaTemplate.send(retryTopic, event.getOrderId(), event);
        metricsService.recordRetry(operation);
    }

    public void scheduleRetry(String orderId, String operation) {
        String retryTopic = "retry-" + operation;
        kafkaTemplate.send(retryTopic, orderId, orderId);
        metricsService.recordRetry(operation);
    }
} 