import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import {
  BarChart,
  Users,
  FileText,
  FileQuestion,
  ArrowUpRight,
  ArrowDownRight,
  Shield,
  Database,
  MessageSquare,
  TrendingUp,
  Clock,
  CheckCircle,
} from "lucide-react"

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">四会培训管理平台</h1>
        <p className="text-muted-foreground">欢迎回来，管理员！这里是您的系统概览。</p>
      </div>

      {/* 核心指标卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总用户数</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">1,248</div>
            <p className="text-xs text-muted-foreground flex items-center">
              <span className="text-green-500 flex items-center mr-1">
                <ArrowUpRight className="h-3 w-3 mr-1" /> 12%
              </span>
              较上月
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">培训内容</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">324</div>
            <p className="text-xs text-muted-foreground flex items-center">
              <span className="text-green-500 flex items-center mr-1">
                <ArrowUpRight className="h-3 w-3 mr-1" /> 8%
              </span>
              较上月
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">问卷数量</CardTitle>
            <FileQuestion className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">56</div>
            <p className="text-xs text-muted-foreground flex items-center">
              <span className="text-red-500 flex items-center mr-1">
                <ArrowDownRight className="h-3 w-3 mr-1" /> 3%
              </span>
              较上月
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">AI问答次数</CardTitle>
            <MessageSquare className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">8,942</div>
            <p className="text-xs text-muted-foreground flex items-center">
              <span className="text-green-500 flex items-center mr-1">
                <ArrowUpRight className="h-3 w-3 mr-1" /> 24%
              </span>
              较上月
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 系统状态监控 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">系统健康度</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">99.8%</div>
            <Progress value={99.8} className="mt-2" />
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">安全评分</CardTitle>
            <Shield className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">A+</div>
            <p className="text-xs text-muted-foreground">JWT + MFA 保护</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">缓存命中率</CardTitle>
            <Database className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">94.2%</div>
            <Progress value={94.2} className="mt-2" />
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">API响应时间</CardTitle>
            <Clock className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">125ms</div>
            <p className="text-xs text-muted-foreground">平均响应时间</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">系统概览</TabsTrigger>
          <TabsTrigger value="security">安全监控</TabsTrigger>
          <TabsTrigger value="performance">性能分析</TabsTrigger>
          <TabsTrigger value="ai">AI服务</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
            <Card className="col-span-4">
              <CardHeader>
                <CardTitle>用户增长趋势</CardTitle>
                <CardDescription>过去30天的用户注册和活跃情况</CardDescription>
              </CardHeader>
              <CardContent className="pl-2">
                <div className="h-[200px] flex items-center justify-center bg-muted/20 rounded-md">
                  <BarChart className="h-8 w-8 text-muted-foreground" />
                  <span className="ml-2 text-muted-foreground">用户增长图表</span>
                </div>
              </CardContent>
            </Card>
            <Card className="col-span-3">
              <CardHeader>
                <CardTitle>最近活动</CardTitle>
                <CardDescription>系统最近的操作记录</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {[
                    { user: "管理员", action: "上传了新的培训视频", time: "10分钟前", type: "content" },
                    { user: "客户经理", action: "创建了满意度调查问卷", time: "1小时前", type: "questionnaire" },
                    { user: "系统", action: "执行了数据备份", time: "3小时前", type: "system" },
                    { user: "零售商户", action: "完成了产品培训", time: "昨天", type: "training" },
                  ].map((item, index) => (
                    <div key={index} className="flex items-center">
                      <div className="space-y-1">
                        <p className="text-sm font-medium leading-none">
                          {item.user} {item.action}
                        </p>
                        <p className="text-sm text-muted-foreground flex items-center">
                          <Clock className="h-3 w-3 mr-1" />
                          {item.time}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* 模块完成度 */}
          <Card>
            <CardHeader>
              <CardTitle>系统模块完成度</CardTitle>
              <CardDescription>基于技术文档的功能实现进度</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2">
                {[
                  { name: "用户认证与权限管理", progress: 100, status: "完成" },
                  { name: "多因素认证系统", progress: 100, status: "完成" },
                  { name: "培训内容管理", progress: 90, status: "进行中" },
                  { name: "AI智能问答系统", progress: 85, status: "进行中" },
                  { name: "问卷管理与分析", progress: 90, status: "进行中" },
                  { name: "系统监控与性能", progress: 100, status: "完成" },
                  { name: "前端管理界面", progress: 80, status: "进行中" },
                  { name: "小程序端接口", progress: 30, status: "待完善" },
                ].map((module, index) => (
                  <div key={index} className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium">{module.name}</span>
                      <div className="flex items-center space-x-2">
                        <Badge
                          variant={
                            module.progress === 100 ? "default" : module.progress >= 80 ? "secondary" : "outline"
                          }
                        >
                          {module.status}
                        </Badge>
                        <span className="text-sm text-muted-foreground">{module.progress}%</span>
                      </div>
                    </div>
                    <Progress value={module.progress} className="h-2" />
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>安全事件监控</CardTitle>
                <CardDescription>实时安全威胁检测</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">登录失败尝试</span>
                    <Badge variant="outline">3次</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">异常访问检测</span>
                    <Badge variant="outline">0次</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">SQL注入尝试</span>
                    <Badge variant="outline">0次</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">XSS攻击尝试</span>
                    <Badge variant="outline">0次</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>认证统计</CardTitle>
                <CardDescription>用户认证方式分布</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">JWT认证</span>
                    <Badge>1,248用户</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">双因素认证</span>
                    <Badge variant="secondary">892用户</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">短信验证</span>
                    <Badge variant="secondary">456用户</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">邮箱验证</span>
                    <Badge variant="secondary">234用户</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>权限系统概览</CardTitle>
              <CardDescription>50+权限点的分配情况</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-3">
                <div className="text-center">
                  <div className="text-2xl font-bold">50+</div>
                  <p className="text-sm text-muted-foreground">权限点</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold">5</div>
                  <p className="text-sm text-muted-foreground">用户角色</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold">100%</div>
                  <p className="text-sm text-muted-foreground">权限覆盖率</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="performance" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>系统性能指标</CardTitle>
                <CardDescription>实时性能监控数据</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">CPU使用率</span>
                    <div className="flex items-center space-x-2">
                      <Progress value={45} className="w-20" />
                      <span className="text-sm">45%</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">内存使用率</span>
                    <div className="flex items-center space-x-2">
                      <Progress value={62} className="w-20" />
                      <span className="text-sm">62%</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">磁盘使用率</span>
                    <div className="flex items-center space-x-2">
                      <Progress value={38} className="w-20" />
                      <span className="text-sm">38%</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">网络吞吐量</span>
                    <span className="text-sm">125 MB/s</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>数据库性能</CardTitle>
                <CardDescription>MySQL 8.0 + Redis 性能监控</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">查询响应时间</span>
                    <Badge variant="secondary">15ms</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">连接池使用率</span>
                    <div className="flex items-center space-x-2">
                      <Progress value={72} className="w-20" />
                      <span className="text-sm">72%</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">慢查询数量</span>
                    <Badge variant="outline">2</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Redis命中率</span>
                    <Badge>94.2%</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>API性能统计</CardTitle>
              <CardDescription>100+ API接口性能分析</CardDescription>
            </CardHeader>
            <CardContent className="h-[200px] flex items-center justify-center bg-muted/20 rounded-md">
              <TrendingUp className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">API性能趋势图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="ai" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>DeepSeek AI 服务</CardTitle>
                <CardDescription>智能问答系统状态</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">服务状态</span>
                    <Badge className="bg-green-500">正常运行</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">今日调用次数</span>
                    <span className="text-sm font-medium">2,847</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">平均响应时间</span>
                    <span className="text-sm font-medium">1.2s</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">成功率</span>
                    <Badge>99.5%</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>知识库统计</CardTitle>
                <CardDescription>四会知识库内容分析</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">文档总数</span>
                    <span className="text-sm font-medium">1,456</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">已向量化文档</span>
                    <span className="text-sm font-medium">1,398</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">检索准确率</span>
                    <Badge>92.8%</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">TF-IDF索引</span>
                    <Badge className="bg-blue-500">已优化</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>AI对话分析</CardTitle>
              <CardDescription>用户对话模式和满意度</CardDescription>
            </CardHeader>
            <CardContent className="h-[200px] flex items-center justify-center bg-muted/20 rounded-md">
              <MessageSquare className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">对话分析图表</span>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
