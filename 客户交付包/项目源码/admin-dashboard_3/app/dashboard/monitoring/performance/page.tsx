import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import {
  Activity,
  Cpu,
  HardDrive,
  MemoryStick,
  Network,
  Clock,
  TrendingUp,
  AlertTriangle,
  CheckCircle,
} from "lucide-react"

export default function PerformancePage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">性能监控</h1>
          <p className="text-muted-foreground">实时系统性能监控和分析</p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline">
            <TrendingUp className="h-4 w-4 mr-2" />
            性能报告
          </Button>
          <Button>
            <Activity className="h-4 w-4 mr-2" />
            实时监控
          </Button>
        </div>
      </div>

      {/* 系统资源监控 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">CPU使用率</CardTitle>
            <Cpu className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">45%</div>
            <Progress value={45} className="mt-2" />
            <p className="text-xs text-muted-foreground mt-1">8核心 Intel Xeon</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">内存使用</CardTitle>
            <MemoryStick className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">62%</div>
            <Progress value={62} className="mt-2" />
            <p className="text-xs text-muted-foreground mt-1">12.4GB / 20GB</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">磁盘使用</CardTitle>
            <HardDrive className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">38%</div>
            <Progress value={38} className="mt-2" />
            <p className="text-xs text-muted-foreground mt-1">190GB / 500GB</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">网络流量</CardTitle>
            <Network className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">125MB/s</div>
            <p className="text-xs text-muted-foreground">入站: 85MB/s</p>
            <p className="text-xs text-muted-foreground">出站: 40MB/s</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">系统概览</TabsTrigger>
          <TabsTrigger value="database">数据库性能</TabsTrigger>
          <TabsTrigger value="api">API性能</TabsTrigger>
          <TabsTrigger value="cache">缓存性能</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>系统健康状态</CardTitle>
                <CardDescription>各组件运行状态</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <CheckCircle className="h-4 w-4 text-green-500" />
                    <span className="text-sm">Spring Boot 应用</span>
                  </div>
                  <Badge className="bg-green-500">正常</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <CheckCircle className="h-4 w-4 text-green-500" />
                    <span className="text-sm">MySQL 数据库</span>
                  </div>
                  <Badge className="bg-green-500">正常</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <CheckCircle className="h-4 w-4 text-green-500" />
                    <span className="text-sm">Redis 缓存</span>
                  </div>
                  <Badge className="bg-green-500">正常</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <AlertTriangle className="h-4 w-4 text-yellow-500" />
                    <span className="text-sm">阿里云 OSS</span>
                  </div>
                  <Badge className="bg-yellow-500">警告</Badge>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>性能指标</CardTitle>
                <CardDescription>关键性能数据</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均响应时间</span>
                  <span className="text-sm font-medium">125ms</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">QPS (每秒请求)</span>
                  <span className="text-sm font-medium">1,247</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">错误率</span>
                  <span className="text-sm font-medium">0.02%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">可用性</span>
                  <span className="text-sm font-medium">99.98%</span>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>系统负载趋势</CardTitle>
              <CardDescription>过去24小时的系统负载变化</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <Activity className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">系统负载趋势图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="database" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>MySQL 性能</CardTitle>
                <CardDescription>数据库连接和查询性能</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">连接池使用率</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={72} className="w-20" />
                    <span className="text-sm">72%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均查询时间</span>
                  <span className="text-sm font-medium">15ms</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">慢查询数量</span>
                  <Badge variant="outline">2</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">QPS</span>
                  <span className="text-sm font-medium">856</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>连接状态</CardTitle>
                <CardDescription>数据库连接详情</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">活跃连接</span>
                  <span className="text-sm font-medium">18 / 25</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">空闲连接</span>
                  <span className="text-sm font-medium">7</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">最大连接数</span>
                  <span className="text-sm font-medium">100</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">连接超时</span>
                  <span className="text-sm font-medium">30s</span>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>慢查询分析</CardTitle>
              <CardDescription>执行时间超过阈值的SQL查询</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">用户查询优化</span>
                    <Badge variant="outline">2.3s</Badge>
                  </div>
                  <code className="text-xs bg-muted p-2 rounded block">
                    SELECT * FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id WHERE u.status = 'ACTIVE'
                  </code>
                </div>
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">问卷数据统计</span>
                    <Badge variant="outline">1.8s</Badge>
                  </div>
                  <code className="text-xs bg-muted p-2 rounded block">
                    SELECT COUNT(*) FROM questionnaire_responses qr GROUP BY qr.questionnaire_id
                  </code>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="api" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>API 性能统计</CardTitle>
                <CardDescription>100+ API接口性能概览</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">总请求数</span>
                  <span className="text-sm font-medium">1,247,856</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">成功率</span>
                  <span className="text-sm font-medium">99.98%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均响应时间</span>
                  <span className="text-sm font-medium">125ms</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">P95响应时间</span>
                  <span className="text-sm font-medium">350ms</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>热门API</CardTitle>
                <CardDescription>调用频率最高的接口</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">/api/auth/login</span>
                  <Badge>15.2K</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">/api/users</span>
                  <Badge>12.8K</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">/api/ai/chat</span>
                  <Badge>8.9K</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">/api/questionnaires</span>
                  <Badge>6.7K</Badge>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>API响应时间分布</CardTitle>
              <CardDescription>不同响应时间区间的请求分布</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <Clock className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">API响应时间分布图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="cache" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Redis 缓存性能</CardTitle>
                <CardDescription>缓存命中率和性能指标</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">缓存命中率</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={94.2} className="w-20" />
                    <span className="text-sm font-medium">94.2%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均响应时间</span>
                  <span className="text-sm font-medium">0.8ms</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">内存使用率</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={68} className="w-20" />
                    <span className="text-sm">68%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">连接数</span>
                  <span className="text-sm font-medium">45 / 100</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>缓存操作统计</CardTitle>
                <CardDescription>缓存读写操作数据</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">读操作</span>
                  <span className="text-sm font-medium">156,789</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">写操作</span>
                  <span className="text-sm font-medium">23,456</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">缓存失效</span>
                  <span className="text-sm font-medium">1,234</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">键空间数量</span>
                  <span className="text-sm font-medium">45,678</span>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>缓存策略配置</CardTitle>
              <CardDescription>多层缓存架构配置</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-3">
                <div className="p-4 border rounded-lg">
                  <h4 className="font-medium mb-2">用户缓存</h4>
                  <div className="space-y-1 text-sm text-muted-foreground">
                    <p>TTL: 30分钟</p>
                    <p>策略: LRU</p>
                    <p>命中率: 96.5%</p>
                  </div>
                </div>
                <div className="p-4 border rounded-lg">
                  <h4 className="font-medium mb-2">权限缓存</h4>
                  <div className="space-y-1 text-sm text-muted-foreground">
                    <p>TTL: 1小时</p>
                    <p>策略: LFU</p>
                    <p>命中率: 98.2%</p>
                  </div>
                </div>
                <div className="p-4 border rounded-lg">
                  <h4 className="font-medium mb-2">内容缓存</h4>
                  <div className="space-y-1 text-sm text-muted-foreground">
                    <p>TTL: 24小时</p>
                    <p>策略: TTL</p>
                    <p>命中率: 89.7%</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
