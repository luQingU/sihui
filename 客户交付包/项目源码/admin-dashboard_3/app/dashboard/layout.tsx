"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter, usePathname } from "next/navigation"
import Link from "next/link"
import {
  Users,
  FileText,
  FileQuestion,
  LogOut,
  Menu,
  Home,
  Settings,
  ChevronDown,
  ChevronRight,
  Shield,
  Activity,
  Brain,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Badge } from "@/components/ui/badge"
import { useMobile } from "@/hooks/use-mobile"

interface NavItemProps {
  href: string
  icon: React.ReactNode
  title: string
  isActive: boolean
  badge?: string
  children?: { href: string; title: string; badge?: string }[]
}

function NavItem({ href, icon, title, isActive, badge, children }: NavItemProps) {
  const [open, setOpen] = useState(false)
  const hasChildren = children && children.length > 0
  const pathname = usePathname()

  useEffect(() => {
    if (hasChildren) {
      const isChildActive = children?.some((child) => pathname === child.href)
      setOpen(isChildActive || isActive)
    }
  }, [pathname, hasChildren, children, isActive])

  if (hasChildren) {
    return (
      <Collapsible open={open} onOpenChange={setOpen} className="w-full">
        <CollapsibleTrigger asChild>
          <Button variant={isActive ? "secondary" : "ghost"} className="w-full justify-between">
            <span className="flex items-center">
              {icon}
              <span className="ml-2">{title}</span>
              {badge && (
                <Badge variant="secondary" className="ml-2 text-xs">
                  {badge}
                </Badge>
              )}
            </span>
            {open ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          </Button>
        </CollapsibleTrigger>
        <CollapsibleContent className="pl-8 pt-2">
          {children?.map((child, index) => (
            <Link key={index} href={child.href}>
              <Button variant={pathname === child.href ? "secondary" : "ghost"} className="w-full justify-start mb-1">
                <span className="flex items-center w-full justify-between">
                  <span>{child.title}</span>
                  {child.badge && (
                    <Badge variant="outline" className="text-xs">
                      {child.badge}
                    </Badge>
                  )}
                </span>
              </Button>
            </Link>
          ))}
        </CollapsibleContent>
      </Collapsible>
    )
  }

  return (
    <Link href={href}>
      <Button variant={isActive ? "secondary" : "ghost"} className="w-full justify-start">
        <span className="flex items-center w-full justify-between">
          <span className="flex items-center">
            {icon}
            <span className="ml-2">{title}</span>
          </span>
          {badge && (
            <Badge variant="secondary" className="text-xs">
              {badge}
            </Badge>
          )}
        </span>
      </Button>
    </Link>
  )
}

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const router = useRouter()
  const pathname = usePathname()
  const isMobile = useMobile()
  const [open, setOpen] = useState(false)
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
    // 检查用户是否已登录
    const isLoggedIn = localStorage.getItem("isLoggedIn") === "true"
    if (!isLoggedIn) {
      router.push("/login")
    }
  }, [router])

  const handleLogout = () => {
    localStorage.removeItem("isLoggedIn")
    router.push("/login")
  }

  const navItems = [
    {
      href: "/dashboard",
      icon: <Home className="h-5 w-5" />,
      title: "系统概览",
      isActive: pathname === "/dashboard",
    },
    {
      href: "/dashboard/users",
      icon: <Users className="h-5 w-5" />,
      title: "用户管理",
      isActive: pathname.startsWith("/dashboard/users"),
      badge: "1,248",
      children: [
        { href: "/dashboard/users", title: "用户列表", badge: "1,248" },
        { href: "/dashboard/users/roles", title: "角色权限", badge: "50+" },
        { href: "/dashboard/users/sessions", title: "会话管理" },
      ],
    },
    {
      href: "/dashboard/content",
      icon: <FileText className="h-5 w-5" />,
      title: "内容管理",
      isActive: pathname.startsWith("/dashboard/content"),
      badge: "324",
      children: [
        { href: "/dashboard/content/videos", title: "视频管理", badge: "156" },
        { href: "/dashboard/content/documents", title: "文档管理", badge: "168" },
        { href: "/dashboard/content/upload", title: "批量上传" },
      ],
    },
    {
      href: "/dashboard/ai",
      icon: <Brain className="h-5 w-5" />,
      title: "AI智能问答",
      isActive: pathname.startsWith("/dashboard/ai"),
      badge: "8.9K",
      children: [
        { href: "/dashboard/ai/chat", title: "对话管理", badge: "8.9K" },
        { href: "/dashboard/ai/knowledge", title: "知识库", badge: "1,456" },
        { href: "/dashboard/ai/analytics", title: "AI分析" },
      ],
    },
    {
      href: "/dashboard/questionnaires",
      icon: <FileQuestion className="h-5 w-5" />,
      title: "问卷系统",
      isActive: pathname.startsWith("/dashboard/questionnaires"),
      badge: "56",
      children: [
        { href: "/dashboard/questionnaires", title: "问卷列表", badge: "56" },
        { href: "/dashboard/questionnaires/create", title: "创建问卷" },
        { href: "/dashboard/questionnaires/designer", title: "可视化设计器" },
        { href: "/dashboard/questionnaires/analytics", title: "数据分析" },
        { href: "/dashboard/questionnaires/reports", title: "报告生成" },
      ],
    },
    {
      href: "/dashboard/security",
      icon: <Shield className="h-5 w-5" />,
      title: "安全中心",
      isActive: pathname.startsWith("/dashboard/security"),
      children: [
        { href: "/dashboard/security/auth", title: "认证管理" },
        { href: "/dashboard/security/mfa", title: "多因素认证" },
        { href: "/dashboard/security/monitoring", title: "安全监控" },
        { href: "/dashboard/security/encryption", title: "数据加密" },
      ],
    },
    {
      href: "/dashboard/monitoring",
      icon: <Activity className="h-5 w-5" />,
      title: "系统监控",
      isActive: pathname.startsWith("/dashboard/monitoring"),
      children: [
        { href: "/dashboard/monitoring/performance", title: "性能监控" },
        { href: "/dashboard/monitoring/health", title: "健康检查" },
        { href: "/dashboard/monitoring/logs", title: "日志分析" },
        { href: "/dashboard/monitoring/alerts", title: "告警管理" },
      ],
    },
    {
      href: "/dashboard/settings",
      icon: <Settings className="h-5 w-5" />,
      title: "系统设置",
      isActive: pathname.startsWith("/dashboard/settings"),
    },
  ]

  if (!mounted) {
    return null
  }

  const renderSidebar = () => (
    <div className="flex flex-col h-full">
      <div className="p-6 border-b">
        <h1 className="text-xl font-bold">四会培训管理平台</h1>
        <p className="text-sm text-muted-foreground">Spring Boot 3.x + Vue 3</p>
      </div>
      <ScrollArea className="flex-1 py-4">
        <div className="space-y-2 px-4">
          {navItems.map((item, index) => (
            <NavItem key={index} {...item} />
          ))}
        </div>
      </ScrollArea>
      <div className="p-4 mt-auto border-t">
        <div className="mb-4 p-3 bg-muted/50 rounded-lg">
          <div className="text-xs text-muted-foreground mb-1">系统状态</div>
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">运行正常</span>
            <Badge className="bg-green-500">99.8%</Badge>
          </div>
        </div>
        <Button variant="outline" className="w-full justify-start" onClick={handleLogout}>
          <LogOut className="h-5 w-5 mr-2" />
          退出登录
        </Button>
      </div>
    </div>
  )

  return (
    <div className="flex h-screen bg-gray-50">
      {isMobile ? (
        <>
          <Sheet open={open} onOpenChange={setOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="fixed top-4 left-4 z-50">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="p-0 w-64">
              {renderSidebar()}
            </SheetContent>
          </Sheet>
          <div className="flex-1 overflow-auto pt-16">
            <main className="container mx-auto p-4">{children}</main>
          </div>
        </>
      ) : (
        <>
          <div className="w-64 border-r bg-white h-full">{renderSidebar()}</div>
          <div className="flex-1 overflow-auto">
            <main className="container mx-auto p-6">{children}</main>
          </div>
        </>
      )}
    </div>
  )
}
