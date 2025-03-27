package com.example.performance;

import com.example.events.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EmbeddedKafka
public class OrderProcessingPerformanceTest extends BasePerformanceTest {

    @Override
    protected void cleanupTestData() {
        // 清理测试数据
    }

    @Test
    void testOrderCreationPerformance() throws Exception {
        List<OrderCreatedEvent> events = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            events.add(createOrderEvent(5)); // 每个订单5个商品
        }

        long executionTime = measureExecutionTime(() -> {
            try {
                sendBatchAndWait(events, "orders");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.printf("Processed %d orders in %d ms%n", BATCH_SIZE, executionTime);
    }

    @Test
    void testConcurrentOrderProcessing() throws Exception {
        int numThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    List<OrderCreatedEvent> events = new ArrayList<>();
                    for (int j = 0; j < BATCH_SIZE; j++) {
                        events.add(createOrderEvent(3)); // 每个订单3个商品
                    }
                    sendBatchAndWait(events, "orders");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }

    @Test
    void testOrderProcessingUnderLoad() throws Exception {
        int totalOrders = BATCH_SIZE * 2;
        int batchSize = 50;
        long totalTime = 0;

        for (int i = 0; i < totalOrders; i += batchSize) {
            List<OrderCreatedEvent> events = new ArrayList<>();
            for (int j = 0; j < Math.min(batchSize, totalOrders - i); j++) {
                events.add(createOrderEvent(4)); // 每个订单4个商品
            }

            long executionTime = measureExecutionTime(() -> {
                try {
                    sendBatchAndWait(events, "orders");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            totalTime += executionTime;
            System.out.printf("Processed batch of %d orders in %d ms%n", events.size(), executionTime);

            // 模拟真实负载下的短暂暂停
            Thread.sleep(100);
        }

        System.out.printf("Total processing time: %d ms%n", totalTime);
        System.out.printf("Average processing time per order: %.2f ms%n", 
            (double) totalTime / totalOrders);
    }
} 