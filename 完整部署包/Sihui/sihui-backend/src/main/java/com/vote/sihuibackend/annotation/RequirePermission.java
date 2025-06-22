package com.vote.sihuibackend.annotation;

import com.vote.sihuibackend.enums.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * 用于方法级别的权限控制
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限
     */
    Permission[] value() default {};

    /**
     * 权限检查逻辑：AND(需要所有权限) 或 OR(需要任意一个权限)
     */
    Logic logic() default Logic.AND;

    /**
     * 是否允许用户访问自己的资源
     */
    boolean allowSelf() default false;

    /**
     * 自己资源的参数名（用于提取resourceUserId）
     */
    String selfParam() default "id";

    /**
     * 权限检查逻辑枚举
     */
    enum Logic {
        AND, // 需要所有权限
        OR // 需要任意一个权限
    }
}