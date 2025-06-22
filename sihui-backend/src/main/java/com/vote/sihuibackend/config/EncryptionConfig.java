package com.vote.sihuibackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 加密配置类
 * 管理应用程序的加密设置和密钥配置
 */
@Configuration
@ConfigurationProperties(prefix = "app.encryption")
@Data
public class EncryptionConfig {

    /**
     * 是否启用数据加密
     */
    private boolean enabled = true;

    /**
     * 主密钥（Base64编码）
     */
    private String masterKey;

    /**
     * PII专用密钥（Base64编码）
     */
    private String piiKey;

    /**
     * 密钥轮换周期（天）
     */
    private int keyRotationDays = 90;

    /**
     * 加密算法
     */
    private String algorithm = "AES";

    /**
     * 密码加密强度
     */
    private int passwordStrength = 12;

    /**
     * 是否在开发环境禁用加密
     */
    private boolean disableInDev = false;

    /**
     * SSL/TLS配置
     */
    private SslConfig ssl = new SslConfig();

    /**
     * HTTPS配置
     */
    private HttpsConfig https = new HttpsConfig();

    /**
     * 密钥存储配置
     */
    private KeyStoreConfig keyStore = new KeyStoreConfig();

    /**
     * 配置密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(passwordStrength);
    }

    /**
     * 配置安全随机数生成器
     */
    @Bean
    public SecureRandom secureRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            return new SecureRandom();
        }
    }

    /**
     * SSL/TLS配置内部类
     */
    @Data
    public static class SslConfig {
        /**
         * 是否启用SSL
         */
        private boolean enabled = true;

        /**
         * SSL协议版本
         */
        private String protocol = "TLSv1.3";

        /**
         * 密码套件
         */
        private String[] cipherSuites = {
                "TLS_AES_256_GCM_SHA384",
                "TLS_CHACHA20_POLY1305_SHA256",
                "TLS_AES_128_GCM_SHA256"
        };

        /**
         * 证书路径
         */
        private String certificatePath;

        /**
         * 私钥路径
         */
        private String privateKeyPath;

        /**
         * 信任存储路径
         */
        private String trustStorePath;

        /**
         * 密钥存储密码
         */
        private String keyStorePassword;
    }

    /**
     * HTTPS配置内部类
     */
    @Data
    public static class HttpsConfig {
        /**
         * 是否强制HTTPS
         */
        private boolean enforced = false;

        /**
         * HTTPS端口
         */
        private int port = 8443;

        /**
         * 是否启用HSTS
         */
        private boolean hstsEnabled = true;

        /**
         * HSTS最大年龄
         */
        private long hstsMaxAge = 31536000; // 1年

        /**
         * 是否包含子域
         */
        private boolean hstsIncludeSubdomains = true;

        /**
         * 重定向策略
         */
        private String redirectStrategy = "REDIRECT";
    }

    /**
     * 密钥存储配置内部类
     */
    @Data
    public static class KeyStoreConfig {
        /**
         * 密钥存储类型
         */
        private String type = "PKCS12";

        /**
         * 密钥存储路径
         */
        private String path;

        /**
         * 密钥存储密码
         */
        private String password;

        /**
         * 密钥别名
         */
        private String keyAlias;

        /**
         * 是否启用硬件安全模块(HSM)
         */
        private boolean hsmEnabled = false;

        /**
         * HSM提供商
         */
        private String hsmProvider;

        /**
         * HSM配置
         */
        private String hsmConfig;
    }

    /**
     * 数据库加密配置内部类
     */
    @Data
    public static class DatabaseEncryptionConfig {
        /**
         * 是否启用数据库连接加密
         */
        private boolean connectionEncrypted = true;

        /**
         * 是否启用字段级加密
         */
        private boolean fieldLevelEncryption = true;

        /**
         * 需要加密的字段列表
         */
        private String[] encryptedFields = {
                "phone", "email", "realName", "lastLoginIp"
        };

        /**
         * 是否启用透明数据加密(TDE)
         */
        private boolean tdeEnabled = false;

        /**
         * 数据库加密密钥
         */
        private String databaseKey;
    }

    /**
     * 验证配置的有效性
     */
    public boolean isConfigValid() {
        if (!enabled) {
            return true; // 如果未启用加密，配置总是有效的
        }

        // 检查必要的配置项
        if (masterKey == null || masterKey.trim().isEmpty()) {
            return false;
        }

        if (piiKey == null || piiKey.trim().isEmpty()) {
            return false;
        }

        // 检查密钥轮换周期
        if (keyRotationDays <= 0 || keyRotationDays > 365) {
            return false;
        }

        // 检查密码强度
        if (passwordStrength < 4 || passwordStrength > 31) {
            return false;
        }

        return true;
    }

    /**
     * 获取配置摘要信息
     */
    public String getConfigSummary() {
        return String.format(
                "Encryption: %s, Algorithm: %s, Key Rotation: %d days, Password Strength: %d, SSL: %s",
                enabled ? "Enabled" : "Disabled",
                algorithm,
                keyRotationDays,
                passwordStrength,
                ssl.enabled ? "Enabled" : "Disabled");
    }

    /**
     * 生成新的AES密钥（用于初始化）
     */
    public static String generateNewAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // AES-256
            SecretKey secretKey = keyGenerator.generateKey();
            return java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成AES密钥", e);
        }
    }
}