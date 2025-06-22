package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.service.MultiFactorAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 多因素认证服务实现类
 */
@Slf4j
@Service
public class MultiFactorAuthServiceImpl implements MultiFactorAuthService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SMS_CODE_PREFIX = "mfa:sms:";
    private static final String EMAIL_CODE_PREFIX = "mfa:email:";
    private static final String TOTP_SECRET_PREFIX = "mfa:totp:";
    private static final String MFA_CONFIG_PREFIX = "mfa:config:";
    private static final String RECOVERY_CODES_PREFIX = "mfa:recovery:";

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int CODE_LENGTH = 6;
    private static final int TOTP_WINDOW = 30; // 30秒窗口
    private static final int TOTP_DIGITS = 6;

    @Override
    public boolean sendSmsVerificationCode(String phoneNumber, Long userId, String purpose) {
        try {
            String code = generateVerificationCode();
            String key = SMS_CODE_PREFIX + phoneNumber + ":" + purpose;

            if (redisTemplate != null) {
                // 存储验证码到Redis，设置5分钟过期
                Map<String, Object> codeData = new HashMap<>();
                codeData.put("code", code);
                codeData.put("userId", userId);
                codeData.put("timestamp", System.currentTimeMillis());
                codeData.put("attempts", 0);

                redisTemplate.opsForHash().putAll(key, codeData);
                redisTemplate.expire(key, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);
            }

            // 这里应该调用短信服务API发送验证码
            // TODO: 集成实际的短信服务提供商 (阿里云短信、腾讯云短信等)
            log.info("发送短信验证码到 {}: {}", phoneNumber, code);

            return true;

        } catch (Exception e) {
            log.error("发送短信验证码失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendEmailVerificationCode(String email, Long userId, String purpose) {
        try {
            String code = generateVerificationCode();
            String key = EMAIL_CODE_PREFIX + email + ":" + purpose;

            if (redisTemplate != null) {
                // 存储验证码到Redis，设置5分钟过期
                Map<String, Object> codeData = new HashMap<>();
                codeData.put("code", code);
                codeData.put("userId", userId);
                codeData.put("timestamp", System.currentTimeMillis());
                codeData.put("attempts", 0);

                redisTemplate.opsForHash().putAll(key, codeData);
                redisTemplate.expire(key, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);
            }

            // 这里应该调用邮件服务发送验证码
            // TODO: 集成邮件服务 (Spring Mail、阿里云邮件推送等)
            log.info("发送邮箱验证码到 {}: {}", email, code);

            return true;

        } catch (Exception e) {
            log.error("发送邮箱验证码失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifySmsCode(String phoneNumber, String code, String purpose) {
        return verifyCode(SMS_CODE_PREFIX + phoneNumber + ":" + purpose, code);
    }

    @Override
    public boolean verifyEmailCode(String email, String code, String purpose) {
        return verifyCode(EMAIL_CODE_PREFIX + email + ":" + purpose, code);
    }

    @Override
    public String generateTotpSecret(Long userId) {
        try {
            // 生成32字节的随机密钥
            SecureRandom random = new SecureRandom();
            byte[] secretBytes = new byte[32];
            random.nextBytes(secretBytes);

            String secret = Base32.encode(secretBytes);

            if (redisTemplate != null) {
                // 临时存储密钥，等待用户验证后正式启用
                String key = TOTP_SECRET_PREFIX + "temp:" + userId;
                redisTemplate.opsForValue().set(key, secret, 10, TimeUnit.MINUTES);
            }

            return secret;

        } catch (Exception e) {
            log.error("生成TOTP密钥失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String generateTotpQrCodeUrl(Long userId, String secret, String appName) {
        try {
            // 构造TOTP URI
            String uri = String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    appName,
                    "user" + userId, // 可以替换为实际的用户名
                    secret,
                    appName);

            // 返回可用于生成二维码的URL
            // 前端可以使用这个URL配合qrcode.js等库生成二维码
            return uri;

        } catch (Exception e) {
            log.error("生成TOTP二维码URL失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean verifyTotpCode(Long userId, String code) {
        try {
            MfaConfig config = getUserMfaConfig(userId);
            if (config == null || !config.isTotpEnabled() || config.getTotpSecret() == null) {
                return false;
            }

            return verifyTotpWithSecret(config.getTotpSecret(), code);

        } catch (Exception e) {
            log.error("验证TOTP代码失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean enableTotp(Long userId, String secret, String verificationCode) {
        try {
            // 验证提供的代码是否正确
            if (!verifyTotpWithSecret(secret, verificationCode)) {
                return false;
            }

            // 获取或创建用户MFA配置
            MfaConfig config = getUserMfaConfig(userId);
            if (config == null) {
                config = new MfaConfig();
            }

            config.setTotpEnabled(true);
            config.setTotpSecret(secret);

            // 保存配置
            updateMfaConfig(userId, config);

            if (redisTemplate != null) {
                // 清理临时密钥
                String tempKey = TOTP_SECRET_PREFIX + "temp:" + userId;
                redisTemplate.delete(tempKey);
            }

            log.info("用户 {} 启用TOTP成功", userId);
            return true;

        } catch (Exception e) {
            log.error("启用TOTP失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean disableTotp(Long userId, String verificationCode) {
        try {
            // 验证当前TOTP代码
            if (!verifyTotpCode(userId, verificationCode)) {
                return false;
            }

            MfaConfig config = getUserMfaConfig(userId);
            if (config != null) {
                config.setTotpEnabled(false);
                config.setTotpSecret(null);
                updateMfaConfig(userId, config);
            }

            log.info("用户 {} 禁用TOTP成功", userId);
            return true;

        } catch (Exception e) {
            log.error("禁用TOTP失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isMfaEnabled(Long userId) {
        try {
            MfaConfig config = getUserMfaConfig(userId);
            return config != null && (config.isSmsEnabled() || config.isEmailEnabled() || config.isTotpEnabled());
        } catch (Exception e) {
            log.error("检查MFA状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MfaConfig getUserMfaConfig(Long userId) {
        try {
            if (redisTemplate == null) {
                return null; // Redis不可用时返回null
            }

            String key = MFA_CONFIG_PREFIX + userId;
            Map<Object, Object> configData = redisTemplate.opsForHash().entries(key);

            if (configData.isEmpty()) {
                return null;
            }

            MfaConfig config = new MfaConfig();
            config.setSmsEnabled(Boolean.parseBoolean(String.valueOf(configData.get("smsEnabled"))));
            config.setEmailEnabled(Boolean.parseBoolean(String.valueOf(configData.get("emailEnabled"))));
            config.setTotpEnabled(Boolean.parseBoolean(String.valueOf(configData.get("totpEnabled"))));
            config.setPhoneNumber((String) configData.get("phoneNumber"));
            config.setEmail((String) configData.get("email"));
            config.setTotpSecret((String) configData.get("totpSecret"));
            config.setRequireMfaForLogin(Boolean.parseBoolean(String.valueOf(configData.get("requireMfaForLogin"))));
            config.setRequireMfaForSensitiveOperations(
                    Boolean.parseBoolean(String.valueOf(configData.get("requireMfaForSensitiveOperations"))));

            return config;

        } catch (Exception e) {
            log.error("获取用户MFA配置失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateMfaConfig(Long userId, MfaConfig config) {
        try {
            if (redisTemplate == null) {
                return false; // Redis不可用时返回false
            }

            String key = MFA_CONFIG_PREFIX + userId;

            Map<String, Object> configData = new HashMap<>();
            configData.put("smsEnabled", config.isSmsEnabled());
            configData.put("emailEnabled", config.isEmailEnabled());
            configData.put("totpEnabled", config.isTotpEnabled());
            configData.put("phoneNumber", config.getPhoneNumber());
            configData.put("email", config.getEmail());
            configData.put("totpSecret", config.getTotpSecret());
            configData.put("requireMfaForLogin", config.isRequireMfaForLogin());
            configData.put("requireMfaForSensitiveOperations", config.isRequireMfaForSensitiveOperations());

            redisTemplate.opsForHash().putAll(key, configData);

            return true;

        } catch (Exception e) {
            log.error("更新用户MFA配置失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MfaVerificationResult verifyMfa(Long userId, String method, String code) {
        try {
            MfaConfig config = getUserMfaConfig(userId);
            if (config == null) {
                return new MfaVerificationResult(false, method, "用户未配置MFA");
            }

            switch (method.toLowerCase()) {
                case "sms":
                    if (!config.isSmsEnabled()) {
                        return new MfaVerificationResult(false, method, "短信MFA未启用");
                    }
                    boolean smsValid = verifySmsCode(config.getPhoneNumber(), code, "mfa");
                    return new MfaVerificationResult(smsValid, method,
                            smsValid ? "短信验证成功" : "短信验证码错误");

                case "email":
                    if (!config.isEmailEnabled()) {
                        return new MfaVerificationResult(false, method, "邮箱MFA未启用");
                    }
                    boolean emailValid = verifyEmailCode(config.getEmail(), code, "mfa");
                    return new MfaVerificationResult(emailValid, method,
                            emailValid ? "邮箱验证成功" : "邮箱验证码错误");

                case "totp":
                    if (!config.isTotpEnabled()) {
                        return new MfaVerificationResult(false, method, "TOTP未启用");
                    }
                    boolean totpValid = verifyTotpCode(userId, code);
                    return new MfaVerificationResult(totpValid, method,
                            totpValid ? "TOTP验证成功" : "TOTP验证码错误");

                case "recovery":
                    boolean recoveryValid = verifyRecoveryCode(userId, code);
                    return new MfaVerificationResult(recoveryValid, method,
                            recoveryValid ? "恢复代码验证成功" : "恢复代码无效");

                default:
                    return new MfaVerificationResult(false, method, "不支持的MFA方法");
            }

        } catch (Exception e) {
            log.error("MFA验证失败: {}", e.getMessage());
            return new MfaVerificationResult(false, method, "验证过程出现错误");
        }
    }

    @Override
    public String[] generateRecoveryCodes(Long userId) {
        try {
            String[] codes = new String[10]; // 生成10个恢复代码
            SecureRandom random = new SecureRandom();

            for (int i = 0; i < codes.length; i++) {
                codes[i] = String.format("%08d", random.nextInt(100000000));
            }

            if (redisTemplate != null) {
                // 存储到Redis
                String key = RECOVERY_CODES_PREFIX + userId;
                Set<String> codeSet = new HashSet<>(Arrays.asList(codes));
                redisTemplate.opsForSet().add(key, codeSet.toArray());
                redisTemplate.expire(key, 365, TimeUnit.DAYS); // 一年有效期
            }

            return codes;

        } catch (Exception e) {
            log.error("生成恢复代码失败: {}", e.getMessage());
            return new String[0];
        }
    }

    @Override
    public boolean verifyRecoveryCode(Long userId, String code) {
        try {
            if (redisTemplate == null) {
                return false; // Redis不可用时返回false
            }

            String key = RECOVERY_CODES_PREFIX + userId;

            // 检查代码是否存在
            if (redisTemplate.opsForSet().isMember(key, code)) {
                // 使用后立即删除，确保一次性使用
                redisTemplate.opsForSet().remove(key, code);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("验证恢复代码失败: {}", e.getMessage());
            return false;
        }
    }

    // 私有辅助方法

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 生成6位数字
        return String.valueOf(code);
    }

    private boolean verifyCode(String key, String inputCode) {
        try {
            if (redisTemplate == null) {
                return false; // Redis不可用时返回false
            }

            Map<Object, Object> codeData = redisTemplate.opsForHash().entries(key);

            if (codeData.isEmpty()) {
                return false;
            }

            String storedCode = (String) codeData.get("code");
            Integer attempts = (Integer) codeData.get("attempts");

            // 检查尝试次数
            if (attempts != null && attempts >= 3) {
                redisTemplate.delete(key); // 删除已超过尝试次数的验证码
                return false;
            }

            // 验证代码
            if (storedCode != null && storedCode.equals(inputCode)) {
                redisTemplate.delete(key); // 验证成功后删除
                return true;
            } else {
                // 增加尝试次数
                redisTemplate.opsForHash().put(key, "attempts", (attempts == null ? 0 : attempts) + 1);
                return false;
            }

        } catch (Exception e) {
            log.error("验证代码失败: {}", e.getMessage());
            return false;
        }
    }

    private boolean verifyTotpWithSecret(String secret, String code) {
        try {
            long currentTime = Instant.now().getEpochSecond() / TOTP_WINDOW;

            // 允许前后一个时间窗口的容错
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTotpCode(secret, currentTime + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("TOTP验证失败: {}", e.getMessage());
            return false;
        }
    }

    private String generateTotpCode(String secret, long timeCounter)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] secretBytes = Base32.decode(secret);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeCounter).array();

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
        return String.format("%0" + TOTP_DIGITS + "d", otp);
    }

    // 简单的Base32编码/解码实现
    private static class Base32 {
        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        private static final int[] DECODE_TABLE = new int[128];

        static {
            Arrays.fill(DECODE_TABLE, -1);
            for (int i = 0; i < ALPHABET.length(); i++) {
                DECODE_TABLE[ALPHABET.charAt(i)] = i;
            }
        }

        public static String encode(byte[] data) {
            StringBuilder result = new StringBuilder();
            int buffer = 0;
            int bufferLength = 0;

            for (byte b : data) {
                buffer = (buffer << 8) | (b & 0xFF);
                bufferLength += 8;

                while (bufferLength >= 5) {
                    result.append(ALPHABET.charAt((buffer >> (bufferLength - 5)) & 0x1F));
                    bufferLength -= 5;
                }
            }

            if (bufferLength > 0) {
                result.append(ALPHABET.charAt((buffer << (5 - bufferLength)) & 0x1F));
            }

            return result.toString();
        }

        public static byte[] decode(String encoded) {
            encoded = encoded.toUpperCase().replaceAll("[^A-Z2-7]", "");

            if (encoded.isEmpty()) {
                return new byte[0];
            }

            List<Byte> result = new ArrayList<>();
            int buffer = 0;
            int bufferLength = 0;

            for (char c : encoded.toCharArray()) {
                int value = DECODE_TABLE[c];
                if (value == -1)
                    continue;

                buffer = (buffer << 5) | value;
                bufferLength += 5;

                if (bufferLength >= 8) {
                    result.add((byte) ((buffer >> (bufferLength - 8)) & 0xFF));
                    bufferLength -= 8;
                }
            }

            byte[] bytes = new byte[result.size()];
            for (int i = 0; i < result.size(); i++) {
                bytes[i] = result.get(i);
            }

            return bytes;
        }
    }
}