package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.AuthResponse;
import com.vote.sihuibackend.security.UserPrincipal;
import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.Map;

/**
 * 增强的JWT服务接口
 */
public interface EnhancedJwtService {

    /**
     * 生成访问令牌（带用户ID和角色信息）
     */
    String generateEnhancedAccessToken(UserPrincipal userPrincipal, String sessionId);

    /**
     * 生成刷新令牌
     */
    String generateEnhancedRefreshToken(UserPrincipal userPrincipal, String sessionId);

    /**
     * 验证令牌并获取详细信息
     */
    TokenInfo validateAndGetTokenInfo(String token);

    /**
     * 刷新访问令牌
     */
    AuthResponse refreshAccessToken(String refreshToken);

    /**
     * 注销指定会话
     */
    void logout(String sessionId, Long userId);

    /**
     * 强制注销用户所有会话
     */
    void forceLogoutAllSessions(Long userId);

    /**
     * 获取用户活跃会话列表
     */
    List<SessionInfo> getUserActiveSessions(Long userId);

    /**
     * 检查并发会话限制
     */
    boolean checkConcurrentSessionLimit(Long userId, int maxSessions);

    /**
     * 更新会话活跃状态
     */
    void updateSessionActivity(String sessionId, Long userId);

    /**
     * 清理过期会话
     */
    void cleanupExpiredSessions();

    /**
     * 令牌信息
     */
    class TokenInfo {
        private final String username;
        private final Long userId;
        private final String sessionId;
        private final List<String> roles;
        private final boolean valid;
        private final boolean expired;

        public TokenInfo(String username, Long userId, String sessionId, List<String> roles, boolean valid,
                boolean expired) {
            this.username = username;
            this.userId = userId;
            this.sessionId = sessionId;
            this.roles = roles;
            this.valid = valid;
            this.expired = expired;
        }

        // Getters
        public String getUsername() {
            return username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public List<String> getRoles() {
            return roles;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isExpired() {
            return expired;
        }
    }

    /**
     * 会话信息
     */
    class SessionInfo {
        private final String sessionId;
        private final String deviceInfo;
        private final String ipAddress;
        private final String location;
        private final long loginTime;
        private final long lastActivity;
        private final boolean current;

        public SessionInfo(String sessionId, String deviceInfo, String ipAddress, String location,
                long loginTime, long lastActivity, boolean current) {
            this.sessionId = sessionId;
            this.deviceInfo = deviceInfo;
            this.ipAddress = ipAddress;
            this.location = location;
            this.loginTime = loginTime;
            this.lastActivity = lastActivity;
            this.current = current;
        }

        // Getters
        public String getSessionId() {
            return sessionId;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getLocation() {
            return location;
        }

        public long getLoginTime() {
            return loginTime;
        }

        public long getLastActivity() {
            return lastActivity;
        }

        public boolean isCurrent() {
            return current;
        }
    }
}