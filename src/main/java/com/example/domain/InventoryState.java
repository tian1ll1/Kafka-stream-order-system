package com.example.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryState {
    private String productId;
    private int quantity;
    private LocalDateTime lastUpdated;
    private String lastOrderId;
} 