package com.vote.sihuibackend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT 性能监控组件
 * 监控 JWT 生成、验证等操作的性能指标
 */
@Slf4j
@Component
public class JwtPerformanceMonitor {

    private final AtomicLong tokenGenerationCount = new AtomicLong(0);
    private final AtomicLong tokenValidationCount = new AtomicLong(0);
    private final AtomicLong tokenValidationFailures = new AtomicLong(0);

    private final ConcurrentHashMap<String, Long> averageExecutionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();

    /**
     * 记录操作执行时间
     */
    public void recordExecutionTime(String operation, long executionTimeMs) {
        // 更新操作计数
        operationCounts.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();

        // 计算平均执行时间（简单移动平均）
        averageExecutionTimes.compute(operation, (key, currentAvg) -> {
            if (currentAvg == null) {
                return executionTimeMs;
            } else {
                // 使用加权平均，新值权重更高
                return Math.round(currentAvg * 0.7 + executionTimeMs * 0.3);
            }
        });

        // 记录慢操作
        if (executionTimeMs > 100) { // 超过100ms认为是慢操作
            log.warn("JWT慢操作检测 - 操作: {}, 执行时间: {}ms", operation, executionTimeMs);
        }
    }

    /**
     * 记录令牌生成
     */
    public void recordTokenGeneration() {
        tokenGenerationCount.incrementAndGet();
    }

    /**
     * 记录令牌验证
     */
    public void recordTokenValidation() {
        tokenValidationCount.incrementAndGet();
    }

    /**
     * 记录令牌验证失败
     */
    public void recordTokenValidationFailure() {
        tokenValidationFailures.incrementAndGet();
    }

    /**
     * 获取性能统计信息
     */
    public String getPerformanceStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== JWT 性能统计 ===\n");
        stats.append(String.format("令牌生成次数: %d\n", tokenGenerationCount.get()));
        stats.append(String.format("令牌验证次数: %d\n", tokenValidationCount.get()));
        stats.append(String.format("令牌验证失败次数: %d\n", tokenValidationFailures.get()));

        if (tokenValidationCount.get() > 0) {
            double failureRate = (double) tokenValidationFailures.get() / tokenValidationCount.get() * 100;
            stats.append(String.format("验证失败率: %.2f%%\n", failureRate));
        }

        stats.append("\n=== 操作执行时间统计 ===\n");
        averageExecutionTimes.forEach((operation, avgTime) -> {
            long count = operationCounts.getOrDefault(operation, new AtomicLong(0)).get();
            stats.append(String.format("%s: 平均 %dms (执行次数: %d)\n", operation, avgTime, count));
        });

        return stats.toString();
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        tokenGenerationCount.set(0);
        tokenValidationCount.set(0);
        tokenValidationFailures.set(0);
        averageExecutionTimes.clear();
        operationCounts.clear();
        log.info("JWT 性能统计已重置");
    }

    /**
     * 检查性能告警
     */
    public void checkPerformanceAlerts() {
        // 检查验证失败率
        if (tokenValidationCount.get() > 100) { // 至少有100次验证才进行检查
            double failureRate = (double) tokenValidationFailures.get() / tokenValidationCount.get() * 100;
            if (failureRate > 10.0) { // 失败率超过10%
                log.error("JWT验证失败率过高: {:.2f}% (阈值: 10%)", failureRate);
            }
        }

        // 检查平均响应时间
        averageExecutionTimes.forEach((operation, avgTime) -> {
            if (avgTime > 50) { // 平均超过50ms
                log.warn("JWT操作响应时间过长 - 操作: {}, 平均时间: {}ms (阈值: 50ms)", operation, avgTime);
            }
        });
    }

    /**
     * 记录缓存命中率（如果使用缓存）
     */
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total * 100 : 0.0;
    }

    /**
     * 获取缓存统计
     */
    public String getCacheStats() {
        return String.format("缓存命中率: %.2f%% (命中: %d, 未命中: %d)",
                getCacheHitRate(), cacheHits.get(), cacheMisses.get());
    }

    /**
     * 定期输出性能报告
     */
    public void printPerformanceReport() {
        log.info("\n{}", getPerformanceStats());
        log.info(getCacheStats());
        checkPerformanceAlerts();
    }
}