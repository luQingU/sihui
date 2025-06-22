package com.vote.sihuibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 性能优化配置类
 */
@Configuration
@EnableCaching
@EnableAsync
public class PerformanceConfig {

    @Value("${spring.cache.type:simple}")
    private String cacheType;

    /**
     * 配置内存缓存管理器（默认/备用）
     */
    @Bean("memoryCacheManager")
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager memoryCacheManager() {
        return new ConcurrentMapCacheManager(
                "users", "usersByStatus", "usersByRole", "usersByRoleName", "userStats",
                "userPermissions", "userPermissionCodes", "rolePermissions", "userAdmin",
                "documents", "documentsByStatus", "documentsByCategory", "documentsByCategoryStatus",
                "publicDocuments", "documentsByUploader", "similarDocuments", "documentStats",
                "recentDocuments", "popularDocuments", "documentsByFileType",
                "questionnaireStats", "questionnaireAnalysis", "reportTemplates");
    }

    /**
     * 配置异步任务执行器
     */
    @Bean(name = "performanceTaskExecutor")
    public Executor performanceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8); // 增加核心线程数
        executor.setMaxPoolSize(32); // 增加最大线程数
        executor.setQueueCapacity(200); // 增加队列容量
        executor.setThreadNamePrefix("async-task-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 配置报表生成专用执行器
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 增加核心线程数
        executor.setMaxPoolSize(16); // 增加最大线程数
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("report-task-");
        executor.setKeepAliveSeconds(120);
        executor.initialize();
        return executor;
    }

    /**
     * 配置数据分析专用执行器
     */
    @Bean(name = "analysisExecutor")
    public Executor analysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 增加核心线程数
        executor.setMaxPoolSize(12); // 增加最大线程数
        executor.setQueueCapacity(60);
        executor.setThreadNamePrefix("analysis-task-");
        executor.setKeepAliveSeconds(180);
        executor.initialize();
        return executor;
    }

    /**
     * 配置文档处理专用执行器
     */
    @Bean(name = "documentExecutor")
    public Executor documentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("document-task-");
        executor.setKeepAliveSeconds(300);
        executor.initialize();
        return executor;
    }
}