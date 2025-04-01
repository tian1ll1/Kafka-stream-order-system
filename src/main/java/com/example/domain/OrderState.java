package com.example.domain;

import com.example.events.OrderItem;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderState {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private String status;
    private String paymentStatus;
    private String inventoryStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 