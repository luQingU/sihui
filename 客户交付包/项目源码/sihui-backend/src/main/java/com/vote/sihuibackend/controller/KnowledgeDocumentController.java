package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.entity.Document;
import com.vote.sihuibackend.security.UserPrincipal;
import com.vote.sihuibackend.service.DocumentService;
import com.vote.sihuibackend.service.TextSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识文档管理控制器
 * 专门处理"四会"相关文档的上传、检索和管理
 * 
 * @author Sihui Team
 */
@RestController
@RequestMapping("/api/knowledge/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "知识文档管理", description = "四会文档上传、检索、管理等操作")
@Validated
public class KnowledgeDocumentController {

    private final DocumentService documentService;
    private final TextSearchService textSearchService;

    /**
     * 上传单个知识文档
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传知识文档", description = "上传Markdown或TXT格式的四会知识文档")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @Parameter(description = "要上传的文档文件", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "文档标题") @RequestParam(value = "title", required = false) String title,

            @Parameter(description = "文档分类") @RequestParam(value = "category", required = false, defaultValue = "四会文档") String category,

            @Parameter(description = "关键词（逗号分隔）") @RequestParam(value = "keywords", required = false) String keywords,

            @Parameter(description = "是否公开访问") @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,

            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // 验证文件格式
            String filename = file.getOriginalFilename();
            if (!isValidDocumentFormat(filename)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("不支持的文件格式，仅支持 .txt, .md, .markdown 格式"));
            }

            // 上传文档
            Document document = documentService.uploadDocument(
                    file, title, category, keywords, isPublic, userPrincipal.getId());

            Map<String, Object> response = createSuccessResponse("文档上传成功");
            response.put("document", convertToResponse(document));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("文档上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("文档上传失败：" + e.getMessage()));
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情", description = "根据ID获取知识文档的详细信息")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getDocument(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {

        try {
            Optional<Document> documentOpt = documentService.getDocumentById(id);

            if (!documentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = createSuccessResponse("获取文档成功");
            response.put("document", convertToDetailResponse(documentOpt.get()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取文档失败: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取文档失败：" + e.getMessage()));
        }
    }

    /**
     * 智能搜索文档
     */
    @GetMapping("/search")
    @Operation(summary = "智能搜索文档", description = "使用TF-IDF算法进行智能文档检索")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> intelligentSearch(
            @Parameter(description = "搜索关键词", required = true) @RequestParam("keyword") String keyword,

            @Parameter(description = "返回结果数量限制") @RequestParam(value = "limit", defaultValue = "20") int limit) {

        try {
            if (!StringUtils.hasText(keyword)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("搜索关键词不能为空"));
            }

            List<TextSearchService.SearchResult> searchResults = textSearchService.intelligentSearch(keyword.trim(),
                    limit);

            Map<String, Object> response = createSuccessResponse("搜索成功");
            response.put("keyword", keyword);
            response.put("results", convertSearchResults(searchResults));
            response.put("totalResults", searchResults.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("智能搜索失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("搜索失败：" + e.getMessage()));
        }
    }

    /**
     * 获取相似文档
     */
    @GetMapping("/{id}/similar")
    @Operation(summary = "获取相似文档", description = "根据文档ID获取相似的文档")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getSimilarDocuments(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id,

            @Parameter(description = "返回结果数量限制") @RequestParam(value = "limit", defaultValue = "10") int limit) {

        try {
            List<TextSearchService.SearchResult> similarResults = textSearchService.findSimilarDocuments(id, limit);

            Map<String, Object> response = createSuccessResponse("获取相似文档成功");
            response.put("results", convertSearchResults(similarResults));
            response.put("totalResults", similarResults.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取相似文档失败: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取相似文档失败：" + e.getMessage()));
        }
    }

    /**
     * 验证文档格式
     */
    private boolean isValidDocumentFormat(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return "txt".equals(extension) || "md".equals(extension) || "markdown".equals(extension);
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 转换为简化响应对象
     */
    private Map<String, Object> convertToResponse(Document document) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", document.getId());
        response.put("title", document.getTitle());
        response.put("category", document.getCategory());
        response.put("summary", document.getSummary());
        response.put("keywords", document.getKeywords());
        response.put("fileType", document.getFileType());
        response.put("fileSize", document.getFileSize());
        response.put("isPublic", document.getIsPublic());
        response.put("viewCount", document.getViewCount());
        response.put("downloadCount", document.getDownloadCount());
        response.put("createdAt", document.getCreatedAt());
        response.put("updatedAt", document.getUpdatedAt());
        return response;
    }

    /**
     * 转换为详细响应对象（包含内容）
     */
    private Map<String, Object> convertToDetailResponse(Document document) {
        Map<String, Object> response = convertToResponse(document);
        response.put("content", document.getContent());
        response.put("originalFilename", document.getOriginalFilename());
        response.put("fileUrl", document.getFileUrl());
        response.put("language", document.getLanguage());
        response.put("version", document.getVersion());
        return response;
    }

    /**
     * 转换搜索结果
     */
    private List<Map<String, Object>> convertSearchResults(List<TextSearchService.SearchResult> searchResults) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (TextSearchService.SearchResult result : searchResults) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("document", convertToResponse(result.getDocument()));
            resultMap.put("relevanceScore", result.getRelevanceScore());
            resultMap.put("highlightedContent", result.getHighlightedContent());
            resultMap.put("matchedKeywords", result.getMatchedKeywords());
            results.add(resultMap);
        }

        return results;
    }
}