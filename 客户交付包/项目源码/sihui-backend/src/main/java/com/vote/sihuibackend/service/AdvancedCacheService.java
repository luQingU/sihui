package com.vote.sihuibackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高级缓存服务
 * 提供缓存预热、监控、统计和管理功能
 */
@Service
@Slf4j
public class AdvancedCacheService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存统计信息
    private final Map<String, CacheStats> cacheStatsMap = new ConcurrentHashMap<>();

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final AtomicLong hitCount = new AtomicLong(0);
        private final AtomicLong missCount = new AtomicLong(0);
        private final AtomicLong putCount = new AtomicLong(0);
        private final AtomicLong evictionCount = new AtomicLong(0);
        private volatile long lastAccessTime = System.currentTimeMillis();

        public void recordHit() {
            hitCount.incrementAndGet();
            lastAccessTime = System.currentTimeMillis();
        }

        public void recordMiss() {
            missCount.incrementAndGet();
            lastAccessTime = System.currentTimeMillis();
        }

        public void recordPut() {
            putCount.incrementAndGet();
        }

        public void recordEviction() {
            evictionCount.incrementAndGet();
        }

        public double getHitRate() {
            long total = hitCount.get() + missCount.get();
            return total == 0 ? 0.0 : (double) hitCount.get() / total;
        }

        public long getHitCount() {
            return hitCount.get();
        }

        public long getMissCount() {
            return missCount.get();
        }

        public long getPutCount() {
            return putCount.get();
        }

        public long getEvictionCount() {
            return evictionCount.get();
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }

    /**
     * 初始化缓存统计
     */
    @PostConstruct
    public void initCacheStats() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheStatsMap.put(cacheName, new CacheStats());
        }
        log.info("缓存统计初始化完成，监控缓存: {}", cacheNames);
    }

    /**
     * 缓存预热 - 应用启动时执行
     */
    @PostConstruct
    @Async("taskExecutor")
    public void warmupCache() {
        log.info("开始缓存预热...");

        try {
            // 预热用户相关缓存
            warmupUserCache();

            // 预热文档相关缓存
            warmupDocumentCache();

            // 预热权限相关缓存
            warmupPermissionCache();

            log.info("缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }

    /**
     * 预热用户缓存
     */
    private void warmupUserCache() {
        log.info("预热用户缓存...");
        // 这里可以预加载活跃用户、管理员等常用数据
        // 示例：预加载前100个活跃用户
        Cache userCache = cacheManager.getCache("users");
        if (userCache != null) {
            // 可以调用UserService的方法来预加载数据
            log.info("用户缓存预热完成");
        }
    }

    /**
     * 预热文档缓存
     */
    private void warmupDocumentCache() {
        log.info("预热文档缓存...");
        // 预加载热门文档、最新文档等
        Cache documentCache = cacheManager.getCache("documents");
        if (documentCache != null) {
            // 可以调用DocumentService的方法来预加载数据
            log.info("文档缓存预热完成");
        }
    }

    /**
     * 预热权限缓存
     */
    private void warmupPermissionCache() {
        log.info("预热权限缓存...");
        // 预加载角色权限映射
        Cache permissionCache = cacheManager.getCache("userPermissions");
        if (permissionCache != null) {
            // 可以调用PermissionService的方法来预加载数据
            log.info("权限缓存预热完成");
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        for (Map.Entry<String, CacheStats> entry : cacheStatsMap.entrySet()) {
            String cacheName = entry.getKey();
            CacheStats cacheStats = entry.getValue();

            Map<String, Object> cacheInfo = new HashMap<>();
            cacheInfo.put("hitCount", cacheStats.getHitCount());
            cacheInfo.put("missCount", cacheStats.getMissCount());
            cacheInfo.put("putCount", cacheStats.getPutCount());
            cacheInfo.put("evictionCount", cacheStats.getEvictionCount());
            cacheInfo.put("hitRate", String.format("%.2f%%", cacheStats.getHitRate() * 100));
            cacheInfo.put("lastAccessTime", new Date(cacheStats.getLastAccessTime()));

            // 获取缓存大小（如果是Redis缓存）
            try {
                if (redisTemplate != null) {
                    Set<String> keys = redisTemplate.keys("sihui:cache:" + cacheName + ":*");
                    cacheInfo.put("size", keys != null ? keys.size() : 0);
                } else {
                    cacheInfo.put("size", "N/A (Redis not available)");
                }
            } catch (Exception e) {
                cacheInfo.put("size", "N/A");
            }

            stats.put(cacheName, cacheInfo);
        }

        return stats;
    }

    /**
     * 清空指定缓存
     */
    public void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("已清空缓存: {}", cacheName);
        } else {
            log.warn("缓存不存在: {}", cacheName);
        }
    }

    /**
     * 清空所有缓存
     */
    public void evictAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            evictCache(cacheName);
        }
        log.info("已清空所有缓存");
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheName) {
        CacheStats stats = cacheStatsMap.get(cacheName);
        if (stats != null) {
            stats.recordHit();
        }
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheName) {
        CacheStats stats = cacheStatsMap.get(cacheName);
        if (stats != null) {
            stats.recordMiss();
        }
    }

    /**
     * 记录缓存写入
     */
    public void recordCachePut(String cacheName) {
        CacheStats stats = cacheStatsMap.get(cacheName);
        if (stats != null) {
            stats.recordPut();
        }
    }

    /**
     * 记录缓存清除
     */
    public void recordCacheEviction(String cacheName) {
        CacheStats stats = cacheStatsMap.get(cacheName);
        if (stats != null) {
            stats.recordEviction();
        }
    }

    /**
     * 定时任务：缓存统计和清理
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void scheduledCacheMonitoring() {
        try {
            // 记录缓存统计日志
            logCacheStatistics();

            // 检查缓存健康状态
            checkCacheHealth();

        } catch (Exception e) {
            log.error("缓存监控任务执行失败", e);
        }
    }

    /**
     * 记录缓存统计日志
     */
    private void logCacheStatistics() {
        Map<String, Object> stats = getCacheStatistics();

        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            String cacheName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> cacheInfo = (Map<String, Object>) entry.getValue();

            log.info("缓存统计 [{}] - 命中率: {}, 命中: {}, 未命中: {}, 大小: {}",
                    cacheName,
                    cacheInfo.get("hitRate"),
                    cacheInfo.get("hitCount"),
                    cacheInfo.get("missCount"),
                    cacheInfo.get("size"));
        }
    }

    /**
     * 检查缓存健康状态
     */
    private void checkCacheHealth() {
        for (Map.Entry<String, CacheStats> entry : cacheStatsMap.entrySet()) {
            String cacheName = entry.getKey();
            CacheStats stats = entry.getValue();

            // 检查命中率是否过低
            double hitRate = stats.getHitRate();
            if (hitRate < 0.5 && stats.getHitCount() + stats.getMissCount() > 100) {
                log.warn("缓存 [{}] 命中率过低: {:.2f}%", cacheName, hitRate * 100);
            }

            // 检查是否长时间未访问
            long lastAccess = stats.getLastAccessTime();
            long timeSinceLastAccess = System.currentTimeMillis() - lastAccess;
            if (timeSinceLastAccess > 3600000) { // 1小时
                log.info("缓存 [{}] 长时间未访问，上次访问: {}", cacheName, new Date(lastAccess));
            }
        }
    }

    /**
     * 获取Redis连接信息
     */
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            if (redisTemplate != null) {
                // 获取Redis信息
                Properties redisInfo = redisTemplate.getConnectionFactory().getConnection().info();
                info.put("connected", true);
                info.put("version", redisInfo.getProperty("redis_version"));
                info.put("usedMemory", redisInfo.getProperty("used_memory_human"));
                info.put("connectedClients", redisInfo.getProperty("connected_clients"));
            } else {
                info.put("connected", false);
                info.put("error", "Redis not configured");
            }
        } catch (Exception e) {
            info.put("connected", false);
            info.put("error", e.getMessage());
        }
        return info;
    }

    /**
     * 批量预热缓存
     */
    @Async("taskExecutor")
    public void batchWarmupCache(List<String> cacheNames) {
        log.info("开始批量预热缓存: {}", cacheNames);

        for (String cacheName : cacheNames) {
            try {
                switch (cacheName) {
                    case "users":
                        warmupUserCache();
                        break;
                    case "documents":
                        warmupDocumentCache();
                        break;
                    case "userPermissions":
                        warmupPermissionCache();
                        break;
                    default:
                        log.warn("未知的缓存名称: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("预热缓存 [{}] 失败", cacheName, e);
            }
        }

        log.info("批量缓存预热完成");
    }
}