package com.vote.sihuibackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.vote.sihuibackend.config.OssConfig;
import com.vote.sihuibackend.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * OSS文件服务实现类
 * 
 * @author Sihui Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OssServiceImpl implements OssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件类型
        if (!isFileTypeAllowed(file.getOriginalFilename())) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 验证文件大小
        if (!isFileSizeAllowed(file.getSize())) {
            throw new IllegalArgumentException("文件大小超出限制");
        }

        try {
            String fileName = generateFileName(file.getOriginalFilename(), folder);
            return uploadFile(file.getInputStream(), fileName, null);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String folder) {
        try {
            String fullFileName = folder != null ? folder + "/" + fileName : fileName;

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(fileName));

            // 上传文件
            ossClient.putObject(ossConfig.getBucketName(), fullFileName, inputStream, metadata);

            log.info("文件上传成功: {}", fullFileName);
            return ossConfig.getFileUrl(fullFileName);

        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new RuntimeException("文件上传失败", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            if (!doesFileExist(fileName)) {
                log.warn("文件不存在: {}", fileName);
                return false;
            }

            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
            log.info("文件删除成功: {}", fileName);
            return true;

        } catch (Exception e) {
            log.error("文件删除失败: {}", fileName, e);
            return false;
        }
    }

    @Override
    public boolean deleteFiles(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return true;
        }

        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(ossConfig.getBucketName());
            deleteObjectsRequest.setKeys(fileNames);

            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);

            log.info("批量删除文件成功，删除数量: {}", deleteObjectsResult.getDeletedObjects().size());
            return true;

        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return false;
        }
    }

    @Override
    public boolean doesFileExist(String fileName) {
        try {
            return ossClient.doesObjectExist(ossConfig.getBucketName(), fileName);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", fileName, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        return ossConfig.getFileUrl(fileName);
    }

    @Override
    public String getSignedUrl(String fileName, int expiredInSeconds) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + expiredInSeconds * 1000L);
            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), fileName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("生成签名URL失败: {}", fileName, e);
            throw new RuntimeException("生成签名URL失败", e);
        }
    }

    @Override
    public boolean isFileTypeAllowed(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        String extension = getFileExtension(fileName);
        return ossConfig.getAllowedFileTypesList().contains(extension.toLowerCase());
    }

    @Override
    public boolean isFileSizeAllowed(long fileSize) {
        return fileSize <= ossConfig.getMaxFileSizeInBytes();
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFileName, String folder) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String fileName = timestamp + "_" + uuid + "." + extension;

        if (StringUtils.hasText(folder)) {
            return folder + "/" + fileName;
        }

        return fileName;
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

        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "mkv":
                return "video/x-matroska";
            default:
                return "application/octet-stream";
        }
    }
}