package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserUpdateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.entity.Role;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.RoleRepository;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 * 优化版本：添加缓存支持、批量操作优化、性能改进
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @CacheEvict(value = { "users", "usersByStatus", "usersByRole", "userStats" }, allEntries = true)
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        // 批量验证唯一性 - 减少数据库查询次数
        validateUserUniqueness(request.getUsername(), request.getEmail(), request.getPhone(), null);

        // 创建用户实体
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .avatarUrl(request.getAvatarUrl())
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        // 批量设置角色 - 优化数据库查询
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new IllegalArgumentException("部分角色不存在");
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return convertToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'id:' + #id")
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'username:' + #username")
    public Optional<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToResponse);
    }

    @Override
    @CacheEvict(value = { "users", "usersByStatus", "usersByRole", "userStats" }, allEntries = true)
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));

        // 批量验证唯一性（排除当前用户）
        if (hasFieldChanges(user, request)) {
            validateUserUniqueness(
                    user.getUsername(), // 用户名不允许修改
                    request.getEmail(),
                    request.getPhone(),
                    id);
        }

        // 批量更新字段
        updateUserFields(user, request);

        // 批量更新角色
        if (request.getRoleIds() != null) {
            updateUserRoles(user, new ArrayList<>(request.getRoleIds()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());

        return convertToResponse(updatedUser);
    }

    @Override
    @CacheEvict(value = { "users", "usersByStatus", "usersByRole", "userStats" }, allEntries = true)
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByStatus", key = "'all'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByStatus(User.UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "users", "usersByStatus", "usersByRole", "userStats" }, allEntries = true)
    public void deleteUsersBatch(List<Long> userIds) {
        log.info("Batch deleting users: {}", userIds);

        // 批量验证用户存在性
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            List<Long> missingIds = userIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("用户不存在: " + missingIds);
        }

        // 批量删除
        userRepository.deleteAllInBatch(users);
        log.info("Batch deleted {} users", userIds.size());
    }

    @Override
    @CacheEvict(value = { "usersByRole", "userPermissions" }, allEntries = true)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("Assigning roles {} to user {}", roleIds, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        // 批量验证角色存在性
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            Set<Long> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            List<Long> missingIds = roleIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("角色不存在: " + missingIds);
        }

        // 批量添加角色（避免重复）
        Set<Role> currentRoles = user.getRoles();
        Set<Role> newRoles = new HashSet<>(roles);
        newRoles.removeAll(currentRoles); // 移除已存在的角色

        if (!newRoles.isEmpty()) {
            currentRoles.addAll(newRoles);
            userRepository.save(user);
            log.info("Added {} new roles to user {}", newRoles.size(), userId);
        } else {
            log.info("No new roles to add for user {}", userId);
        }
    }

    @Override
    @CacheEvict(value = { "usersByRole", "userPermissions" }, allEntries = true)
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        log.info("Removing roles {} from user {}", roleIds, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        // 批量移除角色
        Set<Long> roleIdSet = new HashSet<>(roleIds);
        Set<Role> rolesToKeep = user.getRoles().stream()
                .filter(role -> !roleIdSet.contains(role.getId()))
                .collect(Collectors.toSet());

        int removedCount = user.getRoles().size() - rolesToKeep.size();
        user.setRoles(rolesToKeep);
        userRepository.save(user);

        log.info("Removed {} roles from user {}", removedCount, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    @CacheEvict(value = "users", key = "'id:' + #userId")
    public void updateLastLogin(Long userId, String ipAddress) {
        userRepository.updateLastLogin(userId, LocalDateTime.now(), ipAddress);
        log.debug("Updated last login for user {} from IP {}", userId, ipAddress);
    }

    @Override
    @CacheEvict(value = "users", key = "'id:' + #userId")
    public void updateEmailVerified(Long userId, boolean verified) {
        userRepository.updateEmailVerified(userId, verified);
        log.info("Updated email verification status for user {}: {}", userId, verified);
    }

    @Override
    @CacheEvict(value = "users", key = "'id:' + #userId")
    public void updatePhoneVerified(Long userId, boolean verified) {
        userRepository.updatePhoneVerified(userId, verified);
        log.info("Updated phone verification status for user {}: {}", userId, verified);
    }

    /**
     * 批量验证用户唯一性
     */
    private void validateUserUniqueness(String username, String email, String phone, Long excludeUserId) {
        List<String> errors = new ArrayList<>();

        // 验证用户名
        if (StringUtils.hasText(username) && userRepository.existsByUsername(username)) {
            errors.add("用户名已存在: " + username);
        }

        // 验证邮箱
        if (StringUtils.hasText(email)) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() &&
                    (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId))) {
                errors.add("邮箱已存在: " + email);
            }
        }

        // 验证手机号
        if (StringUtils.hasText(phone)) {
            Optional<User> existingUser = userRepository.findByPhone(phone);
            if (existingUser.isPresent() &&
                    (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId))) {
                errors.add("手机号已存在: " + phone);
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    /**
     * 检查是否有字段变更
     */
    private boolean hasFieldChanges(User user, UserUpdateRequest request) {
        return (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) ||
                (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(user.getPhone()));
    }

    /**
     * 批量更新用户字段
     */
    private void updateUserFields(User user, UserUpdateRequest request) {
        // 更新邮箱
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // 邮箱变更后需要重新验证
        }

        // 更新手机号
        if (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(user.getPhone())) {
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false); // 手机号变更后需要重新验证
        }

        // 更新其他字段
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getPhoneVerified() != null) {
            user.setPhoneVerified(request.getPhoneVerified());
        }
    }

    /**
     * 批量更新用户角色
     */
    private void updateUserRoles(User user, List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            user.setRoles(new HashSet<>());
            return;
        }

        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            Set<Long> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            List<Long> missingIds = roleIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("角色不存在: " + missingIds);
        }

        user.setRoles(new HashSet<>(roles));
    }

    /**
     * 转换User实体为UserResponse DTO
     * 优化：减少对象创建和内存分配
     */
    private UserResponse convertToResponse(User user) {
        Set<UserResponse.RoleInfo> roleInfos = Collections.emptySet();

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleInfos = user.getRoles().stream()
                    .map(role -> UserResponse.RoleInfo.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .displayName(role.getDisplayName())
                            .description(role.getDescription())
                            .build())
                    .collect(Collectors.toSet());
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleInfos)
                .build();
    }
}