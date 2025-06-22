package com.vote.sihuibackend.service;

import java.util.Map;
import java.util.Set;

/**
 * 增强缓存服务接口
 * 提供高性能的文档检索和TF-IDF计算缓存
 * 
 * @author Sihui Team
 */
public interface EnhancedCacheService {

    /**
     * 缓存文档TF-IDF向量
     * 
     * @param documentId 文档ID
     * @param vector     TF-IDF向量
     * @param ttlSeconds 过期时间（秒）
     */
    void cacheTfIdfVector(Long documentId, Map<String, Double> vector, long ttlSeconds);

    /**
     * 获取文档TF-IDF向量
     * 
     * @param documentId 文档ID
     * @return TF-IDF向量，如果不存在返回null
     */
    Map<String, Double> getTfIdfVector(Long documentId);

    /**
     * 缓存IDF值
     * 
     * @param term       词项
     * @param idfValue   IDF值
     * @param ttlSeconds 过期时间（秒）
     */
    void cacheIdfValue(String term, Double idfValue, long ttlSeconds);

    /**
     * 获取IDF值
     * 
     * @param term 词项
     * @return IDF值，如果不存在返回null
     */
    Double getIdfValue(String term);

    /**
     * 批量缓存IDF值
     * 
     * @param idfValues  IDF值映射
     * @param ttlSeconds 过期时间（秒）
     */
    void batchCacheIdfValues(Map<String, Double> idfValues, long ttlSeconds);

    /**
     * 缓存文档相似度
     * 
     * @param doc1Id     文档1 ID
     * @param doc2Id     文档2 ID
     * @param similarity 相似度值
     * @param ttlSeconds 过期时间（秒）
     */
    void cacheDocumentSimilarity(Long doc1Id, Long doc2Id, Double similarity, long ttlSeconds);

    /**
     * 获取文档相似度
     * 
     * @param doc1Id 文档1 ID
     * @param doc2Id 文档2 ID
     * @return 相似度值，如果不存在返回null
     */
    Double getDocumentSimilarity(Long doc1Id, Long doc2Id);

    /**
     * 缓存查询结果
     * 
     * @param queryHash  查询哈希
     * @param results    检索结果
     * @param ttlSeconds 过期时间（秒）
     */
    void cacheQueryResults(String queryHash, Object results, long ttlSeconds);

    /**
     * 获取查询结果
     * 
     * @param queryHash 查询哈希
     * @param clazz     结果类型
     * @return 检索结果，如果不存在返回null
     */
    <T> T getQueryResults(String queryHash, Class<T> clazz);

    /**
     * 缓存AI查询分析结果
     * 
     * @param query      原始查询
     * @param analysis   分析结果
     * @param ttlSeconds 过期时间（秒）
     */
    void cacheQueryAnalysis(String query, Object analysis, long ttlSeconds);

    /**
     * 获取AI查询分析结果
     * 
     * @param query 原始查询
     * @param clazz 结果类型
     * @return 分析结果，如果不存在返回null
     */
    <T> T getQueryAnalysis(String query, Class<T> clazz);

    /**
     * 失效文档相关缓存
     * 
     * @param documentId 文档ID
     */
    void invalidateDocumentCache(Long documentId);

    /**
     * 失效所有TF-IDF相关缓存
     */
    void invalidateAllTfIdfCache();

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    CacheStats getCacheStats();

    /**
     * 预热缓存
     * 
     * @param documentIds 需要预热的文档ID集合
     */
    void warmupCache(Set<Long> documentIds);

    /**
     * 缓存统计信息
     */
    class CacheStats {
        private long tfIdfVectorCacheSize;
        private long idfValueCacheSize;
        private long similarityCacheSize;
        private long queryResultsCacheSize;
        private long queryAnalysisCacheSize;
        private double hitRate;
        private long totalHits;
        private long totalMisses;

        public CacheStats(long tfIdfVectorCacheSize, long idfValueCacheSize, long similarityCacheSize,
                long queryResultsCacheSize, long queryAnalysisCacheSize, double hitRate,
                long totalHits, long totalMisses) {
            this.tfIdfVectorCacheSize = tfIdfVectorCacheSize;
            this.idfValueCacheSize = idfValueCacheSize;
            this.similarityCacheSize = similarityCacheSize;
            this.queryResultsCacheSize = queryResultsCacheSize;
            this.queryAnalysisCacheSize = queryAnalysisCacheSize;
            this.hitRate = hitRate;
            this.totalHits = totalHits;
            this.totalMisses = totalMisses;
        }

        // Getters and Setters
        public long getTfIdfVectorCacheSize() {
            return tfIdfVectorCacheSize;
        }

        public void setTfIdfVectorCacheSize(long tfIdfVectorCacheSize) {
            this.tfIdfVectorCacheSize = tfIdfVectorCacheSize;
        }

        public long getIdfValueCacheSize() {
            return idfValueCacheSize;
        }

        public void setIdfValueCacheSize(long idfValueCacheSize) {
            this.idfValueCacheSize = idfValueCacheSize;
        }

        public long getSimilarityCacheSize() {
            return similarityCacheSize;
        }

        public void setSimilarityCacheSize(long similarityCacheSize) {
            this.similarityCacheSize = similarityCacheSize;
        }

        public long getQueryResultsCacheSize() {
            return queryResultsCacheSize;
        }

        public void setQueryResultsCacheSize(long queryResultsCacheSize) {
            this.queryResultsCacheSize = queryResultsCacheSize;
        }

        public long getQueryAnalysisCacheSize() {
            return queryAnalysisCacheSize;
        }

        public void setQueryAnalysisCacheSize(long queryAnalysisCacheSize) {
            this.queryAnalysisCacheSize = queryAnalysisCacheSize;
        }

        public double getHitRate() {
            return hitRate;
        }

        public void setHitRate(double hitRate) {
            this.hitRate = hitRate;
        }

        public long getTotalHits() {
            return totalHits;
        }

        public void setTotalHits(long totalHits) {
            this.totalHits = totalHits;
        }

        public long getTotalMisses() {
            return totalMisses;
        }

        public void setTotalMisses(long totalMisses) {
            this.totalMisses = totalMisses;
        }
    }
}