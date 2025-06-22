package com.vote.sihuibackend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.service.*;
import com.vote.sihuibackend.service.TextSearchService.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识增强聊天服务实现
 * 集成AI上下文理解能力，提供基于文档知识的智能问答
 * 
 * @author Sihui Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeEnhancedChatServiceImpl implements KnowledgeEnhancedChatService {

    private final DeepSeekService deepSeekService;
    private final TextSearchService textSearchService;
    private final ChatMemoryService chatMemoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 知识检索的相关性阈值
    private static final double RELEVANCE_THRESHOLD = 0.1;
    // 最大检索结果数量
    private static final int MAX_SEARCH_RESULTS = 5;
    // 最大上下文长度（字符数）
    private static final int MAX_CONTEXT_LENGTH = 3000;

    @Override
    public KnowledgeEnhancedResponse chatWithKnowledge(Long userId, String message, String sessionId)
            throws IOException {
        log.info("User {} initiated knowledge-enhanced conversation: {}", userId, message);

        try {
            // 1. 分析查询意图
            QueryAnalysisResult queryAnalysis = analyzeQuery(message);
            log.info("Query analysis result: intent={}, expanded keywords={}", queryAnalysis.getIntent(),
                    queryAnalysis.getExpandedKeywords());

            // 2. 使用扩展关键词进行知识检索
            List<SearchResult> searchResults = performKnowledgeSearch(queryAnalysis);
            log.info("Knowledge search found {} relevant documents", searchResults.size());

            // 3. AI重新排序检索结果
            List<SearchResult> reRankedResults = reRankResults(message, searchResults);

            // 4. 获取会话历史
            String actualSessionId = sessionId;
            if (actualSessionId == null || actualSessionId.trim().isEmpty()) {
                actualSessionId = chatMemoryService.getOrCreateDefaultSession(userId);
            }
            List<Map<String, String>> conversationHistory = chatMemoryService.convertToApiFormat(
                    chatMemoryService.getRecentMessages(actualSessionId, 5));

            // 5. 基于知识生成答案
            String answer;
            boolean hasKnowledgeSupport = !reRankedResults.isEmpty();
            double confidenceScore = 0.7; // 默认置信度

            if (hasKnowledgeSupport) {
                answer = generateKnowledgeBasedAnswer(message, reRankedResults, conversationHistory);
                // 根据检索结果的相关性计算置信度
                confidenceScore = calculateConfidenceScore(reRankedResults);
            } else {
                // 没有找到相关知识，使用普通AI对话
                answer = deepSeekService.chatWithMemory(userId, message, actualSessionId);
                confidenceScore = 0.5; // 降低置信度
            }

            // 6. 构建响应
            KnowledgeEnhancedResponse response = new KnowledgeEnhancedResponse(
                    answer,
                    reRankedResults,
                    queryAnalysis,
                    confidenceScore,
                    hasKnowledgeSupport);

            log.info("Knowledge-enhanced conversation completed, confidence: {}, knowledge support: {}",
                    confidenceScore, hasKnowledgeSupport);
            return response;

        } catch (Exception e) {
            log.error("Knowledge-enhanced conversation failed", e);
            // 降级到普通AI对话
            String fallbackAnswer = deepSeekService.chatWithMemory(userId, message, sessionId);
            return new KnowledgeEnhancedResponse(
                    fallbackAnswer,
                    Collections.emptyList(),
                    new QueryAnalysisResult(message, "unknown", Collections.emptyList(), Collections.emptyList(), 0.0),
                    0.3,
                    false);
        }
    }

    @Override
    public QueryAnalysisResult analyzeQuery(String query) throws IOException {
        log.info("Starting query intent analysis: {}", query);

        String analysisPrompt = buildQueryAnalysisPrompt(query);

        try {
            String aiResponse = deepSeekService.chat(analysisPrompt);
            return parseQueryAnalysisResponse(query, aiResponse);

        } catch (Exception e) {
            log.error("Query analysis failed, using default analysis", e);
            // 降级到简单的关键词提取
            return createFallbackAnalysis(query);
        }
    }

    @Override
    public List<SearchResult> reRankResults(String query, List<SearchResult> searchResults) throws IOException {
        if (searchResults.isEmpty()) {
            return searchResults;
        }

        log.info("Starting AI re-ranking of {} search results", searchResults.size());

        try {
            String reRankPrompt = buildReRankPrompt(query, searchResults);
            String aiResponse = deepSeekService.chat(reRankPrompt);

            return parseReRankResponse(searchResults, aiResponse);

        } catch (Exception e) {
            log.error("AI re-ranking failed, returning original results", e);
            return searchResults;
        }
    }

    @Override
    public String generateKnowledgeBasedAnswer(String query, List<SearchResult> relevantResults,
            List<Map<String, String>> conversationHistory) throws IOException {
        log.info("Generating knowledge answer based on {} documents", relevantResults.size());

        String knowledgePrompt = buildKnowledgeAnswerPrompt(query, relevantResults, conversationHistory);

        return deepSeekService.chat(knowledgePrompt, conversationHistory);
    }

    /**
     * 执行知识检索
     */
    private List<SearchResult> performKnowledgeSearch(QueryAnalysisResult queryAnalysis) {
        List<SearchResult> allResults = new ArrayList<>();

        // 使用原始查询检索
        List<SearchResult> originalResults = textSearchService.intelligentSearch(
                queryAnalysis.getOriginalQuery(), MAX_SEARCH_RESULTS);
        allResults.addAll(originalResults);

        // 使用扩展关键词检索
        for (String keyword : queryAnalysis.getExpandedKeywords()) {
            List<SearchResult> keywordResults = textSearchService.intelligentSearch(keyword, 3);
            allResults.addAll(keywordResults);
        }

        // 去重并按相关性过滤
        return allResults.stream()
                .collect(Collectors.toMap(
                        result -> result.getDocument().getId(),
                        result -> result,
                        (existing, replacement) -> existing.getRelevanceScore() > replacement.getRelevanceScore()
                                ? existing
                                : replacement))
                .values()
                .stream()
                .filter(result -> result.getRelevanceScore() > RELEVANCE_THRESHOLD)
                .sorted((r1, r2) -> Double.compare(r2.getRelevanceScore(), r1.getRelevanceScore()))
                .limit(MAX_SEARCH_RESULTS)
                .collect(Collectors.toList());
    }

    /**
     * 构建查询分析提示词
     */
    private String buildQueryAnalysisPrompt(String query) {
        return String.format(
                "Please analyze the following user query's intent and key information, return results in JSON format:\n\n"
                        +
                        "User query: \"%s\"\n\n" +
                        "Please analyze:\n" +
                        "1. Query intent type (e.g.: information query, operation guidance, concept explanation, problem solving, etc.)\n"
                        +
                        "2. Extract and expand related keywords (including synonyms, related terms)\n" +
                        "3. Identify named entities (person names, place names, organization names, professional terms, etc.)\n"
                        +
                        "4. Analyze confidence (value between 0-1)\n\n" +
                        "Return format:\n" +
                        "{\n" +
                        "    \"intent\": \"query intent\",\n" +
                        "    \"expandedKeywords\": [\"keyword1\", \"keyword2\", \"...\"],\n" +
                        "    \"entities\": [\"entity1\", \"entity2\", \"...\"],\n" +
                        "    \"confidence\": 0.95\n" +
                        "}\n\n" +
                        "Only return JSON, no other explanations.",
                query);
    }

    /**
     * 构建重排序提示词
     */
    private String buildReRankPrompt(String query, List<SearchResult> searchResults) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("User query: \"%s\"\n\n", query));
        prompt.append(
                "The following are retrieved document segments, please re-rank them based on relevance to the user query:\n\n");

        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            prompt.append(String.format("Document %d: %s\n", i + 1, result.getDocument().getTitle()));
            prompt.append(String.format("Content: %s\n",
                    truncateContent(result.getHighlightedContent() != null ? result.getHighlightedContent()
                            : result.getDocument().getContent(), 200)));
            prompt.append(String.format("Original relevance: %.3f\n\n", result.getRelevanceScore()));
        }

        prompt.append(
                "Please return the re-ranked document number list in JSON array format, for example: [2, 1, 4, 3, 5]\n");
        prompt.append("Only return JSON array, no other explanations.");

        return prompt.toString();
    }

    /**
     * 构建知识答案生成提示词
     */
    private String buildKnowledgeAnswerPrompt(String query, List<SearchResult> relevantResults,
            List<Map<String, String>> conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are an intelligent assistant for the Sihui training platform. Please answer user questions based on the following knowledge base content:\n\n");

        prompt.append("User question: ").append(query).append("\n\n");

        prompt.append("Relevant knowledge base content:\n");
        for (int i = 0; i < relevantResults.size(); i++) {
            SearchResult result = relevantResults.get(i);
            prompt.append(String.format("Knowledge segment %d (%s):\n", i + 1, result.getDocument().getTitle()));
            prompt.append(truncateContent(result.getDocument().getContent(), 500)).append("\n\n");
        }

        prompt.append("Answer requirements:\n");
        prompt.append("1. Answer based on the provided knowledge base content, ensure accuracy\n");
        prompt.append("2. If there is no relevant information in the knowledge base, please state honestly\n");
        prompt.append("3. Maintain a professional and friendly tone\n");
        prompt.append("4. Appropriately cite specific content from the knowledge base\n");
        prompt.append("5. If needed, combine conversation history to provide better answers\n\n");

        prompt.append("Please provide a detailed and accurate answer:");

        return prompt.toString();
    }

    /**
     * 解析查询分析响应
     */
    private QueryAnalysisResult parseQueryAnalysisResponse(String originalQuery, String aiResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            String intent = jsonNode.has("intent") ? jsonNode.get("intent").asText() : "unknown";
            double confidence = jsonNode.has("confidence") ? jsonNode.get("confidence").asDouble() : 0.5;

            List<String> expandedKeywords = new ArrayList<>();
            if (jsonNode.has("expandedKeywords") && jsonNode.get("expandedKeywords").isArray()) {
                for (JsonNode keywordNode : jsonNode.get("expandedKeywords")) {
                    expandedKeywords.add(keywordNode.asText());
                }
            }

            List<String> entities = new ArrayList<>();
            if (jsonNode.has("entities") && jsonNode.get("entities").isArray()) {
                for (JsonNode entityNode : jsonNode.get("entities")) {
                    entities.add(entityNode.asText());
                }
            }

            return new QueryAnalysisResult(originalQuery, intent, expandedKeywords, entities, confidence);

        } catch (Exception e) {
            log.error("Failed to parse query analysis response", e);
            return createFallbackAnalysis(originalQuery);
        }
    }

    /**
     * 解析重排序响应
     */
    private List<SearchResult> parseReRankResponse(List<SearchResult> originalResults, String aiResponse) {
        try {
            // 提取JSON数组
            String jsonStr = aiResponse.trim();
            if (jsonStr.startsWith("```")) {
                int start = jsonStr.indexOf('[');
                int end = jsonStr.lastIndexOf(']') + 1;
                if (start >= 0 && end > start) {
                    jsonStr = jsonStr.substring(start, end);
                }
            }

            List<Integer> newOrder = objectMapper.readValue(jsonStr, new TypeReference<List<Integer>>() {
            });

            List<SearchResult> reRankedResults = new ArrayList<>();
            for (Integer index : newOrder) {
                if (index >= 1 && index <= originalResults.size()) {
                    reRankedResults.add(originalResults.get(index - 1));
                }
            }

            // 如果重排序结果不完整，补充剩余的结果
            for (SearchResult result : originalResults) {
                if (!reRankedResults.contains(result)) {
                    reRankedResults.add(result);
                }
            }

            return reRankedResults;

        } catch (Exception e) {
            log.error("Failed to parse re-ranking response", e);
            return originalResults;
        }
    }

    /**
     * 创建降级分析结果
     */
    private QueryAnalysisResult createFallbackAnalysis(String query) {
        // 简单的关键词提取
        String[] words = query.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ")
                .toLowerCase()
                .split("\\s+");

        List<String> keywords = Arrays.stream(words)
                .filter(word -> word.length() >= 2)
                .distinct()
                .collect(Collectors.toList());

        return new QueryAnalysisResult(query, "information_query", keywords, Collections.emptyList(), 0.3);
    }

    /**
     * 计算置信度分数
     */
    private double calculateConfidenceScore(List<SearchResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }

        double avgRelevance = results.stream()
                .mapToDouble(SearchResult::getRelevanceScore)
                .average()
                .orElse(0.0);

        // 基于平均相关性和结果数量计算置信度
        double countFactor = Math.min(results.size() / 3.0, 1.0);
        return Math.min(avgRelevance * countFactor * 1.2, 1.0);
    }

    /**
     * 截断内容到指定长度
     */
    private String truncateContent(String content, int maxLength) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        if (content.length() <= maxLength) {
            return content;
        }

        return content.substring(0, maxLength) + "...";
    }
}