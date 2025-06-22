package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.annotation.RequirePermission;
import com.vote.sihuibackend.config.EncryptionConfig;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.service.DataEncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据加密控制器
 * 提供加密状态查询、密钥管理等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/encryption")
@RequiredArgsConstructor
@Tag(name = "数据加密管理", description = "提供数据加密状态查询和密钥管理功能")
public class DataEncryptionController {

    private final DataEncryptionService encryptionService;
    private final EncryptionConfig encryptionConfig;

    /**
     * 获取加密状态
     */
    @GetMapping("/status")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "获取加密状态", description = "查询系统当前的加密配置和状态信息")
    public ResponseEntity<Map<String, Object>> getEncryptionStatus() {
        try {
            DataEncryptionService.EncryptionStatus status = encryptionService.getEncryptionStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            Map<String, Object> encryptionData = new HashMap<>();
            encryptionData.put("enabled", status.isEnabled());
            encryptionData.put("algorithm", status.getAlgorithm());
            encryptionData.put("keyVersion", status.getKeyVersion());
            encryptionData.put("keyHealthy", status.isKeyHealthy());
            encryptionData.put("lastKeyRotation", status.getLastKeyRotation());

            // 添加配置信息
            Map<String, Object> configData = new HashMap<>();
            configData.put("enabled", encryptionConfig.isEnabled());
            configData.put("algorithm", encryptionConfig.getAlgorithm());
            configData.put("keyRotationDays", encryptionConfig.getKeyRotationDays());
            configData.put("passwordStrength", encryptionConfig.getPasswordStrength());
            configData.put("sslEnabled", encryptionConfig.getSsl().isEnabled());
            configData.put("httpsEnforced", encryptionConfig.getHttps().isEnforced());
            configData.put("configValid", encryptionConfig.isConfigValid());
            configData.put("configSummary", encryptionConfig.getConfigSummary());

            response.put("status", encryptionData);
            response.put("config", configData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取加密状态失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 执行密钥轮换
     */
    @PostMapping("/rotate-keys")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "密钥轮换", description = "执行加密密钥的轮换操作")
    public ResponseEntity<Map<String, Object>> rotateKeys() {
        try {
            boolean success = encryptionService.rotateKeys();

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "密钥轮换成功" : "密钥轮换失败");

            if (success) {
                // 获取新的状态信息
                DataEncryptionService.EncryptionStatus newStatus = encryptionService.getEncryptionStatus();
                Map<String, Object> statusData = new HashMap<>();
                statusData.put("newKeyVersion", newStatus.getKeyVersion());
                statusData.put("lastKeyRotation", newStatus.getLastKeyRotation());
                statusData.put("keyHealthy", newStatus.isKeyHealthy());
                response.put("newStatus", statusData);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("密钥轮换失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 生成新的字段密钥
     */
    @PostMapping("/generate-field-key")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "生成字段密钥", description = "为指定字段生成专用的加密密钥")
    public ResponseEntity<Map<String, Object>> generateFieldKey(@RequestBody Map<String, String> request) {
        try {
            String fieldName = request.get("fieldName");

            if (fieldName == null || fieldName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "字段名不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String fieldKey = encryptionService.generateFieldKey(fieldName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fieldName", fieldName);
            response.put("fieldKey", fieldKey);
            response.put("message", "字段密钥生成成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("生成字段密钥失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 测试加密功能
     */
    @PostMapping("/test")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "测试加密功能", description = "测试数据加密和解密功能是否正常工作")
    public ResponseEntity<Map<String, Object>> testEncryption(@RequestBody Map<String, String> request) {
        try {
            String testData = request.getOrDefault("testData", "Hello, Encryption Test!");
            String encryptionType = request.getOrDefault("type", "general"); // general 或 pii

            String encrypted;
            String decrypted;

            if ("pii".equals(encryptionType)) {
                encrypted = encryptionService.encryptPII(testData);
                decrypted = encryptionService.decryptPII(encrypted);
            } else {
                encrypted = encryptionService.encrypt(testData);
                decrypted = encryptionService.decrypt(encrypted);
            }

            boolean testPassed = testData.equals(decrypted);

            Map<String, Object> response = new HashMap<>();
            response.put("success", testPassed);
            response.put("originalData", testData);
            response.put("encryptedData", encrypted);
            response.put("decryptedData", decrypted);
            response.put("encryptionType", encryptionType);
            response.put("testPassed", testPassed);
            response.put("message", testPassed ? "加密测试通过" : "加密测试失败");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("加密功能测试失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("testPassed", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 测试哈希功能
     */
    @PostMapping("/test-hash")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "测试哈希功能", description = "测试数据哈希和验证功能")
    public ResponseEntity<Map<String, Object>> testHash(@RequestBody Map<String, String> request) {
        try {
            String testData = request.getOrDefault("testData", "Hello, Hash Test!");
            boolean useSalt = Boolean.parseBoolean(request.getOrDefault("useSalt", "true"));

            String hash;
            String salt = null;
            boolean verificationResult;

            if (useSalt) {
                salt = encryptionService.generateSalt();
                hash = encryptionService.hashWithSalt(testData, salt);
                verificationResult = encryptionService.verifyHash(testData, hash, salt);
            } else {
                hash = encryptionService.hash(testData);
                // 对于简单哈希，我们只检查是否能生成哈希值
                verificationResult = hash != null && !hash.isEmpty();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalData", testData);
            response.put("hash", hash);
            response.put("salt", salt);
            response.put("useSalt", useSalt);
            response.put("verificationResult", verificationResult);
            response.put("testPassed", verificationResult);
            response.put("message", verificationResult ? "哈希测试通过" : "哈希测试失败");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("哈希功能测试失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("testPassed", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取加密配置摘要
     */
    @GetMapping("/config-summary")
    @RequirePermission(Permission.SYSTEM_ADMIN)
    @Operation(summary = "获取配置摘要", description = "获取加密配置的摘要信息")
    public ResponseEntity<Map<String, Object>> getConfigSummary() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("configSummary", encryptionConfig.getConfigSummary());
            response.put("configValid", encryptionConfig.isConfigValid());
            response.put("enabled", encryptionConfig.isEnabled());

            // 安全相关配置
            Map<String, Object> securityInfo = new HashMap<>();
            securityInfo.put("sslEnabled", encryptionConfig.getSsl().isEnabled());
            securityInfo.put("sslProtocol", encryptionConfig.getSsl().getProtocol());
            securityInfo.put("httpsEnforced", encryptionConfig.getHttps().isEnforced());
            securityInfo.put("httpsPort", encryptionConfig.getHttps().getPort());
            securityInfo.put("hstsEnabled", encryptionConfig.getHttps().isHstsEnabled());
            securityInfo.put("passwordStrength", encryptionConfig.getPasswordStrength());
            securityInfo.put("keyRotationDays", encryptionConfig.getKeyRotationDays());

            response.put("securityConfig", securityInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取配置摘要失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}