package com.vote.sihuibackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * OSS文件服务接口
 * 
 * @author Sihui Team
 */
public interface OssService {

    /**
     * 上传文件
     * 
     * @param file   文件
     * @param folder 文件夹路径（可选）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 上传文件流
     * 
     * @param inputStream 文件流
     * @param fileName    文件名
     * @param folder      文件夹路径（可选）
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String fileName, String folder);

    /**
     * 删除文件
     * 
     * @param fileName 文件名（包含路径）
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);

    /**
     * 批量删除文件
     * 
     * @param fileNames 文件名列表
     * @return 删除结果
     */
    boolean deleteFiles(List<String> fileNames);

    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean doesFileExist(String fileName);

    /**
     * 获取文件访问URL
     * 
     * @param fileName 文件名
     * @return 文件访问URL
     */
    String getFileUrl(String fileName);

    /**
     * 获取带有过期时间的签名URL
     * 
     * @param fileName         文件名
     * @param expiredInSeconds 过期时间（秒）
     * @return 签名URL
     */
    String getSignedUrl(String fileName, int expiredInSeconds);

    /**
     * 验证文件类型是否允许
     * 
     * @param fileName 文件名
     * @return 是否允许
     */
    boolean isFileTypeAllowed(String fileName);

    /**
     * 验证文件大小是否在限制范围内
     * 
     * @param fileSize 文件大小（字节）
     * @return 是否在限制范围内
     */
    boolean isFileSizeAllowed(long fileSize);
}