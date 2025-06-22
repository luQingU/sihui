// API 基础配置
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

// API Response 类型定义
export interface ApiResponse<T = any> {
  success: boolean
  data: T
  message: string
  code: string
}

// 分页参数
export interface PageParams {
  page: number
  size: number
  sort?: string[]
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 请求配置
interface RequestConfig extends RequestInit {
  timeout?: number
}

// 通用 API 客户端
class ApiClient {
  private baseURL: string
  private defaultHeaders: HeadersInit

  constructor(baseURL: string) {
    this.baseURL = baseURL
    this.defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    }
  }

  // 获取认证 token
  private getAuthToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('token')
    }
    return null
  }

  // 构建请求头
  private buildHeaders(customHeaders?: HeadersInit): HeadersInit {
    const headers = { ...this.defaultHeaders, ...customHeaders }
    const token = this.getAuthToken()
    
    if (token) {
      ;(headers as any)['Authorization'] = `Bearer ${token}`
    }
    
    return headers
  }

  // 处理响应
  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`)
    }
    
    const data = await response.json()
    return data
  }

  // 通用请求方法
  async request<T = any>(
    endpoint: string,
    config: RequestConfig = {}
  ): Promise<T> {
    const { timeout = 10000, headers, ...restConfig } = config
    const url = `${this.baseURL}${endpoint}`
    
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), timeout)
    
    try {
      const response = await fetch(url, {
        headers: this.buildHeaders(headers),
        signal: controller.signal,
        ...restConfig,
      })
      
      clearTimeout(timeoutId)
      return await this.handleResponse<T>(response)
    } catch (error) {
      clearTimeout(timeoutId)
      throw error
    }
  }

  // GET 请求
  async get<T = any>(endpoint: string, params?: Record<string, any>): Promise<T> {
    let url = endpoint
    if (params) {
      const searchParams = new URLSearchParams()
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          if (Array.isArray(value)) {
            value.forEach(v => searchParams.append(key, String(v)))
          } else {
            searchParams.append(key, String(value))
          }
        }
      })
      const queryString = searchParams.toString()
      if (queryString) {
        url += `?${queryString}`
      }
    }
    
    return this.request<T>(url, { method: 'GET' })
  }

  // POST 请求
  async post<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  // PUT 请求
  async put<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  // PATCH 请求
  async patch<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  // DELETE 请求
  async delete<T = any>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' })
  }

  // 文件上传
  async upload<T = any>(endpoint: string, formData: FormData): Promise<T> {
    const headers = this.buildHeaders()
    delete (headers as any)['Content-Type'] // 让浏览器自动设置 multipart/form-data
    
    return this.request<T>(endpoint, {
      method: 'POST',
      headers,
      body: formData,
    })
  }
}

// 创建 API 客户端实例
export const apiClient = new ApiClient(API_BASE_URL)

// 导出便捷方法
export const api = {
  get: <T = any>(endpoint: string, params?: Record<string, any>) => 
    apiClient.get<T>(endpoint, params),
  post: <T = any>(endpoint: string, data?: any) => 
    apiClient.post<T>(endpoint, data),
  put: <T = any>(endpoint: string, data?: any) => 
    apiClient.put<T>(endpoint, data),
  patch: <T = any>(endpoint: string, data?: any) => 
    apiClient.patch<T>(endpoint, data),
  delete: <T = any>(endpoint: string) => 
    apiClient.delete<T>(endpoint),
  upload: <T = any>(endpoint: string, formData: FormData) => 
    apiClient.upload<T>(endpoint, formData),
} 