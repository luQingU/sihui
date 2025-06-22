package com.vote.sihuibackend.service;

import java.util.Map;

/**
 * 性能优化服务接口
 * 
 * @author Sihui System
 * @since 1.0.0
 */
public interface PerformanceOptimizationService {

    /**
     * 分析数据库查询性能
     * 
     * @return 性能分析结果
     */
    Map<String, Object> analyzeQueryPerformance();

    /**
     * 优化用户查询性能
     * 
     * @return 优化结果
     */
    Map<String, Object> optimizeUserQueries();

    /**
     * 分析和优化缓存策略
     * 
     * @return 缓存优化结果
     */
    Map<String, Object> optimizeCaching();

    /**
     * 审查代码最佳实践
     * 
     * @return 最佳实践审查结果
     */
    Map<String, Object> reviewBestPractices();

    /**
     * 性能基准测试
     * 
     * @return 基准测试结果
     */
    Map<String, Object> performBenchmarkTests();

    /**
     * 分析和优化内存使用
     * 
     * @return 内存优化结果
     */
    Map<String, Object> analyzeMemoryUsage();

    /**
     * 审查数据库索引
     * 
     * @return 索引审查结果
     */
    Map<String, Object> reviewDatabaseIndexes();

    /**
     * 分析API性能
     * 
     * @return API性能分析结果
     */
    Map<String, Object> analyzeApiPerformance();

    /**
     * 生成性能优化报告
     * 
     * @return 完整的性能优化报告
     */
    Map<String, Object> generateOptimizationReport();

    /**
     * 执行全面的性能审查
     * 
     * @return 全面性能审查结果
     */
    Map<String, Object> performComprehensiveReview();
}