package com.vote.sihuibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.service.DeepSeekService;
import com.vote.sihuibackend.service.ChatMemoryService;
import com.vote.sihuibackend.service.KnowledgeEnhancedChatService;
import com.vote.sihuibackend.service.UserDetailsServiceImpl;
import com.vote.sihuibackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI聊天控制器安全性测试
 */
@WebMvcTest(value = AiChatController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
public class AiChatControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeepSeekService deepSeekService;

    @MockBean
    private ChatMemoryService chatMemoryService;

    @MockBean
    private KnowledgeEnhancedChatService knowledgeEnhancedChatService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testChatEndpoint_InputValidation_EmptyMessage() throws Exception {
        // 测试空消息输入验证
        mockMvc.perform(post("/api/ai/chat")
                .param("message", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("消息内容不能为空"));
    }

    @Test
    public void testChatEndpoint_SQLInjectionAttempt() throws Exception {
        // 测试SQL注入攻击防护
        String maliciousInput = "'; DROP TABLE users; --";

        when(deepSeekService.chat(anyString())).thenReturn("正常回复");

        mockMvc.perform(post("/api/ai/chat")
                .param("message", maliciousInput)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testChatWithMemory_InputValidation_MissingUserId() throws Exception {
        // 测试记忆聊天缺少用户ID的验证
        Map<String, Object> request = new HashMap<>();
        request.put("message", "测试消息");

        mockMvc.perform(post("/api/ai/chat/memory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户ID不能为空"));
    }
}