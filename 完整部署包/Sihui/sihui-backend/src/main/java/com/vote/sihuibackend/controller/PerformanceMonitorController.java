package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.service.PerformanceMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 性能监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceMonitorController {

    private final PerformanceMonitorService performanceMonitorService;

    /**
     * 获取性能统计概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getPerformanceOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            overview.put("performanceStats", performanceMonitorService.getPerformanceStats());
            overview.put("systemResources", performanceMonitorService.getSystemResourceUsage());
            overview.put("threadPools", performanceMonitorService.getThreadPoolStatus());
            overview.put("cacheStats", performanceMonitorService.getCacheHitRateStats());
            overview.put("databaseStatus", performanceMonitorService.getDatabaseConnectionPoolStatus());
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("Error getting performance overview", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取性能概览失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取详细性能统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPerformanceStats() {
        try {
            Map<String, Object> stats = performanceMonitorService.getPerformanceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting performance stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取性能统计失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取系统资源使用情况
     */
    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getSystemResources() {
        try {
            Map<String, Object> resources = performanceMonitorService.getSystemResourceUsage();
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("Error getting system resources", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取系统资源失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取慢查询日志
     */
    @GetMapping("/slow-queries")
    public ResponseEntity<Map<String, Object>> getSlowQueryLog() {
        try {
            Map<String, Object> slowQueries = performanceMonitorService.getSlowQueryLog();
            return ResponseEntity.ok(slowQueries);
        } catch (Exception e) {
            log.error("Error getting slow query log", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取慢查询日志失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 清理性能统计数据
     */
    @DeleteMapping("/stats")
    public ResponseEntity<Map<String, String>> clearPerformanceStats() {
        try {
            performanceMonitorService.clearPerformanceStats();
            log.info("Performance statistics cleared by admin");
            Map<String, String> response = new HashMap<>();
            response.put("message", "性能统计数据已清理");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing performance stats", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "清理性能统计失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取性能健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getPerformanceHealth() {
        try {
            Map<String, Object> health = new HashMap<>();

            // 系统资源健康检查
            Map<String, Object> resources = performanceMonitorService.getSystemResourceUsage();
            @SuppressWarnings("unchecked")
            Map<String, Object> memory = (Map<String, Object>) resources.get("memory");

            boolean memoryHealthy = true;
            if (memory != null && memory.get("heapUsagePercentage") != null) {
                memoryHealthy = (Double) memory.get("heapUsagePercentage") < 80.0;
            }

            // 数据库健康检查
            Map<String, Object> dbStatus = performanceMonitorService.getDatabaseConnectionPoolStatus();
            boolean dbHealthy = (Boolean) dbStatus.getOrDefault("connectionAvailable", false);

            // 综合健康状态
            boolean overallHealthy = memoryHealthy && dbHealthy;

            health.put("status", overallHealthy ? "HEALTHY" : "WARNING");
            health.put("memory", memoryHealthy ? "HEALTHY" : "WARNING");
            health.put("database", dbHealthy ? "HEALTHY" : "ERROR");
            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting performance health", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取性能健康状态失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}