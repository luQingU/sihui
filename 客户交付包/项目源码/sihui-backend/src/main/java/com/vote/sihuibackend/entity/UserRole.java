package com.vote.sihuibackend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "user_roles", uniqueConstraints = @UniqueConstraint(name = "uk_user_role", columnNames = { "user_id",
        "role_id" }))
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private User grantedBy;

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }

    /**
     * 设置用户ID（辅助方法）
     */
    public void setUserId(Long userId) {
        if (userId != null) {
            this.user = new User();
            this.user.setId(userId);
        }
    }

    /**
     * 设置角色ID（辅助方法）
     */
    public void setRoleId(Long roleId) {
        if (roleId != null) {
            this.role = new Role();
            this.role.setId(roleId);
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * 获取角色ID
     */
    public Long getRoleId() {
        return role != null ? role.getId() : null;
    }
}