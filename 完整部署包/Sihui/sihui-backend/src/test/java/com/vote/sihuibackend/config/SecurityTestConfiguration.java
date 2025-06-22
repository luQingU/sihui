package com.vote.sihuibackend.config;

import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.security.JwtAuthenticationEntryPoint;
import com.vote.sihuibackend.security.JwtAuthenticationFilter;
import com.vote.sihuibackend.security.UserPrincipal;
import com.vote.sihuibackend.service.PermissionService;
import com.vote.sihuibackend.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 安全测试配置类
 * 提供测试环境所需的安全相关Mock Bean
 */
@TestConfiguration
@Slf4j
public class SecurityTestConfiguration {

    /**
     * Mock PermissionService for testing - 移除@Primary避免Bean冲突
     */
    @Bean
    public PermissionService securityTestPermissionService() {
        PermissionService mock = Mockito.mock(PermissionService.class);

        // 默认权限检查都通过（可以在具体测试中重新设置）
        when(mock.hasPermission(anyLong(), any(Permission.class))).thenReturn(true);
        when(mock.hasAllPermissions(anyLong(), any(Permission[].class))).thenReturn(true);
        when(mock.hasAnyPermission(anyLong(), any(Permission[].class))).thenReturn(true);
        when(mock.canAccessSelfResource(anyLong(), anyLong())).thenReturn(true);

        // 具体权限配置
        when(mock.hasPermission(anyLong(), eq(Permission.USER_CREATE))).thenReturn(true);
        when(mock.hasPermission(anyLong(), eq(Permission.USER_READ))).thenReturn(true);
        when(mock.hasPermission(anyLong(), eq(Permission.USER_UPDATE))).thenReturn(true);
        when(mock.hasPermission(anyLong(), eq(Permission.USER_DELETE))).thenReturn(true);
        when(mock.hasPermission(anyLong(), eq(Permission.SYSTEM_ADMIN))).thenReturn(true);

        return mock;
    }

    /**
     * 使用真实的JwtUtil，但关闭一些验证用于测试
     */
    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        JwtUtil mock = Mockito.mock(JwtUtil.class);

        // Mock JWT验证，但保留基本功能用于集成测试
        when(mock.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        when(mock.getUsernameFromToken(anyString())).thenReturn("testuser");
        when(mock.getUserIdFromToken(anyString())).thenReturn(1L);
        when(mock.isTokenExpired(anyString())).thenReturn(false);
        when(mock.isRefreshToken(anyString())).thenReturn(true);

        // Mock token生成 - 返回可识别的测试token
        when(mock.generateToken(any(UserDetails.class))).thenReturn("test-access-token");
        when(mock.generateRefreshToken(any(UserDetails.class))).thenReturn("test-refresh-token");

        return mock;
    }

    /**
     * 密码编码器
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 真实的UserDetailsService，从数据库加载用户
     */
    @Bean
    @Primary
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
                log.debug("测试环境加载用户: {}", usernameOrEmail);

                Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    log.debug("找到用户: {}, 状态: {}", user.getUsername(), user.getStatus());

                    // 创建简单的测试权限
                    return new UserPrincipal(
                            user.getId(),
                            user.getUsername(),
                            user.getPasswordHash(),
                            user.getEmail(),
                            user.getRealName(),
                            user.getStatus(),
                            Arrays.asList(
                                    new SimpleGrantedAuthority("ROLE_USER"),
                                    new SimpleGrantedAuthority("ROLE_ADMIN")));
                }

                log.warn("用户不存在: {}", usernameOrEmail);
                throw new UsernameNotFoundException("用户不存在: " + usernameOrEmail);
            }
        };
    }

    /**
     * 真实的认证提供者，使用真实的密码验证
     */
    @Bean
    @Primary
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // 设置隐藏用户不存在异常，统一为认证失败
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * Mock JwtAuthenticationFilter for testing
     * 让JWT过滤器在测试中自动认证用户
     */
    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        // 创建Mock的依赖
        JwtUtil mockJwtUtil = jwtUtil();
        UserDetailsService testUserDetailsService = Mockito.mock(UserDetailsService.class);

        // 创建默认的测试用户
        UserPrincipal defaultUser = new UserPrincipal(
                1L,
                "testuser",
                "encoded-password",
                "test@example.com",
                "Test User",
                User.UserStatus.ACTIVE,
                Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(testUserDetailsService.loadUserByUsername(anyString())).thenReturn(defaultUser);

        return new JwtAuthenticationFilter(mockJwtUtil, testUserDetailsService) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
                // 在测试环境中，如果请求有Authorization头，就自动设置认证
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            defaultUser, null, defaultUser.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * Mock JwtAuthenticationEntryPoint for testing
     */
    @Bean
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return Mockito.mock(JwtAuthenticationEntryPoint.class);
    }

    /**
     * 创建带有特定权限的测试用户认证
     */
    public static void setupAuthenticatedUser(Long userId, String username, String... roles) {
        UserPrincipal userPrincipal = new UserPrincipal(
                userId,
                username,
                "encoded-password",
                username + "@example.com",
                username,
                User.UserStatus.ACTIVE,
                Arrays.stream(roles)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null,
                userPrincipal.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * 清除认证上下文
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}