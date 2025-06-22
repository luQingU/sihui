package com.vote.sihuibackend.dto;

import com.vote.sihuibackend.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private String avatarUrl;
    private User.UserStatus status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 用户角色信息
     */
    private Set<RoleInfo> roles;

    /**
     * 角色信息嵌套类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleInfo {
        private Long id;
        private String name;
        private String displayName;
        private String description;
    }
}