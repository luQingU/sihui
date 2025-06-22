package com.vote.sihuibackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步处理配置
 * 为文档处理和索引构建提供专门的线程池
 * 
 * @author Sihui Team
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 文档处理线程池
     * 用于文档上传和解析处理
     */
    @Bean("documentProcessingExecutor")
    public Executor documentProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：CPU核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // 最大线程数：CPU核心数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 队列容量：100个任务
        executor.setQueueCapacity(100);

        // 线程名前缀
        executor.setThreadNamePrefix("DocProcess-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间60秒
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("文档处理线程池初始化完成 - 核心线程数: {}, 最大线程数: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * 索引构建线程池
     * 用于TF-IDF索引构建和更新
     */
    @Bean("indexBuildingExecutor")
    public Executor indexBuildingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：较少，因为索引构建是CPU密集型任务
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

        // 最大线程数：CPU核心数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());

        // 队列容量：200个任务
        executor.setQueueCapacity(200);

        // 线程名前缀
        executor.setThreadNamePrefix("IndexBuild-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间120秒（索引构建可能需要更长时间）
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();

        log.info("索引构建线程池初始化完成 - 核心线程数: {}, 最大线程数: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * 相似度计算线程池
     * 用于文档相似度矩阵预计算
     */
    @Bean("similarityComputingExecutor")
    public Executor similarityComputingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：1个，相似度计算通常是单个大任务
        executor.setCorePoolSize(1);

        // 最大线程数：2个
        executor.setMaxPoolSize(2);

        // 队列容量：10个任务
        executor.setQueueCapacity(10);

        // 线程名前缀
        executor.setThreadNamePrefix("SimilarityComp-");

        // 拒绝策略：丢弃最旧的任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间300秒（相似度计算可能需要很长时间）
        executor.setAwaitTerminationSeconds(300);

        executor.initialize();

        log.info("相似度计算线程池初始化完成 - 核心线程数: {}, 最大线程数: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * 默认异步执行器
     * 用于其他一般性异步任务
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(4);

        // 最大线程数
        executor.setMaxPoolSize(8);

        // 队列容量
        executor.setQueueCapacity(50);

        // 线程名前缀
        executor.setThreadNamePrefix("Async-");

        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("默认异步线程池初始化完成 - 核心线程数: {}, 最大线程数: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}