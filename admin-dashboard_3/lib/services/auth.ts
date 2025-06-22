import { api } from '../api'
import type { 
  LoginRequest, 
  AuthResponse, 
  RefreshTokenRequest,
  User,
  MfaConfig,
  TotpSetupResponse,
  MfaVerifyRequest,
  UserSession,
  SessionStats
} from '../../types/api'

// 认证服务
export const authService = {
  // 用户登录
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/login', data)
    
    // 登录成功后保存token
    if (response.token) {
      localStorage.setItem('token', response.token)
      localStorage.setItem('refreshToken', response.refreshToken)
      localStorage.setItem('user', JSON.stringify(response.user))
    }
    
    return response
  },

  // 刷新Token
  async refreshToken(data: RefreshTokenRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/refresh', data)
    
    if (response.token) {
      localStorage.setItem('token', response.token)
      localStorage.setItem('refreshToken', response.refreshToken)
    }
    
    return response
  },

  // 用户登出
  async logout(): Promise<void> {
    try {
      await api.post('/api/auth/logout')
    } finally {
      // 无论是否成功，都清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  },

  // 获取当前用户信息
  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  // 检查是否已登录
  isAuthenticated(): boolean {
    const token = localStorage.getItem('token')
    return !!token
  },

  // 获取Token
  getToken(): string | null {
    return localStorage.getItem('token')
  },

  // 获取刷新Token
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken')
  }
}

// 增强认证服务
export const enhancedAuthService = {
  // 获取用户活跃会话
  async getSessions(): Promise<UserSession[]> {
    return api.get<UserSession[]>('/api/auth/enhanced/sessions')
  },

  // 获取会话统计信息
  async getSessionStats(): Promise<SessionStats> {
    return api.get<SessionStats>('/api/auth/enhanced/sessions/stats')
  },

  // 注销指定会话
  async terminateSession(sessionId: string): Promise<void> {
    return api.delete(`/api/auth/enhanced/sessions/${sessionId}`)
  },

  // 注销所有会话
  async terminateAllSessions(): Promise<void> {
    return api.delete('/api/auth/enhanced/sessions/all')
  },

  // 管理员强制注销用户会话
  async adminTerminateUserSessions(userId: number): Promise<void> {
    return api.delete(`/api/auth/enhanced/admin/users/${userId}/sessions`)
  }
}

// MFA（多因素认证）服务
export const mfaService = {
  // 获取MFA配置
  async getConfig(): Promise<MfaConfig> {
    return api.get<MfaConfig>('/api/auth/mfa/config')
  },

  // 更新MFA配置
  async updateConfig(config: Partial<MfaConfig>): Promise<MfaConfig> {
    return api.put<MfaConfig>('/api/auth/mfa/config', config)
  },

  // 生成TOTP密钥
  async generateTotp(): Promise<TotpSetupResponse> {
    return api.post<TotpSetupResponse>('/api/auth/mfa/totp/generate')
  },

  // 启用TOTP
  async enableTotp(data: { code: string }): Promise<void> {
    return api.post('/api/auth/mfa/totp/enable', data)
  },

  // 禁用TOTP
  async disableTotp(data: { code: string }): Promise<void> {
    return api.post('/api/auth/mfa/totp/disable', data)
  },

  // 发送短信验证码
  async sendSmsCode(data: { phone: string }): Promise<void> {
    return api.post('/api/auth/mfa/sms/send', data)
  },

  // 发送邮箱验证码
  async sendEmailCode(data: { email: string }): Promise<void> {
    return api.post('/api/auth/mfa/email/send', data)
  },

  // 验证MFA代码
  async verifyCode(data: MfaVerifyRequest): Promise<void> {
    return api.post('/api/auth/mfa/verify', data)
  },

  // 生成恢复代码
  async generateRecoveryCodes(): Promise<{ codes: string[] }> {
    return api.post<{ codes: string[] }>('/api/auth/mfa/recovery/generate')
  }
} 