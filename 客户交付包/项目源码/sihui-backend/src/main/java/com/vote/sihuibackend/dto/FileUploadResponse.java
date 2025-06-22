package com.vote.sihuibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件上传响应DTO
 * 
 * @author Sihui Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * 是否上传成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 成功上传的文件信息
     */
    private List<FileInfo> files;

    /**
     * 失败的文件信息
     */
    private List<FailedFileInfo> failedFiles;

    /**
     * 上传总数
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 失败文件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedFileInfo {
        /**
         * 文件名
         */
        private String fileName;

        /**
         * 失败原因
         */
        private String reason;

        /**
         * 错误代码
         */
        private String errorCode;
    }

    /**
     * 创建成功响应
     */
    public static FileUploadResponse success(List<FileInfo> files) {
        return FileUploadResponse.builder()
                .success(true)
                .message("文件上传成功")
                .files(files)
                .totalCount(files.size())
                .successCount(files.size())
                .failedCount(0)
                .build();
    }

    /**
     * 创建部分成功响应
     */
    public static FileUploadResponse partialSuccess(List<FileInfo> successFiles, List<FailedFileInfo> failedFiles) {
        int totalCount = successFiles.size() + failedFiles.size();
        return FileUploadResponse.builder()
                .success(failedFiles.isEmpty())
                .message(failedFiles.isEmpty() ? "文件上传成功" : "部分文件上传失败")
                .files(successFiles)
                .failedFiles(failedFiles)
                .totalCount(totalCount)
                .successCount(successFiles.size())
                .failedCount(failedFiles.size())
                .build();
    }

    /**
     * 创建失败响应
     */
    public static FileUploadResponse failure(String message) {
        return FileUploadResponse.builder()
                .success(false)
                .message(message)
                .totalCount(0)
                .successCount(0)
                .failedCount(0)
                .build();
    }
}