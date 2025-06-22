package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.service.ChatMemoryService;
import com.vote.sihuibackend.service.DeepSeekService;
import com.vote.sihuibackend.service.KnowledgeEnhancedChatService;
import com.vote.sihuibackend.service.TextSearchService.SearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI聊天控制器
 * 提供智能问答API接口
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI聊天", description = "智能问答相关接口")
@Slf4j
public class AiChatController {

    private final DeepSeekService deepSeekService;
    private final ChatMemoryService chatMemoryService;
    private final KnowledgeEnhancedChatService knowledgeEnhancedChatService;

    @Autowired
    public AiChatController(DeepSeekService deepSeekService, ChatMemoryService chatMemoryService,
            KnowledgeEnhancedChatService knowledgeEnhancedChatService) {
        this.deepSeekService = deepSeekService;
        this.chatMemoryService = chatMemoryService;
        this.knowledgeEnhancedChatService = knowledgeEnhancedChatService;
    }

    /**
     * 简单聊天接口
     * 
     * @param message 用户消息
     * @return AI回复
     */
    @PostMapping("/chat")
    @Operation(summary = "发送聊天消息", description = "向AI发送单条消息并获取回复")
    public ResponseEntity<Map<String, Object>> chat(
            @Parameter(description = "用户消息", required = true) @RequestParam String message) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到聊天请求: {}", message);

            // 参数验证
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用DeepSeek API
            String aiResponse = deepSeekService.chat(message.trim());

            response.put("success", true);
            response.put("message", "请求成功");
            response.put("data", aiResponse);

            log.info("AI回复: {}", aiResponse);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("AI聊天服务调用失败", e);
            response.put("success", false);
            response.put("message", "AI服务暂时不可用，请稍后重试");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("聊天接口异常", e);
            response.put("success", false);
            response.put("message", "系统异常，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 带对话历史的聊天接口
     * 
     * @param chatRequest 聊天请求对象
     * @return AI回复
     */
    @PostMapping("/chat/conversation")
    @Operation(summary = "对话聊天", description = "向AI发送消息并包含对话历史")
    public ResponseEntity<Map<String, Object>> chatWithHistory(
            @RequestBody ChatRequest chatRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到对话聊天请求: {}", chatRequest.getMessage());

            // 参数验证
            if (chatRequest.getMessage() == null || chatRequest.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用DeepSeek API
            String aiResponse = deepSeekService.chat(
                    chatRequest.getMessage().trim(),
                    chatRequest.getHistory());

            response.put("success", true);
            response.put("message", "请求成功");
            response.put("data", aiResponse);

            log.info("AI回复: {}", aiResponse);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("AI聊天服务调用失败", e);
            response.put("success", false);
            response.put("message", "AI服务暂时不可用，请稍后重试");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("对话聊天接口异常", e);
            response.put("success", false);
            response.put("message", "系统异常，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 聊天请求对象
     */
    public static class ChatRequest {
        private String message;
        private List<Map<String, String>> history;

        // Getters and Setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Map<String, String>> getHistory() {
            return history;
        }

        public void setHistory(List<Map<String, String>> history) {
            this.history = history;
        }
    }

    /**
     * 带记忆的聊天接口
     * 
     * @param memoryRequest 带记忆的聊天请求
     * @return AI回复
     */
    @PostMapping("/chat/memory")
    @Operation(summary = "智能记忆聊天", description = "自动管理对话历史的智能聊天")
    public ResponseEntity<Map<String, Object>> chatWithMemory(
            @RequestBody MemoryRequest memoryRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到记忆聊天请求 - 用户: {}, 消息: {}", memoryRequest.getUserId(), memoryRequest.getMessage());

            // 参数验证
            if (memoryRequest.getUserId() == null) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (memoryRequest.getMessage() == null || memoryRequest.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用带记忆的DeepSeek API
            String aiResponse = deepSeekService.chatWithMemory(
                    memoryRequest.getUserId(),
                    memoryRequest.getMessage().trim(),
                    memoryRequest.getSessionId());

            response.put("success", true);
            response.put("message", "请求成功");
            response.put("data", aiResponse);

            log.info("记忆聊天AI回复: {}", aiResponse);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("记忆聊天服务调用失败", e);
            response.put("success", false);
            response.put("message", "AI服务暂时不可用，请稍后重试");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("记忆聊天接口异常", e);
            response.put("success", false);
            response.put("message", "系统异常，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 带记忆的聊天请求对象
     */
    public static class MemoryRequest {
        private Long userId;
        private String message;
        private String sessionId; // 可选，为空时自动创建或使用默认会话

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * 知识增强聊天接口
     * 
     * @param knowledgeRequest 知识增强聊天请求
     * @return 增强后的AI回复
     */
    @PostMapping("/chat/knowledge")
    @Operation(summary = "知识增强智能聊天", description = "基于文档知识库的智能问答")
    public ResponseEntity<Map<String, Object>> chatWithKnowledge(
            @RequestBody KnowledgeRequest knowledgeRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到知识增强聊天请求 - 用户: {}, 消息: {}", knowledgeRequest.getUserId(), knowledgeRequest.getMessage());

            // 参数验证
            if (knowledgeRequest.getUserId() == null) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (knowledgeRequest.getMessage() == null || knowledgeRequest.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用知识增强聊天服务
            KnowledgeEnhancedChatService.KnowledgeEnhancedResponse knowledgeResponse = knowledgeEnhancedChatService
                    .chatWithKnowledge(
                            knowledgeRequest.getUserId(),
                            knowledgeRequest.getMessage().trim(),
                            knowledgeRequest.getSessionId());

            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("answer", knowledgeResponse.getAnswer());
            responseData.put("hasKnowledgeSupport", knowledgeResponse.isHasKnowledgeSupport());
            responseData.put("confidenceScore", knowledgeResponse.getConfidenceScore());

            // 添加查询分析信息
            Map<String, Object> queryAnalysis = new HashMap<>();
            queryAnalysis.put("intent", knowledgeResponse.getQueryAnalysis().getIntent());
            queryAnalysis.put("expandedKeywords", knowledgeResponse.getQueryAnalysis().getExpandedKeywords());
            queryAnalysis.put("entities", knowledgeResponse.getQueryAnalysis().getEntities());
            queryAnalysis.put("confidence", knowledgeResponse.getQueryAnalysis().getConfidence());
            responseData.put("queryAnalysis", queryAnalysis);

            // 添加源文档信息
            if (!knowledgeResponse.getSourceDocuments().isEmpty()) {
                List<Map<String, Object>> sourceDocuments = new ArrayList<>();
                for (SearchResult result : knowledgeResponse.getSourceDocuments()) {
                    Map<String, Object> docInfo = new HashMap<>();
                    docInfo.put("title", result.getDocument().getTitle());
                    docInfo.put("relevanceScore", result.getRelevanceScore());
                    docInfo.put("highlightedContent", result.getHighlightedContent());
                    docInfo.put("matchedKeywords", result.getMatchedKeywords());
                    sourceDocuments.add(docInfo);
                }
                responseData.put("sourceDocuments", sourceDocuments);
            }

            response.put("success", true);
            response.put("message", "请求成功");
            response.put("data", responseData);

            log.info("知识增强聊天完成 - 置信度: {}, 知识支持: {}",
                    knowledgeResponse.getConfidenceScore(), knowledgeResponse.isHasKnowledgeSupport());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("知识增强聊天服务调用失败", e);
            response.put("success", false);
            response.put("message", "AI服务暂时不可用，请稍后重试");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("知识增强聊天接口异常", e);
            response.put("success", false);
            response.put("message", "系统异常，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 知识增强聊天请求对象
     */
    public static class KnowledgeRequest {
        private Long userId;
        private String message;
        private String sessionId; // 可选，为空时自动创建或使用默认会话

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}