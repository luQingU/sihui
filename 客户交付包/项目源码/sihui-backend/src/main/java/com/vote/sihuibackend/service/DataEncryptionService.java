package com.vote.sihuibackend.service;

/**
 * 数据加密服务接口
 * 提供敏感数据的加密、解密和密钥管理功能
 */
public interface DataEncryptionService {

    /**
     * 加密字符串
     * 
     * @param plainText 明文数据
     * @return 加密后的数据
     */
    String encrypt(String plainText);

    /**
     * 解密字符串
     * 
     * @param encryptedText 加密后的数据
     * @return 明文数据
     */
    String decrypt(String encryptedText);

    /**
     * 加密敏感个人信息(如手机号、身份证号)
     * 
     * @param pii 个人敏感信息
     * @return 加密后的数据
     */
    String encryptPII(String pii);

    /**
     * 解密敏感个人信息
     * 
     * @param encryptedPii 加密后的个人敏感信息
     * @return 明文个人敏感信息
     */
    String decryptPII(String encryptedPii);

    /**
     * 对数据进行哈希处理(单向加密)
     * 
     * @param data 需要哈希的数据
     * @return 哈希值
     */
    String hash(String data);

    /**
     * 对数据进行加盐哈希处理
     * 
     * @param data 需要哈希的数据
     * @param salt 盐值
     * @return 哈希值
     */
    String hashWithSalt(String data, String salt);

    /**
     * 生成随机盐值
     * 
     * @return 盐值
     */
    String generateSalt();

    /**
     * 验证哈希值
     * 
     * @param data 原始数据
     * @param hash 哈希值
     * @param salt 盐值
     * @return 是否匹配
     */
    boolean verifyHash(String data, String hash, String salt);

    /**
     * 生成数据库字段级加密密钥
     * 
     * @param fieldName 字段名称
     * @return 字段专用密钥
     */
    String generateFieldKey(String fieldName);

    /**
     * 检查加密配置是否正常
     * 
     * @return 配置状态
     */
    EncryptionStatus getEncryptionStatus();

    /**
     * 轮换加密密钥
     * 
     * @return 是否成功
     */
    boolean rotateKeys();

    /**
     * 加密状态信息
     */
    class EncryptionStatus {
        private boolean enabled;
        private String algorithm;
        private String keyVersion;
        private boolean keyHealthy;
        private String lastKeyRotation;

        public EncryptionStatus(boolean enabled, String algorithm, String keyVersion,
                boolean keyHealthy, String lastKeyRotation) {
            this.enabled = enabled;
            this.algorithm = algorithm;
            this.keyVersion = keyVersion;
            this.keyHealthy = keyHealthy;
            this.lastKeyRotation = lastKeyRotation;
        }

        // Getters
        public boolean isEnabled() {
            return enabled;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getKeyVersion() {
            return keyVersion;
        }

        public boolean isKeyHealthy() {
            return keyHealthy;
        }

        public String getLastKeyRotation() {
            return lastKeyRotation;
        }

        // Setters
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public void setKeyVersion(String keyVersion) {
            this.keyVersion = keyVersion;
        }

        public void setKeyHealthy(boolean keyHealthy) {
            this.keyHealthy = keyHealthy;
        }

        public void setLastKeyRotation(String lastKeyRotation) {
            this.lastKeyRotation = lastKeyRotation;
        }
    }
}