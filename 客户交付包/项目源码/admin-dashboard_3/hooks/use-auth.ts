import { useState, useEffect, useCallback } from 'react'
import { authService } from '@/lib/services'
import type { User } from '@/types/api'

interface AuthState {
  isAuthenticated: boolean
  user: User | null
  loading: boolean
  error: string | null
}

export const useAuth = () => {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: false,
    user: null,
    loading: true,
    error: null,
  })

  // 检查认证状态
  const checkAuth = useCallback(() => {
    const token = authService.getToken()
    const user = authService.getCurrentUser()
    
    setState({
      isAuthenticated: !!token && !!user,
      user,
      loading: false,
      error: null,
    })
  }, [])

  // 登录
  const login = useCallback(async (usernameOrEmail: string, password: string) => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    
    try {
      const response = await authService.login({ usernameOrEmail, password })
      
      if (response && response.token) {
        setState({
          isAuthenticated: true,
          user: response.user,
          loading: false,
          error: null,
        })
        return { success: true }
      } else {
        setState(prev => ({
          ...prev,
          loading: false,
          error: '登录失败，请检查用户名和密码',
        }))
        return { success: false, error: '登录失败，请检查用户名和密码' }
      }
    } catch (error: any) {
      const errorMessage = error.message || '登录失败，请稍后重试'
      setState(prev => ({
        ...prev,
        loading: false,
        error: errorMessage,
      }))
      return { success: false, error: errorMessage }
    }
  }, [])

  // 登出
  const logout = useCallback(async () => {
    setState(prev => ({ ...prev, loading: true }))
    
    try {
      await authService.logout()
    } catch (error) {
      // 即使登出请求失败，也要清除本地状态
      console.error('Logout error:', error)
    } finally {
      setState({
        isAuthenticated: false,
        user: null,
        loading: false,
        error: null,
      })
    }
  }, [])

  // 刷新用户信息
  const refreshUser = useCallback(() => {
    const user = authService.getCurrentUser()
    setState(prev => ({ ...prev, user }))
  }, [])

  // 清除错误
  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }))
  }, [])

  // 初始化时检查认证状态
  useEffect(() => {
    checkAuth()
  }, [checkAuth])

  return {
    ...state,
    login,
    logout,
    refreshUser,
    clearError,
    checkAuth,
  }
} 