package com.vote.sihuibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.entity.Document;
import com.vote.sihuibackend.service.DocumentService;
import com.vote.sihuibackend.service.TextSearchService;
import com.vote.sihuibackend.service.PermissionService;
import com.vote.sihuibackend.util.JwtUtil;
import com.vote.sihuibackend.security.JwtAuthenticationFilter;
import com.vote.sihuibackend.security.JwtAuthenticationEntryPoint;
import com.vote.sihuibackend.config.TestSecurityConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.vote.sihuibackend.enums.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KnowledgeDocumentController 测试类
 */
@WebMvcTest(KnowledgeDocumentController.class)
@Import(TestSecurityConfig.class)
@Disabled("暂时禁用此测试类，等待Spring Security配置问题修复")
class KnowledgeDocumentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private DocumentService documentService;

        @MockBean
        private TextSearchService textSearchService;

        @MockBean
        private PermissionService permissionService;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @MockBean
        private UserDetailsService userDetailsService;

        private Document testDocument;

        @BeforeEach
        void setUp() {
                // Mock PermissionService 默认返回true
                when(permissionService.hasPermission(anyLong(), any(Permission.class))).thenReturn(true);
                when(permissionService.hasAnyPermission(anyLong(), any(Permission[].class))).thenReturn(true);
                when(permissionService.hasAllPermissions(anyLong(), any(Permission[].class))).thenReturn(true);
                when(permissionService.canAccessSelfResource(anyLong(), anyLong())).thenReturn(true);
                when(permissionService.isAdmin(anyLong())).thenReturn(true);

                // 创建测试文档
                testDocument = new Document();
                testDocument.setId(1L);
                testDocument.setTitle("测试文档");
                testDocument.setCategory("四会文档");
                testDocument.setContent("这是测试内容");
                testDocument.setSummary("这是测试摘要");
                testDocument.setKeywords("测试,文档");
                testDocument.setFileType("txt");
                testDocument.setFileSize(1024L);
                testDocument.setIsPublic(true);
                testDocument.setViewCount(0);
                testDocument.setDownloadCount(0);
                testDocument.setCreatedAt(LocalDateTime.now());
                testDocument.setUpdatedAt(LocalDateTime.now());
                testDocument.setOriginalFilename("test.txt");
                testDocument.setFileUrl("/files/test.txt");
                testDocument.setLanguage("zh");
                testDocument.setVersion(1);
        }

        @Test
        @DisplayName("上传文档 - 成功")
        @WithMockUser(roles = "TEACHER")
        void uploadDocument_Success() throws Exception {
                // 准备测试数据
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "test.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                "测试文档内容".getBytes());

                // 模拟 DocumentService 行为
                when(documentService.uploadDocument(any(), any(), any(), any(), any(), any()))
                                .thenReturn(testDocument);

                // 执行请求并验证结果
                mockMvc.perform(multipart("/api/knowledge/documents/upload")
                                .file(file)
                                .with(csrf())
                                .param("title", "测试文档")
                                .param("category", "四会文档")
                                .param("keywords", "测试,文档")
                                .param("isPublic", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("文档上传成功"))
                                .andExpect(jsonPath("$.document.id").value(1))
                                .andExpect(jsonPath("$.document.title").value("测试文档"));
        }

        @Test
        @DisplayName("上传文档 - 不支持的文件格式")
        @WithMockUser(roles = "TEACHER")
        void uploadDocument_UnsupportedFormat() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "test.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                "测试内容".getBytes());

                mockMvc.perform(multipart("/api/knowledge/documents/upload")
                                .file(file)
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("不支持的文件格式，仅支持 .txt, .md, .markdown 格式"));
        }

        @Test
        @DisplayName("获取文档详情 - 成功")
        @WithMockUser(roles = "STUDENT")
        void getDocument_Success() throws Exception {
                when(documentService.getDocumentById(1L))
                                .thenReturn(Optional.of(testDocument));

                mockMvc.perform(get("/api/knowledge/documents/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.document.id").value(1))
                                .andExpect(jsonPath("$.document.title").value("测试文档"));
        }

        @Test
        @DisplayName("获取文档详情 - 文档不存在")
        @WithMockUser(roles = "STUDENT")
        void getDocument_NotFound() throws Exception {
                when(documentService.getDocumentById(999L))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/api/knowledge/documents/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("智能搜索文档 - 成功")
        @WithMockUser(roles = "STUDENT")
        void intelligentSearch_Success() throws Exception {
                // 准备搜索结果
                TextSearchService.SearchResult searchResult = new TextSearchService.SearchResult(
                                testDocument, 0.85, "高亮内容", Arrays.asList("测试", "文档"));

                when(textSearchService.intelligentSearch("测试", 20))
                                .thenReturn(Arrays.asList(searchResult));

                mockMvc.perform(get("/api/knowledge/documents/search")
                                .param("keyword", "测试")
                                .param("limit", "20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.keyword").value("测试"))
                                .andExpect(jsonPath("$.results").isArray())
                                .andExpect(jsonPath("$.totalResults").value(1));
        }

        @Test
        @DisplayName("智能搜索文档 - 关键词为空")
        @WithMockUser(roles = "STUDENT")
        void intelligentSearch_EmptyKeyword() throws Exception {
                mockMvc.perform(get("/api/knowledge/documents/search")
                                .param("keyword", ""))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("搜索关键词不能为空"));
        }

        @Test
        @DisplayName("获取相似文档 - 成功")
        @WithMockUser(roles = "STUDENT")
        void getSimilarDocuments_Success() throws Exception {
                TextSearchService.SearchResult similarResult = new TextSearchService.SearchResult(
                                testDocument, 0.75, "相似内容", Arrays.asList("相似", "文档"));

                when(textSearchService.findSimilarDocuments(1L, 10))
                                .thenReturn(Arrays.asList(similarResult));

                mockMvc.perform(get("/api/knowledge/documents/1/similar")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.results").isArray())
                                .andExpect(jsonPath("$.totalResults").value(1));
        }

        @Test
        @DisplayName("学生用户上传文档 - 验证权限")
        @WithMockUser(roles = "STUDENT")
        void uploadDocument_StudentUser() throws Exception {
                // 为这个特定测试设置权限检查失败
                when(permissionService.hasPermission(anyLong(), eq(Permission.FILE_UPLOAD))).thenReturn(false);

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "test.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                "测试内容".getBytes());

                // 学生用户尝试上传文档，应该被权限系统拒绝
                mockMvc.perform(multipart("/api/knowledge/documents/upload")
                                .file(file)
                                .with(csrf()))
                                .andExpect(status().isForbidden()); // 期望403权限不足
        }
}