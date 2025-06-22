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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;

/**
 * 简化的测试配置类
 * 用于那些需要更简单测试环境的控制器测试
 */
@TestConfiguration
public class SimpleTestConfiguration {

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

        @MockBean
        private DatabaseIntegrationService databaseIntegrationService;

        /**
         * 提供简单的缓存管理器 - 无Redis依赖
         */
        @Bean
        @Primary
        public CacheManager simpleCacheManager() {
                return new ConcurrentMapCacheManager();
        }

        /**
         * Mock RedisTemplate 避免Redis依赖
         */
        @Bean
        @Primary
        @SuppressWarnings("unchecked")
        public RedisTemplate<String, Object> mockRedisTemplate() {
                return Mockito.mock(RedisTemplate.class);
        }

        /**
         * 简化的PermissionService - 默认允许所有权限
         */
        @Bean
        @Primary
        public PermissionService simplePermissionService() {
                PermissionService mock = Mockito.mock(PermissionService.class);

                // 默认权限检查都通过
                Mockito.when(mock.hasPermission(Mockito.anyLong(), Mockito.any(Permission.class)))
                                .thenReturn(true);
                Mockito.when(mock.hasPermission(Mockito.anyLong(), Mockito.anyString()))
                                .thenReturn(true);
                Mockito.when(mock.hasAnyPermission(Mockito.anyLong(), Mockito.any(Permission[].class)))
                                .thenReturn(true);
                Mockito.when(mock.hasAllPermissions(Mockito.anyLong(), Mockito.any(Permission[].class)))
                                .thenReturn(true);
                Mockito.when(mock.canAccessSelfResource(Mockito.anyLong(), Mockito.anyLong()))
                                .thenReturn(true);
                Mockito.when(mock.isAdmin(Mockito.anyLong()))
                                .thenReturn(true);

                return mock;
        }

        /**
         * 测试安全配置 - 允许所有请求通过
         */
        @Configuration
        public static class TestSecurityConfig {
                @Bean
                @Primary
                public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
                        http.csrf().disable()
                                        .authorizeRequests()
                                        .anyRequest().permitAll()
                                        .and()
                                        .httpBasic().disable()
                                        .formLogin().disable()
                                        .logout().disable();

                        return http.build();
                }
        }
}