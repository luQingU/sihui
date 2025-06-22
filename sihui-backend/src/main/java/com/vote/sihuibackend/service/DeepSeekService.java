package com.vote.sihuibackend.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI服务接口
 * 提供智能问答功能
 */
public interface DeepSeekService {

    /**
     * 发送单条消息到DeepSeek API
     * 
     * @param message 用户消息
     * @return AI回复
     * @throws IOException 当API调用失败时抛出
     */
    String chat(String message) throws IOException;

    /**
     * 发送消息到DeepSeek API，包含对话历史
     * 
     * @param message             用户消息
     * @param conversationHistory 对话历史
     * @return AI回复
     * @throws IOException 当API调用失败时抛出
     */
    String chat(String message, List<Map<String, String>> conversationHistory) throws IOException;

    /**
     * 发送消息到DeepSeek API，自动管理会话记忆
     * 
     * @param userId    用户ID
     * @param message   用户消息
     * @param sessionId 会话ID（可选）
     * @return AI回复
     * @throws IOException 当API调用失败时抛出
     */
    String chatWithMemory(Long userId, String message, String sessionId) throws IOException;
}