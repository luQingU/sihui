package com.vote.sihuibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息DTO
 * 
 * @author Sihui Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    /**
     * 文件ID（数据库主键）
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名（OSS中的文件名）
     */
    private String fileName;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型/扩展名
     */
    private String fileType;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 文件分类（如：video, document, image）
     */
    private String category;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 上传者姓名
     */
    private String uploaderName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 文件状态（ACTIVE, DELETED）
     */
    private String status;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 是否公开访问
     */
    private Boolean isPublic;
}