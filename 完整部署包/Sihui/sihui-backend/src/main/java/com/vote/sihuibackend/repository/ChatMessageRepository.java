package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天消息Repository接口
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 根据会话ID查找消息列表（按时间排序）
     * 
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sessionId = :sessionId ORDER BY cm.timestamp ASC")
    List<ChatMessage> findBySessionIdOrderByTimestamp(@Param("sessionId") Long sessionId);

    /**
     * 根据会话ID查找最近的N条消息
     * 
     * @param sessionId 会话ID
     * @param pageable  分页参数
     * @return 最近的消息列表
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sessionId = :sessionId ORDER BY cm.timestamp DESC")
    List<ChatMessage> findRecentMessagesBySessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * 删除会话的所有消息
     * 
     * @param sessionId 会话ID
     */
    void deleteBySessionId(Long sessionId);

    /**
     * 统计会话中的消息数量
     * 
     * @param sessionId 会话ID
     * @return 消息数量
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.sessionId = :sessionId")
    Long countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据会话ID查找前N条消息（按时间倒序）
     * 
     * @param sessionId 会话ID
     * @param limit     限制数量
     * @return 消息列表
     */
    @Query(value = "SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findTopNBySessionIdOrderByTimestampDesc(@Param("sessionId") Long sessionId,
            @Param("limit") int limit);
}