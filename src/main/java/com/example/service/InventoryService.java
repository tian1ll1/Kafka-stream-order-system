package com.example.service;

import com.example.events.OrderItem;
import com.example.exception.InventoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    public void checkInventory(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (!isAvailable(item.getProductId(), item.getQuantity())) {
                throw new InventoryException(
                    String.format("Insufficient inventory for product %s", item.getProductId())
                );
            }
        }
    }

    private boolean isAvailable(String productId, int quantity) {
        // 模拟库存检查
        return true;
    }
} 