package com.vote.sihuibackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 文件上传请求DTO
 * 
 * @author Sihui Team
 */
@Data
public class FileUploadRequest {

    /**
     * 文件分类（可选）
     * 如：training-videos, training-documents, course-materials
     */
    private String category;

    /**
     * 文件描述（可选）
     */
    @Size(max = 500, message = "文件描述不能超过500个字符")
    private String description;

    /**
     * 文件夹路径（可选）
     * 用于在OSS中组织文件结构
     */
    private String folder;

    /**
     * 是否公开访问（默认false）
     */
    private Boolean isPublic = false;

    /**
     * 标签（可选，用逗号分隔）
     */
    private String tags;
}