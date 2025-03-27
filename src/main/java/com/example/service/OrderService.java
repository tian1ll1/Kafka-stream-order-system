package com.example.service;

import com.example.domain.OrderState;
import com.example.domain.OrderStatus;
import com.example.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final RetryService retryService;

    public OrderState createOrder(OrderCreatedEvent event) {
        OrderState orderState = new OrderState();
        orderState.setOrderId(event.getOrderId());
        orderState.setStatus(OrderStatus.CREATED);
        orderState.setItems(event.getItems());
        orderState.setTotalAmount(event.getTotalAmount());
        orderState.setCreatedAt(LocalDateTime.now());
        orderState.setUpdatedAt(LocalDateTime.now());

        // 检查库存
        try {
            inventoryService.checkInventory(event.getItems());
            orderState.setStatus(OrderStatus.INVENTORY_CHECKED);
        } catch (Exception e) {
            retryService.scheduleRetry(event, "inventory_check");
            throw e;
        }

        // 处理支付
        try {
            orderState.setStatus(OrderStatus.PAYMENT_PROCESSING);
            paymentService.processPayment(event.getOrderId(), event.getTotalAmount());
            orderState.setStatus(OrderStatus.PAID);
        } catch (Exception e) {
            retryService.scheduleRetry(event, "payment_processing");
            throw e;
        }

        orderState.setStatus(OrderStatus.COMPLETED);
        orderState.setUpdatedAt(LocalDateTime.now());
        return orderState;
    }

    public void cancelOrder(String orderId, String reason) {
        OrderState orderState = new OrderState();
        orderState.setOrderId(orderId);
        orderState.setStatus(OrderStatus.CANCELLING);
        orderState.setCancellationReason(reason);
        orderState.setUpdatedAt(LocalDateTime.now());

        try {
            // 执行取消操作
            orderState.setStatus(OrderStatus.CANCELLED);
            orderState.setCancelledAt(LocalDateTime.now());
        } catch (Exception e) {
            retryService.scheduleRetry(orderId, "order_cancellation");
            throw e;
        }
    }
} 