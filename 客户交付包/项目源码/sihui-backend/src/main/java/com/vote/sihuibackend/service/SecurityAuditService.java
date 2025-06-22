package com.vote.sihuibackend.service;

import java.util.Map;

/**
 * 安全审计服务接口
 */
public interface SecurityAuditService {

    /**
     * 记录用户登录事件
     */
    void logLoginEvent(String username, String ipAddress, boolean success);

    /**
     * 记录用户登出事件
     */
    void logLogoutEvent(String username, String ipAddress);

    /**
     * 记录权限检查事件
     */
    void logPermissionCheck(String username, String resource, String action, boolean granted);

    /**
     * 记录数据访问事件
     */
    void logDataAccess(String username, String dataType, String dataId, String action);

    /**
     * 记录安全违规事件
     */
    void logSecurityViolation(String username, String violationType, String details);

    /**
     * 获取安全审计报告
     */
    Map<String, Object> getSecurityAuditReport(int days);

    /**
     * 获取可疑活动报告
     */
    Map<String, Object> getSuspiciousActivityReport(int days);

    /**
     * 检查用户是否存在异常行为
     */
    boolean hasAnomalousActivity(String username, int hours);

    /**
     * 获取登录失败统计
     */
    Map<String, Object> getLoginFailureStats(int days);

    /**
     * 清理过期的审计日志
     */
    void cleanupExpiredAuditLogs(int retentionDays);
}