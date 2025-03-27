package com.example.service;

import com.example.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    public void processPayment(String orderId, BigDecimal amount) {
        try {
            // 模拟支付处理
            if (Math.random() < 0.1) { // 10% 的失败率
                throw new PaymentException("Payment processing failed");
            }
        } catch (Exception e) {
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }
} 