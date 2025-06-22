package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.AuthResponse;
import com.vote.sihuibackend.dto.LoginRequest;
import com.vote.sihuibackend.dto.RefreshTokenRequest;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.security.UserPrincipal;
import com.vote.sihuibackend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private UserPrincipal userPrincipal;
    private User user;

    @BeforeEach
    void setUp() {
        // 设置 JWT 过期时间
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);

        // 创建测试数据
        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .realName("Test User")
                .status(User.UserStatus.ACTIVE)
                .build();

        userPrincipal = new UserPrincipal(
                1L,
                "testuser",
                "encoded_password",
                "test@example.com",
                "Test User",
                User.UserStatus.ACTIVE,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldLoginSuccessfully() {
        // 准备数据
        String accessToken = "access_token";
        String refreshToken = "refresh_token";

        // 配置 Mock
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtUtil.generateToken(userPrincipal)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userPrincipal)).thenReturn(refreshToken);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // 执行测试
        AuthResponse response = authService.login(loginRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(86400L, response.getExpiresIn()); // 24小时转换为秒

        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertTrue(response.getUser().getRoles().contains("USER"));

        // 验证方法调用
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userPrincipal);
        verify(jwtUtil).generateRefreshToken(userPrincipal);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginFails() {
        // 配置 Mock 抛出认证异常
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Authentication failed"));

        // 执行测试并验证异常
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());

        // 验证没有调用令牌生成方法
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // 准备数据
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid_refresh_token");

        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";

        // 配置 Mock
        when(jwtUtil.isRefreshToken("valid_refresh_token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid_refresh_token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userPrincipal);
        when(jwtUtil.validateToken("valid_refresh_token", userPrincipal)).thenReturn(true);
        when(jwtUtil.generateToken(userPrincipal)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(userPrincipal)).thenReturn(newRefreshToken);

        // 执行测试
        AuthResponse response = authService.refreshToken(refreshTokenRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(newRefreshToken, response.getRefreshToken());

        // 验证方法调用
        verify(jwtUtil).isRefreshToken("valid_refresh_token");
        verify(jwtUtil).validateToken("valid_refresh_token", userPrincipal);
        verify(jwtUtil).generateToken(userPrincipal);
        verify(jwtUtil).generateRefreshToken(userPrincipal);
    }

    @Test
    void shouldThrowExceptionForInvalidRefreshToken() {
        // 准备数据
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("invalid_refresh_token");

        // 配置 Mock
        when(jwtUtil.isRefreshToken("invalid_refresh_token")).thenReturn(false);

        // 执行测试并验证异常
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });

        assertEquals("刷新令牌无效", exception.getMessage());

        // 验证没有调用令牌生成方法
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void shouldThrowExceptionForExpiredRefreshToken() {
        // 准备数据
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("expired_refresh_token");

        // 配置 Mock
        when(jwtUtil.isRefreshToken("expired_refresh_token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("expired_refresh_token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userPrincipal);
        when(jwtUtil.validateToken("expired_refresh_token", userPrincipal)).thenReturn(false);

        // 执行测试并验证异常
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });

        assertEquals("刷新令牌无效", exception.getMessage());

        // 验证没有调用令牌生成方法
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void shouldHandleUserNotFoundDuringLastLoginUpdate() {
        // 准备数据
        String accessToken = "access_token";
        String refreshToken = "refresh_token";

        // 配置 Mock
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtUtil.generateToken(userPrincipal)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userPrincipal)).thenReturn(refreshToken);
        when(userRepository.findById(1L)).thenReturn(Optional.empty()); // 用户不存在

        // 执行测试 - 应该仍然成功登录，即使更新最后登录时间失败
        AuthResponse response = authService.login(loginRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());

        // 验证尝试查找用户
        verify(userRepository).findById(1L);
        // 验证没有调用保存方法
        verify(userRepository, never()).save(any(User.class));
    }
}