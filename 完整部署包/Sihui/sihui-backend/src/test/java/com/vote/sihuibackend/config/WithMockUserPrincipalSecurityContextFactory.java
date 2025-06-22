package com.vote.sihuibackend.config;

import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 用于创建 UserPrincipal 认证上下文的工厂类
 */
public class WithMockUserPrincipalSecurityContextFactory implements WithSecurityContextFactory<WithMockUserPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 创建UserPrincipal
        UserPrincipal userPrincipal = new UserPrincipal(
                annotation.id(),
                annotation.username(),
                "encoded-password", // 测试密码
                annotation.email(),
                annotation.username(), // 使用用户名作为昵称
                User.UserStatus.ACTIVE,
                Arrays.stream(annotation.roles())
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()));

        // 创建认证对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities());

        context.setAuthentication(authentication);
        return context;
    }
}