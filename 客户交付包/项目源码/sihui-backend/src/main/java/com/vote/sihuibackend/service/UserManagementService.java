package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserUpdateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户管理服务接口
 */
public interface UserManagementService {

    /**
     * 创建用户
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * 根据ID获取用户
     */
    Optional<UserResponse> getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    Optional<UserResponse> getUserByUsername(String username);

    /**
     * 根据邮箱获取用户
     */
    Optional<UserResponse> getUserByEmail(String email);

    /**
     * 更新用户信息
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 获取所有用户(分页)
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * 获取所有用户(非分页)
     */
    List<UserResponse> getAllUsers();

    /**
     * 根据状态获取用户(分页)
     */
    Page<UserResponse> getUsersByStatus(User.UserStatus status, Pageable pageable);

    /**
     * 搜索用户(分页)
     */
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    /**
     * 搜索用户(非分页)
     */
    List<UserResponse> searchUsers(String keyword);

    /**
     * 批量删除用户
     */
    void deleteUsersBatch(List<Long> userIds);

    /**
     * 为用户分配角色
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户角色
     */
    void removeRolesFromUser(Long userId, List<Long> roleIds);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 更新用户登录信息
     */
    void updateLastLogin(Long userId, String ipAddress);

    /**
     * 更新用户邮箱验证状态
     */
    void updateEmailVerified(Long userId, boolean verified);

    /**
     * 更新用户手机验证状态
     */
    void updatePhoneVerified(Long userId, boolean verified);
}