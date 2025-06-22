package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.annotation.RequirePermission;
import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.service.DatabaseIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库集成测试控制器
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "数据库集成测试", description = "数据库连接和功能集成测试API")
@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class DatabaseIntegrationController {

    private final DatabaseIntegrationService databaseIntegrationService;

    @Operation(summary = "测试数据库连接", description = "检查数据库连接状态和基本信息")
    @GetMapping("/database/connection")
    @RequirePermission(Permission.SYSTEM_MONITOR)
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        log.info("收到数据库连接测试请求");
        Map<String, Object> result = databaseIntegrationService.testDatabaseConnection();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "测试用户管理操作", description = "测试用户CRUD操作的完整流程")
    @PostMapping("/user-management/test")
    @RequirePermission(Permission.SYSTEM_MONITOR)
    public ResponseEntity<Map<String, Object>> testUserManagementOperations() {
        log.info("收到用户管理操作测试请求");
        Map<String, Object> result = databaseIntegrationService.testUserManagementOperations();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "测试角色权限系统", description = "测试RBAC系统的完整功能")
    @PostMapping("/rbac/test")
    @RequirePermission(Permission.SYSTEM_MONITOR)
    public ResponseEntity<Map<String, Object>> testRoleAndPermissionSystem() {
        log.info("收到角色权限系统测试请求");
        Map<String, Object> result = databaseIntegrationService.testRoleAndPermissionSystem();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "初始化基础数据", description = "创建系统必需的基础角色和管理员用户")
    @PostMapping("/data/initialize")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> initializeBaseData() {
        log.info("收到基础数据初始化请求");
        Map<String, Object> result = databaseIntegrationService.initializeBaseData();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "验证数据一致性", description = "检查数据库数据的完整性和一致性")
    @GetMapping("/data/validate")
    @RequirePermission(Permission.SYSTEM_MONITOR)
    public ResponseEntity<Map<String, Object>> validateDataConsistency() {
        log.info("收到数据一致性验证请求");
        Map<String, Object> result = databaseIntegrationService.validateDataConsistency();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "创建测试用户", description = "创建用于测试的用户账户")
    @PostMapping("/user/test-create")
    @RequirePermission(Permission.USER_CREATE)
    public ResponseEntity<UserResponse> createTestUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("收到创建测试用户请求: {}", request.getUsername());
        UserResponse result = databaseIntegrationService.createTestUser(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "测试权限检查", description = "测试指定用户的权限检查")
    @GetMapping("/permission/test/{userId}/{permission}")
    @RequirePermission(Permission.SYSTEM_MONITOR)
    public ResponseEntity<Map<String, Object>> testPermissionCheck(
            @PathVariable Long userId,
            @PathVariable Permission permission) {
        log.info("收到权限检查测试请求: userId={}, permission={}", userId, permission);

        boolean hasPermission = databaseIntegrationService.testPermissionCheck(userId, permission);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("permission", permission);
        result.put("hasPermission", hasPermission);
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "清理测试数据", description = "清理所有测试产生的数据")
    @DeleteMapping("/data/cleanup")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> cleanupTestData() {
        log.info("收到测试数据清理请求");
        Map<String, Object> result = databaseIntegrationService.cleanupTestData();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "运行完整集成测试", description = "运行所有集成测试的完整流程")
    @PostMapping("/complete-test")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> runCompleteIntegrationTest() {
        log.info("收到完整集成测试请求");

        Map<String, Object> result = new HashMap<>();
        result.put("databaseConnection", databaseIntegrationService.testDatabaseConnection());
        result.put("userManagement", databaseIntegrationService.testUserManagementOperations());
        result.put("rbacSystem", databaseIntegrationService.testRoleAndPermissionSystem());
        result.put("dataConsistency", databaseIntegrationService.validateDataConsistency());
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }
}