package com.vote.sihuibackend.service;

import com.vote.sihuibackend.service.TextSearchService.SearchResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 知识增强聊天服务接口
 * 集成AI上下文理解能力，提供基于文档知识的智能问答
 * 
 * @author Sihui Team
 */
public interface KnowledgeEnhancedChatService {

    /**
     * 基于知识库的智能问答
     * 
     * @param userId    用户ID
     * @param message   用户消息
     * @param sessionId 会话ID（可选）
     * @return 增强后的AI回复
     * @throws IOException 当API调用失败时抛出
     */
    KnowledgeEnhancedResponse chatWithKnowledge(Long userId, String message, String sessionId) throws IOException;

    /**
     * 分析查询意图并扩展关键词
     * 
     * @param query 原始查询
     * @return 扩展后的查询信息
     * @throws IOException 当AI调用失败时抛出
     */
    QueryAnalysisResult analyzeQuery(String query) throws IOException;

    /**
     * 基于AI重新排序检索结果
     * 
     * @param query         原始查询
     * @param searchResults 初始检索结果
     * @return 重新排序后的结果
     * @throws IOException 当AI调用失败时抛出
     */
    List<SearchResult> reRankResults(String query, List<SearchResult> searchResults) throws IOException;

    /**
     * 基于检索到的知识生成答案
     * 
     * @param query               用户查询
     * @param relevantResults     相关文档结果
     * @param conversationHistory 对话历史
     * @return 生成的答案
     * @throws IOException 当AI调用失败时抛出
     */
    String generateKnowledgeBasedAnswer(String query, List<SearchResult> relevantResults,
            List<Map<String, String>> conversationHistory) throws IOException;

    /**
     * 查询分析结果
     */
    class QueryAnalysisResult {
        private String originalQuery;
        private String intent;
        private List<String> expandedKeywords;
        private List<String> entities;
        private double confidence;

        public QueryAnalysisResult(String originalQuery, String intent, List<String> expandedKeywords,
                List<String> entities, double confidence) {
            this.originalQuery = originalQuery;
            this.intent = intent;
            this.expandedKeywords = expandedKeywords;
            this.entities = entities;
            this.confidence = confidence;
        }

        // Getters and Setters
        public String getOriginalQuery() {
            return originalQuery;
        }

        public void setOriginalQuery(String originalQuery) {
            this.originalQuery = originalQuery;
        }

        public String getIntent() {
            return intent;
        }

        public void setIntent(String intent) {
            this.intent = intent;
        }

        public List<String> getExpandedKeywords() {
            return expandedKeywords;
        }

        public void setExpandedKeywords(List<String> expandedKeywords) {
            this.expandedKeywords = expandedKeywords;
        }

        public List<String> getEntities() {
            return entities;
        }

        public void setEntities(List<String> entities) {
            this.entities = entities;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }

    /**
     * 知识增强响应
     */
    class KnowledgeEnhancedResponse {
        private String answer;
        private List<SearchResult> sourceDocuments;
        private QueryAnalysisResult queryAnalysis;
        private double confidenceScore;
        private boolean hasKnowledgeSupport;

        public KnowledgeEnhancedResponse(String answer, List<SearchResult> sourceDocuments,
                QueryAnalysisResult queryAnalysis, double confidenceScore,
                boolean hasKnowledgeSupport) {
            this.answer = answer;
            this.sourceDocuments = sourceDocuments;
            this.queryAnalysis = queryAnalysis;
            this.confidenceScore = confidenceScore;
            this.hasKnowledgeSupport = hasKnowledgeSupport;
        }

        // Getters and Setters
        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public List<SearchResult> getSourceDocuments() {
            return sourceDocuments;
        }

        public void setSourceDocuments(List<SearchResult> sourceDocuments) {
            this.sourceDocuments = sourceDocuments;
        }

        public QueryAnalysisResult getQueryAnalysis() {
            return queryAnalysis;
        }

        public void setQueryAnalysis(QueryAnalysisResult queryAnalysis) {
            this.queryAnalysis = queryAnalysis;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public boolean isHasKnowledgeSupport() {
            return hasKnowledgeSupport;
        }

        public void setHasKnowledgeSupport(boolean hasKnowledgeSupport) {
            this.hasKnowledgeSupport = hasKnowledgeSupport;
        }
    }
}