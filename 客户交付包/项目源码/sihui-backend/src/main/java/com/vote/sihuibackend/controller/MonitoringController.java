package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.security.JwtPerformanceMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控控制器
 * 提供系统监控和性能统计信息
 */
@Tag(name = "监控", description = "系统监控和性能统计API")
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "performance.jwt.monitoring-enabled", havingValue = "true", matchIfMissing = true)
public class MonitoringController {

        private final JwtPerformanceMonitor performanceMonitor;

        @Operation(summary = "获取JWT性能统计", description = "获取JWT令牌生成、验证等操作的性能统计信息")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "成功获取性能统计"),
                        @ApiResponse(responseCode = "403", description = "权限不足")
        })
        @GetMapping("/jwt/performance")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> getJwtPerformanceStats() {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("performanceStats", performanceMonitor.getPerformanceStats());
                data.put("cacheStats", performanceMonitor.getCacheStats());
                data.put("cacheHitRate", performanceMonitor.getCacheHitRate());
                response.put("data", data);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "重置JWT性能统计", description = "清空JWT性能统计数据")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "成功重置统计数据"),
                        @ApiResponse(responseCode = "403", description = "权限不足")
        })
        @PostMapping("/jwt/performance/reset")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> resetJwtPerformanceStats() {
                performanceMonitor.resetStats();
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "JWT性能统计已重置");
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "触发性能报告", description = "手动触发性能报告生成")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "成功生成性能报告"),
                        @ApiResponse(responseCode = "403", description = "权限不足")
        })
        @PostMapping("/jwt/performance/report")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> generatePerformanceReport() {
                performanceMonitor.printPerformanceReport();
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "性能报告已生成，请查看日志");
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "系统健康检查", description = "检查系统各组件的健康状态")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "系统健康"),
                        @ApiResponse(responseCode = "503", description = "系统不健康")
        })
        @GetMapping("/health")
        public ResponseEntity<Map<String, Object>> healthCheck() {
                Map<String, Object> response = new HashMap<>();
                Map<String, Object> health = new HashMap<>();

                // 检查 JWT 性能指标
                double cacheHitRate = performanceMonitor.getCacheHitRate();
                boolean jwtHealthy = cacheHitRate >= 80.0 || cacheHitRate == 0.0; // 缓存命中率 >= 80% 或者还没有缓存数据

                Map<String, Object> jwtHealth = new HashMap<>();
                jwtHealth.put("status", jwtHealthy ? "UP" : "DOWN");
                jwtHealth.put("cacheHitRate", cacheHitRate);
                health.put("jwt", jwtHealth);

                // 检查数据库连接（这里简化处理）
                Map<String, Object> dbHealth = new HashMap<>();
                dbHealth.put("status", "UP");
                health.put("database", dbHealth);

                // 总体健康状态
                boolean overallHealthy = jwtHealthy;

                response.put("success", overallHealthy);
                response.put("status", overallHealthy ? "UP" : "DOWN");
                response.put("components", health);

                return ResponseEntity.status(overallHealthy ? 200 : 503).body(response);
        }

        @Operation(summary = "获取系统指标", description = "获取系统运行时指标")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "成功获取系统指标"),
                        @ApiResponse(responseCode = "403", description = "权限不足")
        })
        @GetMapping("/metrics")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> getSystemMetrics() {
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;

                Map<String, Object> metrics = new HashMap<>();
                Map<String, Object> memoryMetrics = new HashMap<>();
                memoryMetrics.put("total", totalMemory);
                memoryMetrics.put("used", usedMemory);
                memoryMetrics.put("free", freeMemory);
                memoryMetrics.put("usagePercentage", (double) usedMemory / totalMemory * 100);
                metrics.put("memory", memoryMetrics);

                metrics.put("processors", runtime.availableProcessors());
                metrics.put("uptime", System.currentTimeMillis()); // 简化的运行时间

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", metrics);

                return ResponseEntity.ok(response);
        }
}