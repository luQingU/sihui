package com.vote.sihuibackend.config;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自定义安全测试注解，创建 UserPrincipal 认证上下文
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserPrincipalSecurityContextFactory.class)
public @interface WithMockUserPrincipal {

    /**
     * 用户ID
     */
    long id() default 1L;

    /**
     * 用户名
     */
    String username() default "testuser";

    /**
     * 邮箱
     */
    String email() default "test@example.com";

    /**
     * 用户角色
     */
    String[] roles() default { "USER" };
}