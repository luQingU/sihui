package com.vote.sihuibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.config.CommonTestConfiguration;
import com.vote.sihuibackend.config.SecurityTestConfiguration;
import com.vote.sihuibackend.dto.LoginRequest;

import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器集成测试
 * 测试用户登录、注册和JWT相关功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = { CommonTestConfiguration.class, SecurityTestConfiguration.class })
@Transactional
public class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private CacheManager cacheManager;

        private User testUser;

        @BeforeEach
        void setUp() {
                // 清理缓存
                if (cacheManager != null) {
                        cacheManager.getCacheNames().forEach(cacheName -> {
                                if (cacheManager.getCache(cacheName) != null) {
                                        cacheManager.getCache(cacheName).clear();
                                }
                        });
                }

                // 清理数据
                userRepository.deleteAll();

                // 创建测试用户
                testUser = new User();
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setPasswordHash(passwordEncoder.encode("password123"));
                testUser.setEmailVerified(true);
                testUser.setPhoneVerified(false);
                testUser.setStatus(User.UserStatus.ACTIVE); // 确保用户状态为ACTIVE
                testUser.setRealName("Test User"); // 设置必要的字段
                testUser = userRepository.save(testUser);

                // 刷新数据库
                userRepository.flush();
        }

        @AfterEach
        void tearDown() {
                // 清理缓存
                if (cacheManager != null) {
                        cacheManager.getCacheNames().forEach(cacheName -> {
                                if (cacheManager.getCache(cacheName) != null) {
                                        cacheManager.getCache(cacheName).clear();
                                }
                        });
                }
                userRepository.deleteAll();
        }

        @Test
        public void shouldLoginSuccessfully() throws Exception {
                // 验证用户已保存
                assert userRepository.count() == 1;
                assert userRepository.findByUsernameOrEmail("testuser").isPresent();

                // 准备登录请求
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsernameOrEmail("testuser");
                loginRequest.setPassword("password123");

                // 执行登录
                ResultActions result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andDo(print()); // 打印详细信息用于调试

                // 验证响应
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").exists())
                                .andExpect(jsonPath("$.data.refreshToken").exists())
                                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
        }

        @Test
        public void shouldLoginWithEmailSuccessfully() throws Exception {
                // 验证用户已保存
                assert userRepository.findByUsernameOrEmail("test@example.com").isPresent();

                // 准备邮箱登录请求
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsernameOrEmail("test@example.com");
                loginRequest.setPassword("password123");

                // 执行登录
                ResultActions result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andDo(print()); // 打印详细信息用于调试

                // 验证响应
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").exists())
                                .andExpect(jsonPath("$.data.refreshToken").exists());
        }

        @Test
        public void shouldFailLoginWithWrongPassword() throws Exception {
                // 准备错误密码的登录请求
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsernameOrEmail("testuser");
                loginRequest.setPassword("wrongpassword");

                // 执行登录
                ResultActions result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andDo(print());

                // 验证响应 - 应该返回401
                result.andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(401));
        }
}