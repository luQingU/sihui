package com.vote.sihuibackend.config;

import com.vote.sihuibackend.service.*;
import com.vote.sihuibackend.enums.Permission;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 通用测试配置类
 * 提供所有测试需要的Mock服务和配置
 */
@TestConfiguration
public class CommonTestConfiguration {

        // Mock外部服务以避免实际调用
        @MockBean
        private DeepSeekService deepSeekService;

        @MockBean
        private TextSearchService textSearchService;

        @MockBean
        private AsyncDocumentProcessingService asyncDocumentProcessingService;

        @MockBean
        private ChatMemoryService chatMemoryService;

        @MockBean
        private KnowledgeEnhancedChatService knowledgeEnhancedChatService;

        @MockBean
        private OssService ossService;

        @MockBean
        private PerformanceMonitorService performanceMonitorService;

        @MockBean
        private AdvancedCacheService advancedCacheService;

        @MockBean
        private DataEncryptionService dataEncryptionService;

        // Mock DatabaseIntegrationService 以避免复杂的集成问题
        @MockBean
        private DatabaseIntegrationService databaseIntegrationService;

        /**
         * 提供测试专用的缓存管理器 - 不依赖Redis
         */
        @Bean
        @Primary
        public CacheManager testCacheManager() {
                return new ConcurrentMapCacheManager(
                                "testCache",
                                "userCache",
                                "permissionCache",
                                "users", // UserRepository 需要的缓存
                                "usersByStatus",
                                "usersByRole",
                                "usersByRoleName",
                                "userStats");
        }

        /**
         * Mock RedisTemplate 避免Redis依赖
         */
        @Bean
        @Primary
        @SuppressWarnings("unchecked")
        public RedisTemplate<String, Object> redisTemplate() {
                RedisTemplate<String, Object> template = Mockito.mock(RedisTemplate.class);

                // 配置基本的Mock行为
                Mockito.when(template.hasKey(Mockito.anyString())).thenReturn(false);
                Mockito.when(template.opsForValue())
                                .thenReturn(Mockito.mock(org.springframework.data.redis.core.ValueOperations.class));
                Mockito.when(template.opsForHash())
                                .thenReturn(Mockito.mock(org.springframework.data.redis.core.HashOperations.class));
                Mockito.when(template.opsForList())
                                .thenReturn(Mockito.mock(org.springframework.data.redis.core.ListOperations.class));
                Mockito.when(template.opsForSet())
                                .thenReturn(Mockito.mock(org.springframework.data.redis.core.SetOperations.class));

                return template;
        }

        /**
         * 测试专用的PermissionService - 只在test profile下生效
         */
        @Bean
        @Primary
        @Profile("test")
        public PermissionService testPermissionService() {
                PermissionService mock = Mockito.mock(PermissionService.class);

                // 基础权限检查 - 默认允许所有权限（测试环境）
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

                // 管理员权限检查 - 在测试中默认为true
                Mockito.when(mock.isAdmin(Mockito.anyLong())).thenReturn(true);

                return mock;
        }
}