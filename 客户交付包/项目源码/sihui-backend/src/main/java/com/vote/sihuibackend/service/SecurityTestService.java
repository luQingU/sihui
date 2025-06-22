package com.vote.sihuibackend.service;

import java.util.Map;

/**
 * 安全测试服务接口
 * 
 * @author Sihui System
 * @since 1.0.0
 */
public interface SecurityTestService {

    /**
     * 测试JWT令牌安全性
     * 
     * @return 测试结果
     */
    Map<String, Object> testJwtSecurity();

    /**
     * 测试密码加密强度
     * 
     * @return 测试结果
     */
    Map<String, Object> testPasswordEncryption();

    /**
     * 测试SQL注入防护
     * 
     * @return 测试结果
     */
    Map<String, Object> testSqlInjectionProtection();

    /**
     * 测试XSS防护
     * 
     * @return 测试结果
     */
    Map<String, Object> testXssProtection();

    /**
     * 测试CSRF防护
     * 
     * @return 测试结果
     */
    Map<String, Object> testCsrfProtection();

    /**
     * 测试访问控制（RBAC）
     * 
     * @return 测试结果
     */
    Map<String, Object> testAccessControl();

    /**
     * 测试会话管理
     * 
     * @return 测试结果
     */
    Map<String, Object> testSessionManagement();

    /**
     * 测试API安全性
     * 
     * @return 测试结果
     */
    Map<String, Object> testApiSecurity();

    /**
     * 测试敏感数据保护
     * 
     * @return 测试结果
     */
    Map<String, Object> testSensitiveDataProtection();

    /**
     * 执行安全漏洞评估
     * 
     * @return 评估结果
     */
    Map<String, Object> performVulnerabilityAssessment();

    /**
     * 生成安全测试报告
     * 
     * @return 安全测试报告
     */
    Map<String, Object> generateSecurityReport();
}