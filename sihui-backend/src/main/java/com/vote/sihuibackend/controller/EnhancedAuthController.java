package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.annotation.RequirePermission;
import com.vote.sihuibackend.dto.AuthResponse;
import com.vote.sihuibackend.dto.LoginRequest;
import com.vote.sihuibackend.dto.RefreshTokenRequest;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.security.UserPrincipal;
import com.vote.sihuibackend.service.EnhancedJwtService;
import com.vote.sihuibackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 增强认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/enhanced")
@RequiredArgsConstructor
@Tag(name = "增强认证管理", description = "提供增强的认证和会话管理功能")
public class EnhancedAuthController {

    private final EnhancedJwtService enhancedJwtService;
    private final AuthService authService;

    /**
     * 增强登录 - 支持会话管理
     */
    @PostMapping("/login")
    @Operation(summary = "增强登录", description = "支持会话管理的用户登录")
    public ResponseEntity<Map<String, Object>> enhancedLogin(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        try {
            // 使用原有认证服务进行基础认证
            AuthResponse authResponse = authService.login(loginRequest);

            // 生成会话ID
            String sessionId = UUID.randomUUID().toString();

            // 获取用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 检查并发会话限制
            if (!enhancedJwtService.checkConcurrentSessionLimit(userPrincipal.getId(), 5)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "超过最大并发会话数限制");
                errorResponse.put("code", "CONCURRENT_SESSION_LIMIT");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 生成增强令牌
            String enhancedAccessToken = enhancedJwtService.generateEnhancedAccessToken(userPrincipal, sessionId);
            String enhancedRefreshToken = enhancedJwtService.generateEnhancedRefreshToken(userPrincipal, sessionId);

            // 记录设备和IP信息
            String deviceInfo = extractDeviceInfo(request);
            String ipAddress = extractIpAddress(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", enhancedAccessToken);
            response.put("refreshToken", enhancedRefreshToken);
            response.put("expiresIn", 86400); // 24小时
            response.put("sessionId", sessionId);
            response.put("userInfo", authResponse.getUser());
            response.put("deviceInfo", deviceInfo);
            response.put("ipAddress", ipAddress);

            log.info("用户 {} 使用增强登录成功，会话ID: {}, IP: {}",
                    userPrincipal.getUsername(), sessionId, ipAddress);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("增强登录失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        try {
            AuthResponse authResponse = enhancedJwtService.refreshAccessToken(refreshTokenRequest.getRefreshToken());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", authResponse.getAccessToken());
            response.put("refreshToken", authResponse.getRefreshToken());
            response.put("expiresIn", authResponse.getExpiresIn());
            response.put("userInfo", authResponse.getUser());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 注销当前会话
     */
    @PostMapping("/logout")
    @Operation(summary = "注销当前会话", description = "注销用户当前会话")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                EnhancedJwtService.TokenInfo tokenInfo = enhancedJwtService.validateAndGetTokenInfo(token);
                if (tokenInfo.isValid()) {
                    enhancedJwtService.logout(tokenInfo.getSessionId(), tokenInfo.getUserId());

                    log.info("用户 {} 注销成功，会话ID: {}", tokenInfo.getUsername(), tokenInfo.getSessionId());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "注销成功");
                    return ResponseEntity.ok(response);
                }
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无效的令牌");
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("注销失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取用户活跃会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取用户活跃会话", description = "获取当前用户所有活跃会话列表")
    public ResponseEntity<Map<String, Object>> getUserSessions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            List<EnhancedJwtService.SessionInfo> sessions = enhancedJwtService
                    .getUserActiveSessions(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessions", sessions);
            response.put("totalSessions", sessions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取用户会话失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 注销指定会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "注销指定会话", description = "注销用户指定的会话")
    public ResponseEntity<Map<String, Object>> logoutSession(@PathVariable String sessionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            enhancedJwtService.logout(sessionId, userPrincipal.getId());

            log.info("用户 {} 注销会话 {} 成功", userPrincipal.getUsername(), sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会话注销成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("注销会话失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 强制注销所有会话
     */
    @DeleteMapping("/sessions/all")
    @Operation(summary = "注销所有会话", description = "强制注销用户所有会话")
    public ResponseEntity<Map<String, Object>> logoutAllSessions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            enhancedJwtService.forceLogoutAllSessions(userPrincipal.getId());

            log.info("用户 {} 强制注销所有会话成功", userPrincipal.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "所有会话已注销");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("强制注销所有会话失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 管理员强制注销用户会话
     */
    @DeleteMapping("/admin/users/{userId}/sessions")
    @RequirePermission(Permission.USER_DELETE)
    @Operation(summary = "管理员强制注销用户会话", description = "管理员强制注销指定用户的所有会话")
    public ResponseEntity<Map<String, Object>> adminForceLogout(@PathVariable Long userId) {
        try {
            enhancedJwtService.forceLogoutAllSessions(userId);

            log.info("管理员强制注销用户 {} 的所有会话", userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户会话已强制注销");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("管理员强制注销用户会话失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/sessions/stats")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "获取会话统计", description = "获取系统会话统计信息")
    public ResponseEntity<Map<String, Object>> getSessionStats() {
        try {
            // 这里可以实现会话统计逻辑
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalActiveSessions", 0); // 需要实现统计逻辑
            stats.put("totalUsers", 0);
            stats.put("averageSessionDuration", 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取会话统计失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 私有辅助方法

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            // 简化的设备信息提取，实际可以使用专门的库解析
            if (userAgent.contains("Mobile")) {
                return "Mobile Device";
            } else if (userAgent.contains("Chrome")) {
                return "Chrome Browser";
            } else if (userAgent.contains("Firefox")) {
                return "Firefox Browser";
            } else if (userAgent.contains("Safari")) {
                return "Safari Browser";
            }
        }
        return "Unknown Device";
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}