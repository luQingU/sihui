package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.service.SecurityAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 安全审计服务实现
 */
@Slf4j
@Service
public class SecurityAuditServiceImpl implements SecurityAuditService {

    // 内存存储审计日志（生产环境应使用数据库）
    private final List<Map<String, Object>> auditLogs = Collections.synchronizedList(new ArrayList<>());

    // 登录失败计数器
    private final Map<String, AtomicInteger> loginFailureCount = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastLoginFailure = new ConcurrentHashMap<>();

    @Override
    public void logLoginEvent(String username, String ipAddress, boolean success) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("eventType", "LOGIN");
        logEntry.put("username", username);
        logEntry.put("ipAddress", ipAddress);
        logEntry.put("success", success);
        logEntry.put("details", success ? "Login successful" : "Login failed");

        auditLogs.add(logEntry);

        if (success) {
            // 重置失败计数
            loginFailureCount.remove(username);
            lastLoginFailure.remove(username);
            log.info("User {} logged in successfully from {}", username, ipAddress);
        } else {
            // 增加失败计数
            loginFailureCount.computeIfAbsent(username, k -> new AtomicInteger(0)).incrementAndGet();
            lastLoginFailure.put(username, LocalDateTime.now());

            int failures = loginFailureCount.get(username).get();
            log.warn("Login failed for user {} from {} (failure count: {})", username, ipAddress, failures);

            // 检查是否需要告警
            if (failures >= 5) {
                logSecurityViolation(username, "MULTIPLE_LOGIN_FAILURES",
                        "User has " + failures + " consecutive login failures");
            }
        }
    }

    @Override
    public void logLogoutEvent(String username, String ipAddress) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("eventType", "LOGOUT");
        logEntry.put("username", username);
        logEntry.put("ipAddress", ipAddress);
        logEntry.put("success", true);
        logEntry.put("details", "User logged out");

        auditLogs.add(logEntry);
        log.info("User {} logged out from {}", username, ipAddress);
    }

    @Override
    public void logPermissionCheck(String username, String resource, String action, boolean granted) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("eventType", "PERMISSION_CHECK");
        logEntry.put("username", username);
        logEntry.put("resource", resource);
        logEntry.put("action", action);
        logEntry.put("granted", granted);
        logEntry.put("details", String.format("Permission %s for %s on %s",
                granted ? "granted" : "denied", action, resource));

        auditLogs.add(logEntry);

        if (!granted) {
            log.warn("Permission denied for user {} on resource {} (action: {})",
                    username, resource, action);
        }
    }

    @Override
    public void logDataAccess(String username, String dataType, String dataId, String action) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("eventType", "DATA_ACCESS");
        logEntry.put("username", username);
        logEntry.put("dataType", dataType);
        logEntry.put("dataId", dataId);
        logEntry.put("action", action);
        logEntry.put("details", String.format("User %s %s %s (ID: %s)",
                username, action, dataType, dataId));

        auditLogs.add(logEntry);
        log.debug("Data access: {} {} {} (ID: {})", username, action, dataType, dataId);
    }

    @Override
    public void logSecurityViolation(String username, String violationType, String details) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("eventType", "SECURITY_VIOLATION");
        logEntry.put("username", username);
        logEntry.put("violationType", violationType);
        logEntry.put("severity", "HIGH");
        logEntry.put("details", details);

        auditLogs.add(logEntry);
        log.error("SECURITY VIOLATION - User: {}, Type: {}, Details: {}",
                username, violationType, details);
    }

    @Override
    public Map<String, Object> getSecurityAuditReport(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        List<Map<String, Object>> recentLogs = auditLogs.stream()
                .filter(log -> ((LocalDateTime) log.get("timestamp")).isAfter(startTime))
                .sorted((a, b) -> ((LocalDateTime) b.get("timestamp"))
                        .compareTo((LocalDateTime) a.get("timestamp")))
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("reportPeriod", days + " days");
        report.put("reportGenerated", LocalDateTime.now());
        report.put("totalEvents", recentLogs.size());

        // 按事件类型统计
        Map<String, Long> eventTypeStats = recentLogs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> (String) log.get("eventType"),
                        java.util.stream.Collectors.counting()));
        report.put("eventTypeStats", eventTypeStats);

        // 获取最近的安全违规
        List<Map<String, Object>> violations = recentLogs.stream()
                .filter(log -> "SECURITY_VIOLATION".equals(log.get("eventType")))
                .limit(50)
                .collect(Collectors.toList());
        report.put("securityViolations", violations);

        // 获取登录统计
        long successfulLogins = recentLogs.stream()
                .filter(log -> "LOGIN".equals(log.get("eventType")) &&
                        Boolean.TRUE.equals(log.get("success")))
                .count();
        long failedLogins = recentLogs.stream()
                .filter(log -> "LOGIN".equals(log.get("eventType")) &&
                        Boolean.FALSE.equals(log.get("success")))
                .count();

        Map<String, Object> loginStats = new HashMap<>();
        loginStats.put("successful", successfulLogins);
        loginStats.put("failed", failedLogins);
        loginStats.put("successRate",
                successfulLogins + failedLogins > 0
                        ? (double) successfulLogins / (successfulLogins + failedLogins) * 100
                        : 0);
        report.put("loginStats", loginStats);

        return report;
    }

    @Override
    public Map<String, Object> getSuspiciousActivityReport(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        List<Map<String, Object>> suspiciousEvents = auditLogs.stream()
                .filter(log -> {
                    LocalDateTime timestamp = (LocalDateTime) log.get("timestamp");
                    String eventType = (String) log.get("eventType");
                    return timestamp.isAfter(startTime) &&
                            ("SECURITY_VIOLATION".equals(eventType) ||
                                    ("LOGIN".equals(eventType) && Boolean.FALSE.equals(log.get("success"))));
                })
                .sorted((a, b) -> ((LocalDateTime) b.get("timestamp"))
                        .compareTo((LocalDateTime) a.get("timestamp")))
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("reportPeriod", days + " days");
        report.put("reportGenerated", LocalDateTime.now());
        report.put("suspiciousEvents", suspiciousEvents);
        report.put("totalSuspiciousEvents", suspiciousEvents.size());

        // 按用户统计可疑活动
        Map<String, Long> userActivityStats = suspiciousEvents.stream()
                .filter(log -> log.get("username") != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> (String) log.get("username"),
                        java.util.stream.Collectors.counting()));
        report.put("userActivityStats", userActivityStats);

        return report;
    }

    @Override
    public boolean hasAnomalousActivity(String username, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);

        // 检查登录失败次数
        AtomicInteger failures = loginFailureCount.get(username);
        if (failures != null && failures.get() >= 3) {
            return true;
        }

        // 检查最近的安全违规
        long violations = auditLogs.stream()
                .filter(log -> {
                    LocalDateTime timestamp = (LocalDateTime) log.get("timestamp");
                    String logUsername = (String) log.get("username");
                    String eventType = (String) log.get("eventType");
                    return timestamp.isAfter(startTime) &&
                            username.equals(logUsername) &&
                            "SECURITY_VIOLATION".equals(eventType);
                })
                .count();

        return violations > 0;
    }

    @Override
    public Map<String, Object> getLoginFailureStats(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        List<Map<String, Object>> failedLogins = auditLogs.stream()
                .filter(log -> {
                    LocalDateTime timestamp = (LocalDateTime) log.get("timestamp");
                    String eventType = (String) log.get("eventType");
                    Boolean success = (Boolean) log.get("success");
                    return timestamp.isAfter(startTime) &&
                            "LOGIN".equals(eventType) &&
                            Boolean.FALSE.equals(success);
                })
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFailures", failedLogins.size());
        stats.put("reportPeriod", days + " days");

        // 按用户统计失败次数
        Map<String, Long> userFailureStats = failedLogins.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> (String) log.get("username"),
                        java.util.stream.Collectors.counting()));
        stats.put("userFailureStats", userFailureStats);

        // 按IP统计失败次数
        Map<String, Long> ipFailureStats = failedLogins.stream()
                .filter(log -> log.get("ipAddress") != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> (String) log.get("ipAddress"),
                        java.util.stream.Collectors.counting()));
        stats.put("ipFailureStats", ipFailureStats);

        return stats;
    }

    @Override
    public void cleanupExpiredAuditLogs(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

        int sizeBefore = auditLogs.size();
        auditLogs.removeIf(log -> ((LocalDateTime) log.get("timestamp")).isBefore(cutoffTime));
        int sizeAfter = auditLogs.size();

        log.info("Cleaned up {} expired audit logs (retention: {} days)",
                sizeBefore - sizeAfter, retentionDays);
    }
}