package com.vote.sihuibackend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Redis缓存配置类
 * 实现高级缓存策略和优化
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
@Slf4j
public class RedisCacheConfig extends CachingConfigurerSupport {

    @Value("${spring.cache.redis.time-to-live:PT30M}")
    private Duration defaultTtl;

    @Value("${spring.cache.redis.cache-null-values:false}")
    private boolean cacheNullValues;

    @Value("${spring.cache.redis.key-prefix:sihui:cache:}")
    private String keyPrefix;

    /**
     * 自定义键生成器
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");

            for (Object param : params) {
                if (param != null) {
                    sb.append(param.toString()).append(":");
                } else {
                    sb.append("null:");
                }
            }

            // 移除最后一个冒号
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ':') {
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        };
    }

    /**
     * 缓存错误处理器
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache,
                    Object key) {
                log.warn("缓存获取失败 - Cache: {}, Key: {}, Error: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache,
                    Object key, Object value) {
                log.warn("缓存写入失败 - Cache: {}, Key: {}, Error: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache,
                    Object key) {
                log.warn("缓存清除失败 - Cache: {}, Key: {}, Error: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                log.warn("缓存清空失败 - Cache: {}, Error: {}", cache.getName(), exception.getMessage());
            }
        };
    }

    /**
     * Redis缓存管理器
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 创建自定义的JSON序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .computePrefixWith(cacheName -> keyPrefix + cacheName + ":")
                .disableCachingNullValues();

        if (!cacheNullValues) {
            defaultConfig = defaultConfig.disableCachingNullValues();
        }

        // 为不同业务场景配置不同的缓存策略
        Map<String, RedisCacheConfiguration> cacheConfigurations = createCacheConfigurations(defaultConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();
    }

    /**
     * 创建不同业务场景的缓存配置
     */
    private Map<String, RedisCacheConfiguration> createCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

        // 用户相关缓存 - 30分钟，高频访问
        configurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configurations.put("usersByStatus", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configurations.put("usersByRole", defaultConfig.entryTtl(Duration.ofHours(1)));
        configurations.put("usersByRoleName", defaultConfig.entryTtl(Duration.ofHours(1)));
        configurations.put("userStats", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 权限相关缓存 - 1小时，变化频率低
        configurations.put("userPermissions", defaultConfig.entryTtl(Duration.ofHours(1)));
        configurations.put("userPermissionCodes", defaultConfig.entryTtl(Duration.ofHours(1)));
        configurations.put("rolePermissions", defaultConfig.entryTtl(Duration.ofHours(2)));
        configurations.put("userAdmin", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 文档相关缓存 - 15分钟，内容可能更新
        configurations.put("documents", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configurations.put("documentsByStatus", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configurations.put("documentsByCategory", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configurations.put("documentsByCategoryStatus", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configurations.put("publicDocuments", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        configurations.put("documentsByUploader", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configurations.put("similarDocuments", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configurations.put("documentsByFileType", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 统计数据缓存 - 5分钟，需要实时性
        configurations.put("documentStats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configurations.put("recentDocuments", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configurations.put("popularDocuments", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 问卷相关缓存 - 20分钟，分析结果相对稳定
        configurations.put("questionnaireStats", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        configurations.put("questionnaireAnalysis", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configurations.put("reportTemplates", defaultConfig.entryTtl(Duration.ofHours(2)));

        // AI相关缓存 - 1小时，计算成本较高
        configurations.put("aiResponses", defaultConfig.entryTtl(Duration.ofHours(1)));
        configurations.put("documentVectors", defaultConfig.entryTtl(Duration.ofHours(2)));
        configurations.put("similarityResults", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return configurations;
    }

    /**
     * Redis模板配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 缓存预热服务
     */
    @Bean
    public CacheWarmupService cacheWarmupService() {
        return new CacheWarmupService();
    }

    /**
     * 缓存监控服务
     */
    @Bean
    public CacheMonitorService cacheMonitorService() {
        return new CacheMonitorService();
    }

    /**
     * 缓存预热服务实现
     */
    public static class CacheWarmupService {

        public void warmupUserCache() {
            log.info("开始预热用户缓存...");
            // 这里可以预加载常用的用户数据
            // 例如：活跃用户、管理员用户等
        }

        public void warmupDocumentCache() {
            log.info("开始预热文档缓存...");
            // 预加载热门文档、最新文档等
        }

        public void warmupPermissionCache() {
            log.info("开始预热权限缓存...");
            // 预加载角色权限映射
        }
    }

    /**
     * 缓存监控服务实现
     */
    public static class CacheMonitorService {

        public void logCacheStatistics() {
            log.info("缓存统计信息记录...");
            // 记录缓存命中率、大小等统计信息
        }

        public void alertOnCacheIssues() {
            log.info("缓存问题告警检查...");
            // 检查缓存异常情况并告警
        }
    }
}