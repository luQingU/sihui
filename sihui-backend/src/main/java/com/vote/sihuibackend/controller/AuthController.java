package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.dto.AuthResponse;
import com.vote.sihuibackend.dto.LoginRequest;
import com.vote.sihuibackend.dto.RefreshTokenRequest;
import com.vote.sihuibackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "用户通过用户名或邮箱进行登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        try {
            log.info("登录请求来自 IP: {}, 用户: {}",
                    getClientIpAddress(request), loginRequest.getUsernameOrEmail());

            AuthResponse authResponse = authService.login(loginRequest);

            return ResponseEntity.ok(createSuccessResponse("登录成功", authResponse));
        } catch (BadCredentialsException e) {
            log.warn("登录失败: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(createErrorResponse(401, "用户名或密码错误"));
        } catch (Exception e) {
            log.error("登录处理异常: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(createErrorResponse(500, "服务器内部错误"));
        }
    }

    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "令牌刷新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "刷新令牌无效或已过期")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        try {
            log.info("令牌刷新请求来自 IP: {}", getClientIpAddress(request));

            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest);

            return ResponseEntity.ok(createSuccessResponse("令牌刷新成功", authResponse));
        } catch (BadCredentialsException e) {
            log.warn("令牌刷新失败: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(createErrorResponse(401, "刷新令牌无效或已过期"));
        } catch (Exception e) {
            log.error("令牌刷新处理异常: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(createErrorResponse(500, "服务器内部错误"));
        }
    }

    @Operation(summary = "用户登出", description = "用户登出（客户端应清除本地令牌）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            log.info("用户登出请求来自 IP: {}", getClientIpAddress(request));

            // 注意：在无状态 JWT 实现中，服务端通常不维护令牌状态
            // 实际的登出操作由客户端完成（清除本地存储的令牌）
            // 如果需要服务端令牌撤销功能，可以考虑维护一个令牌黑名单

            return ResponseEntity.ok(createSuccessResponse("登出成功", null));
        } catch (Exception e) {
            log.error("登出处理异常: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(createErrorResponse(500, "服务器内部错误"));
        }
    }

    /**
     * 获取客户端真实 IP 地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}