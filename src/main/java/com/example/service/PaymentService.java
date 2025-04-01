package com.example.service;

import com.example.events.PaymentEvent;
import com.example.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final KeyValueStore<String, PaymentEvent> paymentStore;
    private final Random random = new Random();

    public void processPayment(String orderId, BigDecimal amount) {
        // 模拟支付处理
        if (random.nextDouble() < 0.1) { // 10% 的失败率
            PaymentEvent failedEvent = new PaymentEvent();
            failedEvent.setOrderId(orderId);
            failedEvent.setAmount(amount);
            failedEvent.setStatus("FAILED");
            failedEvent.setErrorMessage("Payment processing failed");
            failedEvent.setTimestamp(LocalDateTime.now());
            
            kafkaTemplate.send("payment-events", orderId, failedEvent);
            throw new PaymentException("Payment processing failed");
        }

        PaymentEvent successEvent = new PaymentEvent();
        successEvent.setOrderId(orderId);
        successEvent.setAmount(amount);
        successEvent.setStatus("COMPLETED");
        successEvent.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send("payment-events", orderId, successEvent);
    }

    public void refundPayment(String orderId, BigDecimal amount) {
        PaymentEvent refundEvent = new PaymentEvent();
        refundEvent.setOrderId(orderId);
        refundEvent.setAmount(amount);
        refundEvent.setStatus("REFUNDED");
        refundEvent.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send("payment-events", orderId, refundEvent);
    }

    public PaymentEvent getPaymentStatus(String orderId) {
        return paymentStore.get(orderId);
    }
} 