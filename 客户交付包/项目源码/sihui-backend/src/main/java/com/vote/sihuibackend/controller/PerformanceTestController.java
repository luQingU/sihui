package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.service.AdvancedCacheService;
import com.vote.sihuibackend.service.PerformanceMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 性能测试控制器
 * 用于负载测试和性能监控
 * 
 * @author Sihui Team
 */
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "性能测试", description = "负载测试和性能监控接口")
public class PerformanceTestController {

    private final AdvancedCacheService cacheService;
    private final PerformanceMonitorService performanceMonitorService;

    /**
     * 获取系统性能指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取系统性能指标")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // 缓存统计
            metrics.put("cache", cacheService.getCacheStatistics());

            // Redis连接信息
            metrics.put("redis", cacheService.getRedisInfo());

            // JVM内存信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memory = new HashMap<>();
            memory.put("totalMemory", runtime.totalMemory());
            memory.put("freeMemory", runtime.freeMemory());
            memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            memory.put("maxMemory", runtime.maxMemory());
            metrics.put("memory", memory);

            // 系统信息
            Map<String, Object> system = new HashMap<>();
            system.put("availableProcessors", runtime.availableProcessors());
            system.put("currentTimeMillis", System.currentTimeMillis());
            metrics.put("system", system);

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 负载测试 - CPU密集型任务
     */
    @PostMapping("/load-test/cpu")
    @Operation(summary = "CPU负载测试")
    public ResponseEntity<Map<String, Object>> cpuLoadTest(
            @RequestParam(defaultValue = "1000") int iterations,
            @RequestParam(defaultValue = "1") int threads) {

        log.info("开始CPU负载测试: iterations={}, threads={}", iterations, threads);
        long startTime = System.currentTimeMillis();

        try {
            CompletableFuture<Void>[] futures = new CompletableFuture[threads];

            for (int i = 0; i < threads; i++) {
                futures[i] = CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < iterations; j++) {
                        // CPU密集型计算
                        double result = 0;
                        for (int k = 0; k < 1000; k++) {
                            result += Math.sqrt(k) * Math.sin(k) * Math.cos(k);
                        }
                    }
                });
            }

            CompletableFuture.allOf(futures).join();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("testType", "CPU Load Test");
            result.put("iterations", iterations);
            result.put("threads", threads);
            result.put("duration", duration + "ms");
            result.put("averagePerIteration", (double) duration / iterations + "ms");

            log.info("CPU负载测试完成: {}ms", duration);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("CPU负载测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 负载测试 - 内存密集型任务
     */
    @PostMapping("/load-test/memory")
    @Operation(summary = "内存负载测试")
    public ResponseEntity<Map<String, Object>> memoryLoadTest(
            @RequestParam(defaultValue = "1000") int arraySize,
            @RequestParam(defaultValue = "100") int iterations) {

        log.info("开始内存负载测试: arraySize={}, iterations={}", arraySize, iterations);
        long startTime = System.currentTimeMillis();

        try {
            Runtime runtime = Runtime.getRuntime();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            for (int i = 0; i < iterations; i++) {
                // 创建大数组
                int[][] arrays = new int[arraySize][arraySize];

                // 填充随机数据
                for (int j = 0; j < arraySize; j++) {
                    for (int k = 0; k < arraySize; k++) {
                        arrays[j][k] = ThreadLocalRandom.current().nextInt();
                    }
                }

                // 强制垃圾回收
                if (i % 10 == 0) {
                    System.gc();
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();

            Map<String, Object> result = new HashMap<>();
            result.put("testType", "Memory Load Test");
            result.put("arraySize", arraySize);
            result.put("iterations", iterations);
            result.put("duration", duration + "ms");
            result.put("initialMemory", initialMemory);
            result.put("finalMemory", finalMemory);
            result.put("memoryIncrease", finalMemory - initialMemory);

            log.info("内存负载测试完成: {}ms", duration);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("内存负载测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 缓存性能测试
     */
    @PostMapping("/load-test/cache")
    @Operation(summary = "缓存性能测试")
    public ResponseEntity<Map<String, Object>> cacheLoadTest(
            @RequestParam(defaultValue = "1000") int operations) {

        log.info("开始缓存性能测试: operations={}", operations);
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> result = new HashMap<>();

            // 测试缓存写入性能
            long writeStartTime = System.currentTimeMillis();
            for (int i = 0; i < operations; i++) {
                String key = "test:key:" + i;
                String value = "test:value:" + i + ":" + System.currentTimeMillis();
                cacheService.recordCachePut("testCache");
            }
            long writeEndTime = System.currentTimeMillis();
            long writeDuration = writeEndTime - writeStartTime;

            // 测试缓存读取性能
            long readStartTime = System.currentTimeMillis();
            for (int i = 0; i < operations; i++) {
                String key = "test:key:" + i;
                // 模拟缓存读取
                if (i % 2 == 0) {
                    cacheService.recordCacheHit("testCache");
                } else {
                    cacheService.recordCacheMiss("testCache");
                }
            }
            long readEndTime = System.currentTimeMillis();
            long readDuration = readEndTime - readStartTime;

            long totalDuration = System.currentTimeMillis() - startTime;

            result.put("testType", "Cache Performance Test");
            result.put("operations", operations);
            result.put("totalDuration", totalDuration + "ms");
            result.put("writeDuration", writeDuration + "ms");
            result.put("readDuration", readDuration + "ms");
            result.put("writeOpsPerSecond", (double) operations / writeDuration * 1000);
            result.put("readOpsPerSecond", (double) operations / readDuration * 1000);
            result.put("cacheStats", cacheService.getCacheStatistics());

            log.info("缓存性能测试完成: {}ms", totalDuration);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("缓存性能测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 数据库连接池测试
     */
    @PostMapping("/load-test/database")
    @Operation(summary = "数据库连接池测试")
    public ResponseEntity<Map<String, Object>> databaseLoadTest(
            @RequestParam(defaultValue = "100") int connections) {

        log.info("开始数据库连接池测试: connections={}", connections);
        long startTime = System.currentTimeMillis();

        try {
            // 这里可以添加实际的数据库连接测试
            // 由于没有具体的数据库服务，这里只是模拟

            CompletableFuture<Void>[] futures = new CompletableFuture[connections];

            for (int i = 0; i < connections; i++) {
                final int index = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        // 模拟数据库查询延迟
                        Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                        log.debug("数据库连接测试 #{} 完成", index);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            CompletableFuture.allOf(futures).join();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("testType", "Database Connection Pool Test");
            result.put("connections", connections);
            result.put("duration", duration + "ms");
            result.put("averageConnectionTime", (double) duration / connections + "ms");
            result.put("connectionsPerSecond", (double) connections / duration * 1000);

            log.info("数据库连接池测试完成: {}ms", duration);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("数据库连接池测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清理缓存
     */
    @DeleteMapping("/cache/clear")
    @Operation(summary = "清理所有缓存")
    public ResponseEntity<Map<String, Object>> clearCache() {
        try {
            cacheService.evictAllCaches();

            Map<String, Object> result = new HashMap<>();
            result.put("message", "所有缓存已清理");
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 预热缓存
     */
    @PostMapping("/cache/warmup")
    @Operation(summary = "预热缓存")
    public ResponseEntity<Map<String, Object>> warmupCache() {
        try {
            cacheService.warmupCache();

            Map<String, Object> result = new HashMap<>();
            result.put("message", "缓存预热已启动");
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("缓存预热失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/test-health")
    @Operation(summary = "系统健康检查")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            // 检查内存使用率
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / runtime.maxMemory();

            health.put("status", memoryUsage < 0.9 ? "UP" : "WARNING");
            health.put("memoryUsage", String.format("%.2f%%", memoryUsage * 100));
            health.put("availableProcessors", runtime.availableProcessors());

            // 检查缓存状态
            try {
                Map<String, Object> cacheStats = cacheService.getCacheStatistics();
                health.put("cache", "UP");
                health.put("cacheSize", cacheStats.size());
            } catch (Exception e) {
                health.put("cache", "DOWN");
                health.put("cacheError", e.getMessage());
            }

            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }
}