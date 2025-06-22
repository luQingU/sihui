package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话Repository接口
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * 根据会话ID查找会话
     * 
     * @param sessionId 会话ID
     * @return 会话信息
     */
    Optional<ChatSession> findBySessionId(String sessionId);

    /**
     * 根据用户ID查找激活的会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND cs.isActive = true ORDER BY cs.updatedAt DESC")
    List<ChatSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查找最近的会话
     * 
     * @param userId 用户ID
     * @return 最近的会话
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND cs.isActive = true ORDER BY cs.updatedAt DESC")
    Optional<ChatSession> findLatestSessionByUserId(@Param("userId") Long userId);

    /**
     * 检查会话是否存在且激活
     * 
     * @param sessionId 会话ID
     * @return 是否存在且激活
     */
    @Query("SELECT COUNT(cs) > 0 FROM ChatSession cs WHERE cs.sessionId = :sessionId AND cs.isActive = true")
    boolean existsActiveSession(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID查找所有会话，按创建时间倒序
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}