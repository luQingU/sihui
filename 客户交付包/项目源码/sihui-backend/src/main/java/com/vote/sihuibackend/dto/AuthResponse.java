package com.vote.sihuibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 认证响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo user;

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    /**
     * 用户信息内嵌类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private Set<String> roles;
        private String status;
    }
}