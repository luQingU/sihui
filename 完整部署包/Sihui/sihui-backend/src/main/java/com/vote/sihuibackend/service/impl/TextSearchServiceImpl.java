package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.entity.Document;
import com.vote.sihuibackend.repository.DocumentRepository;
import com.vote.sihuibackend.service.TextSearchService;
import com.vote.sihuibackend.service.EnhancedCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本检索服务实现
 * 基于TF-IDF算法的高级文本检索功能
 * 
 * @author Sihui Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TextSearchServiceImpl implements TextSearchService {

    private final DocumentRepository documentRepository;
    private final EnhancedCacheService cacheService;

    // 停用词集合
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "个", "上", "也", "很", "到", "说", "要", "去", "你",
            "会", "着", "没有", "看", "好", "自己", "这", "还", "为", "过", "来", "可以", "大", "小", "多", "少", "能", "下", "对", "生", "同",
            "老", "从", "时", "用", "地", "们", "出", "什么", "进", "如果", "开始", "那", "现在", "因为", "所以", "但是", "或者", "已经", "还是",
            "只是", "这样", "那样", "这里", "那里", "怎么", "为什么", "什么时候", "哪里", "哪个", "怎样", "等", "等等", "可能", "应该", "必须"));

    // 缓存文档的TF-IDF向量
    private final Map<Long, Map<String, Double>> documentVectors = new HashMap<>();

    // 缓存IDF值
    private final Map<String, Double> idfCache = new HashMap<>();

    @Override
    public List<SearchResult> intelligentSearch(String query, int limit) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        try {
            // 预处理查询文本
            List<String> queryTerms = preprocessText(query);
            if (queryTerms.isEmpty()) {
                return Collections.emptyList();
            }

            // 获取候选文档
            List<Document> candidateDocuments = getCandidateDocuments(queryTerms);
            if (candidateDocuments.isEmpty()) {
                return Collections.emptyList();
            }

            // 计算相关性分数
            List<SearchResult> results = new ArrayList<>();
            Map<String, Double> queryVector = generateQueryVector(queryTerms);

            for (Document doc : candidateDocuments) {
                Map<String, Double> docVector = generateTfIdfVector(doc.getId());
                double similarity = calculateCosineSimilarity(queryVector, docVector);

                if (similarity > 0.01) { // 过滤掉相关性太低的结果
                    String highlightedContent = highlightKeywords(doc.getContent(), queryTerms);
                    List<String> matchedKeywords = findMatchedKeywords(doc.getContent(), queryTerms);

                    SearchResult result = new SearchResult(doc, similarity, highlightedContent, matchedKeywords);
                    results.add(result);
                }
            }

            // 按相关性排序并限制结果数量
            return results.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getRelevanceScore(), r1.getRelevanceScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("智能检索失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<SearchResult> tfidfSearch(String query, int limit) {
        return intelligentSearch(query, limit);
    }

    @Override
    public List<SearchResult> findSimilarDocuments(Long documentId, int limit) {
        try {
            Optional<Document> targetDocOpt = documentRepository.findById(documentId);
            if (!targetDocOpt.isPresent()) {
                return Collections.emptyList();
            }

            Document targetDoc = targetDocOpt.get();
            Map<String, Double> targetVector = generateTfIdfVector(documentId);

            List<Document> allDocuments = documentRepository.findByStatus("ACTIVE");
            List<SearchResult> results = new ArrayList<>();

            for (Document doc : allDocuments) {
                if (!doc.getId().equals(documentId)) {
                    Map<String, Double> docVector = generateTfIdfVector(doc.getId());
                    double similarity = calculateCosineSimilarity(targetVector, docVector);

                    if (similarity > 0.05) { // 相似度阈值
                        SearchResult result = new SearchResult(doc, similarity);
                        results.add(result);
                    }
                }
            }

            // 按相似度排序并限制结果数量
            return results.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getRelevanceScore(), r1.getRelevanceScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查找相似文档失败: {}", documentId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public double calculateDocumentSimilarity(Long doc1Id, Long doc2Id) {
        try {
            Map<String, Double> vector1 = generateTfIdfVector(doc1Id);
            Map<String, Double> vector2 = generateTfIdfVector(doc2Id);
            return calculateCosineSimilarity(vector1, vector2);
        } catch (Exception e) {
            log.error("计算文档相似度失败: {} vs {}", doc1Id, doc2Id, e);
            return 0.0;
        }
    }

    @Override
    public Map<String, Double> generateTfIdfVector(Long documentId) {
        // 首先检查增强缓存
        Map<String, Double> cachedVector = cacheService.getTfIdfVector(documentId);
        if (cachedVector != null) {
            return cachedVector;
        }

        // 检查内存缓存
        if (documentVectors.containsKey(documentId)) {
            return documentVectors.get(documentId);
        }

        try {
            Optional<Document> docOpt = documentRepository.findById(documentId);
            if (!docOpt.isPresent()) {
                return Collections.emptyMap();
            }

            Document document = docOpt.get();
            String content = document.getContent();

            if (!StringUtils.hasText(content)) {
                return Collections.emptyMap();
            }

            // 预处理文本
            List<String> terms = preprocessText(content);

            // 计算TF值
            Map<String, Double> tfMap = calculateTermFrequency(terms);

            // 计算TF-IDF向量
            Map<String, Double> tfidfVector = new HashMap<>();
            for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
                String term = entry.getKey();
                double tf = entry.getValue();
                double idf = getIdfValue(term);
                tfidfVector.put(term, tf * idf);
            }

            // 缓存结果到内存和增强缓存
            documentVectors.put(documentId, tfidfVector);
            cacheService.cacheTfIdfVector(documentId, tfidfVector, 3600); // 1小时TTL

            return tfidfVector;

        } catch (Exception e) {
            log.error("生成TF-IDF向量失败: {}", documentId, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void updateDocumentIndex(Long documentId) {
        // 清除所有相关缓存，强制重新计算
        documentVectors.remove(documentId);
        cacheService.invalidateDocumentCache(documentId);
        generateTfIdfVector(documentId);
    }

    @Override
    public void rebuildAllIndexes() {
        log.info("开始重建所有文档的TF-IDF索引");

        // 清除所有缓存
        documentVectors.clear();
        idfCache.clear();

        // 重新计算所有文档的向量
        List<Document> allDocuments = documentRepository.findByStatus("ACTIVE");
        for (Document doc : allDocuments) {
            generateTfIdfVector(doc.getId());
        }

        log.info("完成重建 {} 个文档的TF-IDF索引", allDocuments.size());
    }

    @Override
    public double getIdfValue(String term) {
        // 首先检查增强缓存
        Double cachedIdf = cacheService.getIdfValue(term);
        if (cachedIdf != null) {
            return cachedIdf;
        }

        // 检查内存缓存
        if (idfCache.containsKey(term)) {
            return idfCache.get(term);
        }

        try {
            // 获取包含该词的文档数量
            long documentsContainingTerm = documentRepository.findByStatus("ACTIVE").stream()
                    .mapToLong(doc -> {
                        String content = doc.getContent();
                        if (StringUtils.hasText(content)) {
                            List<String> terms = preprocessText(content);
                            return terms.contains(term) ? 1 : 0;
                        }
                        return 0;
                    })
                    .sum();

            // 总文档数
            long totalDocuments = documentRepository.countByStatus("ACTIVE");

            // 计算IDF值
            double idf = documentsContainingTerm > 0
                    ? Math.log((double) totalDocuments / documentsContainingTerm)
                    : 0.0;

            // 缓存结果到内存和增强缓存
            idfCache.put(term, idf);
            cacheService.cacheIdfValue(term, idf, 7200); // 2小时TTL

            return idf;

        } catch (Exception e) {
            log.error("计算IDF值失败: {}", term, e);
            return 0.0;
        }
    }

    /**
     * 预处理文本：分词、去停用词、转小写
     */
    private List<String> preprocessText(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }

        // 移除标点符号，保留中文、英文、数字
        String cleanText = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ");

        // 简单分词（按空格分割）
        String[] words = cleanText.toLowerCase().split("\\s+");

        List<String> terms = new ArrayList<>();
        for (String word : words) {
            word = word.trim();
            // 过滤停用词和长度太短的词
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                terms.add(word);
            }
        }

        return terms;
    }

    /**
     * 计算词频（TF）
     */
    private Map<String, Double> calculateTermFrequency(List<String> terms) {
        Map<String, Integer> termCounts = new HashMap<>();

        // 统计词频
        for (String term : terms) {
            termCounts.put(term, termCounts.getOrDefault(term, 0) + 1);
        }

        // 计算TF值（归一化）
        Map<String, Double> tfMap = new HashMap<>();
        int totalTerms = terms.size();

        for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
            double tf = (double) entry.getValue() / totalTerms;
            tfMap.put(entry.getKey(), tf);
        }

        return tfMap;
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        // 计算点积
        double dotProduct = 0.0;
        Set<String> commonTerms = new HashSet<>(vector1.keySet());
        commonTerms.retainAll(vector2.keySet());

        for (String term : commonTerms) {
            dotProduct += vector1.get(term) * vector2.get(term);
        }

        // 计算向量长度
        double norm1 = Math.sqrt(vector1.values().stream().mapToDouble(v -> v * v).sum());
        double norm2 = Math.sqrt(vector2.values().stream().mapToDouble(v -> v * v).sum());

        // 计算余弦相似度
        return (norm1 > 0 && norm2 > 0) ? dotProduct / (norm1 * norm2) : 0.0;
    }

    /**
     * 生成查询向量
     */
    private Map<String, Double> generateQueryVector(List<String> queryTerms) {
        Map<String, Double> tfMap = calculateTermFrequency(queryTerms);
        Map<String, Double> queryVector = new HashMap<>();

        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            String term = entry.getKey();
            double tf = entry.getValue();
            double idf = getIdfValue(term);
            queryVector.put(term, tf * idf);
        }

        return queryVector;
    }

    /**
     * 获取候选文档
     */
    private List<Document> getCandidateDocuments(List<String> queryTerms) {
        Set<Document> candidates = new HashSet<>();

        // 基于关键词匹配获取候选文档
        for (String term : queryTerms) {
            List<Document> matchedDocs = documentRepository.searchByKeyword(term, "ACTIVE");
            candidates.addAll(matchedDocs);
        }

        return new ArrayList<>(candidates);
    }

    /**
     * 高亮关键词
     */
    private String highlightKeywords(String content, List<String> keywords) {
        if (!StringUtils.hasText(content) || keywords.isEmpty()) {
            return content;
        }

        String result = content;
        for (String keyword : keywords) {
            // 使用正则表达式进行不区分大小写的替换
            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);
            result = matcher.replaceAll("<mark>$0</mark>");
        }

        // 截取包含关键词的片段（最多300字符）
        if (result.length() > 300) {
            int firstMarkIndex = result.indexOf("<mark>");
            if (firstMarkIndex > 0) {
                int start = Math.max(0, firstMarkIndex - 100);
                int end = Math.min(result.length(), firstMarkIndex + 200);
                result = "..." + result.substring(start, end) + "...";
            } else {
                result = result.substring(0, 300) + "...";
            }
        }

        return result;
    }

    /**
     * 查找匹配的关键词
     */
    private List<String> findMatchedKeywords(String content, List<String> queryTerms) {
        List<String> matched = new ArrayList<>();
        String lowerContent = content.toLowerCase();

        for (String term : queryTerms) {
            if (lowerContent.contains(term.toLowerCase())) {
                matched.add(term);
            }
        }

        return matched;
    }
}