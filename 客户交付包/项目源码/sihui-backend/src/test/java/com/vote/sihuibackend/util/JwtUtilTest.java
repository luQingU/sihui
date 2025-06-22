package com.vote.sihuibackend.util;

import com.vote.sihuibackend.security.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类单元测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserPrincipal testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // 设置测试配置 - 需要至少64字符以满足HS512算法要求（512位）
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "testSecretKeyForJwtUtilTestCases1234567890123456789012345678901234567890abcdefghijklmnopqrstuvwxyz");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24小时
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L); // 7天

        // 创建测试用户
        testUser = new UserPrincipal(
                1L,
                "testuser",
                "password123",
                "test@example.com",
                "Test User",
                null,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldGenerateToken() {
        // 生成令牌
        String token = jwtUtil.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void shouldGenerateRefreshToken() {
        // 生成刷新令牌
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(refreshToken.contains("."));
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // 生成令牌并提取用户名
        String token = jwtUtil.generateToken(testUser);
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertEquals(testUser.getUsername(), extractedUsername);
    }

    @Test
    void shouldExtractExpirationDateFromToken() {
        // 生成令牌并提取过期时间
        String token = jwtUtil.generateToken(testUser);
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void shouldValidateValidToken() {
        // 生成并验证有效令牌
        String token = jwtUtil.generateToken(testUser);
        Boolean isValid = jwtUtil.validateToken(token, testUser);

        assertTrue(isValid);
    }

    @Test
    void shouldRejectTokenWithWrongUser() {
        // 生成令牌，但用不同用户验证
        String token = jwtUtil.generateToken(testUser);

        UserPrincipal differentUser = new UserPrincipal(
                2L,
                "differentuser",
                "password123",
                "different@example.com",
                "Different User",
                null,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    void shouldDetectExpiredToken() {
        // 创建一个短期过期的令牌用于测试
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1毫秒

        String token = jwtUtil.generateToken(testUser);

        // 等待令牌过期
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        String malformedToken = "invalid.token.format";

        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.getUsernameFromToken(malformedToken);
        });
    }

    @Test
    void shouldThrowExceptionForEmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.getUsernameFromToken("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.getUsernameFromToken(null);
        });
    }

    @Test
    void shouldDistinguishBetweenAccessAndRefreshTokens() {
        String accessToken = jwtUtil.generateToken(testUser);
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        assertFalse(jwtUtil.isRefreshToken(accessToken));
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }

    @Test
    void shouldReturnNullForInvalidTokenUserId() {
        String token = jwtUtil.generateToken(testUser);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 由于我们没有在令牌中添加 userId claim，应该返回 null
        assertNull(userId);
    }

    @Test
    void shouldValidateTokenExpirationTime() {
        String token = jwtUtil.generateToken(testUser);
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        // 验证过期时间大约是当前时间 + 24小时
        long expectedExpiration = System.currentTimeMillis() + 86400000L;
        long actualExpiration = expirationDate.getTime();

        // 允许5秒的误差（考虑到测试执行时间）
        assertTrue(Math.abs(actualExpiration - expectedExpiration) < 5000);
    }
}