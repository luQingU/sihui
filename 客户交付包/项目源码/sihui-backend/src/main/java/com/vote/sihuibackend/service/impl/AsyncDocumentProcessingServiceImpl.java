package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.entity.Document;
import com.vote.sihuibackend.service.AsyncDocumentProcessingService;
import com.vote.sihuibackend.service.DocumentService;
import com.vote.sihuibackend.service.TextSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步文档处理服务实现
 * 提供高性能的文档上传、处理和索引构建功能
 * 
 * @author Sihui Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncDocumentProcessingServiceImpl implements AsyncDocumentProcessingService {

    private final DocumentService documentService;
    private final TextSearchService textSearchService;

    // 处理统计
    private final AtomicInteger pendingUploads = new AtomicInteger(0);
    private final AtomicInteger pendingIndexBuilds = new AtomicInteger(0);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    @Override
    @Async("documentProcessingExecutor")
    public CompletableFuture<Document> uploadDocumentAsync(MultipartFile file, String title, String category,
            String keywords, Boolean isPublic, Long uploaderId) {
        long startTime = System.currentTimeMillis();
        pendingUploads.incrementAndGet();
        activeTasks.incrementAndGet();

        try {
            log.info("开始异步上传文档: {}", file.getOriginalFilename());

            // 上传文档
            Document document = documentService.uploadDocument(file, title, category, keywords, isPublic, uploaderId);

            // 异步构建索引
            buildDocumentIndexAsync(document.getId());

            long processingTime = System.currentTimeMillis() - startTime;
            totalProcessed.incrementAndGet();
            totalProcessingTime.addAndGet(processingTime);

            log.info("文档异步上传完成: {}, 耗时: {}ms", document.getTitle(), processingTime);
            return CompletableFuture.completedFuture(document);

        } catch (Exception e) {
            log.error("文档异步上传失败: {}", file.getOriginalFilename(), e);
            CompletableFuture<Document> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        } finally {
            pendingUploads.decrementAndGet();
            activeTasks.decrementAndGet();
        }
    }

    @Override
    @Async("documentProcessingExecutor")
    public CompletableFuture<List<Document>> uploadDocumentsBatchAsync(MultipartFile[] files, String category,
            Boolean isPublic, Long uploaderId) {
        long startTime = System.currentTimeMillis();
        pendingUploads.addAndGet(files.length);
        activeTasks.incrementAndGet();

        try {
            log.info("开始异步批量上传 {} 个文档", files.length);

            List<CompletableFuture<Document>> uploadFutures = new ArrayList<>();

            // 并行处理每个文件
            for (MultipartFile file : files) {
                String title = generateTitleFromFilename(file.getOriginalFilename());
                CompletableFuture<Document> uploadFuture = uploadDocumentAsync(file, title, category, null, isPublic,
                        uploaderId);
                uploadFutures.add(uploadFuture);
            }

            // 等待所有上传完成
            CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                    uploadFutures.toArray(new CompletableFuture[0]));

            return allUploads.thenApply(v -> {
                List<Document> results = new ArrayList<>();
                for (CompletableFuture<Document> future : uploadFutures) {
                    try {
                        results.add(future.get());
                    } catch (Exception e) {
                        log.error("批量上传中某个文档处理失败", e);
                    }
                }

                long processingTime = System.currentTimeMillis() - startTime;
                log.info("批量上传完成，成功: {}/{}, 耗时: {}ms", results.size(), files.length, processingTime);

                return results;
            });

        } catch (Exception e) {
            log.error("批量文档异步上传失败", e);
            CompletableFuture<List<Document>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        } finally {
            activeTasks.decrementAndGet();
        }
    }

    @Override
    @Async("indexBuildingExecutor")
    public CompletableFuture<Void> buildDocumentIndexAsync(Long documentId) {
        long startTime = System.currentTimeMillis();
        pendingIndexBuilds.incrementAndGet();
        activeTasks.incrementAndGet();

        try {
            log.debug("开始异步构建文档索引: {}", documentId);

            // 更新文档索引
            textSearchService.updateDocumentIndex(documentId);

            long processingTime = System.currentTimeMillis() - startTime;
            log.debug("文档索引构建完成: {}, 耗时: {}ms", documentId, processingTime);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("文档索引构建失败: {}", documentId, e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        } finally {
            pendingIndexBuilds.decrementAndGet();
            activeTasks.decrementAndGet();
        }
    }

    @Override
    @Async("indexBuildingExecutor")
    public CompletableFuture<Void> buildDocumentIndexesBatchAsync(List<Long> documentIds) {
        long startTime = System.currentTimeMillis();
        pendingIndexBuilds.addAndGet(documentIds.size());
        activeTasks.incrementAndGet();

        try {
            log.info("开始异步批量构建 {} 个文档索引", documentIds.size());

            List<CompletableFuture<Void>> indexFutures = new ArrayList<>();

            // 并行构建索引
            for (Long documentId : documentIds) {
                CompletableFuture<Void> indexFuture = buildDocumentIndexAsync(documentId);
                indexFutures.add(indexFuture);
            }

            // 等待所有索引构建完成
            return CompletableFuture.allOf(indexFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        long processingTime = System.currentTimeMillis() - startTime;
                        log.info("批量索引构建完成，处理: {} 个文档, 耗时: {}ms", documentIds.size(), processingTime);
                    });

        } catch (Exception e) {
            log.error("批量索引构建失败", e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        } finally {
            activeTasks.decrementAndGet();
        }
    }

    @Override
    @Async("indexBuildingExecutor")
    public CompletableFuture<Void> rebuildAllIndexesAsync() {
        long startTime = System.currentTimeMillis();
        activeTasks.incrementAndGet();

        try {
            log.info("开始异步重建所有文档索引");

            // 重建所有索引
            textSearchService.rebuildAllIndexes();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("所有文档索引重建完成，耗时: {}ms", processingTime);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("重建所有索引失败", e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        } finally {
            activeTasks.decrementAndGet();
        }
    }

    @Override
    @Async("similarityComputingExecutor")
    public CompletableFuture<Void> precomputeSimilarityMatrixAsync() {
        long startTime = System.currentTimeMillis();
        activeTasks.incrementAndGet();

        try {
            log.info("开始异步预计算文档相似度矩阵");

            // 这里可以实现相似度矩阵的预计算逻辑
            // 为了性能考虑，可以只预计算热门文档之间的相似度

            // TODO: 实现相似度矩阵预计算
            // 1. 获取所有活跃文档
            // 2. 计算文档两两之间的相似度
            // 3. 将结果缓存到Redis或数据库中

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("文档相似度矩阵预计算完成，耗时: {}ms", processingTime);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("预计算相似度矩阵失败", e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        } finally {
            activeTasks.decrementAndGet();
        }
    }

    @Override
    public ProcessingQueueStatus getQueueStatus() {
        double averageTime = totalProcessed.get() > 0
                ? (double) totalProcessingTime.get() / totalProcessed.get()
                : 0.0;

        return new ProcessingQueueStatus(
                pendingUploads.get(),
                pendingIndexBuilds.get(),
                activeTasks.get(),
                totalProcessed.get(),
                averageTime);
    }

    /**
     * 从文件名生成标题
     */
    private String generateTitleFromFilename(String filename) {
        if (filename == null) {
            return "未命名文档";
        }

        // 移除文件扩展名
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filename = filename.substring(0, lastDotIndex);
        }

        // 替换特殊字符
        return filename.replaceAll("[_-]", " ").trim();
    }
}