// 错误类型定义
export interface ApiError {
  code: string
  message: string
  details?: any
  timestamp?: string
}

// 错误处理类
export class ErrorHandler {
  // 处理API错误
  static handleApiError(error: any): ApiError {
    if (error?.response?.data) {
      return {
        code: error.response.data.code || 'API_ERROR',
        message: error.response.data.message || '请求失败',
        details: error.response.data.details,
        timestamp: new Date().toISOString()
      }
    }

    if (error?.message) {
      return {
        code: 'NETWORK_ERROR',
        message: error.message,
        timestamp: new Date().toISOString()
      }
    }

    return {
      code: 'UNKNOWN_ERROR',
      message: '未知错误',
      timestamp: new Date().toISOString()
    }
  }

  // 显示错误消息
  static showError(error: ApiError | Error | string) {
    let message: string

    if (typeof error === 'string') {
      message = error
    } else if (error instanceof Error) {
      message = error.message
    } else {
      message = error.message
    }

    // 在实际项目中，这里应该使用 toast 库来显示错误
    console.error('错误:', message)
    
    // 如果有全局错误处理组件，可以在这里调用
    if (typeof window !== 'undefined') {
      // 可以使用 React Toast 库，如 react-hot-toast 或 sonner
      // toast.error(message)
    }
  }

  // 处理认证错误
  static handleAuthError(error: any) {
    const apiError = this.handleApiError(error)
    
    if (apiError.code === 'UNAUTHORIZED' || apiError.code === 'TOKEN_EXPIRED') {
      // 清除本地存储的认证信息
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        
        // 重定向到登录页
        window.location.href = '/login'
      }
    }
    
    this.showError(apiError)
  }

  // 处理权限错误
  static handlePermissionError(error: any) {
    const apiError = this.handleApiError(error)
    
    if (apiError.code === 'FORBIDDEN' || apiError.code === 'ACCESS_DENIED') {
      this.showError('您没有权限执行此操作')
      return
    }
    
    this.showError(apiError)
  }

  // 处理验证错误
  static handleValidationError(error: any) {
    const apiError = this.handleApiError(error)
    
    if (apiError.details && Array.isArray(apiError.details)) {
      // 如果是表单验证错误，显示具体字段错误
      const fieldErrors = apiError.details.map((detail: any) => detail.message).join(', ')
      this.showError(`输入验证失败: ${fieldErrors}`)
      return
    }
    
    this.showError(apiError)
  }
}

// 错误重试工具
export class RetryHandler {
  static async withRetry<T>(
    operation: () => Promise<T>,
    maxRetries: number = 3,
    delay: number = 1000
  ): Promise<T> {
    let lastError: any
    
    for (let i = 0; i <= maxRetries; i++) {
      try {
        return await operation()
      } catch (error) {
        lastError = error
        
        if (i === maxRetries) {
          throw error
        }
        
        // 指数退避延迟
        const retryDelay = delay * Math.pow(2, i)
        await new Promise(resolve => setTimeout(resolve, retryDelay))
      }
    }
    
    throw lastError
  }
}

// 加载状态管理
export class LoadingManager {
  private static loadingStates = new Map<string, boolean>()
  
  static setLoading(key: string, isLoading: boolean) {
    this.loadingStates.set(key, isLoading)
    
    // 如果有全局加载状态管理，可以在这里更新
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('loadingStateChange', {
        detail: { key, isLoading }
      }))
    }
  }
  
  static isLoading(key: string): boolean {
    return this.loadingStates.get(key) || false
  }
  
  static clearAll() {
    this.loadingStates.clear()
  }
}

// API调用包装器，统一处理错误和加载状态
export async function withErrorHandling<T>(
  operation: () => Promise<T>,
  options: {
    loadingKey?: string
    showError?: boolean
    retries?: number
    onError?: (error: any) => void
  } = {}
): Promise<T | null> {
  const { loadingKey, showError = true, retries = 0, onError } = options
  
  if (loadingKey) {
    LoadingManager.setLoading(loadingKey, true)
  }
  
  try {
    const result = retries > 0 
      ? await RetryHandler.withRetry(operation, retries)
      : await operation()
    
    return result
  } catch (error) {
    if (onError) {
      onError(error)
    } else if (showError) {
      ErrorHandler.handleApiError(error)
    }
    
    return null
  } finally {
    if (loadingKey) {
      LoadingManager.setLoading(loadingKey, false)
    }
  }
} 