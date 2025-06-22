package com.vote.sihuibackend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 安全审计日志记录器
 * 用于记录安全相关事件，提升系统安全监控能力
 */
@Slf4j
@Component
public class SecurityAuditLogger {

    private static final String AUDIT_LOG_PREFIX = "[SECURITY_AUDIT]";

    /**
     * 记录成功登录事件
     */
    public void logSuccessfulLogin(String username, String ipAddress, String userAgent) {
        log.info("{} 成功登录 - 用户: {}, IP: {}, UserAgent: {}, 时间: {}",
                AUDIT_LOG_PREFIX, username, ipAddress, userAgent, LocalDateTime.now());
    }

    /**
     * 记录失败登录事件
     */
    public void logFailedLogin(String username, String ipAddress, String userAgent, String reason) {
        log.warn("{} 登录失败 - 用户: {}, IP: {}, UserAgent: {}, 原因: {}, 时间: {}",
                AUDIT_LOG_PREFIX, username, ipAddress, userAgent, reason, LocalDateTime.now());
    }

    /**
     * 记录令牌刷新事件
     */
    public void logTokenRefresh(String username, String ipAddress) {
        log.info("{} 令牌刷新 - 用户: {}, IP: {}, 时间: {}",
                AUDIT_LOG_PREFIX, username, ipAddress, LocalDateTime.now());
    }

    /**
     * 记录登出事件
     */
    public void logLogout(String username, String ipAddress) {
        log.info("{} 用户登出 - 用户: {}, IP: {}, 时间: {}",
                AUDIT_LOG_PREFIX, username, ipAddress, LocalDateTime.now());
    }

    /**
     * 记录可疑活动
     */
    public void logSuspiciousActivity(String activity, String ipAddress, String details) {
        log.warn("{} 可疑活动 - 活动: {}, IP: {}, 详情: {}, 时间: {}",
                AUDIT_LOG_PREFIX, activity, ipAddress, details, LocalDateTime.now());
    }

    /**
     * 记录权限拒绝事件
     */
    public void logAccessDenied(String username, String resource, String ipAddress) {
        log.warn("{} 访问被拒绝 - 用户: {}, 资源: {}, IP: {}, 时间: {}",
                AUDIT_LOG_PREFIX, username, resource, ipAddress, LocalDateTime.now());
    }

    /**
     * 记录JWT验证失败
     */
    public void logJwtValidationFailure(String reason, String ipAddress, String token) {
        // 只记录token的前8个字符以保护敏感信息
        String tokenPrefix = token != null && token.length() > 8 ? token.substring(0, 8) + "..." : "null";
        log.warn("{} JWT验证失败 - 原因: {}, IP: {}, Token前缀: {}, 时间: {}",
                AUDIT_LOG_PREFIX, reason, ipAddress, tokenPrefix, LocalDateTime.now());
    }

    /**
     * 记录账户锁定事件
     */
    public void logAccountLocked(String username, String reason, String ipAddress) {
        log.error("{} 账户锁定 - 用户: {}, 原因: {}, IP: {}, 时间: {}",
                AUDIT_LOG_PREFIX, username, reason, ipAddress, LocalDateTime.now());
    }

    /**
     * 从HttpServletRequest提取客户端IP地址
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // 取第一个IP地址（如果有多个代理）
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 从HttpServletRequest提取User-Agent
     */
    public String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }

    /**
     * 检查IP地址是否来自本地网络
     */
    public boolean isLocalNetwork(String ipAddress) {
        if (ipAddress == null)
            return false;

        return ipAddress.startsWith("192.168.") ||
                ipAddress.startsWith("10.") ||
                ipAddress.startsWith("172.") ||
                ipAddress.equals("127.0.0.1") ||
                ipAddress.equals("0:0:0:0:0:0:0:1") ||
                ipAddress.equals("::1");
    }

    /**
     * 记录系统启动/关闭事件
     */
    public void logSystemEvent(String event, String details) {
        log.info("{} 系统事件 - 事件: {}, 详情: {}, 时间: {}",
                AUDIT_LOG_PREFIX, event, details, LocalDateTime.now());
    }
}