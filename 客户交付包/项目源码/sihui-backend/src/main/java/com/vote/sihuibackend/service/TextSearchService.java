package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.Document;

import java.util.List;
import java.util.Map;

/**
 * 文本检索服务接口
 * 提供高级文本检索功能，包括TF-IDF算法和相似度匹配
 * 
 * @author Sihui Team
 */
public interface TextSearchService {

    /**
     * 智能检索文档
     * 
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @return 检索结果列表，按相关性排序
     */
    List<SearchResult> intelligentSearch(String query, int limit);

    /**
     * 基于TF-IDF的文档检索
     * 
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @return 检索结果列表
     */
    List<SearchResult> tfidfSearch(String query, int limit);

    /**
     * 查找相似文档
     * 
     * @param documentId 参考文档ID
     * @param limit      返回结果数量限制
     * @return 相似文档列表
     */
    List<SearchResult> findSimilarDocuments(Long documentId, int limit);

    /**
     * 计算两个文档的相似度
     * 
     * @param doc1Id 文档1的ID
     * @param doc2Id 文档2的ID
     * @return 相似度分数 (0-1)
     */
    double calculateDocumentSimilarity(Long doc1Id, Long doc2Id);

    /**
     * 为文档生成TF-IDF向量
     * 
     * @param documentId 文档ID
     * @return TF-IDF向量
     */
    Map<String, Double> generateTfIdfVector(Long documentId);

    /**
     * 更新文档的TF-IDF索引
     * 
     * @param documentId 文档ID
     */
    void updateDocumentIndex(Long documentId);

    /**
     * 重建所有文档的TF-IDF索引
     */
    void rebuildAllIndexes();

    /**
     * 获取关键词在语料库中的IDF值
     * 
     * @param term 关键词
     * @return IDF值
     */
    double getIdfValue(String term);

    /**
     * 检索结果类
     */
    class SearchResult {
        private Document document;
        private double relevanceScore;
        private String highlightedContent;
        private List<String> matchedKeywords;

        public SearchResult(Document document, double relevanceScore) {
            this.document = document;
            this.relevanceScore = relevanceScore;
        }

        public SearchResult(Document document, double relevanceScore, String highlightedContent,
                List<String> matchedKeywords) {
            this.document = document;
            this.relevanceScore = relevanceScore;
            this.highlightedContent = highlightedContent;
            this.matchedKeywords = matchedKeywords;
        }

        // Getters and Setters
        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }

        public double getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(double relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public String getHighlightedContent() {
            return highlightedContent;
        }

        public void setHighlightedContent(String highlightedContent) {
            this.highlightedContent = highlightedContent;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords;
        }
    }
}