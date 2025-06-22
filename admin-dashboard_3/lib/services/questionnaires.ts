import { api } from '../api'
import type { 
  Questionnaire,
  CreateQuestionnaireRequest,
  PaginationParams,
  PageResponse,
  QuestionnaireStats,
  ReportTemplate,
  ReportRequest
} from '../../types/api'

// 问卷管理服务
export const questionnaireService = {
  // 创建问卷
  async createQuestionnaire(data: CreateQuestionnaireRequest): Promise<Questionnaire> {
    return api.post<Questionnaire>('/api/questionnaires', data)
  },

  // 获取问卷详情
  async getQuestionnaire(id: number): Promise<Questionnaire> {
    return api.get<Questionnaire>(`/api/questionnaires/${id}`)
  },

  // 获取问卷列表
  async getQuestionnaires(params?: PaginationParams & {
    status?: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
    type?: string
    keyword?: string
  }): Promise<PageResponse<Questionnaire>> {
    return api.get<PageResponse<Questionnaire>>('/api/questionnaires', params)
  },

  // 获取当前用户的问卷列表
  async getMyQuestionnaires(params?: PaginationParams): Promise<PageResponse<Questionnaire>> {
    return api.get<PageResponse<Questionnaire>>('/api/questionnaires/my', params)
  },

  // 获取可用问卷列表（已发布且在有效期内）
  async getAvailableQuestionnaires(params?: PaginationParams): Promise<PageResponse<Questionnaire>> {
    return api.get<PageResponse<Questionnaire>>('/api/questionnaires/available', params)
  },

  // 更新问卷
  async updateQuestionnaire(id: number, data: Partial<CreateQuestionnaireRequest>): Promise<Questionnaire> {
    return api.put<Questionnaire>(`/api/questionnaires/${id}`, data)
  },

  // 发布问卷
  async publishQuestionnaire(id: number): Promise<void> {
    return api.post(`/api/questionnaires/${id}/publish`)
  },

  // 关闭问卷
  async closeQuestionnaire(id: number): Promise<void> {
    return api.post(`/api/questionnaires/${id}/close`)
  },

  // 验证问卷访问权限
  async validateAccess(id: number, password?: string): Promise<{ hasAccess: boolean; message?: string }> {
    return api.post<{ hasAccess: boolean; message?: string }>(`/api/questionnaires/${id}/validate-access`, {
      password
    })
  },

  // 删除问卷
  async deleteQuestionnaire(id: number): Promise<void> {
    return api.delete(`/api/questionnaires/${id}`)
  },

  // 批量删除问卷
  async batchDeleteQuestionnaires(ids: number[]): Promise<void> {
    return api.delete('/api/questionnaires/batch', { ids })
  },

  // 复制问卷
  async copyQuestionnaire(id: number, newTitle?: string): Promise<Questionnaire> {
    return api.post<Questionnaire>(`/api/questionnaires/${id}/copy`, {
      title: newTitle
    })
  },

  // 获取问卷统计信息
  async getQuestionnaireStats(id: number): Promise<QuestionnaireStats> {
    return api.get<QuestionnaireStats>(`/api/questionnaires/${id}/stats`)
  }
}

// 公开问卷服务（不需要认证）
export const publicQuestionnaireService = {
  // 获取公开问卷列表
  async getPublicQuestionnaires(params?: PaginationParams): Promise<PageResponse<Questionnaire>> {
    return api.get<PageResponse<Questionnaire>>('/api/public/questionnaires', params)
  },

  // 获取公开问卷详情
  async getPublicQuestionnaire(id: number): Promise<Questionnaire> {
    return api.get<Questionnaire>(`/api/public/questionnaires/${id}`)
  },

  // 提交问卷答案
  async submitQuestionnaire(id: number, answers: Record<string, any>): Promise<{
    success: boolean
    responseId: string
    message: string
  }> {
    return api.post(`/api/public/questionnaires/${id}/submit`, { answers })
  },

  // 获取问卷完成状态
  async getCompletionStatus(id: number, sessionId?: string): Promise<{
    isCompleted: boolean
    submissionTime?: string
    canResubmit: boolean
  }> {
    return api.get(`/api/public/questionnaires/${id}/completion-status`, {
      sessionId
    })
  }
}

// 问卷分析服务
export const questionnaireAnalysisService = {
  // 获取问卷详细统计分析
  async getDetailedStats(questionnaireId: number): Promise<QuestionnaireStats> {
    return api.get<QuestionnaireStats>(`/api/questionnaires/${questionnaireId}/analysis/stats`)
  },

  // 获取回答时间分布
  async getTimeDistribution(questionnaireId: number): Promise<Array<{
    timeRange: string
    count: number
    percentage: number
  }>> {
    return api.get(`/api/questionnaires/${questionnaireId}/analysis/time-distribution`)
  },

  // 获取地域分布统计
  async getGeographicDistribution(questionnaireId: number): Promise<Array<{
    region: string
    count: number
    percentage: number
  }>> {
    return api.get(`/api/questionnaires/${questionnaireId}/analysis/geographic-distribution`)
  },

  // 获取用户角色分布
  async getRoleDistribution(questionnaireId: number): Promise<Array<{
    role: string
    count: number
    percentage: number
  }>> {
    return api.get(`/api/questionnaires/${questionnaireId}/analysis/role-distribution`)
  },

  // 导出分析数据
  async exportAnalysis(questionnaireId: number, format: 'CSV' | 'EXCEL' | 'PDF'): Promise<Blob> {
    return api.get(`/api/questionnaires/${questionnaireId}/analysis/export/${format.toLowerCase()}`, {}, {
      responseType: 'blob'
    })
  },

  // 获取趋势分析
  async getTrendAnalysis(questionnaireId: number, params: {
    startDate: string
    endDate: string
    granularity: 'hour' | 'day' | 'week' | 'month'
  }): Promise<Array<{
    timestamp: string
    responseCount: number
    completionRate: number
  }>> {
    return api.get(`/api/questionnaires/${questionnaireId}/analysis/trends`, params)
  }
}

// 问卷报告服务
export const questionnaireReportService = {
  // 获取报告模板列表
  async getReportTemplates(questionnaireId: number): Promise<ReportTemplate[]> {
    return api.get<ReportTemplate[]>(`/api/questionnaires/${questionnaireId}/reports/templates`)
  },

  // 生成汇总报告
  async generateSummaryReport(questionnaireId: number): Promise<{
    reportId: string
    summary: any
    generatedAt: string
  }> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/summary`)
  },

  // 预览报告
  async previewReport(questionnaireId: number, template?: string): Promise<{
    content: string
    metadata: any
  }> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/preview`, {
      template: template || 'summary'
    })
  },

  // 生成HTML报告
  async generateHtmlReport(questionnaireId: number, template?: string): Promise<{
    url: string
    content: string
  }> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/html`, {
      template
    })
  },

  // 生成PDF报告
  async generatePdfReport(questionnaireId: number, template?: string): Promise<Blob> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/pdf`, {
      template
    }, {
      responseType: 'blob'
    })
  },

  // 生成Excel报告
  async generateExcelReport(questionnaireId: number, template?: string): Promise<Blob> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/excel`, {
      template
    }, {
      responseType: 'blob'
    })
  },

  // 生成对比报告
  async generateComparisonReport(questionnaireId: number, data: {
    questionnaireIds: number[]
    currentUser: any
  }): Promise<{
    reportId: string
    comparisonData: any
    generatedAt: string
  }> {
    return api.post(`/api/questionnaires/${questionnaireId}/reports/comparison`, data)
  },

  // 下载报告文件
  async downloadReport(questionnaireId: number, reportId: string, format: 'PDF' | 'EXCEL' | 'HTML'): Promise<Blob> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/${reportId}/download`, {
      format: format.toLowerCase()
    }, {
      responseType: 'blob'
    })
  },

  // 获取报告历史
  async getReportHistory(questionnaireId: number, params?: PaginationParams): Promise<PageResponse<{
    id: string
    type: string
    format: string
    generatedAt: string
    generatedBy: string
    fileSize: number
    downloadCount: number
  }>> {
    return api.get(`/api/questionnaires/${questionnaireId}/reports/history`, params)
  }
} 