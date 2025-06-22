package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.annotation.RequirePermission;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.service.PerformanceOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 性能优化控制器
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceOptimizationController {

    private final PerformanceOptimizationService performanceOptimizationService;

    /**
     * 分析数据库查询性能
     */
    @GetMapping("/analysis/query")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> analyzeQueryPerformance() {
        log.info("开始执行数据库查询性能分析");

        try {
            Map<String, Object> result = performanceOptimizationService.analyzeQueryPerformance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("数据库查询性能分析失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "数据库查询性能分析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 优化用户查询性能
     */
    @PostMapping("/optimization/user-queries")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> optimizeUserQueries() {
        log.info("开始执行用户查询性能优化");

        try {
            Map<String, Object> result = performanceOptimizationService.optimizeUserQueries();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("用户查询性能优化失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "用户查询性能优化失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 分析和优化缓存策略
     */
    @GetMapping("/analysis/caching")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> analyzeCaching() {
        log.info("开始执行缓存策略分析");

        try {
            Map<String, Object> result = performanceOptimizationService.optimizeCaching();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("缓存策略分析失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "缓存策略分析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 审查代码最佳实践
     */
    @GetMapping("/review/best-practices")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> reviewBestPractices() {
        log.info("开始执行代码最佳实践审查");

        try {
            Map<String, Object> result = performanceOptimizationService.reviewBestPractices();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("代码最佳实践审查失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "代码最佳实践审查失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 执行性能基准测试
     */
    @PostMapping("/benchmark")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> performBenchmarkTests() {
        log.info("开始执行性能基准测试");

        try {
            Map<String, Object> result = performanceOptimizationService.performBenchmarkTests();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("性能基准测试失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "性能基准测试失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 分析内存使用情况
     */
    @GetMapping("/analysis/memory")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> analyzeMemoryUsage() {
        log.info("开始执行内存使用分析");

        try {
            Map<String, Object> result = performanceOptimizationService.analyzeMemoryUsage();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("内存使用分析失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "内存使用分析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 审查数据库索引
     */
    @GetMapping("/review/database-indexes")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> reviewDatabaseIndexes() {
        log.info("开始执行数据库索引审查");

        try {
            Map<String, Object> result = performanceOptimizationService.reviewDatabaseIndexes();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("数据库索引审查失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "数据库索引审查失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 分析API性能
     */
    @GetMapping("/analysis/api")
    @RequirePermission(Permission.USER_READ)
    public ResponseEntity<Map<String, Object>> analyzeApiPerformance() {
        log.info("开始执行API性能分析");

        try {
            Map<String, Object> result = performanceOptimizationService.analyzeApiPerformance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("API性能分析失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "API性能分析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 生成性能优化报告
     */
    @PostMapping("/report/optimization")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> generateOptimizationReport() {
        log.info("开始生成性能优化报告");

        try {
            Map<String, Object> result = performanceOptimizationService.generateOptimizationReport();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("性能优化报告生成失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "性能优化报告生成失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 执行全面的性能审查
     */
    @PostMapping("/review/comprehensive")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> performComprehensiveReview() {
        log.info("开始执行全面性能审查");

        try {
            Map<String, Object> result = performanceOptimizationService.performComprehensiveReview();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("全面性能审查失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "全面性能审查失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}