package com.example.performance;

import com.example.events.OrderCreatedEvent;
import com.example.events.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@EmbeddedKafka
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.streams.application-id=test-order-processing-streams"
})
public abstract class BasePerformanceTest {

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    protected static final int BATCH_SIZE = 100;
    protected static final long TIMEOUT_MS = 5000;

    @BeforeEach
    void setUp() {
        cleanupTestData();
    }

    protected abstract void cleanupTestData();

    protected OrderCreatedEvent createOrderEvent(int numItems) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("order-" + System.currentTimeMillis());
        event.setUserId("user-" + (System.currentTimeMillis() % 100));
        event.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            OrderItem item = new OrderItem();
            item.setProductId("product-" + (i + 1));
            item.setProductName("Product " + (i + 1));
            item.setQuantity(1);
            item.setPrice(new BigDecimal("10.00"));
            items.add(item);
        }
        event.setItems(items);
        event.setTotalAmount(new BigDecimal(items.size() * 10.00));

        return event;
    }

    protected void sendBatchAndWait(List<OrderCreatedEvent> events, String topic) throws Exception {
        for (OrderCreatedEvent event : events) {
            kafkaTemplate.send(topic, event.getOrderId(), event).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
    }

    protected long measureExecutionTime(Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        return System.currentTimeMillis() - startTime;
    }
} 