# AI上下文理解API使用指南

## 概述

四会培训平台现已集成AI上下文理解能力，通过结合文档知识库和智能问答技术，为用户提供更准确、更有针对性的回答。

## 核心功能

### 1. 智能查询分析
- 自动识别用户查询意图
- 扩展相关关键词和同义词
- 提取命名实体
- 评估查询置信度

### 2. 知识检索增强
- 基于TF-IDF的文档检索
- AI重新排序检索结果
- 多层次关键词匹配

### 3. 上下文感知回答
- 基于检索到的知识生成答案
- 结合对话历史提供连贯回复
- 智能置信度评估

## API接口

### 知识增强聊天

**接口地址：** `POST /api/ai/chat/knowledge`

**请求参数：**
```json
{
    "userId": 1,
    "message": "什么是四会培训？",
    "sessionId": "optional-session-id"
}
```

**响应示例：**
```json
{
    "success": true,
    "message": "请求成功",
    "data": {
        "answer": "根据知识库，四会培训是指'会听、会说、会读、会写'的基础培训内容...",
        "hasKnowledgeSupport": true,
        "confidenceScore": 0.85,
        "queryAnalysis": {
            "intent": "概念解释",
            "expandedKeywords": ["四会培训", "培训内容", "培训方法"],
            "entities": ["四会"],
            "confidence": 0.9
        },
        "sourceDocuments": [
            {
                "title": "四会培训基础知识",
                "relevanceScore": 0.92,
                "highlightedContent": "四会培训是指<mark>会听</mark>、<mark>会说</mark>、<mark>会读</mark>、<mark>会写</mark>的基础培训...",
                "matchedKeywords": ["四会培训", "培训"]
            }
        ]
    }
}
```

## 技术特性

### 1. 智能意图识别
系统能够自动识别用户查询的类型：
- **信息查询**：寻求特定信息的问题
- **操作指导**：如何执行某项操作
- **概念解释**：要求解释某个概念
- **问题解决**：寻求解决方案的问题

### 2. 关键词扩展
- 自动提取查询中的关键词
- 生成相关同义词和术语
- 扩展搜索范围提高召回率

### 3. AI重排序
- 使用AI模型重新评估文档相关性
- 考虑语义相似度而非仅仅关键词匹配
- 提高搜索结果的准确性

### 4. 置信度评估
- 基于检索结果质量计算置信度
- 当置信度较低时自动降级到普通对话
- 为用户提供答案可信度参考

## 使用场景

### 1. 培训内容查询
```javascript
// 查询培训相关问题
const response = await fetch('/api/ai/chat/knowledge', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        userId: 1,
        message: "四会培训的具体内容包括哪些？"
    })
});
```

### 2. 操作指导
```javascript
// 询问操作步骤
const response = await fetch('/api/ai/chat/knowledge', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        userId: 1,
        message: "如何上传培训文档？"
    })
});
```

### 3. 政策解释
```javascript
// 查询政策相关信息
const response = await fetch('/api/ai/chat/knowledge', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        userId: 1,
        message: "最新的培训政策有哪些变化？"
    })
});
```

## 最佳实践

### 1. 文档管理
- 确保知识库文档内容准确、完整
- 定期更新文档内容
- 使用清晰的文档标题和结构

### 2. 查询优化
- 使用具体、明确的问题
- 避免过于宽泛的查询
- 可以包含相关上下文信息

### 3. 结果处理
- 检查 `hasKnowledgeSupport` 标识
- 根据 `confidenceScore` 评估答案可信度
- 利用 `sourceDocuments` 提供引用信息

## 错误处理

### 常见错误码
- **400**: 请求参数错误（用户ID或消息为空）
- **500**: AI服务不可用或系统异常

### 降级机制
当知识增强功能不可用时，系统会自动降级到普通AI对话模式，确保用户仍能获得基本的问答服务。

## 性能考虑

### 1. 响应时间
- 知识增强聊天通常需要2-5秒
- 包含多次AI调用（查询分析、重排序、答案生成）
- 建议在前端显示加载状态

### 2. 并发处理
- 系统支持多用户并发访问
- 每个用户的会话独立管理
- 自动处理会话超时和清理

### 3. 缓存优化
- 查询分析结果可缓存
- 文档检索结果有缓存机制
- TF-IDF向量计算结果缓存

## 监控和调试

### 日志信息
系统会记录详细的操作日志：
- 查询分析结果
- 检索到的文档数量
- AI重排序效果
- 最终的置信度评分

### 性能指标
- 查询响应时间
- 知识支持命中率
- 平均置信度分数
- 用户满意度反馈

通过这些功能，四会培训平台能够为用户提供更智能、更准确的问答服务，大大提升用户体验和培训效果。 