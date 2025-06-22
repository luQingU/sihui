package com.vote.sihuibackend.service;

import java.util.Map;

/**
 * 性能监控服务接口
 */
public interface PerformanceMonitorService {

    /**
     * 记录方法执行时间
     */
    void recordExecutionTime(String methodName, long executionTime);

    /**
     * 获取性能统计信息
     */
    Map<String, Object> getPerformanceStats();

    /**
     * 获取系统资源使用情况
     */
    Map<String, Object> getSystemResourceUsage();

    /**
     * 获取数据库连接池状态
     */
    Map<String, Object> getDatabaseConnectionPoolStatus();

    /**
     * 获取缓存命中率统计
     */
    Map<String, Object> getCacheHitRateStats();

    /**
     * 获取线程池状态
     */
    Map<String, Object> getThreadPoolStatus();

    /**
     * 清理性能统计数据
     */
    void clearPerformanceStats();

    /**
     * 获取慢查询日志
     */
    Map<String, Object> getSlowQueryLog();
}