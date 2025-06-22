package com.vote.sihuibackend.service;

import com.vote.sihuibackend.service.impl.KnowledgeEnhancedChatServiceImpl;
import com.vote.sihuibackend.service.TextSearchService.SearchResult;
import com.vote.sihuibackend.service.KnowledgeEnhancedChatService.QueryAnalysisResult;
import com.vote.sihuibackend.service.KnowledgeEnhancedChatService.KnowledgeEnhancedResponse;
import com.vote.sihuibackend.entity.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 知识增强聊天服务测试
 */
@ExtendWith(MockitoExtension.class)
class KnowledgeEnhancedChatServiceTest {

        @Mock
        private DeepSeekService deepSeekService;

        @Mock
        private TextSearchService textSearchService;

        @Mock
        private ChatMemoryService chatMemoryService;

        private KnowledgeEnhancedChatServiceImpl knowledgeEnhancedChatService;

        @BeforeEach
        void setUp() {
                knowledgeEnhancedChatService = new KnowledgeEnhancedChatServiceImpl(
                                deepSeekService, textSearchService, chatMemoryService);
        }

        @Test
        void testAnalyzeQuery_Success() throws IOException {
                // 准备测试数据
                String query = "什么是四会培训？";
                String mockAiResponse = "{\n" +
                                "    \"intent\": \"概念解释\",\n" +
                                "    \"expandedKeywords\": [\"四会培训\", \"培训内容\", \"培训方法\"],\n" +
                                "    \"entities\": [\"四会\"],\n" +
                                "    \"confidence\": 0.9\n" +
                                "}";

                when(deepSeekService.chat(anyString())).thenReturn(mockAiResponse);

                // 执行测试
                QueryAnalysisResult result = knowledgeEnhancedChatService.analyzeQuery(query);

                // 验证结果
                assertNotNull(result);
                assertEquals(query, result.getOriginalQuery());
                assertEquals("概念解释", result.getIntent());
                assertEquals(3, result.getExpandedKeywords().size());
                assertTrue(result.getExpandedKeywords().contains("四会培训"));
                assertEquals(0.9, result.getConfidence(), 0.01);
        }

        @Test
        void testAnalyzeQuery_Fallback() throws IOException {
                // 准备测试数据 - AI调用失败
                String query = "什么是培训？";
                when(deepSeekService.chat(anyString())).thenThrow(new IOException("API调用失败"));

                // 执行测试
                QueryAnalysisResult result = knowledgeEnhancedChatService.analyzeQuery(query);

                // 验证降级结果
                assertNotNull(result);
                assertEquals(query, result.getOriginalQuery());
                assertEquals("information_query", result.getIntent());
                assertEquals(0.3, result.getConfidence(), 0.01);
        }

        @Test
        void testReRankResults_Success() throws IOException {
                // 准备测试数据
                String query = "四会培训内容";
                List<SearchResult> originalResults = createMockSearchResults();
                String mockReRankResponse = "[2, 1, 3]";

                when(deepSeekService.chat(anyString())).thenReturn(mockReRankResponse);

                // 执行测试
                List<SearchResult> reRankedResults = knowledgeEnhancedChatService.reRankResults(query, originalResults);

                // 验证结果
                assertNotNull(reRankedResults);
                assertEquals(3, reRankedResults.size());
                // 验证重排序是否生效（第2个文档应该排在第一位）
                assertEquals(2L, reRankedResults.get(0).getDocument().getId());
        }

        @Test
        void testGenerateKnowledgeBasedAnswer() throws IOException {
                // 准备测试数据
                String query = "四会培训的主要内容是什么？";
                List<SearchResult> relevantResults = createMockSearchResults();
                List<Map<String, String>> conversationHistory = new ArrayList<>();
                String expectedAnswer = "根据知识库，四会培训包括...";

                when(deepSeekService.chat(anyString(), eq(conversationHistory)))
                                .thenReturn(expectedAnswer);

                // 执行测试
                String answer = knowledgeEnhancedChatService.generateKnowledgeBasedAnswer(
                                query, relevantResults, conversationHistory);

                // 验证结果
                assertEquals(expectedAnswer, answer);
                verify(deepSeekService).chat(anyString(), eq(conversationHistory));
        }

        @Test
        void testChatWithKnowledge_WithKnowledgeSupport() throws IOException {
                // 准备测试数据
                Long userId = 1L;
                String message = "四会培训包括哪些内容？";
                String sessionId = "test-session";

                // Mock检索结果
                List<SearchResult> mockSearchResults = createMockSearchResults();

                // Mock会话管理
                lenient().when(chatMemoryService.getOrCreateDefaultSession(userId)).thenReturn(sessionId);
                lenient().when(chatMemoryService.getRecentMessages(sessionId, 5)).thenReturn(new ArrayList<>());
                lenient().when(chatMemoryService.convertToApiFormat(any())).thenReturn(new ArrayList<>());

                // Mock AI调用 - 使用lenient避免不必要的stubbing错误
                lenient().when(deepSeekService.chat(anyString()))
                                .thenReturn("{\"intent\":\"信息查询\",\"expandedKeywords\":[\"四会培训\",\"培训内容\"],\"entities\":[\"四会\"],\"confidence\":0.8}");
                lenient().when(deepSeekService.chat(anyString(), any()))
                                .thenReturn("根据知识库，四会培训主要包括...");

                // Mock文本检索
                lenient().when(textSearchService.intelligentSearch(anyString(), anyInt()))
                                .thenReturn(mockSearchResults);

                // 执行测试
                KnowledgeEnhancedResponse response = knowledgeEnhancedChatService
                                .chatWithKnowledge(userId, message, sessionId);

                // 验证结果
                assertNotNull(response);
                assertNotNull(response.getAnswer());
                assertTrue(response.isHasKnowledgeSupport());
                assertTrue(response.getConfidenceScore() > 0);
                assertNotNull(response.getQueryAnalysis());
                assertFalse(response.getSourceDocuments().isEmpty());
        }

        /**
         * 创建模拟的搜索结果
         */
        private List<SearchResult> createMockSearchResults() {
                List<SearchResult> results = new ArrayList<>();

                Document doc1 = new Document();
                doc1.setId(1L);
                doc1.setTitle("四会培训基础知识");
                doc1.setContent("四会培训是指会听、会说、会读、会写的基础培训内容...");

                Document doc2 = new Document();
                doc2.setId(2L);
                doc2.setTitle("四会培训实施方案");
                doc2.setContent("四会培训的实施需要制定详细的方案和计划...");

                Document doc3 = new Document();
                doc3.setId(3L);
                doc3.setTitle("四会培训评估标准");
                doc3.setContent("四会培训的评估应该从多个维度进行考核...");

                results.add(new SearchResult(doc1, 0.8, "四会培训是指<mark>会听</mark>、<mark>会说</mark>...",
                                Arrays.asList("四会培训", "培训")));
                results.add(new SearchResult(doc2, 0.7, "四会培训的<mark>实施</mark>需要制定...",
                                Arrays.asList("四会培训", "实施")));
                results.add(new SearchResult(doc3, 0.6, "四会培训的<mark>评估</mark>应该从...",
                                Arrays.asList("四会培训", "评估")));

                return results;
        }
}