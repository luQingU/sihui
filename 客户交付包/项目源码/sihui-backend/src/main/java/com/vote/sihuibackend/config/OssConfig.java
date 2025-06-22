package com.vote.sihuibackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * 阿里云OSS配置类
 * 
 * @author Sihui Team
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
@Validated
public class OssConfig {

    /**
     * OSS服务端点
     */
    @NotBlank(message = "OSS endpoint不能为空")
    private String endpoint;

    /**
     * 访问密钥ID
     */
    @NotBlank(message = "OSS access key ID不能为空")
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    @NotBlank(message = "OSS access key secret不能为空")
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    @NotBlank(message = "OSS bucket name不能为空")
    private String bucketName;

    /**
     * 自定义域名（可选）
     */
    private String domain;

    /**
     * 最大文件大小（如：100MB）
     */
    @NotBlank(message = "最大文件大小不能为空")
    private String maxFileSize;

    /**
     * 允许的文件类型（逗号分隔）
     */
    @NotBlank(message = "允许的文件类型不能为空")
    private String allowedFileTypes;

    /**
     * 创建OSS客户端Bean
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 获取允许的文件类型列表
     */
    public List<String> getAllowedFileTypesList() {
        return Arrays.asList(allowedFileTypes.toLowerCase().split(","));
    }

    /**
     * 获取最大文件大小（字节）
     */
    public long getMaxFileSizeInBytes() {
        String size = maxFileSize.toUpperCase();
        long multiplier = 1;

        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        }

        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            // 默认100MB
            return 100 * 1024 * 1024;
        }
    }

    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String fileName) {
        if (domain != null && !domain.isEmpty()) {
            return domain + "/" + fileName;
        }
        return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + fileName;
    }
}