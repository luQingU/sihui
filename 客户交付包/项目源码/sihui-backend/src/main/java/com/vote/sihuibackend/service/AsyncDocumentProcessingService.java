package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步文档处理服务接口
 * 提供高性能的文档上传、处理和索引构建功能
 * 
 * @author Sihui Team
 */
public interface AsyncDocumentProcessingService {

    /**
     * 异步上传单个文档
     * 
     * @param file       上传的文件
     * @param title      文档标题
     * @param category   文档分类
     * @param keywords   关键词
     * @param isPublic   是否公开
     * @param uploaderId 上传者ID
     * @return 异步处理结果
     */
    CompletableFuture<Document> uploadDocumentAsync(MultipartFile file, String title, String category,
            String keywords, Boolean isPublic, Long uploaderId);

    /**
     * 异步批量上传文档
     * 
     * @param files      上传的文件数组
     * @param category   文档分类
     * @param isPublic   是否公开
     * @param uploaderId 上传者ID
     * @return 异步处理结果列表
     */
    CompletableFuture<List<Document>> uploadDocumentsBatchAsync(MultipartFile[] files, String category,
            Boolean isPublic, Long uploaderId);

    /**
     * 异步构建文档索引
     * 
     * @param documentId 文档ID
     * @return 异步处理结果
     */
    CompletableFuture<Void> buildDocumentIndexAsync(Long documentId);

    /**
     * 异步批量构建索引
     * 
     * @param documentIds 文档ID列表
     * @return 异步处理结果
     */
    CompletableFuture<Void> buildDocumentIndexesBatchAsync(List<Long> documentIds);

    /**
     * 异步重建所有索引
     * 
     * @return 异步处理结果
     */
    CompletableFuture<Void> rebuildAllIndexesAsync();

    /**
     * 异步预计算文档相似度矩阵
     * 
     * @return 异步处理结果
     */
    CompletableFuture<Void> precomputeSimilarityMatrixAsync();

    /**
     * 获取处理队列状态
     * 
     * @return 队列状态信息
     */
    ProcessingQueueStatus getQueueStatus();

    /**
     * 处理队列状态
     */
    class ProcessingQueueStatus {
        private int pendingUploads;
        private int pendingIndexBuilds;
        private int activeTasks;
        private long totalProcessed;
        private double averageProcessingTime;

        public ProcessingQueueStatus(int pendingUploads, int pendingIndexBuilds, int activeTasks,
                long totalProcessed, double averageProcessingTime) {
            this.pendingUploads = pendingUploads;
            this.pendingIndexBuilds = pendingIndexBuilds;
            this.activeTasks = activeTasks;
            this.totalProcessed = totalProcessed;
            this.averageProcessingTime = averageProcessingTime;
        }

        // Getters and Setters
        public int getPendingUploads() {
            return pendingUploads;
        }

        public void setPendingUploads(int pendingUploads) {
            this.pendingUploads = pendingUploads;
        }

        public int getPendingIndexBuilds() {
            return pendingIndexBuilds;
        }

        public void setPendingIndexBuilds(int pendingIndexBuilds) {
            this.pendingIndexBuilds = pendingIndexBuilds;
        }

        public int getActiveTasks() {
            return activeTasks;
        }

        public void setActiveTasks(int activeTasks) {
            this.activeTasks = activeTasks;
        }

        public long getTotalProcessed() {
            return totalProcessed;
        }

        public void setTotalProcessed(long totalProcessed) {
            this.totalProcessed = totalProcessed;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public void setAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
        }
    }
}