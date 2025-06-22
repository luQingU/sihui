package com.vote.sihuibackend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.config.DeepSeekConfig;
import com.vote.sihuibackend.entity.ChatMessage;
import com.vote.sihuibackend.service.ChatMemoryService;
import com.vote.sihuibackend.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek服务实现类
 * 处理与DeepSeek API的交互
 */
@Service
@Slf4j
public class DeepSeekServiceImpl implements DeepSeekService {

    private final CloseableHttpClient httpClient;
    private final DeepSeekConfig deepSeekConfig;
    private final ChatMemoryService chatMemoryService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeepSeekServiceImpl(CloseableHttpClient httpClient,
            DeepSeekConfig deepSeekConfig,
            ChatMemoryService chatMemoryService) {
        this.httpClient = httpClient;
        this.deepSeekConfig = deepSeekConfig;
        this.chatMemoryService = chatMemoryService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String chat(String message) throws IOException {
        return chat(message, null);
    }

    @Override
    public String chat(String message, List<Map<String, String>> conversationHistory) throws IOException {
        log.info("发送消息到DeepSeek API: {}", message);

        // 构建请求体
        Map<String, Object> requestBody = buildRequestBody(message, conversationHistory);
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        // 创建HTTP请求
        HttpPost httpPost = new HttpPost(deepSeekConfig.getApiUrl());
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
        httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == 200) {
                return parseResponse(responseBody);
            } else {
                log.error("DeepSeek API调用失败，状态码: {}, 响应: {}", statusCode, responseBody);
                throw new IOException("DeepSeek API调用失败: " + statusCode + " - " + responseBody);
            }
        } catch (Exception e) {
            log.error("DeepSeek API调用异常", e);
            throw new IOException("DeepSeek API调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String message, List<Map<String, String>> conversationHistory) {
        Map<String, Object> requestBody = new HashMap<>();

        // 设置模型参数
        requestBody.put("model", deepSeekConfig.getModel());
        requestBody.put("temperature", deepSeekConfig.getTemperature());
        requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是四会培训平台的智能助手，专门为用户提供培训相关的问答服务。请用中文回答问题，保持专业、友好的语调。");
        messages.add(systemMessage);

        // 添加对话历史
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            messages.addAll(conversationHistory);
        }

        // 添加当前用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        return requestBody;
    }

    /**
     * 解析API响应
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choicesNode = rootNode.get("choices");

            if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.get("message");
                JsonNode contentNode = messageNode.get("content");

                if (contentNode != null) {
                    return contentNode.asText();
                }
            }

            log.error("无法解析DeepSeek API响应: {}", responseBody);
            throw new IOException("无法解析DeepSeek API响应");

        } catch (Exception e) {
            log.error("解析DeepSeek API响应时发生异常", e);
            throw new IOException("解析DeepSeek API响应失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithMemory(Long userId, String message, String sessionId) throws IOException {
        log.info("用户 {} 发送带记忆的消息: {}", userId, message);

        // 获取或创建会话
        String actualSessionId = sessionId;
        if (actualSessionId == null || actualSessionId.trim().isEmpty()) {
            actualSessionId = chatMemoryService.getOrCreateDefaultSession(userId);
        }

        // 获取会话历史（最近10条消息）
        List<ChatMessage> recentMessages = chatMemoryService.getRecentMessages(actualSessionId, 10);
        List<Map<String, String>> conversationHistory = chatMemoryService.convertToApiFormat(recentMessages);

        // 保存用户消息
        chatMemoryService.saveMessage(actualSessionId, "user", message);

        try {
            // 调用AI API
            String aiResponse = chat(message, conversationHistory);

            // 保存AI回复
            chatMemoryService.saveMessage(actualSessionId, "assistant", aiResponse);

            log.info("用户 {} 会话 {} AI回复: {}", userId, actualSessionId, aiResponse);
            return aiResponse;

        } catch (IOException e) {
            log.error("AI API调用失败，用户: {}, 会话: {}", userId, actualSessionId, e);
            throw e;
        }
    }
}