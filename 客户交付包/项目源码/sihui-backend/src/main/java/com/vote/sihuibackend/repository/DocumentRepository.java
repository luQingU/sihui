package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文档数据访问层
 * 
 * @author Sihui Team
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

        /**
         * 根据ID查找文档（重写以添加缓存）
         */
        @Override
        @Cacheable(value = "documents", key = "'id:' + #id")
        Optional<Document> findById(Long id);

        /**
         * 根据状态查找文档
         */
        @Cacheable(value = "documentsByStatus", key = "#status")
        List<Document> findByStatus(String status);

        /**
         * 根据分类查找文档
         */
        @Cacheable(value = "documentsByCategory", key = "#category")
        List<Document> findByCategory(String category);

        /**
         * 根据分类和状态查找文档
         */
        @Cacheable(value = "documentsByCategoryStatus", key = "#category + ':' + #status")
        List<Document> findByCategoryAndStatus(String category, String status);

        /**
         * 根据标题模糊查询
         */
        List<Document> findByTitleContainingIgnoreCase(String title);

        /**
         * 根据关键词模糊查询
         */
        List<Document> findByKeywordsContainingIgnoreCase(String keywords);

        /**
         * 查找公开的文档
         */
        @Cacheable(value = "publicDocuments", key = "#status")
        List<Document> findByIsPublicTrueAndStatus(String status);

        /**
         * 根据上传者查找文档
         */
        @Cacheable(value = "documentsByUploader", key = "#uploaderId + ':' + #status")
        List<Document> findByUploaderIdAndStatus(Long uploaderId, String status);

        /**
         * 分页查询文档
         */
        Page<Document> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

        /**
         * 根据分类分页查询
         */
        Page<Document> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status, Pageable pageable);

        /**
         * 全文搜索（标题、内容、关键词）
         */
        @Query("SELECT d FROM Document d WHERE d.status = :status AND " +
                        "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<Document> searchByKeyword(@Param("keyword") String keyword, @Param("status") String status);

        /**
         * 全文搜索（分页）
         */
        @Query("SELECT d FROM Document d WHERE d.status = :status AND " +
                        "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Document> searchByKeyword(@Param("keyword") String keyword, @Param("status") String status,
                        Pageable pageable);

        /**
         * 高级搜索（支持多个关键词）
         */
        @Query("SELECT d FROM Document d WHERE d.status = :status AND " +
                        "(:category IS NULL OR d.category = :category) AND " +
                        "(:isPublic IS NULL OR d.isPublic = :isPublic) AND " +
                        "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Document> advancedSearch(@Param("keyword") String keyword,
                        @Param("category") String category,
                        @Param("isPublic") Boolean isPublic,
                        @Param("status") String status,
                        Pageable pageable);

        /**
         * 查找相似文档（基于关键词匹配）
         */
        @Query("SELECT d FROM Document d WHERE d.status = :status AND d.id != :excludeId AND " +
                        "(d.keywords IS NOT NULL AND " +
                        "EXISTS (SELECT 1 FROM Document d2 WHERE d2.id = :excludeId AND " +
                        "d2.keywords IS NOT NULL AND " +
                        "LOWER(d.keywords) LIKE LOWER(CONCAT('%', SUBSTRING(d2.keywords, 1, LOCATE(',', d2.keywords + ',') - 1), '%'))))")
        @Cacheable(value = "similarDocuments", key = "#excludeId + ':' + #status")
        List<Document> findSimilarDocuments(@Param("excludeId") Long excludeId, @Param("status") String status);

        /**
         * 统计各分类的文档数量
         */
        @Query("SELECT d.category, COUNT(d) FROM Document d WHERE d.status = :status GROUP BY d.category")
        @Cacheable(value = "documentStats", key = "'categoryCount:' + #status")
        List<Object[]> countByCategory(@Param("status") String status);

        /**
         * 查找最近上传的文档
         */
        @Query("SELECT d FROM Document d WHERE d.status = :status ORDER BY d.createdAt DESC")
        @Cacheable(value = "recentDocuments", key = "#status + ':recent'")
        List<Document> findRecentDocuments(@Param("status") String status, Pageable pageable);

        /**
         * 查找热门文档（按查看次数排序）
         */
        @Query("SELECT d FROM Document d WHERE d.status = :status ORDER BY d.viewCount DESC")
        @Cacheable(value = "popularDocuments", key = "#status + ':popular'")
        List<Document> findPopularDocuments(@Param("status") String status, Pageable pageable);

        /**
         * 根据文件类型查找文档
         */
        @Cacheable(value = "documentsByFileType", key = "#fileType + ':' + #status")
        List<Document> findByFileTypeAndStatus(String fileType, String status);

        /**
         * 检查文档是否存在（根据文件名和上传者）
         */
        Optional<Document> findByOriginalFilenameAndUploaderIdAndStatus(String originalFilename, Long uploaderId,
                        String status);

        /**
         * 统计指定状态的文档数量
         */
        @Cacheable(value = "documentStats", key = "'count:' + #status")
        long countByStatus(String status);

        /**
         * 清除文档相关缓存
         */
        @CacheEvict(value = { "documents", "documentsByStatus", "documentsByCategory", "documentsByCategoryStatus",
                        "publicDocuments", "documentsByUploader", "similarDocuments", "documentStats",
                        "recentDocuments", "popularDocuments", "documentsByFileType" }, allEntries = true)
        default void clearDocumentCache() {
                // 用于手动清除文档相关缓存
        }
}