import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { MessageSquare, Users, Clock, TrendingUp, Search, Filter, Download, RefreshCw } from "lucide-react"
import { aiChatService, aiAnalyticsService } from "@/lib/services"
import { withErrorHandling } from "@/lib/error-handler"
import type { ChatSession } from "@/types/api"

export default function AIChatPage() {
  const [sessions, setSessions] = useState<ChatSession[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [aiStats, setAiStats] = useState({
    todayChats: 0,
    activeSessions: 0,
    averageResponseTime: 0,
    userSatisfaction: 0,
    topQuestions: [],
    roleDistribution: []
  })
  const [aiConfig, setAiConfig] = useState({
    apiStatus: 'UP' as const,
    modelVersion: '',
    maxTokens: 0,
    temperature: 0,
    dailyUsage: 0,
    monthlyQuota: 0,
    usedQuota: 0,
    averageCost: 0
  })

  // 获取AI统计数据
  const fetchAiStats = async () => {
    const result = await withErrorHandling(
      () => aiChatService.getAiStats(),
      { loadingKey: 'fetchAiStats' }
    )
    
    if (result) {
      setAiStats(result)
    }
  }

  // 获取AI配置信息
  const fetchAiConfig = async () => {
    const result = await withErrorHandling(
      () => aiChatService.getAiConfig(),
      { loadingKey: 'fetchAiConfig' }
    )
    
    if (result) {
      setAiConfig(result)
    }
  }

  // 获取聊天会话列表
  const fetchSessions = async () => {
    const result = await withErrorHandling(
      () => aiChatService.getChatSessions({
        page: 0,
        size: 50,
      }),
      { loadingKey: 'fetchSessions' }
    )
    
    if (result) {
      setSessions(result.content)
    }
    setLoading(false)
  }

  // 刷新数据
  const handleRefresh = async () => {
    setLoading(true)
    await Promise.all([
      fetchAiStats(),
      fetchAiConfig(),
      fetchSessions()
    ])
  }

  // 初始化数据
  useEffect(() => {
    handleRefresh()
  }, [])

  // 过滤会话
  const filteredSessions = sessions.filter(session =>
    session.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    session.topic.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">AI智能问答管理</h1>
          <p className="text-muted-foreground">基于DeepSeek API的智能对话系统</p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" onClick={handleRefresh} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            刷新数据
          </Button>
          <Button variant="outline">
            <Download className="h-4 w-4 mr-2" />
            导出报告
          </Button>
        </div>
      </div>

      {/* AI服务状态卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">今日对话数</CardTitle>
            <MessageSquare className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? '-' : aiStats.todayChats.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              今日AI对话总数
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">活跃会话</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? '-' : aiStats.activeSessions}
            </div>
            <p className="text-xs text-muted-foreground">当前活跃对话</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">平均响应时间</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? '-' : `${aiStats.averageResponseTime.toFixed(1)}s`}
            </div>
            <p className="text-xs text-muted-foreground">
              系统响应速度
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">用户满意度</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? '-' : `${aiStats.userSatisfaction.toFixed(1)}/5`}
            </div>
            <p className="text-xs text-muted-foreground">基于用户评价</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="sessions" className="space-y-4">
        <TabsList>
          <TabsTrigger value="sessions">对话会话</TabsTrigger>
          <TabsTrigger value="analytics">数据分析</TabsTrigger>
          <TabsTrigger value="settings">AI配置</TabsTrigger>
        </TabsList>

        <TabsContent value="sessions" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>对话会话管理</CardTitle>
              <CardDescription>实时监控和管理用户AI对话会话</CardDescription>
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
                <Button variant="outline" size="sm">
                  <Filter className="h-4 w-4 mr-2" />
                  筛选
                </Button>
              </div>

              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>会话ID</TableHead>
                      <TableHead>用户</TableHead>
                      <TableHead>角色</TableHead>
                      <TableHead>主题</TableHead>
                      <TableHead>消息数</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>满意度</TableHead>
                      <TableHead>最后活动</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {loading ? (
                      <TableRow>
                        <TableCell colSpan={9} className="text-center py-4">
                          加载中...
                        </TableCell>
                      </TableRow>
                    ) : filteredSessions.length > 0 ? (
                      filteredSessions.map((session) => (
                        <TableRow key={session.id}>
                          <TableCell className="font-mono text-sm">{session.id}</TableCell>
                          <TableCell>{session.userName}</TableCell>
                          <TableCell>
                            <Badge variant="outline">{session.userRole}</Badge>
                          </TableCell>
                          <TableCell>{session.topic}</TableCell>
                          <TableCell>{session.messageCount}</TableCell>
                          <TableCell>
                            <Badge variant={session.status === "active" ? "default" : "secondary"}>
                              {session.status === "active" ? "进行中" : "已完成"}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center">
                              <span className="text-sm">
                                {session.satisfaction ? session.satisfaction.toFixed(1) : '-'}
                              </span>
                              {session.satisfaction && (
                                <span className="text-xs text-muted-foreground ml-1">/5</span>
                              )}
                            </div>
                          </TableCell>
                          <TableCell className="text-sm text-muted-foreground">
                            {new Date(session.lastActivity).toLocaleString()}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button variant="ghost" size="sm">
                              查看详情
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={9} className="text-center py-4">
                          没有找到匹配的会话
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>对话趋势分析</CardTitle>
                <CardDescription>过去30天的对话数量趋势</CardDescription>
              </CardHeader>
              <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
                <TrendingUp className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">对话趋势图表</span>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>用户角色分布</CardTitle>
                <CardDescription>不同用户角色的使用情况</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {loading ? (
                    <div className="text-center py-4 text-muted-foreground">加载中...</div>
                  ) : aiStats.roleDistribution.length > 0 ? (
                    aiStats.roleDistribution.map((roleData, index) => (
                      <div key={roleData.role} className="flex items-center justify-between">
                        <span className="text-sm">{roleData.role}</span>
                        <div className="flex items-center space-x-2">
                          <div className="w-20 h-2 bg-muted rounded-full overflow-hidden">
                            <div 
                              className={`h-full ${
                                index === 0 ? 'bg-blue-500' : 
                                index === 1 ? 'bg-green-500' : 
                                'bg-purple-500'
                              }`}
                              style={{ width: `${roleData.percentage}%` }}
                            ></div>
                          </div>
                          <span className="text-sm">{roleData.percentage}%</span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-4 text-muted-foreground">暂无数据</div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>热门问题分析</CardTitle>
              <CardDescription>用户最常咨询的问题类型</CardDescription>
            </CardHeader>
                          <CardContent>
                <div className="grid gap-4 md:grid-cols-3">
                  {loading ? (
                    <div className="col-span-3 text-center py-4 text-muted-foreground">加载中...</div>
                  ) : aiStats.topQuestions.length > 0 ? (
                    aiStats.topQuestions.slice(0, 5).map((item, index) => (
                      <div key={index} className="p-4 border rounded-lg">
                        <div className="text-lg font-semibold truncate" title={item.question}>
                          {item.question}
                        </div>
                        <div className="text-2xl font-bold text-blue-600">{item.count}</div>
                        <div className="text-sm text-muted-foreground">提问次数</div>
                      </div>
                    ))
                  ) : (
                    <div className="col-span-3 text-center py-4 text-muted-foreground">暂无数据</div>
                  )}
                </div>
              </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="settings" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>DeepSeek AI 配置</CardTitle>
              <CardDescription>AI模型参数和服务配置</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">API状态</span>
                    <Badge className={aiConfig.apiStatus === 'UP' ? "bg-green-500" : "bg-red-500"}>
                      {aiConfig.apiStatus === 'UP' ? '正常' : '异常'}
                    </Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">模型版本</span>
                    <span className="text-sm">{loading ? '-' : aiConfig.modelVersion || 'deepseek-chat'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">最大Token数</span>
                    <span className="text-sm">{loading ? '-' : aiConfig.maxTokens || 4096}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">温度参数</span>
                    <span className="text-sm">{loading ? '-' : aiConfig.temperature || 0.7}</span>
                  </div>
                </div>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">今日调用次数</span>
                    <span className="text-sm">{loading ? '-' : aiConfig.dailyUsage.toLocaleString()}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">月度配额</span>
                    <span className="text-sm">{loading ? '-' : aiConfig.monthlyQuota.toLocaleString()}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">已使用配额</span>
                    <span className="text-sm">
                      {loading ? '-' : `${aiConfig.usedQuota.toLocaleString()} (${((aiConfig.usedQuota / aiConfig.monthlyQuota) * 100).toFixed(1)}%)`}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">平均成本</span>
                    <span className="text-sm">{loading ? '-' : `¥${aiConfig.averageCost.toFixed(3)}/次`}</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>知识增强配置</CardTitle>
              <CardDescription>知识库检索和增强问答设置</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">TF-IDF检索</span>
                  <Badge className="bg-blue-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">相似度阈值</span>
                  <span className="text-sm">0.75</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">最大检索文档数</span>
                  <span className="text-sm">5</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">关键词扩展</span>
                  <Badge variant="secondary">已启用</Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
