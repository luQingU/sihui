"use client"

import { Label } from "@/components/ui/label"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Search, Monitor, Smartphone, Tablet, MapPin, Clock, Shield, AlertTriangle } from "lucide-react"

// 模拟会话数据
const mockSessions = [
  {
    id: "sess_001",
    userId: "user_123",
    userName: "张三",
    userRole: "零售商户",
    deviceType: "desktop",
    browser: "Chrome 120.0",
    os: "Windows 11",
    ip: "192.168.1.100",
    location: "广东省四会市",
    loginTime: "2025-01-19 09:30:25",
    lastActivity: "2025-01-19 14:45:12",
    status: "active",
    duration: "5小时15分钟",
  },
  {
    id: "sess_002",
    userId: "user_456",
    userName: "李四",
    userRole: "客户经理",
    deviceType: "mobile",
    browser: "Safari 17.0",
    os: "iOS 17.2",
    ip: "192.168.1.101",
    location: "广东省四会市",
    loginTime: "2025-01-19 08:15:30",
    lastActivity: "2025-01-19 14:30:45",
    status: "active",
    duration: "6小时15分钟",
  },
  {
    id: "sess_003",
    userId: "user_789",
    userName: "王五",
    userRole: "管理员",
    deviceType: "tablet",
    browser: "Edge 120.0",
    os: "Android 14",
    ip: "192.168.1.102",
    location: "广东省四会市",
    loginTime: "2025-01-19 07:45:10",
    lastActivity: "2025-01-19 13:02:33",
    status: "expired",
    duration: "5小时17分钟",
  },
]

export default function SessionsPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedSession, setSelectedSession] = useState<any>(null)
  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState(false)

  const filteredSessions = mockSessions.filter(
    (session) =>
      session.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      session.ip.includes(searchTerm) ||
      session.location.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const getDeviceIcon = (deviceType: string) => {
    switch (deviceType) {
      case "desktop":
        return <Monitor className="h-4 w-4" />
      case "mobile":
        return <Smartphone className="h-4 w-4" />
      case "tablet":
        return <Tablet className="h-4 w-4" />
      default:
        return <Monitor className="h-4 w-4" />
    }
  }

  const openDetailDialog = (session: any) => {
    setSelectedSession(session)
    setIsDetailDialogOpen(true)
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">会话管理</h1>
          <p className="text-muted-foreground">监控和管理用户登录会话</p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline">
            <Shield className="h-4 w-4 mr-2" />
            安全检查
          </Button>
          <Button variant="destructive">
            <AlertTriangle className="h-4 w-4 mr-2" />
            强制下线全部
          </Button>
        </div>
      </div>

      {/* 会话统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">活跃会话</CardTitle>
            <Shield className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {mockSessions.filter((s) => s.status === "active").length}
            </div>
            <p className="text-xs text-muted-foreground">当前在线用户</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">桌面端</CardTitle>
            <Monitor className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {mockSessions.filter((s) => s.deviceType === "desktop").length}
            </div>
            <p className="text-xs text-muted-foreground">PC端登录</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">移动端</CardTitle>
            <Smartphone className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">
              {mockSessions.filter((s) => s.deviceType === "mobile").length}
            </div>
            <p className="text-xs text-muted-foreground">手机端登录</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">平均时长</CardTitle>
            <Clock className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">5.5h</div>
            <p className="text-xs text-muted-foreground">平均会话时长</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="active" className="space-y-4">
        <TabsList>
          <TabsTrigger value="active">活跃会话</TabsTrigger>
          <TabsTrigger value="all">全部会话</TabsTrigger>
          <TabsTrigger value="security">安全监控</TabsTrigger>
        </TabsList>

        <TabsContent value="active" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>活跃会话列表</CardTitle>
              <CardDescription>当前在线的用户会话</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2 mb-4">
                <div className="relative flex-1 max-w-sm">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="搜索会话..."
                    className="pl-8"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
              </div>

              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>用户</TableHead>
                      <TableHead>设备</TableHead>
                      <TableHead>位置</TableHead>
                      <TableHead>登录时间</TableHead>
                      <TableHead>最后活动</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredSessions
                      .filter((session) => session.status === "active")
                      .map((session) => (
                        <TableRow key={session.id}>
                          <TableCell>
                            <div>
                              <div className="font-medium">{session.userName}</div>
                              <div className="text-sm text-muted-foreground">{session.userRole}</div>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center space-x-2">
                              {getDeviceIcon(session.deviceType)}
                              <div>
                                <div className="text-sm">{session.browser}</div>
                                <div className="text-xs text-muted-foreground">{session.os}</div>
                              </div>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center space-x-1">
                              <MapPin className="h-3 w-3 text-muted-foreground" />
                              <span className="text-sm">{session.location}</span>
                            </div>
                            <div className="text-xs text-muted-foreground">{session.ip}</div>
                          </TableCell>
                          <TableCell className="text-sm">{session.loginTime}</TableCell>
                          <TableCell className="text-sm">{session.lastActivity}</TableCell>
                          <TableCell>
                            <Badge variant="default">在线</Badge>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button variant="ghost" size="sm" onClick={() => openDetailDialog(session)}>
                              详情
                            </Button>
                            <Button variant="ghost" size="sm" className="text-red-600">
                              下线
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="all" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>全部会话记录</CardTitle>
              <CardDescription>包括活跃和已过期的会话</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>会话ID</TableHead>
                      <TableHead>用户</TableHead>
                      <TableHead>设备</TableHead>
                      <TableHead>持续时间</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mockSessions.map((session) => (
                      <TableRow key={session.id}>
                        <TableCell className="font-mono text-sm">{session.id}</TableCell>
                        <TableCell>{session.userName}</TableCell>
                        <TableCell>
                          <div className="flex items-center space-x-2">
                            {getDeviceIcon(session.deviceType)}
                            <span className="text-sm">{session.deviceType}</span>
                          </div>
                        </TableCell>
                        <TableCell>{session.duration}</TableCell>
                        <TableCell>
                          <Badge variant={session.status === "active" ? "default" : "secondary"}>
                            {session.status === "active" ? "活跃" : "已过期"}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button variant="ghost" size="sm" onClick={() => openDetailDialog(session)}>
                            查看
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>安全事件</CardTitle>
                <CardDescription>异常登录和安全威胁</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">异常IP登录</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">多地登录</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">暴力破解尝试</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">会话劫持检测</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>地理分布</CardTitle>
                <CardDescription>用户登录地理位置</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">广东省四会市</span>
                    <Badge>100%</Badge>
                  </div>
                  <div className="text-sm text-muted-foreground">所有登录均来自本地网络</div>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>

      {/* 会话详情对话框 */}
      <Dialog open={isDetailDialogOpen} onOpenChange={setIsDetailDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>会话详情</DialogTitle>
            <DialogDescription>查看会话的详细信息</DialogDescription>
          </DialogHeader>
          {selectedSession && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-medium">会话ID</Label>
                  <p className="text-sm font-mono">{selectedSession.id}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">用户</Label>
                  <p className="text-sm">{selectedSession.userName}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">角色</Label>
                  <p className="text-sm">{selectedSession.userRole}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">设备类型</Label>
                  <p className="text-sm">{selectedSession.deviceType}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">浏览器</Label>
                  <p className="text-sm">{selectedSession.browser}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">操作系统</Label>
                  <p className="text-sm">{selectedSession.os}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">IP地址</Label>
                  <p className="text-sm">{selectedSession.ip}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">位置</Label>
                  <p className="text-sm">{selectedSession.location}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">登录时间</Label>
                  <p className="text-sm">{selectedSession.loginTime}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">最后活动</Label>
                  <p className="text-sm">{selectedSession.lastActivity}</p>
                </div>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDetailDialogOpen(false)}>
              关闭
            </Button>
            <Button variant="destructive">强制下线</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
