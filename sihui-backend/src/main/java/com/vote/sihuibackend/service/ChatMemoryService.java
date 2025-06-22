package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.ChatMessage;
import com.vote.sihuibackend.entity.ChatSession;

import java.util.List;
import java.util.Map;

/**
 * 聊天记忆管理服务接口
 * 负责管理对话历史和上下文记忆
 */
public interface ChatMemoryService {

    /**
     * 创建新的聊天会话
     * 
     * @param userId 用户ID
     * @param title  会话标题
     * @return 会话ID
     */
    String createSession(Long userId, String title);

    /**
     * 保存消息到会话
     * 
     * @param sessionId 会话ID
     * @param role      角色（user/assistant/system）
     * @param content   消息内容
     * @return 保存的消息
     */
    ChatMessage saveMessage(String sessionId, String role, String content);

    /**
     * 获取会话的完整历史
     * 
     * @param sessionId 会话ID
     * @return 消息历史列表
     */
    List<ChatMessage> getSessionHistory(String sessionId);

    /**
     * 获取会话的最近N条消息
     * 
     * @param sessionId 会话ID
     * @param limit     消息数量限制
     * @return 最近的消息列表
     */
    List<ChatMessage> getRecentMessages(String sessionId, int limit);

    /**
     * 获取用户的会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(Long userId);

    /**
     * 获取或创建用户的默认会话
     * 
     * @param userId 用户ID
     * @return 会话ID
     */
    String getOrCreateDefaultSession(Long userId);

    /**
     * 将消息历史转换为API调用格式
     * 
     * @param messages 消息列表
     * @return API格式的消息历史
     */
    List<Map<String, String>> convertToApiFormat(List<ChatMessage> messages);

    /**
     * 清理会话（删除会话和相关消息）
     * 
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);

    /**
     * 更新会话标题
     * 
     * @param sessionId 会话ID
     * @param title     新标题
     */
    void updateSessionTitle(String sessionId, String title);

    /**
     * 检查会话是否存在且激活
     * 
     * @param sessionId 会话ID
     * @return 是否存在且激活
     */
    boolean isSessionActive(String sessionId);
}