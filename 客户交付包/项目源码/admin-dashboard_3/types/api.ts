// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  phone?: string
  realName?: string
  avatarUrl?: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  emailVerified: boolean
  phoneVerified: boolean
  createdAt: string
  updatedAt: string
  roles: Role[]
}

export interface Role {
  id: number
  name: string
  description?: string
  permissions: Permission[]
}

export interface Permission {
  id: number
  name: string
  description?: string
}

export interface CreateUserRequest {
  username: string
  email: string
  phone?: string
  password: string
  realName?: string
  avatarUrl?: string
  roleIds: number[]
}

export interface UpdateUserRequest {
  email?: string
  phone?: string
  realName?: string
  avatarUrl?: string
  status?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  emailVerified?: boolean
  phoneVerified?: boolean
  roleIds?: number[]
}

// 认证相关类型
export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface AuthResponse {
  success: boolean
  token: string
  refreshToken: string
  user: User
  expiresIn: number
}

export interface RefreshTokenRequest {
  refreshToken: string
}

// 问卷相关类型
export interface Questionnaire {
  id: number
  title: string
  description?: string
  type: string
  version?: string
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
  startTime?: string
  endTime?: string
  createdBy: number
  createdAt: string
  updatedAt: string
  settings: QuestionnaireSettings
  questions: Question[]
  responseCount?: number
}

export interface QuestionnaireSettings {
  isAnonymous: boolean
  allowMultipleSubmissions: boolean
  themeColor?: string
  layout?: string
  requirePassword: boolean
  accessPassword?: string
}

export interface Question {
  id: number
  title: string
  description?: string
  type: 'TEXT' | 'TEXTAREA' | 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'DROPDOWN' | 'RATING' | 'SCALE' | 'DATE' | 'NUMBER' | 'EMAIL' | 'PHONE' | 'FILE_UPLOAD'
  required: boolean
  sortOrder: number
  placeholder?: string
  ratingStyle?: string
  fileTypes?: string[]
  validationRules?: Record<string, any>
  options: QuestionOption[]
}

export interface QuestionOption {
  id: number
  text: string
  image?: string
  value?: string
  sortOrder: number
}

export interface CreateQuestionnaireRequest {
  request: {
    title: string
    description?: string
    type: string
    version?: string
    startTime?: string
    endTime?: string
    settings: QuestionnaireSettings
    questions: Omit<Question, 'id'>[]
  }
  currentUser: any
}

// AI 聊天相关类型
export interface ChatSession {
  id: string
  userId: number
  userName: string
  userRole: string
  startTime: string
  lastActivity: string
  messageCount: number
  status: 'active' | 'completed'
  topic: string
  satisfaction?: number
}

export interface ChatMessage {
  id: string
  sessionId: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
  metadata?: Record<string, any>
}

export interface ChatRequest {
  message: string
  sessionId?: string
  userId?: number
}

export interface ChatResponse {
  response: string
  sessionId: string
  messageId: string
}

// 知识文档相关类型
export interface KnowledgeDocument {
  id: number
  title: string
  content: string
  category: string
  keywords: string[]
  isPublic: boolean
  createdAt: string
  updatedAt: string
  createdBy: number
}

export interface UploadDocumentRequest {
  title?: string
  category?: string
  keywords?: string
  isPublic?: boolean
}

export interface DocumentSearchResult {
  id: number
  title: string
  content: string
  score: number
  highlightedContent?: string
}

// 内容管理相关类型
export interface FileInfo {
  id: number
  fileName: string
  originalName: string
  fileSize: number
  mimeType: string
  category?: string
  description?: string
  folder?: string
  isPublic: boolean
  tags: string[]
  uploadTime: string
  uploadedBy: number
  downloadCount: number
  status: string
  url?: string
}

export interface UploadFileRequest {
  category?: string
  description?: string
  folder?: string
  isPublic?: boolean
  tags?: string
}

// 系统监控相关类型
export interface SystemHealth {
  status: 'UP' | 'DOWN' | 'DEGRADED'
  components: Record<string, ComponentHealth>
}

export interface ComponentHealth {
  status: 'UP' | 'DOWN' | 'DEGRADED'
  details?: Record<string, any>
}

export interface SystemMetrics {
  timestamp: string
  cpu: {
    usage: number
    cores: number
  }
  memory: {
    used: number
    total: number
    usage: number
  }
  disk: {
    used: number
    total: number
    usage: number
  }
  jvm: {
    heapUsed: number
    heapMax: number
    heapUsage: number
  }
}

export interface PerformanceStats {
  totalRequests: number
  averageResponseTime: number
  errorRate: number
  throughput: number
  slowQueries: SlowQuery[]
}

export interface SlowQuery {
  query: string
  executionTime: number
  timestamp: string
}

// MFA 相关类型
export interface MfaConfig {
  totpEnabled: boolean
  smsEnabled: boolean
  emailEnabled: boolean
  recoveryCodesCount: number
}

export interface TotpSetupResponse {
  secretKey: string
  qrCodeUrl: string
  backupCodes: string[]
}

export interface MfaVerifyRequest {
  code: string
  type: 'TOTP' | 'SMS' | 'EMAIL' | 'RECOVERY'
}

// 分页和搜索参数
export interface PaginationParams {
  page: number
  size: number
  sort?: string[]
}

export interface SearchParams extends PaginationParams {
  keyword?: string
  status?: string
  category?: string
  startDate?: string
  endDate?: string
}

// 统计数据类型
export interface QuestionnaireStats {
  totalResponses: number
  completionRate: number
  averageCompletionTime: number
  questionStats: QuestionStats[]
}

export interface QuestionStats {
  questionId: number
  questionTitle: string
  responseCount: number
  responseRate: number
  optionStats?: OptionStats[]
  textResponses?: string[]
}

export interface OptionStats {
  optionId: number
  optionText: string
  count: number
  percentage: number
}

// 报告相关类型
export interface ReportTemplate {
  id: string
  name: string
  description: string
  type: 'SUMMARY' | 'DETAILED' | 'COMPARISON'
}

export interface ReportRequest {
  templateId: string
  format: 'HTML' | 'PDF' | 'EXCEL' | 'CSV'
  questionnaireIds?: number[]
}

// 会话管理相关类型
export interface UserSession {
  id: string
  userId: number
  deviceInfo: string
  ipAddress: string
  userAgent: string
  loginTime: string
  lastActivity: string
  isActive: boolean
  location?: string
}

export interface SessionStats {
  totalSessions: number
  activeSessions: number
  averageSessionDuration: number
  loginAttempts: number
  successfulLogins: number
  failedLogins: number
} 