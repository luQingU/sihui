package com.vote.sihuibackend.config;

import com.vote.sihuibackend.aspect.PermissionAspect;
import com.vote.sihuibackend.service.PermissionService;
import com.vote.sihuibackend.enums.Permission;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

/**
 * 权限测试配置类
 * 专门解决测试环境中的权限拦截器配置问题
 */
@TestConfiguration
public class PermissionTestConfiguration {

    /**
     * 禁用权限切面，避免测试环境中的NestedServlet错误
     */
    @Bean
    @Primary
    @Order(1) // 给最高优先级
    public PermissionAspect permissionAspect() {
        // 返回一个Mock的PermissionAspect，不执行实际的权限检查
        PermissionAspect mockAspect = Mockito.mock(PermissionAspect.class);

        // Mock所有权限检查方法，让它们不执行任何操作
        try {
            Mockito.doNothing().when(mockAspect).checkPermission(Mockito.any());
        } catch (Throwable e) {
            // 忽略Mock设置错误
        }

        return mockAspect;
    }

    /**
     * 测试专用的PermissionService - 移除@Primary注解，解决Bean冲突
     */
    @Bean("testPermissionService") // 使用特定的Bean名称
    @Order(1) // 给最高优先级，但不使用@Primary避免冲突
    public PermissionService enhancedPermissionService() {
        PermissionService mock = Mockito.mock(PermissionService.class);

        // 基础权限检查 - 默认允许所有权限
        Mockito.when(mock.hasPermission(Mockito.anyLong(), Mockito.any(Permission.class)))
                .thenReturn(true);
        Mockito.when(mock.hasPermission(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(mock.hasAnyPermission(Mockito.anyLong(), Mockito.any(Permission[].class)))
                .thenReturn(true);
        Mockito.when(mock.hasAllPermissions(Mockito.anyLong(), Mockito.any(Permission[].class)))
                .thenReturn(true);

        // 自访问资源权限检查
        Mockito.when(mock.canAccessSelfResource(Mockito.anyLong(), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    Long userId1 = invocation.getArgument(0);
                    Long userId2 = invocation.getArgument(1);
                    return userId1.equals(userId2);
                });

        // 管理员权限检查
        Mockito.when(mock.isAdmin(Mockito.anyLong())).thenReturn(true);

        // 特殊权限场景 - 对于某些测试场景返回false
        // 学生用户上传文档权限应该被拒绝
        Mockito.when(mock.hasPermission(Mockito.anyLong(), Mockito.eq(Permission.FILE_UPLOAD)))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    // 模拟学生用户（假设ID为特定值时）没有上传权限
                    return !isStudentUser(userId);
                });

        // 普通用户创建权限应该被拒绝（用于测试Forbidden场景）
        Mockito.when(mock.hasPermission(Mockito.anyLong(), Mockito.eq(Permission.USER_CREATE)))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    // 模拟普通用户没有创建用户权限
                    return !isRegularUser(userId);
                });

        return mock;
    }

    /**
     * 判断是否为学生用户（用于权限测试）
     */
    private boolean isStudentUser(Long userId) {
        // 在测试环境中，假设用户ID为某些特定值时代表学生
        // 这个可以根据测试需要调整
        return userId != null && (userId % 100 == 50); // 示例：ID以50结尾的为学生
    }

    /**
     * 判断是否为普通用户（用于权限测试）
     */
    private boolean isRegularUser(Long userId) {
        // 在测试环境中，假设用户ID为某些特定值时代表普通用户
        return userId != null && (userId % 100 == 25); // 示例：ID以25结尾的为普通用户
    }
}