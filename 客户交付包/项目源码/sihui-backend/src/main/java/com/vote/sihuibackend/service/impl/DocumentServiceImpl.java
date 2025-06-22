package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.entity.Document;
import com.vote.sihuibackend.repository.DocumentRepository;
import com.vote.sihuibackend.service.DocumentService;
import com.vote.sihuibackend.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文档管理服务实现
 * 
 * @author Sihui Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final OssService ossService;

    // 支持的文档格式
    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList("txt", "md", "markdown"));

    // 常见停用词
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "个", "上", "也", "很", "到", "说", "要", "去", "你",
            "会", "着", "没有", "看", "好", "自己", "这", "还", "为", "过", "来", "可以", "大", "小", "多", "少", "能", "下", "对", "生", "同",
            "老", "从", "时", "用", "地", "们", "出", "什么", "进", "如果", "开始", "那", "现在", "因为", "所以", "但是", "或者", "已经", "还是",
            "只是", "这样", "那样", "这里", "那里", "怎么", "为什么", "什么时候", "哪里", "哪个", "怎样"));

    @Override
    public Document uploadDocument(MultipartFile file, String title, String category,
            String keywords, Boolean isPublic, Long uploaderId) {
        try {
            // 验证文件格式
            String fileExtension = getFileExtension(file.getOriginalFilename());
            if (!SUPPORTED_FORMATS.contains(fileExtension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的文件格式：" + fileExtension);
            }

            // 检查文档是否已存在
            if (documentExists(file.getOriginalFilename(), uploaderId)) {
                throw new IllegalArgumentException("文档已存在：" + file.getOriginalFilename());
            }

            // 解析文档内容
            String content = parseDocumentContent(file);
            if (!StringUtils.hasText(content)) {
                throw new IllegalArgumentException("文档内容为空");
            }

            // 上传文件到OSS
            String fileUrl = ossService.uploadFile(file, "knowledge/documents");

            // 生成标题（如果未提供）
            if (!StringUtils.hasText(title)) {
                title = generateTitleFromFilename(file.getOriginalFilename());
            }

            // 生成摘要
            String summary = generateSummary(content);

            // 提取关键词（如果未提供）
            if (!StringUtils.hasText(keywords)) {
                List<String> extractedKeywords = extractKeywords(content);
                keywords = String.join(",", extractedKeywords);
            }

            // 创建文档实体
            Document document = Document.builder()
                    .title(title)
                    .originalFilename(file.getOriginalFilename())
                    .fileType(fileExtension)
                    .content(content)
                    .summary(summary)
                    .category(StringUtils.hasText(category) ? category : "四会文档")
                    .keywords(keywords)
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .status("ACTIVE")
                    .isPublic(isPublic != null ? isPublic : false)
                    .uploaderId(uploaderId)
                    .language("zh-CN")
                    .build();

            // 保存到数据库
            Document savedDocument = documentRepository.save(document);
            log.info("文档上传成功：{}", savedDocument.getTitle());

            return savedDocument;

        } catch (Exception e) {
            log.error("文档上传失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("文档上传失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<Document> uploadDocuments(MultipartFile[] files, String category,
            Boolean isPublic, Long uploaderId) {
        List<Document> uploadedDocuments = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String title = generateTitleFromFilename(file.getOriginalFilename());
                Document document = uploadDocument(file, title, category, null, isPublic, uploaderId);
                uploadedDocuments.add(document);
            } catch (Exception e) {
                log.error("批量上传中文档失败：{}", file.getOriginalFilename(), e);
                // 继续处理其他文件，不中断整个批量上传过程
            }
        }

        return uploadedDocuments;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id);
    }

    @Override
    public Optional<Document> getDocumentById(Long id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (documentOpt.isPresent()) {
            incrementViewCount(id);
        }
        return documentOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> findAllActiveDocuments() {
        return documentRepository.findByStatus("ACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> findByCategory(String category) {
        return documentRepository.findByCategoryAndStatus(category, "ACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> findDocuments(Pageable pageable) {
        return documentRepository.findByStatusOrderByCreatedAtDesc("ACTIVE", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> findDocumentsByCategory(String category, Pageable pageable) {
        return documentRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, "ACTIVE", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> searchByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return documentRepository.searchByKeyword(keyword.trim(), "ACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> searchByKeyword(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return Page.empty(pageable);
        }
        return documentRepository.searchByKeyword(keyword.trim(), "ACTIVE", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> advancedSearch(String keyword, String category, Boolean isPublic, Pageable pageable) {
        return documentRepository.advancedSearch(
                StringUtils.hasText(keyword) ? keyword.trim() : "",
                category,
                isPublic,
                "ACTIVE",
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> findSimilarDocuments(Long documentId) {
        return documentRepository.findSimilarDocuments(documentId, "ACTIVE");
    }

    @Override
    public Document updateDocument(Long id, String title, String category, String keywords,
            String summary, Boolean isPublic) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            throw new IllegalArgumentException("文档不存在：" + id);
        }

        Document document = documentOpt.get();

        if (StringUtils.hasText(title)) {
            document.setTitle(title);
        }
        if (StringUtils.hasText(category)) {
            document.setCategory(category);
        }
        if (StringUtils.hasText(keywords)) {
            document.setKeywords(keywords);
        }
        if (StringUtils.hasText(summary)) {
            document.setSummary(summary);
        }
        if (isPublic != null) {
            document.setIsPublic(isPublic);
        }

        return documentRepository.save(document);
    }

    @Override
    public boolean deleteDocument(Long id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            return false;
        }

        Document document = documentOpt.get();
        document.setStatus("DELETED");
        documentRepository.save(document);

        log.info("文档已软删除：{}", document.getTitle());
        return true;
    }

    @Override
    public boolean permanentDeleteDocument(Long id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            return false;
        }

        Document document = documentOpt.get();

        // 删除OSS文件
        try {
            if (StringUtils.hasText(document.getFileUrl())) {
                String fileName = extractFileNameFromUrl(document.getFileUrl());
                ossService.deleteFile(fileName);
            }
        } catch (Exception e) {
            log.warn("删除OSS文件失败：{}", document.getFileUrl(), e);
        }

        // 删除数据库记录
        documentRepository.deleteById(id);

        log.info("文档已永久删除：{}", document.getTitle());
        return true;
    }

    @Override
    public boolean restoreDocument(Long id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            return false;
        }

        Document document = documentOpt.get();
        if (!"DELETED".equals(document.getStatus())) {
            return false;
        }

        document.setStatus("ACTIVE");
        documentRepository.save(document);

        log.info("文档已恢复：{}", document.getTitle());
        return true;
    }

    @Override
    public void incrementDownloadCount(Long id) {
        documentRepository.findById(id).ifPresent(document -> {
            document.setDownloadCount(document.getDownloadCount() + 1);
            documentRepository.save(document);
        });
    }

    @Override
    public void incrementViewCount(Long id) {
        documentRepository.findById(id).ifPresent(document -> {
            document.setViewCount(document.getViewCount() + 1);
            documentRepository.save(document);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDocumentStatsByCategory() {
        return documentRepository.countByCategory("ACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getRecentDocuments(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return documentRepository.findRecentDocuments("ACTIVE", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getPopularDocuments(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return documentRepository.findPopularDocuments("ACTIVE", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> findByFileType(String fileType) {
        return documentRepository.findByFileTypeAndStatus(fileType, "ACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean documentExists(String originalFilename, Long uploaderId) {
        return documentRepository.findByOriginalFilenameAndUploaderIdAndStatus(
                originalFilename, uploaderId, "ACTIVE").isPresent();
    }

    @Override
    public String parseDocumentContent(MultipartFile file) {
        try {
            String fileExtension = getFileExtension(file.getOriginalFilename());

            switch (fileExtension.toLowerCase()) {
                case "txt":
                case "md":
                case "markdown":
                    return parseTextFile(file);
                default:
                    throw new IllegalArgumentException("不支持的文件格式：" + fileExtension);
            }
        } catch (IOException e) {
            log.error("解析文档内容失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("解析文档内容失败", e);
        }
    }

    @Override
    public String generateSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        // 简单的摘要生成：取前200个字符
        String cleanContent = content.replaceAll("\\s+", " ").trim();
        if (cleanContent.length() <= 200) {
            return cleanContent;
        }

        return cleanContent.substring(0, 200) + "...";
    }

    @Override
    public List<String> extractKeywords(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }

        // 简单的关键词提取：基于词频统计
        Map<String, Integer> wordCount = new HashMap<>();

        // 移除标点符号，分词
        String cleanContent = content.replaceAll("[\\p{Punct}\\s]+", " ");
        String[] words = cleanContent.split("\\s+");

        for (String word : words) {
            word = word.trim();
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        // 按词频排序，取前10个关键词
        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateSimilarity(String content1, String content2) {
        if (!StringUtils.hasText(content1) || !StringUtils.hasText(content2)) {
            return 0.0;
        }

        // 简单的相似度计算：基于共同关键词数量
        List<String> keywords1 = extractKeywords(content1);
        List<String> keywords2 = extractKeywords(content2);

        Set<String> set1 = new HashSet<>(keywords1);
        Set<String> set2 = new HashSet<>(keywords2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 解析文本文件内容
     */
    private String parseTextFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 从文件名生成标题
     */
    private String generateTitleFromFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "未命名文档";
        }

        // 移除扩展名
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            filename = filename.substring(0, lastDotIndex);
        }

        return filename;
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