package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.service.MultiFactorAuthService;
import com.vote.sihuibackend.service.MultiFactorAuthService.MfaConfig;
import com.vote.sihuibackend.service.MultiFactorAuthService.MfaVerificationResult;
import com.vote.sihuibackend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 多因素认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
@Tag(name = "多因素认证", description = "提供多因素认证配置和验证功能")
public class MfaController {

    private final MultiFactorAuthService mfaService;

    /**
     * 获取用户MFA配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取MFA配置", description = "获取当前用户的多因素认证配置")
    public ResponseEntity<Map<String, Object>> getMfaConfig() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            MfaConfig config = mfaService.getUserMfaConfig(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            if (config != null) {
                Map<String, Object> configData = new HashMap<>();
                configData.put("smsEnabled", config.isSmsEnabled());
                configData.put("emailEnabled", config.isEmailEnabled());
                configData.put("totpEnabled", config.isTotpEnabled());
                configData.put("phoneNumber", maskPhoneNumber(config.getPhoneNumber()));
                configData.put("email", maskEmail(config.getEmail()));
                configData.put("requireMfaForLogin", config.isRequireMfaForLogin());
                configData.put("requireMfaForSensitiveOperations", config.isRequireMfaForSensitiveOperations());

                response.put("config", configData);
            } else {
                response.put("config", null);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取MFA配置失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送MFA验证码")
    public ResponseEntity<Map<String, Object>> sendSmsCode(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String phoneNumber = request.get("phoneNumber");
            String purpose = request.getOrDefault("purpose", "mfa");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "手机号不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean success = mfaService.sendSmsVerificationCode(phoneNumber, userPrincipal.getId(), purpose);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "验证码已发送" : "发送失败，请稍后重试");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("发送短信验证码失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email/send")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送MFA验证码")
    public ResponseEntity<Map<String, Object>> sendEmailCode(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String email = request.get("email");
            String purpose = request.getOrDefault("purpose", "mfa");

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "邮箱地址不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean success = mfaService.sendEmailVerificationCode(email, userPrincipal.getId(), purpose);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "验证码已发送" : "发送失败，请稍后重试");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("发送邮箱验证码失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 生成TOTP密钥
     */
    @PostMapping("/totp/generate")
    @Operation(summary = "生成TOTP密钥", description = "为用户生成TOTP认证器密钥和二维码")
    public ResponseEntity<Map<String, Object>> generateTotpSecret() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String secret = mfaService.generateTotpSecret(userPrincipal.getId());
            String qrCodeUrl = mfaService.generateTotpQrCodeUrl(userPrincipal.getId(), secret, "四会培训平台");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("secret", secret);
            response.put("qrCodeUrl", qrCodeUrl);
            response.put("message", "请使用认证器应用扫描二维码");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("生成TOTP密钥失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 启用TOTP
     */
    @PostMapping("/totp/enable")
    @Operation(summary = "启用TOTP", description = "验证TOTP代码并启用TOTP认证")
    public ResponseEntity<Map<String, Object>> enableTotp(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String secret = request.get("secret");
            String code = request.get("code");

            if (secret == null || code == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "密钥和验证码不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean success = mfaService.enableTotp(userPrincipal.getId(), secret, code);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "TOTP已成功启用" : "验证码错误，请重试");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("启用TOTP失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 禁用TOTP
     */
    @PostMapping("/totp/disable")
    @Operation(summary = "禁用TOTP", description = "验证TOTP代码并禁用TOTP认证")
    public ResponseEntity<Map<String, Object>> disableTotp(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String code = request.get("code");

            if (code == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "验证码不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean success = mfaService.disableTotp(userPrincipal.getId(), code);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "TOTP已禁用" : "验证码错误，请重试");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("禁用TOTP失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 验证MFA
     */
    @PostMapping("/verify")
    @Operation(summary = "验证MFA", description = "验证多因素认证代码")
    public ResponseEntity<Map<String, Object>> verifyMfa(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String method = request.get("method");
            String code = request.get("code");

            if (method == null || code == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "认证方法和验证码不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            MfaVerificationResult result = mfaService.verifyMfa(userPrincipal.getId(), method, code);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("method", result.getMethod());
            response.put("message", result.getMessage());

            if (result.getAdditionalData() != null) {
                response.put("additionalData", result.getAdditionalData());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("MFA验证失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 生成恢复代码
     */
    @PostMapping("/recovery/generate")
    @Operation(summary = "生成恢复代码", description = "生成MFA恢复代码")
    public ResponseEntity<Map<String, Object>> generateRecoveryCodes(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 需要先验证当前MFA状态
            String method = request.get("method");
            String code = request.get("code");

            if (method == null || code == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请先通过MFA验证");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            MfaVerificationResult verifyResult = mfaService.verifyMfa(userPrincipal.getId(), method, code);
            if (!verifyResult.isSuccess()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "MFA验证失败，无法生成恢复代码");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String[] recoveryCodes = mfaService.generateRecoveryCodes(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recoveryCodes", recoveryCodes);
            response.put("message", "恢复代码已生成，请妥善保存");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("生成恢复代码失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 更新MFA配置
     */
    @PutMapping("/config")
    @Operation(summary = "更新MFA配置", description = "更新用户的多因素认证配置")
    public ResponseEntity<Map<String, Object>> updateMfaConfig(@RequestBody MfaConfigRequest configRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            MfaConfig config = mfaService.getUserMfaConfig(userPrincipal.getId());
            if (config == null) {
                config = new MfaConfig();
            }

            // 更新配置
            if (configRequest.getPhoneNumber() != null) {
                config.setPhoneNumber(configRequest.getPhoneNumber());
                config.setSmsEnabled(configRequest.isSmsEnabled());
            }

            if (configRequest.getEmail() != null) {
                config.setEmail(configRequest.getEmail());
                config.setEmailEnabled(configRequest.isEmailEnabled());
            }

            config.setRequireMfaForLogin(configRequest.isRequireMfaForLogin());
            config.setRequireMfaForSensitiveOperations(configRequest.isRequireMfaForSensitiveOperations());

            boolean success = mfaService.updateMfaConfig(userPrincipal.getId(), config);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "MFA配置已更新" : "更新失败，请重试");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("更新MFA配置失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 私有辅助方法

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return email;
        }
        return localPart.substring(0, 2) + "***@" + parts[1];
    }

    // 内部类：MFA配置请求
    public static class MfaConfigRequest {
        private boolean smsEnabled;
        private boolean emailEnabled;
        private String phoneNumber;
        private String email;
        private boolean requireMfaForLogin;
        private boolean requireMfaForSensitiveOperations;

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
}