package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.repository.RoleRepository;
import com.vote.sihuibackend.service.impl.PerformanceOptimizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 性能优化服务测试类
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PerformanceOptimizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PerformanceOptimizationServiceImpl performanceOptimizationService;

    private User testUser;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();

        testUsers = Arrays.asList(testUser);
    }

    @Test
    void testAnalyzeQueryPerformance() {
        // 准备测试数据
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(testUsers));
        when(userRepository.findByStatus(any(User.UserStatus.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(testUsers));
        when(userRepository.searchUsers(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(testUsers));

        // 执行测试
        Map<String, Object> result = performanceOptimizationService.analyzeQueryPerformance();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("userCount")).isEqualTo(100L);
        assertThat(result.get("queryTimes")).isNotNull();
        assertThat(result.get("timestamp")).isNotNull();
    }

    @Test
    void testOptimizeUserQueries() {
        // 执行测试
        Map<String, Object> result = performanceOptimizationService.optimizeUserQueries();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("optimizations")).isNotNull();
        assertThat(result.get("recommendations")).isNotNull();
        assertThat(result.get("indexSuggestions")).isEqualTo(5);
        assertThat(result.get("timestamp")).isNotNull();
    }

    @Test
    void testOptimizeCaching() {
        // 执行测试
        Map<String, Object> result = performanceOptimizationService.optimizeCaching();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("currentCaching")).isNotNull();
        assertThat(result.get("recommendations")).isNotNull();
        assertThat(result.get("implementations")).isNotNull();
        assertThat(result.get("suggestedConfig")).isNotNull();
        assertThat(result.get("timestamp")).isNotNull();
    }

    @Test
    void testReviewBestPractices() {
        // 执行测试
        Map<String, Object> result = performanceOptimizationService.reviewBestPractices();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("goodPractices")).isNotNull();
        assertThat(result.get("improvements")).isNotNull();
        assertThat(result.get("criticalIssues")).isNotNull();
        assertThat(result.get("overallScore")).isNotNull();
        assertThat(result.get("timestamp")).isNotNull();

        // 验证评分是合理的
        Integer overallScore = (Integer) result.get("overallScore");
        assertThat(overallScore).isBetween(0, 100);
    }

    @Test
    void testAnalyzeMemoryUsage() {
        // 执行测试
        Map<String, Object> result = performanceOptimizationService.analyzeMemoryUsage();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("heapMemory")).isNotNull();
        assertThat(result.get("nonHeapMemory")).isNotNull();
        assertThat(result.get("memoryAdvice")).isNotNull();
        assertThat(result.get("jvmAdvice")).isNotNull();
        assertThat(result.get("timestamp")).isNotNull();
    }

    @Test
    void testReviewDatabaseIndexes() {
        // 执行测试
        Map<String, Object> result = performanceOptimizationService.reviewDatabaseIndexes();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("existingIndexes")).isNotNull();
        assertThat(result.get("recommendedIndexes")).isNotNull();
        assertThat(result.get("optimizations")).isNotNull();
        assertThat(result.get("performanceImpact")).isNotNull();
        assertThat(result.get("totalRecommendations")).isNotNull();
        assertThat(result.get("timestamp")).isNotNull();
    }

    @Test
    void testAnalyzeApiPerformance() {
        // 执行测试
        Map<String, Object> result = performanceOptimizationService.analyzeApiPerformance();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("performanceMetrics")).isNotNull();
        assertThat(result.get("bottlenecks")).isNotNull();
        assertThat(result.get("optimizations")).isNotNull();
        assertThat(result.get("responseTimeOptimization")).isNotNull();
        assertThat(result.get("concurrencyAnalysis")).isNotNull();
        assertThat(result.get("timestamp")).isNotNull();
    }
}