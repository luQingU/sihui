package com.vote.sihuibackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * 控制器测试基类
 * 提供通用的测试环境配置和工具方法
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import({ CommonTestConfiguration.class, SecurityTestConfiguration.class })
@SpringJUnitConfig
@Transactional
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS, listeners = WithSecurityContextTestExecutionListener.class)
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    /**
     * 测试前的通用设置
     */
    @BeforeEach
    void baseSetUp() {
        // 使用Spring Security的标准MockMvc配置
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity()) // 添加Spring Security测试支持
                .build();

        // 通用的测试前置设置
        setupCommonMocks();
    }

    /**
     * 测试后的清理工作
     */
    @AfterEach
    void baseTearDown() {
        // 清理工作会由Spring Security测试框架自动处理
    }

    /**
     * 设置通用的Mock行为
     */
    protected void setupCommonMocks() {
        // 这里可以添加所有测试都需要的通用Mock设置
        // Spring Security测试框架会自动处理认证上下文
    }

    /**
     * 创建有效的JWT Token用于测试
     * 
     * @param userId 用户ID
     * @param role   用户角色
     * @return JWT Token字符串
     */
    protected String createValidJwtToken(Long userId, String role) {
        return "Bearer test-jwt-token-" + userId + "-" + role;
    }

    /**
     * 设置认证用户用于测试
     * 
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    用户角色
     */
    protected void setupAuthenticatedUser(Long userId, String username, String... roles) {
        SecurityTestConfiguration.setupAuthenticatedUser(userId, username, roles);
    }

    /**
     * 将对象转换为JSON字符串
     * 
     * @param obj 要转换的对象
     * @return JSON字符串
     */
    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * 从JSON字符串解析对象
     * 
     * @param json  JSON字符串
     * @param clazz 目标类型
     * @return 解析后的对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to object", e);
        }
    }
}