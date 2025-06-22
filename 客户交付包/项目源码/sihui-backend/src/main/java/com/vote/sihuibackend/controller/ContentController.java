package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.dto.FileInfo;
import com.vote.sihuibackend.dto.FileUploadRequest;
import com.vote.sihuibackend.dto.FileUploadResponse;
import com.vote.sihuibackend.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 培训内容文件管理控制器
 * 
 * @author Sihui Team
 */
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "培训内容管理", description = "培训内容文件上传、下载、删除等操作")
@Validated
public class ContentController {

    private final OssService ossService;

    /**
     * 单文件上传
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传单个文件", description = "上传单个培训内容文件到OSS")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "要上传的文件", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "文件分类") @RequestParam(value = "category", required = false) String category,

            @Parameter(description = "文件描述") @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "文件夹路径") @RequestParam(value = "folder", required = false) String folder,

            @Parameter(description = "是否公开访问") @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,

            @Parameter(description = "标签（逗号分隔）") @RequestParam(value = "tags", required = false) String tags) {

        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponse.failure("文件不能为空"));
            }

            // 验证文件类型
            if (!ossService.isFileTypeAllowed(file.getOriginalFilename())) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponse.failure("不支持的文件类型"));
            }

            // 验证文件大小
            if (!ossService.isFileSizeAllowed(file.getSize())) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponse.failure("文件大小超出限制"));
            }

            // 确定文件夹路径
            String uploadFolder = determineUploadFolder(category, folder);

            // 上传文件
            String fileUrl = ossService.uploadFile(file, uploadFolder);

            // 构建文件信息
            FileInfo fileInfo = buildFileInfo(file, fileUrl, category, description, isPublic, tags);

            // TODO: 保存文件信息到数据库

            List<FileInfo> files = new ArrayList<>();
            files.add(fileInfo);
            return ResponseEntity.ok(FileUploadResponse.success(files));

        } catch (IllegalArgumentException e) {
            log.warn("文件上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError()
                    .body(FileUploadResponse.failure("文件上传失败：" + e.getMessage()));
        }
    }

    /**
     * 多文件上传
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量上传文件", description = "批量上传培训内容文件到OSS")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<FileUploadResponse> uploadFiles(
            @Parameter(description = "要上传的文件列表", required = true) @RequestParam("files") MultipartFile[] files,

            @Parameter(description = "文件分类") @RequestParam(value = "category", required = false) String category,

            @Parameter(description = "文件描述") @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "文件夹路径") @RequestParam(value = "folder", required = false) String folder,

            @Parameter(description = "是否公开访问") @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,

            @Parameter(description = "标签（逗号分隔）") @RequestParam(value = "tags", required = false) String tags) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.failure("文件列表不能为空"));
        }

        List<FileInfo> successFiles = new ArrayList<>();
        List<FileUploadResponse.FailedFileInfo> failedFiles = new ArrayList<>();

        // 确定文件夹路径
        String uploadFolder = determineUploadFolder(category, folder);

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    failedFiles.add(FileUploadResponse.FailedFileInfo.builder()
                            .fileName(file.getOriginalFilename())
                            .reason("文件为空")
                            .errorCode("EMPTY_FILE")
                            .build());
                    continue;
                }

                // 验证文件类型
                if (!ossService.isFileTypeAllowed(file.getOriginalFilename())) {
                    failedFiles.add(FileUploadResponse.FailedFileInfo.builder()
                            .fileName(file.getOriginalFilename())
                            .reason("不支持的文件类型")
                            .errorCode("UNSUPPORTED_FILE_TYPE")
                            .build());
                    continue;
                }

                // 验证文件大小
                if (!ossService.isFileSizeAllowed(file.getSize())) {
                    failedFiles.add(FileUploadResponse.FailedFileInfo.builder()
                            .fileName(file.getOriginalFilename())
                            .reason("文件大小超出限制")
                            .errorCode("FILE_SIZE_EXCEEDED")
                            .build());
                    continue;
                }

                // 上传文件
                String fileUrl = ossService.uploadFile(file, uploadFolder);

                // 构建文件信息
                FileInfo fileInfo = buildFileInfo(file, fileUrl, category, description, isPublic, tags);
                successFiles.add(fileInfo);

                // TODO: 保存文件信息到数据库

            } catch (Exception e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                failedFiles.add(FileUploadResponse.FailedFileInfo.builder()
                        .fileName(file.getOriginalFilename())
                        .reason("上传失败：" + e.getMessage())
                        .errorCode("UPLOAD_ERROR")
                        .build());
            }
        }

        return ResponseEntity.ok(FileUploadResponse.partialSuccess(successFiles, failedFiles));
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileName:.+}")
    @Operation(summary = "删除文件", description = "从OSS删除指定的文件")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "文件名（包含路径）", required = true) @PathVariable String fileName) {

        try {
            boolean deleted = ossService.deleteFile(fileName);
            if (deleted) {
                // TODO: 更新数据库中的文件状态
                return ResponseEntity.ok("文件删除成功");
            } else {
                return ResponseEntity.badRequest().body("文件删除失败或文件不存在");
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileName, e);
            return ResponseEntity.internalServerError().body("删除文件失败：" + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists/{fileName:.+}")
    @Operation(summary = "检查文件是否存在", description = "检查OSS中是否存在指定文件")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Boolean> checkFileExists(
            @Parameter(description = "文件名（包含路径）", required = true) @PathVariable String fileName) {

        try {
            boolean exists = ossService.doesFileExist(fileName);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", fileName, e);
            return ResponseEntity.internalServerError().body(false);
        }
    }

    /**
     * 获取文件签名URL
     */
    @GetMapping("/signed-url/{fileName:.+}")
    @Operation(summary = "获取文件签名URL", description = "获取文件的临时访问URL")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<String> getSignedUrl(
            @Parameter(description = "文件名（包含路径）", required = true) @PathVariable String fileName,

            @Parameter(description = "过期时间（秒），默认3600秒（1小时）") @RequestParam(value = "expiredInSeconds", required = false, defaultValue = "3600") int expiredInSeconds) {

        try {
            String signedUrl = ossService.getSignedUrl(fileName, expiredInSeconds);
            return ResponseEntity.ok(signedUrl);
        } catch (Exception e) {
            log.error("获取签名URL失败: {}", fileName, e);
            return ResponseEntity.internalServerError().body("获取签名URL失败：" + e.getMessage());
        }
    }

    /**
     * 确定上传文件夹路径
     */
    private String determineUploadFolder(String category, String folder) {
        if (StringUtils.hasText(folder)) {
            return folder;
        }

        if (StringUtils.hasText(category)) {
            switch (category.toLowerCase()) {
                case "training-videos":
                case "video":
                    return "training/videos";
                case "training-documents":
                case "document":
                    return "training/documents";
                case "course-materials":
                case "material":
                    return "training/materials";
                case "images":
                case "image":
                    return "training/images";
                default:
                    return "training/others";
            }
        }

        return "training/general";
    }

    /**
     * 构建文件信息对象
     */
    private FileInfo buildFileInfo(MultipartFile file, String fileUrl, String category,
            String description, Boolean isPublic, String tags) {
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);

        return FileInfo.builder()
                .originalName(fileName)
                .fileName(extractFileNameFromUrl(fileUrl))
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .fileType(fileExtension)
                .mimeType(file.getContentType())
                .category(category)
                .description(description)
                .uploadTime(LocalDateTime.now())
                .status("ACTIVE")
                .downloadCount(0)
                .isPublic(isPublic != null ? isPublic : false)
                .build();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return "";
        }

        int lastSlashIndex = fileUrl.lastIndexOf("/");
        if (lastSlashIndex == -1) {
            return fileUrl;
        }

        return fileUrl.substring(lastSlashIndex + 1);
    }
}