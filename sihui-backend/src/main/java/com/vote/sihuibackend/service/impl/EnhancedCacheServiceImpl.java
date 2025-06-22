package com.vote.sihuibackend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.service.EnhancedCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强缓存服务实现
 * 基于内存的高性能缓存，支持TTL和LRU策略
 * 
 * @author Sihui Team
 */
@Service
@Slf4j
public class EnhancedCacheServiceImpl implements EnhancedCacheService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 缓存存储
    private final Map<String, CacheEntry> tfIdfVectorCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> idfValueCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> similarityCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> queryResultsCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> queryAnalysisCache = new ConcurrentHashMap<>();

    // 统计信息
    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);

    // 缓存配置
    private static final long DEFAULT_TTL_SECONDS = 3600; // 1小时
    private static final int MAX_CACHE_SIZE = 10000; // 最大缓存条目数

    @Override
    public void cacheTfIdfVector(Long documentId, Map<String, Double> vector, long ttlSeconds) {
        String key = "tfidf:doc:" + documentId;
        CacheEntry entry = new CacheEntry(vector, System.currentTimeMillis() + ttlSeconds * 1000);
        tfIdfVectorCache.put(key, entry);

        // 检查缓存大小并清理
        cleanupCacheIfNeeded(tfIdfVectorCache);

        log.debug("缓存TF-IDF向量: 文档ID={}, 向量大小={}", documentId, vector.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> getTfIdfVector(Long documentId) {
        String key = "tfidf:doc:" + documentId;
        CacheEntry entry = tfIdfVectorCache.get(key);

        if (entry == null || entry.isExpired()) {
            totalMisses.incrementAndGet();
            if (entry != null) {
                tfIdfVectorCache.remove(key);
            }
            return null;
        }

        totalHits.incrementAndGet();
        entry.updateAccessTime();
        return (Map<String, Double>) entry.getValue();
    }

    @Override
    public void cacheIdfValue(String term, Double idfValue, long ttlSeconds) {
        String key = "idf:" + term;
        CacheEntry entry = new CacheEntry(idfValue, System.currentTimeMillis() + ttlSeconds * 1000);
        idfValueCache.put(key, entry);

        cleanupCacheIfNeeded(idfValueCache);

        log.debug("缓存IDF值: 词项={}, 值={}", term, idfValue);
    }

    @Override
    public Double getIdfValue(String term) {
        String key = "idf:" + term;
        CacheEntry entry = idfValueCache.get(key);

        if (entry == null || entry.isExpired()) {
            totalMisses.incrementAndGet();
            if (entry != null) {
                idfValueCache.remove(key);
            }
            return null;
        }

        totalHits.incrementAndGet();
        entry.updateAccessTime();
        return (Double) entry.getValue();
    }

    @Override
    public void batchCacheIdfValues(Map<String, Double> idfValues, long ttlSeconds) {
        long expirationTime = System.currentTimeMillis() + ttlSeconds * 1000;

        for (Map.Entry<String, Double> entry : idfValues.entrySet()) {
            String key = "idf:" + entry.getKey();
            CacheEntry cacheEntry = new CacheEntry(entry.getValue(), expirationTime);
            idfValueCache.put(key, cacheEntry);
        }

        cleanupCacheIfNeeded(idfValueCache);

        log.info("批量缓存IDF值: {} 个词项", idfValues.size());
    }

    @Override
    public void cacheDocumentSimilarity(Long doc1Id, Long doc2Id, Double similarity, long ttlSeconds) {
        String key = buildSimilarityKey(doc1Id, doc2Id);
        CacheEntry entry = new CacheEntry(similarity, System.currentTimeMillis() + ttlSeconds * 1000);
        similarityCache.put(key, entry);

        cleanupCacheIfNeeded(similarityCache);

        log.debug("缓存文档相似度: {}↔{} = {}", doc1Id, doc2Id, similarity);
    }

    @Override
    public Double getDocumentSimilarity(Long doc1Id, Long doc2Id) {
        String key = buildSimilarityKey(doc1Id, doc2Id);
        CacheEntry entry = similarityCache.get(key);

        if (entry == null || entry.isExpired()) {
            totalMisses.incrementAndGet();
            if (entry != null) {
                similarityCache.remove(key);
            }
            return null;
        }

        totalHits.incrementAndGet();
        entry.updateAccessTime();
        return (Double) entry.getValue();
    }

    @Override
    public void cacheQueryResults(String queryHash, Object results, long ttlSeconds) {
        String key = "query:" + queryHash;
        CacheEntry entry = new CacheEntry(results, System.currentTimeMillis() + ttlSeconds * 1000);
        queryResultsCache.put(key, entry);

        cleanupCacheIfNeeded(queryResultsCache);

        log.debug("缓存查询结果: 哈希={}", queryHash);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getQueryResults(String queryHash, Class<T> clazz) {
        String key = "query:" + queryHash;
        CacheEntry entry = queryResultsCache.get(key);

        if (entry == null || entry.isExpired()) {
            totalMisses.incrementAndGet();
            if (entry != null) {
                queryResultsCache.remove(key);
            }
            return null;
        }

        totalHits.incrementAndGet();
        entry.updateAccessTime();

        try {
            Object value = entry.getValue();
            if (clazz.isInstance(value)) {
                return (T) value;
            } else {
                // 尝试JSON转换
                String json = objectMapper.writeValueAsString(value);
                return objectMapper.readValue(json, clazz);
            }
        } catch (Exception e) {
            log.error("缓存结果类型转换失败", e);
            return null;
        }
    }

    @Override
    public void cacheQueryAnalysis(String query, Object analysis, long ttlSeconds) {
        String key = "analysis:" + query.hashCode();
        CacheEntry entry = new CacheEntry(analysis, System.currentTimeMillis() + ttlSeconds * 1000);
        queryAnalysisCache.put(key, entry);

        cleanupCacheIfNeeded(queryAnalysisCache);

        log.debug("缓存查询分析: 查询={}", query);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getQueryAnalysis(String query, Class<T> clazz) {
        String key = "analysis:" + query.hashCode();
        CacheEntry entry = queryAnalysisCache.get(key);

        if (entry == null || entry.isExpired()) {
            totalMisses.incrementAndGet();
            if (entry != null) {
                queryAnalysisCache.remove(key);
            }
            return null;
        }

        totalHits.incrementAndGet();
        entry.updateAccessTime();

        try {
            Object value = entry.getValue();
            if (clazz.isInstance(value)) {
                return (T) value;
            } else {
                // 尝试JSON转换
                String json = objectMapper.writeValueAsString(value);
                return objectMapper.readValue(json, clazz);
            }
        } catch (Exception e) {
            log.error("缓存分析结果类型转换失败", e);
            return null;
        }
    }

    @Override
    public void invalidateDocumentCache(Long documentId) {
        // 清除文档相关的所有缓存
        String tfIdfKey = "tfidf:doc:" + documentId;
        tfIdfVectorCache.remove(tfIdfKey);

        // 清除相似度缓存中涉及该文档的条目
        Set<String> keysToRemove = new HashSet<>();
        for (String key : similarityCache.keySet()) {
            if (key.contains(":" + documentId + ":") || key.endsWith(":" + documentId)) {
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(similarityCache::remove);

        log.info("清除文档缓存: 文档ID={}", documentId);
    }

    @Override
    public void invalidateAllTfIdfCache() {
        tfIdfVectorCache.clear();
        idfValueCache.clear();
        similarityCache.clear();

        log.info("清除所有TF-IDF相关缓存");
    }

    @Override
    public CacheStats getCacheStats() {
        long totalRequests = totalHits.get() + totalMisses.get();
        double hitRate = totalRequests > 0 ? (double) totalHits.get() / totalRequests : 0.0;

        return new CacheStats(
                tfIdfVectorCache.size(),
                idfValueCache.size(),
                similarityCache.size(),
                queryResultsCache.size(),
                queryAnalysisCache.size(),
                hitRate,
                totalHits.get(),
                totalMisses.get());
    }

    @Override
    public void warmupCache(Set<Long> documentIds) {
        log.info("开始预热缓存: {} 个文档", documentIds.size());

        // 这里可以预加载常用的TF-IDF向量
        // 由于需要依赖TextSearchService，这里先预留接口
        // 实际实现时可以注入TextSearchService并调用相关方法

        log.info("缓存预热完成");
    }

    /**
     * 构建相似度缓存键
     */
    private String buildSimilarityKey(Long doc1Id, Long doc2Id) {
        // 确保键的一致性，较小的ID在前
        if (doc1Id.compareTo(doc2Id) <= 0) {
            return "sim:" + doc1Id + ":" + doc2Id;
        } else {
            return "sim:" + doc2Id + ":" + doc1Id;
        }
    }

    /**
     * 清理过期和超量缓存
     */
    private void cleanupCacheIfNeeded(Map<String, CacheEntry> cache) {
        if (cache.size() <= MAX_CACHE_SIZE) {
            return;
        }

        // 移除过期条目
        Set<String> expiredKeys = new HashSet<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredKeys.add(entry.getKey());
            }
        }
        expiredKeys.forEach(cache::remove);

        // 如果还是超量，移除最久未访问的条目
        if (cache.size() > MAX_CACHE_SIZE) {
            List<Map.Entry<String, CacheEntry>> entries = new ArrayList<>(cache.entrySet());
            entries.sort(
                    (e1, e2) -> Long.compare(e1.getValue().getLastAccessTime(), e2.getValue().getLastAccessTime()));

            int toRemove = cache.size() - MAX_CACHE_SIZE + 100; // 多删除一些，避免频繁清理
            for (int i = 0; i < toRemove && i < entries.size(); i++) {
                cache.remove(entries.get(i).getKey());
            }
        }
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;
        private volatile long lastAccessTime;

        public CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}