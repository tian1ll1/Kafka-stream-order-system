package com.example.service;

import com.example.domain.OrderState;
import com.example.domain.OrderStatus;
import com.example.events.OrderCreatedEvent;
import com.example.events.OrderItem;
import com.example.events.InventoryEvent;
import com.example.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private ReadOnlyKeyValueStore<String, OrderState> orderStore;

    public void init() {
        orderStore = Objects.requireNonNull(streamsBuilderFactoryBean.getKafkaStreams())
            .store("order-store", QueryableStoreTypes.keyValueStore());
    }

    public void createOrder(OrderCreatedEvent event) {
        // 发送订单创建事件
        kafkaTemplate.send("orders", event.getOrderId(), event);
    }

    public void handleInventoryChecked(String orderId, boolean available) {
        OrderState state = orderStore.get(orderId);
        if (state != null) {
            if (available) {
                state.setStatus(OrderStatus.INVENTORY_CHECKED.name());
                // 触发库存更新事件
                for (OrderItem item : state.getItems()) {
                    InventoryEvent inventoryEvent = new InventoryEvent();
                    inventoryEvent.setProductId(item.getProductId());
                    inventoryEvent.setQuantity(item.getQuantity());
                    inventoryEvent.setOperation("UPDATE");
                    inventoryEvent.setOrderId(orderId);
                    inventoryEvent.setTimestamp(LocalDateTime.now());
                    kafkaTemplate.send("inventory-events", item.getProductId(), inventoryEvent);
                }
            } else {
                state.setStatus(OrderStatus.FAILED.name());
            }
            state.setUpdatedAt(LocalDateTime.now());
            // 发送状态更新事件
            kafkaTemplate.send("orders", orderId, state);
        }
    }

    public void handlePaymentProcessed(String orderId, PaymentEvent paymentEvent) {
        OrderState state = orderStore.get(orderId);
        if (state != null) {
            state.setPaymentStatus(paymentEvent.getStatus());
            if ("COMPLETED".equals(paymentEvent.getStatus())) {
                state.setStatus(OrderStatus.COMPLETED.name());
            } else if ("FAILED".equals(paymentEvent.getStatus())) {
                state.setStatus(OrderStatus.PAYMENT_FAILED.name());
                // 触发库存恢复事件
                for (OrderItem item : state.getItems()) {
                    InventoryEvent inventoryEvent = new InventoryEvent();
                    inventoryEvent.setProductId(item.getProductId());
                    inventoryEvent.setQuantity(item.getQuantity());
                    inventoryEvent.setOperation("RESTORE");
                    inventoryEvent.setOrderId(orderId);
                    inventoryEvent.setTimestamp(LocalDateTime.now());
                    kafkaTemplate.send("inventory-events", item.getProductId(), inventoryEvent);
                }
            }
            state.setUpdatedAt(LocalDateTime.now());
            // 发送状态更新事件
            kafkaTemplate.send("orders", orderId, state);
        }
    }

    public void cancelOrder(String orderId) {
        OrderState state = orderStore.get(orderId);
        if (state != null && OrderStatus.CREATED.name().equals(state.getStatus())) {
            state.setStatus(OrderStatus.CANCELLED.name());
            state.setUpdatedAt(LocalDateTime.now());
            // 发送状态更新事件
            kafkaTemplate.send("orders", orderId, state);

            // 触发库存恢复事件
            for (OrderItem item : state.getItems()) {
                InventoryEvent inventoryEvent = new InventoryEvent();
                inventoryEvent.setProductId(item.getProductId());
                inventoryEvent.setQuantity(item.getQuantity());
                inventoryEvent.setOperation("RESTORE");
                inventoryEvent.setOrderId(orderId);
                inventoryEvent.setTimestamp(LocalDateTime.now());
                kafkaTemplate.send("inventory-events", item.getProductId(), inventoryEvent);
            }

            // 触发退款事件
            PaymentEvent refundEvent = new PaymentEvent();
            refundEvent.setOrderId(orderId);
            refundEvent.setAmount(state.getTotalAmount());
            refundEvent.setStatus("REFUNDED");
            refundEvent.setTimestamp(LocalDateTime.now());
            kafkaTemplate.send("payment-events", orderId, refundEvent);
        }
    }

    public OrderState getOrderStatus(String orderId) {
        return orderStore.get(orderId);
    }
} 