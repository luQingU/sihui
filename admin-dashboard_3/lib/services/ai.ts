import { api } from '../api'
import type { 
  ChatSession,
  ChatMessage,
  ChatRequest,
  ChatResponse,
  KnowledgeDocument,
  UploadDocumentRequest,
  DocumentSearchResult,
  PaginationParams,
  PageResponse
} from '../../types/api'

// AI聊天服务
export const aiChatService = {
  // 简单聊天
  async chat(data: ChatRequest): Promise<ChatResponse> {
    return api.post<ChatResponse>('/api/ai/chat', data)
  },

  // 带历史记录的对话
  async chatWithHistory(data: ChatRequest): Promise<ChatResponse> {
    return api.post<ChatResponse>('/api/ai/chat/conversation', data)
  },

  // 智能记忆对话
  async chatWithMemory(data: ChatRequest): Promise<ChatResponse> {
    return api.post<ChatResponse>('/api/ai/chat/memory', data)
  },

  // 知识增强问答
  async chatWithKnowledge(data: ChatRequest): Promise<ChatResponse> {
    return api.post<ChatResponse>('/api/ai/chat/knowledge', data)
  },

  // 获取对话会话列表
  async getChatSessions(params?: PaginationParams & {
    userId?: number
    status?: 'active' | 'completed'
    startDate?: string
    endDate?: string
  }): Promise<PageResponse<ChatSession>> {
    return api.get<PageResponse<ChatSession>>('/api/ai/chat/sessions', params)
  },

  // 获取指定会话详情
  async getChatSession(sessionId: string): Promise<ChatSession> {
    return api.get<ChatSession>(`/api/ai/chat/sessions/${sessionId}`)
  },

  // 获取会话消息历史
  async getChatMessages(sessionId: string, params?: PaginationParams): Promise<PageResponse<ChatMessage>> {
    return api.get<PageResponse<ChatMessage>>(`/api/ai/chat/sessions/${sessionId}/messages`, params)
  },

  // 删除会话
  async deleteChatSession(sessionId: string): Promise<void> {
    return api.delete(`/api/ai/chat/sessions/${sessionId}`)
  },

  // 获取AI服务统计
  async getAiStats(): Promise<{
    todayChats: number
    activeSessions: number
    averageResponseTime: number
    userSatisfaction: number
    topQuestions: Array<{
      question: string
      count: number
    }>
    roleDistribution: Array<{
      role: string
      percentage: number
      count: number
    }>
  }> {
    return api.get('/api/ai/chat/stats')
  },

  // 获取AI配置信息
  async getAiConfig(): Promise<{
    apiStatus: 'UP' | 'DOWN'
    modelVersion: string
    maxTokens: number
    temperature: number
    dailyUsage: number
    monthlyQuota: number
    usedQuota: number
    averageCost: number
  }> {
    return api.get('/api/ai/config')
  }
}

// 知识文档服务
export const knowledgeService = {
  // 上传知识文档
  async uploadDocument(file: File, metadata: UploadDocumentRequest): Promise<KnowledgeDocument> {
    const formData = new FormData()
    formData.append('file', file)
    
    if (metadata.title) formData.append('title', metadata.title)
    if (metadata.category) formData.append('category', metadata.category)
    if (metadata.keywords) formData.append('keywords', metadata.keywords)
    if (metadata.isPublic !== undefined) formData.append('isPublic', String(metadata.isPublic))

    return api.upload<KnowledgeDocument>('/api/knowledge/documents/upload', formData)
  },

  // 获取文档详情
  async getDocument(id: number): Promise<KnowledgeDocument> {
    return api.get<KnowledgeDocument>(`/api/knowledge/documents/${id}`)
  },

  // 获取文档列表
  async getDocuments(params?: PaginationParams & {
    category?: string
    keyword?: string
    isPublic?: boolean
  }): Promise<PageResponse<KnowledgeDocument>> {
    return api.get<PageResponse<KnowledgeDocument>>('/api/knowledge/documents', params)
  },

  // 智能搜索文档
  async searchDocuments(params: {
    keyword: string
    limit?: number
  }): Promise<DocumentSearchResult[]> {
    return api.get<DocumentSearchResult[]>('/api/knowledge/documents/search', {
      keyword: params.keyword,
      limit: params.limit || 20
    })
  },

  // 获取相似文档
  async getSimilarDocuments(id: number, limit?: number): Promise<DocumentSearchResult[]> {
    return api.get<DocumentSearchResult[]>(`/api/knowledge/documents/${id}/similar`, {
      limit: limit || 10
    })
  },

  // 更新文档
  async updateDocument(id: number, data: Partial<Omit<KnowledgeDocument, 'id' | 'createdAt' | 'updatedAt' | 'createdBy'>>): Promise<KnowledgeDocument> {
    return api.put<KnowledgeDocument>(`/api/knowledge/documents/${id}`, data)
  },

  // 删除文档
  async deleteDocument(id: number): Promise<void> {
    return api.delete(`/api/knowledge/documents/${id}`)
  },

  // 批量删除文档
  async batchDeleteDocuments(ids: number[]): Promise<void> {
    return api.delete('/api/knowledge/documents/batch', { ids })
  },

  // 获取文档分类
  async getCategories(): Promise<string[]> {
    return api.get<string[]>('/api/knowledge/documents/categories')
  },

  // 获取热门关键词
  async getPopularKeywords(limit?: number): Promise<Array<{
    keyword: string
    count: number
  }>> {
    return api.get('/api/knowledge/documents/keywords/popular', {
      limit: limit || 20
    })
  }
}

// AI分析服务
export const aiAnalyticsService = {
  // 获取对话趋势数据
  async getChatTrends(params: {
    startDate: string
    endDate: string
    granularity?: 'hour' | 'day' | 'week' | 'month'
  }): Promise<Array<{
    timestamp: string
    chatCount: number
    userCount: number
    avgResponseTime: number
  }>> {
    return api.get('/api/ai/analytics/trends', params)
  },

  // 获取用户角色分布
  async getRoleDistribution(): Promise<Array<{
    role: string
    count: number
    percentage: number
  }>> {
    return api.get('/api/ai/analytics/role-distribution')
  },

  // 获取热门问题分析
  async getPopularQuestions(params?: {
    startDate?: string
    endDate?: string
    limit?: number
  }): Promise<Array<{
    category: string
    questions: string[]
    count: number
    percentage: number
  }>> {
    return api.get('/api/ai/analytics/popular-questions', params)
  },

  // 获取满意度统计
  async getSatisfactionStats(params?: {
    startDate?: string
    endDate?: string
  }): Promise<{
    averageRating: number
    totalRatings: number
    ratingDistribution: Array<{
      rating: number
      count: number
      percentage: number
    }>
  }> {
    return api.get('/api/ai/analytics/satisfaction', params)
  },

  // 获取响应时间分析
  async getResponseTimeAnalysis(params?: {
    startDate?: string
    endDate?: string
  }): Promise<{
    averageTime: number
    medianTime: number
    p95Time: number
    p99Time: number
    distribution: Array<{
      range: string
      count: number
      percentage: number
    }>
  }> {
    return api.get('/api/ai/analytics/response-time', params)
  },

  // 导出分析报告
  async exportAnalyticsReport(params: {
    startDate: string
    endDate: string
    format: 'PDF' | 'EXCEL' | 'CSV'
    includeCharts?: boolean
  }): Promise<Blob> {
    return api.get('/api/ai/analytics/export', params, {
      responseType: 'blob'
    })
  }
} 