'use client'

import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuth } from '@/hooks/use-auth'

interface AuthGuardProps {
  children: React.ReactNode
}

export function AuthGuard({ children }: AuthGuardProps) {
  const { isAuthenticated, loading } = useAuth()
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    // 如果正在加载，不执行任何操作
    if (loading) return

    // 如果未认证且不在登录页面，重定向到登录页
    if (!isAuthenticated && pathname !== '/login') {
      router.push('/login')
      return
    }

    // 如果已认证且在登录页面，重定向到仪表板
    if (isAuthenticated && pathname === '/login') {
      router.push('/dashboard')
      return
    }
  }, [isAuthenticated, loading, pathname, router])

  // 如果正在加载，显示加载状态
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
          <p className="mt-2 text-muted-foreground">加载中...</p>
        </div>
      </div>
    )
  }

  // 如果未认证且不在登录页面，不渲染子组件
  if (!isAuthenticated && pathname !== '/login') {
    return null
  }

  // 如果已认证且在登录页面，不渲染子组件（会重定向）
  if (isAuthenticated && pathname === '/login') {
    return null
  }

  return <>{children}</>
} 