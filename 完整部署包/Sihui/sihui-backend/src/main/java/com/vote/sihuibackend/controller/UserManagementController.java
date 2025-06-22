package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.annotation.RequirePermission;
import com.vote.sihuibackend.dto.ApiResponse;
import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserUpdateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * 创建用户
     */
    @PostMapping
    @RequirePermission(Permission.USER_CREATE)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());
        UserResponse response = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @RequirePermission(value = Permission.USER_READ, allowSelf = true, selfParam = "id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable @NotNull Long id) {
        Optional<UserResponse> user = userManagementService.getUserById(id);
        return user.map(userResponse -> ResponseEntity.ok(ApiResponse.success(userResponse)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有用户(分页)
     */
    @GetMapping
    @RequirePermission(Permission.USER_READ)
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userManagementService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 根据状态获取用户(分页)
     */
    @GetMapping("/status/{status}")
    @RequirePermission(Permission.USER_READ)
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByStatus(
            @PathVariable User.UserStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userManagementService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    @RequirePermission(Permission.USER_SEARCH)
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userManagementService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @RequirePermission(value = Permission.USER_UPDATE, allowSelf = true, selfParam = "id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        UserResponse response = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RequirePermission(Permission.USER_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable @NotNull Long id) {
        log.info("Deleting user with ID: {}", id);
        userManagementService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @RequirePermission(Permission.USER_BATCH_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteUsersBatch(@RequestBody List<Long> userIds) {
        log.info("Batch deleting users: {}", userIds);
        userManagementService.deleteUsersBatch(userIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/{userId}/roles")
    @RequirePermission(Permission.USER_ASSIGN_ROLE)
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(
            @PathVariable @NotNull Long userId,
            @RequestBody List<Long> roleIds) {
        log.info("Assigning roles {} to user {}", roleIds, userId);
        userManagementService.assignRolesToUser(userId, roleIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping("/{userId}/roles")
    @RequirePermission(Permission.USER_REMOVE_ROLE)
    public ResponseEntity<ApiResponse<Void>> removeRolesFromUser(
            @PathVariable @NotNull Long userId,
            @RequestBody List<Long> roleIds) {
        log.info("Removing roles {} from user {}", roleIds, userId);
        userManagementService.removeRolesFromUser(userId, roleIds);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 更新用户状态
     */
    @PatchMapping("/{id}/status")
    @RequirePermission(Permission.USER_STATUS_UPDATE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable @NotNull Long id,
            @RequestParam User.UserStatus status) {
        log.info("Updating user {} status to {}", id, status);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .status(status)
                .build();

        UserResponse response = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userManagementService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userManagementService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone")
    public ResponseEntity<ApiResponse<Boolean>> checkPhone(@RequestParam String phone) {
        boolean exists = userManagementService.existsByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}