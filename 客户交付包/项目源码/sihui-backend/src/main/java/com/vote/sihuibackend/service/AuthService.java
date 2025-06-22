package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.AuthResponse;
import com.vote.sihuibackend.dto.LoginRequest;
import com.vote.sihuibackend.dto.RefreshTokenRequest;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.security.UserPrincipal;
import com.vote.sihuibackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * 用户登录
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            log.info("用户尝试登录: {}", loginRequest.getUsernameOrEmail());

            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()));

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 生成 JWT 令牌
            String accessToken = jwtUtil.generateToken(userPrincipal);
            String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

            // 更新最后登录时间
            updateLastLoginTime(userPrincipal.getId());

            // 构建响应
            AuthResponse.UserInfo userInfo = buildUserInfo(userPrincipal);

            log.info("用户登录成功: {}", userPrincipal.getUsername());
            return new AuthResponse(accessToken, refreshToken, jwtExpiration / 1000, userInfo);

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {}, 原因: {}", loginRequest.getUsernameOrEmail(), e.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    /**
     * 刷新访问令牌
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();

            // 验证刷新令牌
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new BadCredentialsException("无效的刷新令牌");
            }

            String username = jwtUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                // 生成新的访问令牌
                String newAccessToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

                // 构建用户信息
                UserPrincipal userPrincipal = (UserPrincipal) userDetails;
                AuthResponse.UserInfo userInfo = buildUserInfo(userPrincipal);

                log.info("令牌刷新成功: {}", username);
                return new AuthResponse(newAccessToken, newRefreshToken, jwtExpiration / 1000, userInfo);
            } else {
                throw new BadCredentialsException("刷新令牌已过期或无效");
            }

        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());
            throw new BadCredentialsException("刷新令牌无效");
        }
    }

    /**
     * 更新用户最后登录时间
     */
    private void updateLastLoginTime(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.warn("更新最后登录时间失败: {}", e.getMessage());
        }
    }

    /**
     * 构建用户信息
     */
    private AuthResponse.UserInfo buildUserInfo(UserPrincipal userPrincipal) {
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return new AuthResponse.UserInfo(
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                userPrincipal.getNickname(),
                roles,
                userPrincipal.getStatus().name());
    }
}