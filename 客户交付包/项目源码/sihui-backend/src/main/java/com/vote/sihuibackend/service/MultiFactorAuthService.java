package com.vote.sihuibackend.service;

import java.util.Map;

/**
 * 多因素认证服务接口
 */
public interface MultiFactorAuthService {

    /**
     * 发送短信验证码
     */
    boolean sendSmsVerificationCode(String phoneNumber, Long userId, String purpose);

    /**
     * 发送邮箱验证码
     */
    boolean sendEmailVerificationCode(String email, Long userId, String purpose);

    /**
     * 验证短信验证码
     */
    boolean verifySmsCode(String phoneNumber, String code, String purpose);

    /**
     * 验证邮箱验证码
     */
    boolean verifyEmailCode(String email, String code, String purpose);

    /**
     * 生成TOTP密钥
     */
    String generateTotpSecret(Long userId);

    /**
     * 生成TOTP二维码URL
     */
    String generateTotpQrCodeUrl(Long userId, String secret, String appName);

    /**
     * 验证TOTP代码
     */
    boolean verifyTotpCode(Long userId, String code);

    /**
     * 启用用户的TOTP
     */
    boolean enableTotp(Long userId, String secret, String verificationCode);

    /**
     * 禁用用户的TOTP
     */
    boolean disableTotp(Long userId, String verificationCode);

    /**
     * 检查用户是否启用了MFA
     */
    boolean isMfaEnabled(Long userId);

    /**
     * 获取用户的MFA配置
     */
    MfaConfig getUserMfaConfig(Long userId);

    /**
     * 更新用户MFA配置
     */
    boolean updateMfaConfig(Long userId, MfaConfig config);

    /**
     * 验证MFA（支持多种方式）
     */
    MfaVerificationResult verifyMfa(Long userId, String method, String code);

    /**
     * 生成备用恢复代码
     */
    String[] generateRecoveryCodes(Long userId);

    /**
     * 验证备用恢复代码
     */
    boolean verifyRecoveryCode(Long userId, String code);

    /**
     * MFA配置类
     */
    class MfaConfig {
        private boolean smsEnabled;
        private boolean emailEnabled;
        private boolean totpEnabled;
        private String phoneNumber;
        private String email;
        private String totpSecret;
        private boolean requireMfaForLogin;
        private boolean requireMfaForSensitiveOperations;

        public MfaConfig() {
        }

        public MfaConfig(boolean smsEnabled, boolean emailEnabled, boolean totpEnabled,
                String phoneNumber, String email, String totpSecret,
                boolean requireMfaForLogin, boolean requireMfaForSensitiveOperations) {
            this.smsEnabled = smsEnabled;
            this.emailEnabled = emailEnabled;
            this.totpEnabled = totpEnabled;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.totpSecret = totpSecret;
            this.requireMfaForLogin = requireMfaForLogin;
            this.requireMfaForSensitiveOperations = requireMfaForSensitiveOperations;
        }

        // Getters and Setters
        public boolean isSmsEnabled() {
            return smsEnabled;
        }

        public void setSmsEnabled(boolean smsEnabled) {
            this.smsEnabled = smsEnabled;
        }

        public boolean isEmailEnabled() {
            return emailEnabled;
        }

        public void setEmailEnabled(boolean emailEnabled) {
            this.emailEnabled = emailEnabled;
        }

        public boolean isTotpEnabled() {
            return totpEnabled;
        }

        public void setTotpEnabled(boolean totpEnabled) {
            this.totpEnabled = totpEnabled;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTotpSecret() {
            return totpSecret;
        }

        public void setTotpSecret(String totpSecret) {
            this.totpSecret = totpSecret;
        }

        public boolean isRequireMfaForLogin() {
            return requireMfaForLogin;
        }

        public void setRequireMfaForLogin(boolean requireMfaForLogin) {
            this.requireMfaForLogin = requireMfaForLogin;
        }

        public boolean isRequireMfaForSensitiveOperations() {
            return requireMfaForSensitiveOperations;
        }

        public void setRequireMfaForSensitiveOperations(boolean requireMfaForSensitiveOperations) {
            this.requireMfaForSensitiveOperations = requireMfaForSensitiveOperations;
        }
    }

    /**
     * MFA验证结果类
     */
    class MfaVerificationResult {
        private boolean success;
        private String method;
        private String message;
        private Map<String, Object> additionalData;

        public MfaVerificationResult(boolean success, String method, String message) {
            this.success = success;
            this.method = method;
            this.message = message;
        }

        public MfaVerificationResult(boolean success, String method, String message,
                Map<String, Object> additionalData) {
            this.success = success;
            this.method = method;
            this.message = message;
            this.additionalData = additionalData;
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getAdditionalData() {
            return additionalData;
        }

        public void setAdditionalData(Map<String, Object> additionalData) {
            this.additionalData = additionalData;
        }
    }
}