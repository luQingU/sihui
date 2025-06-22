package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.entity.Role;
import com.vote.sihuibackend.enums.Permission;

import java.util.List;
import java.util.Map;

/**
 * 数据库集成测试服务接口
 * 
 * @author Sihui System
 * @since 1.0.0
 */
public interface DatabaseIntegrationService {

    /**
     * 测试数据库连接
     * 
     * @return 连接状态信息
     */
    Map<String, Object> testDatabaseConnection();

    /**
     * 测试用户管理CRUD操作
     * 
     * @return 测试结果
     */
    Map<String, Object> testUserManagementOperations();

    /**
     * 测试角色和权限系统
     * 
     * @return 测试结果
     */
    Map<String, Object> testRoleAndPermissionSystem();

    /**
     * 初始化基础数据
     * 
     * @return 初始化结果
     */
    Map<String, Object> initializeBaseData();

    /**
     * 验证数据一致性
     * 
     * @return 验证结果
     */
    Map<String, Object> validateDataConsistency();

    /**
     * 创建测试用户
     * 
     * @param request 用户创建请求
     * @return 创建的用户
     */
    UserResponse createTestUser(UserCreateRequest request);

    /**
     * 测试权限检查
     * 
     * @param userId     用户ID
     * @param permission 权限
     * @return 权限检查结果
     */
    boolean testPermissionCheck(Long userId, Permission permission);

    /**
     * 清理测试数据
     * 
     * @return 清理结果
     */
    Map<String, Object> cleanupTestData();
}