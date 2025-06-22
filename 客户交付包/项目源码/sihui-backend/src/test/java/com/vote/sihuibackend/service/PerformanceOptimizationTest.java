package com.vote.sihuibackend.service;

import com.vote.sihuibackend.service.impl.TextSearchServiceImpl;
import com.vote.sihuibackend.service.impl.EnhancedCacheServiceImpl;
import com.vote.sihuibackend.service.impl.AsyncDocumentProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能优化测试
 * 验证文本处理流程优化的效果
 * 
 * @author Sihui Team
 */
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class PerformanceOptimizationTest {

    /**
     * 测试缓存性能提升
     */
    @Test
    void testCachePerformanceImprovement() {
        // 模拟缓存命中率测试
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        // 第一次访问（缓存未命中）
        long startTime1 = System.currentTimeMillis();
        Map<String, Double> testVector = new HashMap<>();
        testVector.put("四会", 0.5);
        testVector.put("培训", 0.8);
        cacheService.cacheTfIdfVector(1L, testVector, 3600);
        long endTime1 = System.currentTimeMillis();

        // 第二次访问（缓存命中）
        long startTime2 = System.currentTimeMillis();
        Map<String, Double> cachedVector = cacheService.getTfIdfVector(1L);
        long endTime2 = System.currentTimeMillis();

        // 验证缓存命中
        assertNotNull(cachedVector);
        assertEquals(testVector, cachedVector);

        // 验证性能提升（缓存访问应该更快）
        long cacheTime = endTime2 - startTime2;
        long firstAccessTime = endTime1 - startTime1;

        System.out.println("首次访问耗时: " + firstAccessTime + "ms");
        System.out.println("缓存访问耗时: " + cacheTime + "ms");

        // 缓存访问应该比首次访问快（至少不会更慢）
        assertTrue(cacheTime <= firstAccessTime + 5); // 允许5ms误差
    }

    /**
     * 测试批量缓存操作性能
     */
    @Test
    void testBatchCacheOperations() {
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        // 准备测试数据
        Map<String, Double> idfValues = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            idfValues.put("term" + i, Math.random());
        }

        // 测试批量缓存
        long startTime = System.currentTimeMillis();
        cacheService.batchCacheIdfValues(idfValues, 3600);
        long endTime = System.currentTimeMillis();

        long batchTime = endTime - startTime;
        System.out.println("批量缓存1000个IDF值耗时: " + batchTime + "ms");

        // 验证缓存效果
        for (int i = 0; i < 10; i++) {
            String term = "term" + i;
            Double cachedValue = cacheService.getIdfValue(term);
            assertNotNull(cachedValue);
            assertEquals(idfValues.get(term), cachedValue);
        }

        // 批量操作应该在合理时间内完成（<100ms）
        assertTrue(batchTime < 100);
    }

    /**
     * 测试缓存统计功能
     */
    @Test
    void testCacheStatistics() {
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        // 进行一些缓存操作
        Map<String, Double> testVector = new HashMap<>();
        testVector.put("test", 1.0);
        cacheService.cacheTfIdfVector(1L, testVector, 3600);
        cacheService.cacheIdfValue("test", 0.5, 3600);

        // 命中缓存
        cacheService.getTfIdfVector(1L);
        cacheService.getIdfValue("test");

        // 未命中缓存
        cacheService.getTfIdfVector(999L);
        cacheService.getIdfValue("nonexistent");

        // 获取统计信息
        EnhancedCacheService.CacheStats stats = cacheService.getCacheStats();

        // 验证统计信息
        assertTrue(stats.getTfIdfVectorCacheSize() > 0);
        assertTrue(stats.getIdfValueCacheSize() > 0);
        assertTrue(stats.getTotalHits() > 0);
        assertTrue(stats.getTotalMisses() > 0);
        assertTrue(stats.getHitRate() > 0 && stats.getHitRate() <= 1.0);

        System.out.println("缓存统计信息:");
        System.out.println("TF-IDF向量缓存大小: " + stats.getTfIdfVectorCacheSize());
        System.out.println("IDF值缓存大小: " + stats.getIdfValueCacheSize());
        System.out.println("命中率: " + String.format("%.2f%%", stats.getHitRate() * 100));
        System.out.println("总命中次数: " + stats.getTotalHits());
        System.out.println("总未命中次数: " + stats.getTotalMisses());
    }

    /**
     * 测试缓存失效机制
     */
    @Test
    void testCacheInvalidation() {
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        // 缓存一些数据
        Map<String, Double> testVector = new HashMap<>();
        testVector.put("test", 1.0);
        cacheService.cacheTfIdfVector(1L, testVector, 3600);
        cacheService.cacheDocumentSimilarity(1L, 2L, 0.8, 3600);

        // 验证数据已缓存
        assertNotNull(cacheService.getTfIdfVector(1L));
        assertNotNull(cacheService.getDocumentSimilarity(1L, 2L));

        // 失效文档缓存
        cacheService.invalidateDocumentCache(1L);

        // 验证相关缓存已失效
        assertNull(cacheService.getTfIdfVector(1L));
        assertNull(cacheService.getDocumentSimilarity(1L, 2L));

        System.out.println("缓存失效机制测试通过");
    }

    /**
     * 测试TTL过期机制
     */
    @Test
    void testCacheTTLExpiration() throws InterruptedException {
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        // 缓存一个短TTL的数据（1秒）
        cacheService.cacheIdfValue("shortlived", 1.0, 1);

        // 立即访问应该成功
        assertNotNull(cacheService.getIdfValue("shortlived"));

        // 等待过期
        Thread.sleep(1100);

        // 过期后访问应该返回null
        assertNull(cacheService.getIdfValue("shortlived"));

        System.out.println("TTL过期机制测试通过");
    }

    /**
     * 性能基准测试
     */
    @Test
    void performanceBenchmark() {
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        int iterations = 10000;

        // 准备测试数据
        List<Map<String, Double>> testVectors = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            Map<String, Double> vector = new HashMap<>();
            vector.put("term" + i, Math.random());
            testVectors.add(vector);
        }

        // 测试缓存写入性能
        long writeStartTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            cacheService.cacheTfIdfVector((long) i, testVectors.get(i), 3600);
        }
        long writeEndTime = System.currentTimeMillis();
        long writeTime = writeEndTime - writeStartTime;

        // 测试缓存读取性能
        long readStartTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            cacheService.getTfIdfVector((long) i);
        }
        long readEndTime = System.currentTimeMillis();
        long readTime = readEndTime - readStartTime;

        System.out.println("性能基准测试结果:");
        System.out.println("写入" + iterations + "个向量耗时: " + writeTime + "ms");
        System.out.println("读取" + iterations + "个向量耗时: " + readTime + "ms");
        System.out.println("平均写入耗时: " + (double) writeTime / iterations + "ms/次");
        System.out.println("平均读取耗时: " + (double) readTime / iterations + "ms/次");

        // 性能要求：平均操作时间应该小于1ms
        assertTrue((double) writeTime / iterations < 1.0);
        assertTrue((double) readTime / iterations < 1.0);
    }

    /**
     * 并发访问测试
     */
    @Test
    void testConcurrentAccess() throws InterruptedException, ExecutionException {
        EnhancedCacheServiceImpl cacheService = new EnhancedCacheServiceImpl();

        int threadCount = 10;
        int operationsPerThread = 1000;

        // 预填充一些数据
        for (int i = 0; i < operationsPerThread; i++) {
            Map<String, Double> vector = new HashMap<>();
            vector.put("term" + i, Math.random());
            cacheService.cacheTfIdfVector((long) i, vector, 3600);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 启动多个线程并发访问缓存
        for (int t = 0; t < threadCount; t++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    // 混合读写操作
                    if (i % 2 == 0) {
                        cacheService.getTfIdfVector((long) (i % operationsPerThread));
                    } else {
                        Map<String, Double> vector = new HashMap<>();
                        vector.put("concurrent" + i, Math.random());
                        cacheService.cacheTfIdfVector((long) (operationsPerThread + i), vector, 3600);
                    }
                }
            });
            futures.add(future);
        }

        // 等待所有线程完成
        long startTime = System.currentTimeMillis();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        long endTime = System.currentTimeMillis();

        long totalTime = endTime - startTime;
        int totalOperations = threadCount * operationsPerThread;

        System.out.println("并发测试结果:");
        System.out.println("线程数: " + threadCount);
        System.out.println("每线程操作数: " + operationsPerThread);
        System.out.println("总操作数: " + totalOperations);
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均操作耗时: " + (double) totalTime / totalOperations + "ms/次");

        // 获取最终统计信息
        EnhancedCacheService.CacheStats stats = cacheService.getCacheStats();
        System.out.println("最终缓存命中率: " + String.format("%.2f%%", stats.getHitRate() * 100));

        // 并发操作应该在合理时间内完成
        assertTrue(totalTime < 10000); // 10秒内完成
        assertTrue(stats.getHitRate() > 0); // 应该有缓存命中
    }
}