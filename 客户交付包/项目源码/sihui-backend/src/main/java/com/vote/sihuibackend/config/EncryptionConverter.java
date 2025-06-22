package com.vote.sihuibackend.config;

import com.vote.sihuibackend.service.DataEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA加密转换器
 * 自动处理敏感数据的加密和解密
 */
@Slf4j
@Component
public class EncryptionConverter {

    /**
     * PII数据加密转换器（用于手机号、身份证号等敏感个人信息）
     */
    @Converter
    public static class PIIEncryptionConverter implements AttributeConverter<String, String> {

        @Autowired
        private DataEncryptionService encryptionService;

        @Override
        public String convertToDatabaseColumn(String attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return attribute;
            }

            try {
                return encryptionService.encryptPII(attribute);
            } catch (Exception e) {
                log.error("PII数据加密失败: {}", e.getMessage());
                // 在生产环境中，可能需要抛出异常而不是返回原始数据
                return attribute;
            }
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return dbData;
            }

            try {
                return encryptionService.decryptPII(dbData);
            } catch (Exception e) {
                log.error("PII数据解密失败: {}", e.getMessage());
                // 在生产环境中，可能需要抛出异常而不是返回加密数据
                return dbData;
            }
        }
    }

    /**
     * 通用数据加密转换器（用于其他敏感信息）
     */
    @Converter
    public static class GeneralEncryptionConverter implements AttributeConverter<String, String> {

        @Autowired
        private DataEncryptionService encryptionService;

        @Override
        public String convertToDatabaseColumn(String attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return attribute;
            }

            try {
                return encryptionService.encrypt(attribute);
            } catch (Exception e) {
                log.error("数据加密失败: {}", e.getMessage());
                return attribute;
            }
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return dbData;
            }

            try {
                return encryptionService.decrypt(dbData);
            } catch (Exception e) {
                log.error("数据解密失败: {}", e.getMessage());
                return dbData;
            }
        }
    }

    /**
     * 邮箱加密转换器
     */
    @Converter
    public static class EmailEncryptionConverter implements AttributeConverter<String, String> {

        @Autowired
        private DataEncryptionService encryptionService;

        @Override
        public String convertToDatabaseColumn(String email) {
            if (email == null || email.isEmpty()) {
                return email;
            }

            try {
                // 邮箱使用PII加密
                return encryptionService.encryptPII(email);
            } catch (Exception e) {
                log.error("邮箱加密失败: {}", e.getMessage());
                return email;
            }
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return dbData;
            }

            try {
                return encryptionService.decryptPII(dbData);
            } catch (Exception e) {
                log.error("邮箱解密失败: {}", e.getMessage());
                return dbData;
            }
        }
    }
}