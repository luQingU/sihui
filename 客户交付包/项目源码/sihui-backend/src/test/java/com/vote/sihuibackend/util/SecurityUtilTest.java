package com.vote.sihuibackend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityUtil测试类
 */
public class SecurityUtilTest {

    @Test
    public void testContainsSqlInjection_SafeTestText() {
        // 测试安全的测试文本不应该被标记为SQL注入
        assertFalse(SecurityUtil.containsSqlInjection("SQL injection"),
                "包含'SQL injection'的测试文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("XSS attack"),
                "包含'XSS attack'的测试文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("security test"),
                "包含'security test'的测试文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("test data"),
                "包含'test data'的测试文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("example text"),
                "包含'example text'的测试文本不应该被标记为攻击");
    }

    @Test
    public void testContainsSqlInjection_RealAttacks() {
        // 测试真正的SQL注入攻击应该被检测到
        assertTrue(SecurityUtil.containsSqlInjection("1' OR '1'='1"),
                "真正的SQL注入攻击应该被检测到");
        assertTrue(SecurityUtil.containsSqlInjection("admin'--"),
                "SQL注释攻击应该被检测到");
        assertTrue(SecurityUtil.containsSqlInjection("1=1"),
                "简单的SQL条件攻击应该被检测到");
        assertTrue(SecurityUtil.containsSqlInjection("UNION SELECT * FROM users"),
                "UNION SELECT攻击应该被检测到");
        assertTrue(SecurityUtil.containsSqlInjection("'; DROP TABLE users; --"),
                "删除表攻击应该被检测到");
    }

    @Test
    public void testContainsSqlInjection_NormalText() {
        // 测试正常文本不应该被标记为攻击
        assertFalse(SecurityUtil.containsSqlInjection("hello world"),
                "正常文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("user@example.com"),
                "邮箱地址不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("我要选择一个产品"),
                "包含'选择'的中文不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("selection of items"),
                "包含'selection'的正常英文不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("please insert the disk"),
                "包含'insert'的正常英文不应该被标记为攻击");
    }

    @Test
    public void testContainsXss_SafeTestText() {
        // 测试XSS检测也不会误报测试文本
        assertFalse(SecurityUtil.containsXss("XSS attack"),
                "包含'XSS attack'的测试文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsXss("security test"),
                "包含'security test'的测试文本不应该被标记为攻击");
    }

    @Test
    public void testContainsXss_RealAttacks() {
        // 测试真正的XSS攻击应该被检测到
        assertTrue(SecurityUtil.containsXss("<script>alert('xss')</script>"),
                "script标签攻击应该被检测到");
        assertTrue(SecurityUtil.containsXss("javascript:alert('xss')"),
                "javascript伪协议攻击应该被检测到");
        assertTrue(SecurityUtil.containsXss("<img onerror='alert(1)'>"),
                "onerror事件攻击应该被检测到");
    }

    @Test
    public void testSanitizeInput() {
        // 测试输入清理功能
        assertEquals("hello world", SecurityUtil.sanitizeInput("hello world"),
                "正常文本应该保持不变");
        assertEquals("&lt;script&gt;", SecurityUtil.sanitizeInput("<script>"),
                "script标签应该被转义");
        assertEquals("Tom &amp; Jerry", SecurityUtil.sanitizeInput("Tom & Jerry"),
                "特殊字符应该被转义");
    }

    @Test
    public void testIsValidEmail() {
        // 测试邮箱验证
        assertTrue(SecurityUtil.isValidEmail("user@example.com"),
                "有效邮箱应该通过验证");
        assertTrue(SecurityUtil.isValidEmail("test.email+tag@domain.co.uk"),
                "复杂邮箱应该通过验证");
        assertFalse(SecurityUtil.isValidEmail("invalid-email"),
                "无效邮箱应该验证失败");
        assertFalse(SecurityUtil.isValidEmail("@domain.com"),
                "缺少用户名的邮箱应该验证失败");
    }

    @Test
    public void testIsValidPhone() {
        // 测试手机号验证
        assertTrue(SecurityUtil.isValidPhone("13812345678"),
                "有效手机号应该通过验证");
        assertTrue(SecurityUtil.isValidPhone("18900000000"),
                "不同前缀的有效手机号应该通过验证");
        assertFalse(SecurityUtil.isValidPhone("12345678901"),
                "无效前缀的手机号应该验证失败");
        assertFalse(SecurityUtil.isValidPhone("1381234567"),
                "位数不足的手机号应该验证失败");
    }

    @Test
    public void testMaskPhone() {
        // 测试手机号脱敏
        assertEquals("138****5678", SecurityUtil.maskPhone("13812345678"),
                "手机号应该正确脱敏");
        assertEquals("123", SecurityUtil.maskPhone("123"),
                "短号码应该保持不变");
    }

    @Test
    public void testMaskEmail() {
        // 测试邮箱脱敏
        assertEquals("te***@example.com", SecurityUtil.maskEmail("test@example.com"),
                "邮箱应该正确脱敏");
        assertEquals("a@b.com", SecurityUtil.maskEmail("a@b.com"),
                "短邮箱应该保持不变");
    }

    @Test
    public void testIsStrongPassword() {
        // 测试密码强度验证
        assertTrue(SecurityUtil.isStrongPassword("Test@123"),
                "符合要求的强密码应该通过验证");
        assertFalse(SecurityUtil.isStrongPassword("weak"),
                "弱密码应该验证失败");
        assertFalse(SecurityUtil.isStrongPassword("NoSymbol123"),
                "缺少特殊字符的密码应该验证失败");
        assertFalse(SecurityUtil.isStrongPassword("test@123"),
                "缺少大写字母的密码应该验证失败");
    }

    @Test
    public void testSqlInjectionEdgeCases() {
        // 测试边界情况
        assertFalse(SecurityUtil.containsSqlInjection(null),
                "null值不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection(""),
                "空字符串不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("   "),
                "空白字符串不应该被标记为攻击");

        // 测试包含SQL关键字但不是攻击的正常文本
        assertFalse(SecurityUtil.containsSqlInjection("I need to select a product"),
                "正常使用select单词的文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("Please update your profile"),
                "正常使用update单词的文本不应该被标记为攻击");
        assertFalse(SecurityUtil.containsSqlInjection("Delete this message"),
                "正常使用delete单词的文本不应该被标记为攻击");
    }
}