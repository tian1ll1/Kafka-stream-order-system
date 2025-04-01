package com.example.service;

import com.example.events.InventoryEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final KeyValueStore<String, Integer> inventoryStore;

    public boolean checkInventory(String productId, int quantity) {
        Integer currentStock = inventoryStore.get(productId);
        if (currentStock == null) {
            currentStock = 100; // 默认库存
        }
        return currentStock >= quantity;
    }

    public void updateInventory(String productId, int quantity, String orderId) {
        InventoryEvent event = new InventoryEvent();
        event.setProductId(productId);
        event.setQuantity(quantity);
        event.setOperation("UPDATE");
        event.setOrderId(orderId);
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send("inventory-events", productId, event);
    }

    public void restoreInventory(String productId, int quantity, String orderId) {
        InventoryEvent event = new InventoryEvent();
        event.setProductId(productId);
        event.setQuantity(quantity);
        event.setOperation("RESTORE");
        event.setOrderId(orderId);
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send("inventory-events", productId, event);
    }

    public int getCurrentStock(String productId) {
        Integer currentStock = inventoryStore.get(productId);
        return currentStock != null ? currentStock : 100; // 默认库存
    }
} 