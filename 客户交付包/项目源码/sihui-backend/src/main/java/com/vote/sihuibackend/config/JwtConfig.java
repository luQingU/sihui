package com.vote.sihuibackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * JWT 配置类 - 优化性能和安全性
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtConfig {

    /**
     * JWT 密钥 - 必须至少64个字符以满足HS512安全要求
     */
    @NotBlank(message = "JWT 密钥不能为空")
    @Size(min = 64, message = "JWT 密钥长度必须至少64个字符以确保HS512算法安全")
    private String secret;

    /**
     * 访问令牌过期时间（毫秒）- 建议不超过24小时
     */
    @Min(value = 300000, message = "访问令牌过期时间不能少于5分钟")
    private Long expiration = 86400000L; // 24小时

    /**
     * 刷新令牌过期时间（毫秒）- 建议不超过7天
     */
    @Min(value = 3600000, message = "刷新令牌过期时间不能少于1小时")
    private Long refreshExpiration = 604800000L; // 7天

    /**
     * 令牌发行者
     */
    @NotBlank(message = "JWT 发行者不能为空")
    private String issuer = "sihui-training-platform";

    /**
     * 令牌受众
     */
    @NotBlank(message = "JWT 受众不能为空")
    private String audience = "sihui-users";

    /**
     * 允许的时钟偏差（秒）- 用于处理服务器时间差异
     */
    @Min(value = 0, message = "时钟偏差不能为负数")
    private Long clockSkewSeconds = 5L;

    /**
     * 是否启用令牌黑名单功能
     */
    private boolean blacklistEnabled = false;

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(Long clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public boolean isBlacklistEnabled() {
        return blacklistEnabled;
    }

    public void setBlacklistEnabled(boolean blacklistEnabled) {
        this.blacklistEnabled = blacklistEnabled;
    }
}