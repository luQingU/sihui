package com.vote.sihuibackend.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 * 用于存储具体的对话消息
 */
@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话ID（关联chat_sessions表的id）
     */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * 消息角色：user, assistant, system
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 创建时间（使用timestamp字段名以匹配数据库）
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * 消息类型：text, image, file等
     */
    @Column(name = "message_type", length = 20)
    private String messageType = "text";

    /**
     * 元数据（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}