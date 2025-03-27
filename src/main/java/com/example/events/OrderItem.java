package com.example.events;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
} 