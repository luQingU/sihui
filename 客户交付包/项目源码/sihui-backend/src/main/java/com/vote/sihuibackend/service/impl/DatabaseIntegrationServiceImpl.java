package com.vote.sihuibackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.entity.Role;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.entity.User.UserStatus;
import com.vote.sihuibackend.entity.UserRole;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.repository.RoleRepository;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.repository.UserRoleRepository;
import com.vote.sihuibackend.service.DatabaseIntegrationService;
import com.vote.sihuibackend.service.PermissionService;
import com.vote.sihuibackend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库集成测试服务实现类
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseIntegrationServiceImpl implements DatabaseIntegrationService {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserManagementService userManagementService;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始测试数据库连接...");

            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();

                result.put("success", true);
                result.put("databaseProductName", metaData.getDatabaseProductName());
                result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                result.put("driverName", metaData.getDriverName());
                result.put("driverVersion", metaData.getDriverVersion());
                result.put("url", metaData.getURL());
                result.put("userName", metaData.getUserName());
                result.put("catalogName", connection.getCatalog());
                result.put("autoCommit", connection.getAutoCommit());
                result.put("readOnly", connection.isReadOnly());
                result.put("transactionIsolation", connection.getTransactionIsolation());
                result.put("timestamp", LocalDateTime.now());

                log.info("数据库连接测试成功: {}", metaData.getDatabaseProductName());

            }
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> testUserManagementOperations() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始测试用户管理CRUD操作...");

            // 测试用户创建
            UserCreateRequest createRequest = new UserCreateRequest();
            createRequest.setUsername("testuser_" + System.currentTimeMillis());
            createRequest.setEmail("test_" + System.currentTimeMillis() + "@example.com");
            createRequest.setPassword("TestPassword123!");
            createRequest.setRealName("测试用户");
            createRequest.setPhone("13800138000");

            UserResponse createdUser = userManagementService.createUser(createRequest);
            result.put("userCreation", "SUCCESS");
            result.put("createdUserId", createdUser.getId());

            // 测试用户查询
            Optional<UserResponse> foundUser = userManagementService.getUserById(createdUser.getId());
            result.put("userQuery", foundUser.isPresent() ? "SUCCESS" : "FAILED");

            // 测试用户列表查询
            List<UserResponse> users = userManagementService.getAllUsers();
            result.put("userList", "SUCCESS");
            result.put("totalUsers", users.size());

            // 测试用户搜索
            List<UserResponse> searchResults = userManagementService.searchUsers(createRequest.getUsername());
            result.put("userSearch", searchResults.size() > 0 ? "SUCCESS" : "FAILED");

            // 测试用户存在性检查
            boolean exists = userManagementService.existsByUsername(createRequest.getUsername());
            result.put("userExists", exists ? "SUCCESS" : "FAILED");

            result.put("success", true);
            result.put("timestamp", LocalDateTime.now());

            log.info("用户管理CRUD操作测试成功");

        } catch (Exception e) {
            log.error("用户管理CRUD操作测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> testRoleAndPermissionSystem() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始测试角色和权限系统...");

            // 测试角色创建 - 使用JSON字符串存储权限
            Role testRole = new Role();
            testRole.setName("TEST_ROLE_" + System.currentTimeMillis());
            testRole.setDisplayName("测试角色");
            testRole.setDescription("用于测试的角色");

            // 将权限列表转换为JSON字符串
            List<String> permissionCodes = Arrays.asList(
                    Permission.USER_VIEW.getCode(),
                    Permission.USER_CREATE.getCode());
            try {
                testRole.setPermissions(objectMapper.writeValueAsString(permissionCodes));
            } catch (JsonProcessingException e) {
                log.error("权限序列化失败", e);
                testRole.setPermissions("[]");
            }

            Role savedRole = roleRepository.save(testRole);
            result.put("roleCreation", "SUCCESS");
            result.put("createdRoleId", savedRole.getId());

            // 创建测试用户
            UserCreateRequest createRequest = new UserCreateRequest();
            createRequest.setUsername("permissiontest_" + System.currentTimeMillis());
            createRequest.setEmail("permtest_" + System.currentTimeMillis() + "@example.com");
            createRequest.setPassword("TestPassword123!");
            createRequest.setRealName("权限测试用户");

            UserResponse testUser = userManagementService.createUser(createRequest);

            // 手动创建用户角色关联（如果UserManagementService没有此方法）
            UserRole userRole = new UserRole();
            userRole.setUserId(testUser.getId());
            userRole.setRoleId(savedRole.getId());
            userRole.setGrantedAt(LocalDateTime.now());
            userRoleRepository.save(userRole);

            result.put("roleAssignment", "SUCCESS");

            // 测试权限检查
            boolean hasViewPermission = permissionService.hasPermission(testUser.getId(), Permission.USER_VIEW);
            boolean hasCreatePermission = permissionService.hasPermission(testUser.getId(), Permission.USER_CREATE);
            boolean hasDeletePermission = permissionService.hasPermission(testUser.getId(), Permission.USER_DELETE);

            result.put("permissionCheck", "SUCCESS");
            result.put("hasViewPermission", hasViewPermission);
            result.put("hasCreatePermission", hasCreatePermission);
            result.put("hasDeletePermission", hasDeletePermission);

            result.put("success", true);
            result.put("timestamp", LocalDateTime.now());

            log.info("角色和权限系统测试成功");

        } catch (Exception e) {
            log.error("角色和权限系统测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> initializeBaseData() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始初始化基础数据...");

            List<Role> roles = new ArrayList<>();

            // 创建超级管理员角色
            if (!roleRepository.existsByName("SUPER_ADMIN")) {
                Role superAdmin = createRole("SUPER_ADMIN", "超级管理员", "拥有所有权限的超级管理员",
                        Arrays.asList(Permission.values()));
                roles.add(roleRepository.save(superAdmin));
            }

            // 创建管理员角色
            if (!roleRepository.existsByName("ADMIN")) {
                Role admin = createRole("ADMIN", "管理员", "系统管理员", Arrays.asList(
                        Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_EDIT, Permission.USER_DELETE,
                        Permission.ROLE_VIEW, Permission.ROLE_CREATE, Permission.ROLE_EDIT,
                        Permission.CONTENT_VIEW, Permission.CONTENT_CREATE, Permission.CONTENT_EDIT,
                        Permission.CONTENT_DELETE));
                roles.add(roleRepository.save(admin));
            }

            // 创建普通用户角色
            if (!roleRepository.existsByName("USER")) {
                Role user = createRole("USER", "普通用户", "系统普通用户", Arrays.asList(
                        Permission.USER_VIEW_SELF, Permission.USER_EDIT_SELF,
                        Permission.CONTENT_VIEW));
                roles.add(roleRepository.save(user));
            }

            result.put("rolesInitialized", roles.size());
            result.put("roleNames", roles.stream().map(Role::getName).collect(Collectors.toList()));

            // 创建默认管理员用户（如果不存在）
            if (!userRepository.existsByUsername("admin")) {
                UserCreateRequest adminRequest = new UserCreateRequest();
                adminRequest.setUsername("admin");
                adminRequest.setEmail("admin@sihui.com");
                adminRequest.setPassword("Admin123!");
                adminRequest.setRealName("系统管理员");

                UserResponse adminUser = userManagementService.createUser(adminRequest);

                // 分配超级管理员角色
                Optional<Role> superAdminRole = roleRepository.findByName("SUPER_ADMIN");
                if (superAdminRole.isPresent()) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(adminUser.getId());
                    userRole.setRoleId(superAdminRole.get().getId());
                    userRole.setGrantedAt(LocalDateTime.now());
                    userRoleRepository.save(userRole);
                }

                result.put("adminUserCreated", true);
                result.put("adminUserId", adminUser.getId());
            } else {
                result.put("adminUserCreated", false);
                result.put("message", "管理员用户已存在");
            }

            result.put("success", true);
            result.put("timestamp", LocalDateTime.now());

            log.info("基础数据初始化成功");

        } catch (Exception e) {
            log.error("基础数据初始化失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @Override
    public Map<String, Object> validateDataConsistency() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始验证数据一致性...");

            // 检查用户表
            long userCount = userRepository.count();
            result.put("userCount", userCount);

            // 检查角色表
            long roleCount = roleRepository.count();
            result.put("roleCount", roleCount);

            // 检查用户角色关联表
            long userRoleCount = userRoleRepository.count();
            result.put("userRoleCount", userRoleCount);

            // 检查孤立的用户角色关联
            List<UserRole> orphanedUserRoles = userRoleRepository.findAll().stream()
                    .filter(ur -> !userRepository.existsById(ur.getUserId()) ||
                            !roleRepository.existsById(ur.getRoleId()))
                    .collect(Collectors.toList());
            result.put("orphanedUserRoles", orphanedUserRoles.size());

            // 检查用户状态分布
            Map<UserStatus, Long> statusDistribution = userRepository.findAll().stream()
                    .collect(Collectors.groupingBy(User::getStatus, Collectors.counting()));
            result.put("userStatusDistribution", statusDistribution);

            // 检查角色权限完整性
            List<Role> rolesWithInvalidPermissions = roleRepository.findAll().stream()
                    .filter(role -> role.getPermissions() == null || role.getPermissions().trim().isEmpty())
                    .collect(Collectors.toList());
            result.put("rolesWithoutPermissions", rolesWithInvalidPermissions.size());

            result.put("success", true);
            result.put("timestamp", LocalDateTime.now());

            log.info("数据一致性验证完成");

        } catch (Exception e) {
            log.error("数据一致性验证失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    @Override
    public UserResponse createTestUser(UserCreateRequest request) {
        return userManagementService.createUser(request);
    }

    @Override
    public boolean testPermissionCheck(Long userId, Permission permission) {
        return permissionService.hasPermission(userId, permission);
    }

    @Override
    @Transactional
    public Map<String, Object> cleanupTestData() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始清理测试数据...");

            // 删除测试用户（用户名包含test的用户）
            List<User> testUsers = userRepository.findAll().stream()
                    .filter(user -> user.getUsername().contains("test") ||
                            user.getUsername().contains("permissiontest"))
                    .collect(Collectors.toList());

            for (User testUser : testUsers) {
                // 删除用户角色关联
                userRoleRepository.deleteByUserId(testUser.getId());
                // 删除用户
                userRepository.delete(testUser);
            }
            result.put("deletedTestUsers", testUsers.size());

            // 删除测试角色
            List<Role> testRoles = roleRepository.findAll().stream()
                    .filter(role -> role.getName().contains("TEST_ROLE"))
                    .collect(Collectors.toList());

            for (Role testRole : testRoles) {
                // 删除角色关联
                userRoleRepository.deleteByRoleId(testRole.getId());
                // 删除角色
                roleRepository.delete(testRole);
            }
            result.put("deletedTestRoles", testRoles.size());

            result.put("success", true);
            result.put("timestamp", LocalDateTime.now());

            log.info("测试数据清理完成");

        } catch (Exception e) {
            log.error("测试数据清理失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    /**
     * 创建角色的辅助方法
     */
    private Role createRole(String name, String displayName, String description, List<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDisplayName(displayName);
        role.setDescription(description);

        // 将权限列表转换为JSON字符串
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());

        try {
            role.setPermissions(objectMapper.writeValueAsString(permissionCodes));
        } catch (JsonProcessingException e) {
            log.error("权限序列化失败", e);
            role.setPermissions("[]");
        }

        return role;
    }
}