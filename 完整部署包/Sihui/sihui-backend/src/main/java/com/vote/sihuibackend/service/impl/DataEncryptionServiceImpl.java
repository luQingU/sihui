package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.service.DataEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 数据加密服务实现类
 * 使用AES-256-GCM加密算法提供企业级数据保护
 */
@Slf4j
@Service
public class DataEncryptionServiceImpl implements DataEncryptionService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // 配置参数
    @Value("${app.encryption.master-key:}")
    private String masterKeyBase64;

    @Value("${app.encryption.pii-key:}")
    private String piiKeyBase64;

    @Value("${app.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Value("${app.encryption.key-rotation-days:90}")
    private int keyRotationDays;

    // 加密算法常量
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int AES_KEY_LENGTH = 256;

    // Redis键前缀
    private static final String ENCRYPTION_KEY_PREFIX = "encryption:keys:";
    private static final String ENCRYPTION_STATUS_KEY = "encryption:status";

    // 主密钥和PII专用密钥
    private SecretKey masterKey;
    private SecretKey piiKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        try {
            initializeKeys();
            updateEncryptionStatus();
            log.info("数据加密服务初始化完成，加密功能: {}", encryptionEnabled ? "启用" : "禁用");
        } catch (Exception e) {
            log.error("数据加密服务初始化失败: {}", e.getMessage());
            throw new RuntimeException("加密服务初始化失败", e);
        }
    }

    @Override
    public String encrypt(String plainText) {
        if (!encryptionEnabled || plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            return performEncryption(plainText, masterKey);
        } catch (Exception e) {
            log.error("数据加密失败: {}", e.getMessage());
            throw new RuntimeException("数据加密失败", e);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        if (!encryptionEnabled || encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            return performDecryption(encryptedText, masterKey);
        } catch (Exception e) {
            log.error("数据解密失败: {}", e.getMessage());
            throw new RuntimeException("数据解密失败", e);
        }
    }

    @Override
    public String encryptPII(String pii) {
        if (!encryptionEnabled || pii == null || pii.isEmpty()) {
            return pii;
        }

        try {
            return performEncryption(pii, piiKey);
        } catch (Exception e) {
            log.error("PII数据加密失败: {}", e.getMessage());
            throw new RuntimeException("PII数据加密失败", e);
        }
    }

    @Override
    public String decryptPII(String encryptedPii) {
        if (!encryptionEnabled || encryptedPii == null || encryptedPii.isEmpty()) {
            return encryptedPii;
        }

        try {
            return performDecryption(encryptedPii, piiKey);
        } catch (Exception e) {
            log.error("PII数据解密失败: {}", e.getMessage());
            throw new RuntimeException("PII数据解密失败", e);
        }
    }

    @Override
    public String hash(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("哈希算法不可用: {}", e.getMessage());
            throw new RuntimeException("哈希处理失败", e);
        }
    }

    @Override
    public String hashWithSalt(String data, String salt) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("加盐哈希处理失败: {}", e.getMessage());
            throw new RuntimeException("加盐哈希处理失败", e);
        }
    }

    @Override
    public String generateSalt() {
        byte[] salt = new byte[32]; // 256-bit salt
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Override
    public boolean verifyHash(String data, String hash, String salt) {
        try {
            String computedHash = hashWithSalt(data, salt);
            return MessageDigest.isEqual(hash.getBytes(), computedHash.getBytes());
        } catch (Exception e) {
            log.error("哈希验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String generateFieldKey(String fieldName) {
        try {
            // 为特定字段生成专用密钥
            String keyData = masterKey.getEncoded().toString() + ":" + fieldName;
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] keyBytes = digest.digest(keyData.getBytes(StandardCharsets.UTF_8));

            // 截取前32字节用作AES-256密钥
            byte[] truncatedKey = new byte[32];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);

            return Base64.getEncoder().encodeToString(truncatedKey);
        } catch (Exception e) {
            log.error("生成字段密钥失败: {}", e.getMessage());
            throw new RuntimeException("生成字段密钥失败", e);
        }
    }

    @Override
    public EncryptionStatus getEncryptionStatus() {
        try {
            String lastRotation = null;
            if (redisTemplate != null) {
                lastRotation = (String) redisTemplate.opsForValue().get(ENCRYPTION_STATUS_KEY + ":last_rotation");
            }
            if (lastRotation == null) {
                lastRotation = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            return new EncryptionStatus(
                    encryptionEnabled,
                    CIPHER_TRANSFORMATION,
                    getCurrentKeyVersion(),
                    checkKeyHealth(),
                    lastRotation);
        } catch (Exception e) {
            log.error("获取加密状态失败: {}", e.getMessage());
            return new EncryptionStatus(false, "UNKNOWN", "UNKNOWN", false, "UNKNOWN");
        }
    }

    @Override
    public boolean rotateKeys() {
        try {
            log.info("开始密钥轮换操作");

            // 备份当前密钥
            backupCurrentKeys();

            // 生成新密钥
            generateNewKeys();

            // 更新Redis中的密钥版本信息（如果Redis可用）
            if (redisTemplate != null) {
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":last_rotation", currentTime, 365,
                        TimeUnit.DAYS);
                redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":version", getCurrentKeyVersion(), 365,
                        TimeUnit.DAYS);
            }

            updateEncryptionStatus();

            log.info("密钥轮换完成");
            return true;

        } catch (Exception e) {
            log.error("密钥轮换失败: {}", e.getMessage());
            return false;
        }
    }

    // 私有辅助方法

    private void initializeKeys() {
        try {
            if (masterKeyBase64 != null && !masterKeyBase64.isEmpty()) {
                // 从配置加载主密钥
                byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
                masterKey = new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
                log.info("从配置加载主密钥成功");
            } else {
                // 生成新的主密钥
                masterKey = generateAESKey();
                log.info("生成新的主密钥");
            }

            if (piiKeyBase64 != null && !piiKeyBase64.isEmpty()) {
                // 从配置加载PII密钥
                byte[] keyBytes = Base64.getDecoder().decode(piiKeyBase64);
                piiKey = new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
                log.info("从配置加载PII密钥成功");
            } else {
                // 生成新的PII密钥
                piiKey = generateAESKey();
                log.info("生成新的PII密钥");
            }

        } catch (Exception e) {
            log.error("密钥初始化失败: {}", e.getMessage());
            throw new RuntimeException("密钥初始化失败", e);
        }
    }

    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(AES_KEY_LENGTH);
        return keyGenerator.generateKey();
    }

    private String performEncryption(String plainText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);

        // 生成随机IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 将IV和加密数据组合
        byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    private String performDecryption(String encryptedText, SecretKey key) throws Exception {
        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

        // 提取IV和加密数据
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];

        System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    private void updateEncryptionStatus() {
        try {
            if (redisTemplate != null) {
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":enabled", encryptionEnabled, 365,
                        TimeUnit.DAYS);
                redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":algorithm", CIPHER_TRANSFORMATION, 365,
                        TimeUnit.DAYS);
                redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":updated", currentTime, 365, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            log.warn("更新加密状态失败: {}", e.getMessage());
        }
    }

    private String getCurrentKeyVersion() {
        try {
            if (redisTemplate != null) {
                String version = (String) redisTemplate.opsForValue().get(ENCRYPTION_STATUS_KEY + ":version");
                if (version == null) {
                    version = "v1.0-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":version", version, 365, TimeUnit.DAYS);
                }
                return version;
            } else {
                return "v1.0-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (Exception e) {
            return "v1.0-unknown";
        }
    }

    private boolean checkKeyHealth() {
        try {
            // 测试加密和解密功能
            String testData = "encryption-health-check-" + System.currentTimeMillis();
            String encrypted = performEncryption(testData, masterKey);
            String decrypted = performDecryption(encrypted, masterKey);

            return testData.equals(decrypted);
        } catch (Exception e) {
            log.error("密钥健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    private void backupCurrentKeys() {
        try {
            if (redisTemplate != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String backupKey = ENCRYPTION_KEY_PREFIX + "backup:" + timestamp;

                redisTemplate.opsForHash().put(backupKey, "master_key",
                        Base64.getEncoder().encodeToString(masterKey.getEncoded()));
                redisTemplate.opsForHash().put(backupKey, "pii_key",
                        Base64.getEncoder().encodeToString(piiKey.getEncoded()));
                redisTemplate.opsForHash().put(backupKey, "version", getCurrentKeyVersion());
                redisTemplate.expire(backupKey, 365, TimeUnit.DAYS);

                log.info("密钥备份完成: {}", backupKey);
            } else {
                log.info("Redis不可用，跳过密钥备份");
            }
        } catch (Exception e) {
            log.error("密钥备份失败: {}", e.getMessage());
        }
    }

    private void generateNewKeys() throws Exception {
        masterKey = generateAESKey();
        piiKey = generateAESKey();

        String newVersion = "v2.0-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(ENCRYPTION_STATUS_KEY + ":version", newVersion, 365, TimeUnit.DAYS);
        }

        log.info("生成新密钥完成，版本: {}", newVersion);
    }
}