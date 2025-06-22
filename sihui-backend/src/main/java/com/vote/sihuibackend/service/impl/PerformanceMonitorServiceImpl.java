package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.service.PerformanceMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控服务实现
 */
@Slf4j
@Service
public class PerformanceMonitorServiceImpl implements PerformanceMonitorService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    // 性能统计数据存储
    private final Map<String, AtomicLong> executionTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> executionCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> slowQueries = new ConcurrentHashMap<>();

    @Override
    public void recordExecutionTime(String methodName, long executionTime) {
        executionTimes.computeIfAbsent(methodName, k -> new AtomicLong(0))
                .addAndGet(executionTime);
        executionCounts.computeIfAbsent(methodName, k -> new AtomicLong(0))
                .incrementAndGet();

        // 记录慢查询（超过1秒）
        if (executionTime > 1000) {
            slowQueries.put(methodName + "_" + System.currentTimeMillis(), executionTime);
            log.warn("Slow execution detected: {} took {}ms", methodName, executionTime);
        }
    }

    @Override
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Map<String, Object>> methodStats = new HashMap<>();

        for (String methodName : executionTimes.keySet()) {
            long totalTime = executionTimes.get(methodName).get();
            long count = executionCounts.get(methodName).get();
            double avgTime = count > 0 ? (double) totalTime / count : 0;

            Map<String, Object> methodStat = new HashMap<>();
            methodStat.put("totalExecutionTime", totalTime);
            methodStat.put("executionCount", count);
            methodStat.put("averageExecutionTime", avgTime);
            methodStats.put(methodName, methodStat);
        }

        stats.put("methodStats", methodStats);
        stats.put("totalMethods", methodStats.size());
        stats.put("collectionTime", new Date());
        return stats;
    }

    @Override
    public Map<String, Object> getSystemResourceUsage() {
        Map<String, Object> resourceUsage = new HashMap<>();

        // 内存使用情况
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

        Map<String, Object> memoryStats = new HashMap<>();
        memoryStats.put("heapUsed", heapUsed);
        memoryStats.put("heapMax", heapMax);
        memoryStats.put("heapUsagePercentage", (double) heapUsed / heapMax * 100);
        memoryStats.put("nonHeapUsed", nonHeapUsed);

        // CPU使用情况
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double systemLoadAverage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();

        Map<String, Object> cpuStats = new HashMap<>();
        cpuStats.put("systemLoadAverage", systemLoadAverage);
        cpuStats.put("availableProcessors", availableProcessors);
        cpuStats.put("loadPerProcessor", systemLoadAverage / availableProcessors);

        // 线程使用情况
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();

        Map<String, Object> threadStats = new HashMap<>();
        threadStats.put("currentThreadCount", threadCount);
        threadStats.put("peakThreadCount", peakThreadCount);

        resourceUsage.put("memory", memoryStats);
        resourceUsage.put("cpu", cpuStats);
        resourceUsage.put("threads", threadStats);
        resourceUsage.put("collectionTime", new Date());

        return resourceUsage;
    }

    @Override
    public Map<String, Object> getDatabaseConnectionPoolStatus() {
        Map<String, Object> dbStats = new HashMap<>();

        try {
            // 尝试获取连接来测试连接池状态
            Connection connection = dataSource.getConnection();
            dbStats.put("connectionAvailable", true);
            dbStats.put("connectionValid", connection.isValid(5));
            connection.close();
        } catch (SQLException e) {
            log.error("Database connection error", e);
            dbStats.put("connectionAvailable", false);
            dbStats.put("error", e.getMessage());
        }

        dbStats.put("dataSourceClass", dataSource.getClass().getSimpleName());
        dbStats.put("checkTime", new Date());

        return dbStats;
    }

    @Override
    public Map<String, Object> getCacheHitRateStats() {
        Map<String, Object> cacheStats = new HashMap<>();

        if (cacheManager != null) {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            Map<String, Object> cacheDetails = new HashMap<>();

            for (String cacheName : cacheNames) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", cacheName);
                cacheInfo.put("available", cacheManager.getCache(cacheName) != null);
                cacheDetails.put(cacheName, cacheInfo);
            }

            cacheStats.put("caches", cacheDetails);
            cacheStats.put("totalCaches", cacheNames.size());
        } else {
            cacheStats.put("cacheManagerAvailable", false);
        }

        cacheStats.put("checkTime", new Date());
        return cacheStats;
    }

    @Override
    public Map<String, Object> getThreadPoolStatus() {
        Map<String, Object> threadPoolStats = new HashMap<>();

        if (taskExecutor != null) {
            Map<String, Object> executorStats = new HashMap<>();
            executorStats.put("corePoolSize", taskExecutor.getCorePoolSize());
            executorStats.put("maxPoolSize", taskExecutor.getMaxPoolSize());
            executorStats.put("activeCount", taskExecutor.getActiveCount());
            executorStats.put("poolSize", taskExecutor.getPoolSize());
            executorStats.put("queueSize", taskExecutor.getThreadPoolExecutor().getQueue().size());
            executorStats.put("completedTaskCount", taskExecutor.getThreadPoolExecutor().getCompletedTaskCount());

            threadPoolStats.put("taskExecutor", executorStats);
        } else {
            threadPoolStats.put("taskExecutorAvailable", false);
        }

        threadPoolStats.put("checkTime", new Date());
        return threadPoolStats;
    }

    @Override
    public void clearPerformanceStats() {
        executionTimes.clear();
        executionCounts.clear();
        slowQueries.clear();
        log.info("Performance statistics cleared");
    }

    @Override
    public Map<String, Object> getSlowQueryLog() {
        Map<String, Object> slowQueryStats = new HashMap<>();

        // 获取最近的慢查询（最多100条）
        List<Map<String, Object>> recentSlowQueries = new ArrayList<>();
        slowQueries.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByKey().reversed())
                .limit(100)
                .forEach(entry -> {
                    Map<String, Object> queryInfo = new HashMap<>();
                    String[] parts = entry.getKey().split("_");
                    if (parts.length >= 2) {
                        queryInfo.put("method", parts[0]);
                        queryInfo.put("timestamp", new Date(Long.parseLong(parts[1])));
                    }
                    queryInfo.put("executionTime", entry.getValue());
                    recentSlowQueries.add(queryInfo);
                });

        slowQueryStats.put("slowQueries", recentSlowQueries);
        slowQueryStats.put("totalSlowQueries", slowQueries.size());
        slowQueryStats.put("collectionTime", new Date());

        return slowQueryStats;
    }
}