package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.annotation.RequirePermission;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.service.SecurityTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 安全测试控制器
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityTestController {

    private final SecurityTestService securityTestService;

    /**
     * JWT安全性测试
     */
    @GetMapping("/test/jwt")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testJwtSecurity() {
        log.info("执行JWT安全性测试");
        Map<String, Object> result = securityTestService.testJwtSecurity();
        return ResponseEntity.ok(result);
    }

    /**
     * 密码加密强度测试
     */
    @GetMapping("/test/password")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testPasswordEncryption() {
        log.info("执行密码加密强度测试");
        Map<String, Object> result = securityTestService.testPasswordEncryption();
        return ResponseEntity.ok(result);
    }

    /**
     * SQL注入防护测试
     */
    @GetMapping("/test/sql-injection")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testSqlInjectionProtection() {
        log.info("执行SQL注入防护测试");
        Map<String, Object> result = securityTestService.testSqlInjectionProtection();
        return ResponseEntity.ok(result);
    }

    /**
     * XSS防护测试
     */
    @GetMapping("/test/xss")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testXssProtection() {
        log.info("执行XSS防护测试");
        Map<String, Object> result = securityTestService.testXssProtection();
        return ResponseEntity.ok(result);
    }

    /**
     * CSRF防护测试
     */
    @GetMapping("/test/csrf")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testCsrfProtection() {
        log.info("执行CSRF防护测试");
        Map<String, Object> result = securityTestService.testCsrfProtection();
        return ResponseEntity.ok(result);
    }

    /**
     * 访问控制(RBAC)测试
     */
    @GetMapping("/test/access-control")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testAccessControl() {
        log.info("执行访问控制测试");
        Map<String, Object> result = securityTestService.testAccessControl();
        return ResponseEntity.ok(result);
    }

    /**
     * 会话管理测试
     */
    @GetMapping("/test/session")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testSessionManagement() {
        log.info("执行会话管理测试");
        Map<String, Object> result = securityTestService.testSessionManagement();
        return ResponseEntity.ok(result);
    }

    /**
     * API安全性测试
     */
    @GetMapping("/test/api")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testApiSecurity() {
        log.info("执行API安全性测试");
        Map<String, Object> result = securityTestService.testApiSecurity();
        return ResponseEntity.ok(result);
    }

    /**
     * 敏感数据保护测试
     */
    @GetMapping("/test/data-protection")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> testSensitiveDataProtection() {
        log.info("执行敏感数据保护测试");
        Map<String, Object> result = securityTestService.testSensitiveDataProtection();
        return ResponseEntity.ok(result);
    }

    /**
     * 执行安全漏洞评估
     */
    @PostMapping("/assessment")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> performVulnerabilityAssessment() {
        log.info("执行安全漏洞评估");
        Map<String, Object> result = securityTestService.performVulnerabilityAssessment();
        return ResponseEntity.ok(result);
    }

    /**
     * 生成安全测试报告
     */
    @GetMapping("/report")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> generateSecurityReport() {
        log.info("生成安全测试报告");
        Map<String, Object> result = securityTestService.generateSecurityReport();
        return ResponseEntity.ok(result);
    }

    /**
     * 执行完整的安全测试套件
     */
    @PostMapping("/full-test")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    public ResponseEntity<Map<String, Object>> performFullSecurityTest() {
        log.info("执行完整的安全测试套件");

        Map<String, Object> fullTestResult = new java.util.HashMap<>();

        try {
            // 执行所有测试
            fullTestResult.put("jwtTest", securityTestService.testJwtSecurity());
            fullTestResult.put("passwordTest", securityTestService.testPasswordEncryption());
            fullTestResult.put("sqlInjectionTest", securityTestService.testSqlInjectionProtection());
            fullTestResult.put("xssTest", securityTestService.testXssProtection());
            fullTestResult.put("csrfTest", securityTestService.testCsrfProtection());
            fullTestResult.put("accessControlTest", securityTestService.testAccessControl());
            fullTestResult.put("sessionTest", securityTestService.testSessionManagement());
            fullTestResult.put("apiTest", securityTestService.testApiSecurity());
            fullTestResult.put("dataProtectionTest", securityTestService.testSensitiveDataProtection());

            // 生成综合报告
            fullTestResult.put("assessment", securityTestService.performVulnerabilityAssessment());
            fullTestResult.put("report", securityTestService.generateSecurityReport());

            fullTestResult.put("success", true);
            fullTestResult.put("message", "完整安全测试执行成功");
            fullTestResult.put("timestamp", java.time.LocalDateTime.now());

            log.info("完整安全测试执行成功");

        } catch (Exception e) {
            log.error("完整安全测试执行失败", e);
            fullTestResult.put("success", false);
            fullTestResult.put("error", e.getMessage());
            fullTestResult.put("timestamp", java.time.LocalDateTime.now());
        }

        return ResponseEntity.ok(fullTestResult);
    }
}