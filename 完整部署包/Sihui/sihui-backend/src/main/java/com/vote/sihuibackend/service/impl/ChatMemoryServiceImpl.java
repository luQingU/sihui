package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.entity.ChatMessage;
import com.vote.sihuibackend.entity.ChatSession;
import com.vote.sihuibackend.repository.ChatMessageRepository;
import com.vote.sihuibackend.repository.ChatSessionRepository;
import com.vote.sihuibackend.service.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天记忆管理服务实现类
 */
@Service
@Slf4j
public class ChatMemoryServiceImpl implements ChatMemoryService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatMemoryServiceImpl(ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    @Transactional
    public String createSession(Long userId, String title) {
        log.info("为用户 {} 创建新会话，标题: {}", userId, title);

        String sessionUuid = UUID.randomUUID().toString();

        ChatSession session = new ChatSession();
        session.setSessionId(sessionUuid);
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setIsActive(true);

        ChatSession savedSession = chatSessionRepository.save(session);

        log.info("成功创建会话: {} (DB ID: {})", sessionUuid, savedSession.getId());
        return sessionUuid;
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(String sessionId, String role, String content) {
        log.debug("保存消息到会话 {}: {} - {}", sessionId, role, content);

        // 查找会话
        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        Long sessionDbId = sessionOpt.get().getId();

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionDbId); // 使用数据库ID而不是UUID
        message.setRole(role);
        message.setContent(content);
        message.setMessageType("text");

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 更新会话的最后更新时间
        updateSessionTimestamp(sessionId);

        return savedMessage;
    }

    @Override
    public List<ChatMessage> getSessionHistory(String sessionId) {
        log.debug("获取会话 {} 的完整历史", sessionId);

        // 查找会话
        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            return new ArrayList<>();
        }

        Long sessionDbId = sessionOpt.get().getId();
        return chatMessageRepository.findBySessionIdOrderByTimestamp(sessionDbId);
    }

    @Override
    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        log.debug("获取会话 {} 的最近 {} 条消息", sessionId, limit);

        // 查找会话
        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            return new ArrayList<>();
        }

        Long sessionDbId = sessionOpt.get().getId();
        List<ChatMessage> recentMessages = chatMessageRepository
                .findRecentMessagesBySessionId(sessionDbId, PageRequest.of(0, limit));

        // 因为查询是按降序排列的，需要反转列表以保持时间顺序
        Collections.reverse(recentMessages);
        return recentMessages;
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        log.debug("获取用户 {} 的会话列表", userId);
        return chatSessionRepository.findActiveSessionsByUserId(userId);
    }

    @Override
    @Transactional
    public String getOrCreateDefaultSession(Long userId) {
        log.debug("获取或创建用户 {} 的默认会话", userId);

        Optional<ChatSession> latestSession = chatSessionRepository.findLatestSessionByUserId(userId);

        if (latestSession.isPresent()) {
            return latestSession.get().getSessionId();
        } else {
            return createSession(userId, "默认对话");
        }
    }

    @Override
    public List<Map<String, String>> convertToApiFormat(List<ChatMessage> messages) {
        return messages.stream()
                .map(message -> {
                    Map<String, String> apiMessage = new HashMap<>();
                    apiMessage.put("role", message.getRole());
                    apiMessage.put("content", message.getContent());
                    return apiMessage;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearSession(String sessionId) {
        log.info("清理会话: {}", sessionId);

        // 查找会话
        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            log.warn("会话 {} 不存在，无法清理", sessionId);
            return;
        }

        Long sessionDbId = sessionOpt.get().getId();

        // 删除消息
        chatMessageRepository.deleteBySessionId(sessionDbId);

        // 删除会话
        chatSessionRepository.delete(sessionOpt.get());

        log.info("会话 {} 已清理完成", sessionId);
    }

    @Override
    @Transactional
    public void updateSessionTitle(String sessionId, String title) {
        log.debug("更新会话 {} 的标题为: {}", sessionId, title);

        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setTitle(title);
            chatSessionRepository.save(session);
        } else {
            log.warn("会话 {} 不存在，无法更新标题", sessionId);
        }
    }

    @Override
    public boolean isSessionActive(String sessionId) {
        return chatSessionRepository.existsActiveSession(sessionId);
    }

    /**
     * 更新会话的时间戳
     */
    private void updateSessionTimestamp(String sessionId) {
        Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            // 由于使用了@PreUpdate，只需要保存即可触发时间戳更新
            chatSessionRepository.save(session);
        }
    }
}