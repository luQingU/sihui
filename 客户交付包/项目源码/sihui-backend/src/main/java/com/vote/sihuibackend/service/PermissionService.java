package com.vote.sihuibackend.service;

import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.entity.Role;
import com.vote.sihuibackend.entity.User;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 检查用户是否拥有指定权限
     */
    boolean hasPermission(Long userId, Permission permission);

    /**
     * 检查用户是否拥有指定权限代码
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否拥有任意一个权限
     */
    boolean hasAnyPermission(Long userId, Permission... permissions);

    /**
     * 检查用户是否拥有所有权限
     */
    boolean hasAllPermissions(Long userId, Permission... permissions);

    /**
     * 获取用户的所有权限
     */
    Set<Permission> getUserPermissions(Long userId);

    /**
     * 获取用户的所有权限代码
     */
    Set<String> getUserPermissionCodes(Long userId);

    /**
     * 获取角色的权限列表
     */
    Set<Permission> getRolePermissions(Long roleId);

    /**
     * 为角色设置权限
     */
    void setRolePermissions(Long roleId, Set<Permission> permissions);

    /**
     * 为角色添加权限
     */
    void addPermissionToRole(Long roleId, Permission permission);

    /**
     * 从角色移除权限
     */
    void removePermissionFromRole(Long roleId, Permission permission);

    /**
     * 检查用户是否为管理员（拥有ADMIN_ALL权限）
     */
    boolean isAdmin(Long userId);

    /**
     * 检查用户是否可以访问自己的资源
     */
    boolean canAccessSelfResource(Long currentUserId, Long resourceUserId);

    /**
     * 获取所有可用权限
     */
    List<Permission> getAllPermissions();

    /**
     * 根据权限类型分组获取权限
     */
    Set<Permission> getPermissionsByCategory(String category);
}