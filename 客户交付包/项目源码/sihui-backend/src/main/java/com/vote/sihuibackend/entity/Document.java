package com.vote.sihuibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 知识文档实体
 * 用于存储"四会"相关文档的内容和元数据
 * 
 * @author Sihui Team
 */
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_file_type", columnList = "file_type"),
        @Index(name = "idx_uploader_id", columnList = "uploader_id"),
        @Index(name = "idx_status_category", columnList = "status, category"),
        @Index(name = "idx_status_created_at", columnList = "status, created_at"),
        @Index(name = "idx_category_created_at", columnList = "category, created_at"),
        @Index(name = "idx_is_public", columnList = "is_public"),
        @Index(name = "idx_parent_id", columnList = "parent_id"),
        @Index(name = "idx_original_filename_uploader", columnList = "original_filename, uploader_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文档标题
     */
    @Column(nullable = false, length = 500)
    private String title;

    /**
     * 原始文件名
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * 文件类型 (txt, md, pdf等)
     */
    @Column(name = "file_type", length = 10)
    private String fileType;

    /**
     * 文档内容（文本格式）
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 文档摘要
     */
    @Column(name = "summary", length = 1000)
    private String summary;

    /**
     * 文档分类/标签
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * 关键词（用于检索，逗号分隔）
     */
    @Column(name = "keywords", length = 500)
    private String keywords;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * OSS文件URL
     */
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /**
     * 文档状态：ACTIVE, DELETED, ARCHIVED
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 是否可公开访问
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * 下载次数
     */
    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    /**
     * 查看次数
     */
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 上传者ID
     */
    @Column(name = "uploader_id")
    private Long uploaderId;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 文档版本号
     */
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    /**
     * 父文档ID（用于版本管理）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 文档语言
     */
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "zh-CN";
}