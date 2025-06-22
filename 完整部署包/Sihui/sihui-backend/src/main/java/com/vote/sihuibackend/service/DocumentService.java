package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 文档管理服务接口
 * 
 * @author Sihui Team
 */
public interface DocumentService {

    /**
     * 上传并保存文档
     * 
     * @param file       上传的文件
     * @param title      文档标题
     * @param category   文档分类
     * @param keywords   关键词
     * @param isPublic   是否公开
     * @param uploaderId 上传者ID
     * @return 保存的文档实体
     */
    Document uploadDocument(MultipartFile file, String title, String category,
            String keywords, Boolean isPublic, Long uploaderId);

    /**
     * 批量上传文档
     * 
     * @param files      文件列表
     * @param category   文档分类
     * @param isPublic   是否公开
     * @param uploaderId 上传者ID
     * @return 成功上传的文档列表
     */
    List<Document> uploadDocuments(MultipartFile[] files, String category,
            Boolean isPublic, Long uploaderId);

    /**
     * 根据ID查找文档
     */
    Optional<Document> findById(Long id);

    /**
     * 根据ID获取文档（会增加查看次数）
     */
    Optional<Document> getDocumentById(Long id);

    /**
     * 获取所有活跃文档
     */
    List<Document> findAllActiveDocuments();

    /**
     * 根据分类查找文档
     */
    List<Document> findByCategory(String category);

    /**
     * 分页查询文档
     */
    Page<Document> findDocuments(Pageable pageable);

    /**
     * 根据分类分页查询
     */
    Page<Document> findDocumentsByCategory(String category, Pageable pageable);

    /**
     * 关键词搜索
     */
    List<Document> searchByKeyword(String keyword);

    /**
     * 关键词搜索（分页）
     */
    Page<Document> searchByKeyword(String keyword, Pageable pageable);

    /**
     * 高级搜索
     */
    Page<Document> advancedSearch(String keyword, String category, Boolean isPublic, Pageable pageable);

    /**
     * 查找相似文档
     */
    List<Document> findSimilarDocuments(Long documentId);

    /**
     * 更新文档信息
     */
    Document updateDocument(Long id, String title, String category, String keywords,
            String summary, Boolean isPublic);

    /**
     * 删除文档（软删除）
     */
    boolean deleteDocument(Long id);

    /**
     * 永久删除文档
     */
    boolean permanentDeleteDocument(Long id);

    /**
     * 恢复已删除的文档
     */
    boolean restoreDocument(Long id);

    /**
     * 增加下载次数
     */
    void incrementDownloadCount(Long id);

    /**
     * 增加查看次数
     */
    void incrementViewCount(Long id);

    /**
     * 获取文档统计信息
     */
    List<Object[]> getDocumentStatsByCategory();

    /**
     * 获取最近上传的文档
     */
    List<Document> getRecentDocuments(int limit);

    /**
     * 获取热门文档
     */
    List<Document> getPopularDocuments(int limit);

    /**
     * 根据文件类型查找文档
     */
    List<Document> findByFileType(String fileType);

    /**
     * 检查文档是否存在
     */
    boolean documentExists(String originalFilename, Long uploaderId);

    /**
     * 解析文档内容
     * 
     * @param file 文件
     * @return 解析后的文本内容
     */
    String parseDocumentContent(MultipartFile file);

    /**
     * 生成文档摘要
     * 
     * @param content 文档内容
     * @return 文档摘要
     */
    String generateSummary(String content);

    /**
     * 提取关键词
     * 
     * @param content 文档内容
     * @return 关键词列表
     */
    List<String> extractKeywords(String content);

    /**
     * 计算文档相似度
     * 
     * @param content1 文档1内容
     * @param content2 文档2内容
     * @return 相似度分数 (0-1)
     */
    double calculateSimilarity(String content1, String content2);
}