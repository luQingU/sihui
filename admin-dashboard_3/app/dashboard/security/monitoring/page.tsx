import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Shield, AlertTriangle, Eye, Activity, Lock, Unlock, MapPin, Clock, User, Globe } from "lucide-react"

export default function SecurityMonitoringPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">安全监控</h1>
          <p className="text-muted-foreground">实时安全威胁检测和事件监控</p>
        </div>
        <Button>
          <Eye className="h-4 w-4 mr-2" />
          实时监控
        </Button>
      </div>

      {/* 安全监控统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">安全事件</CardTitle>
            <AlertTriangle className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">3</div>
            <p className="text-xs text-muted-foreground">今日检测到</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">登录尝试</CardTitle>
            <Lock className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">2,847</div>
            <p className="text-xs text-muted-foreground">成功率: 99.2%</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">异常IP</CardTitle>
            <Globe className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">0</div>
            <p className="text-xs text-muted-foreground">已拦截</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">系统状态</CardTitle>
            <Shield className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">安全</div>
            <p className="text-xs text-muted-foreground">无威胁</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="events" className="space-y-4">
        <TabsList>
          <TabsTrigger value="events">安全事件</TabsTrigger>
          <TabsTrigger value="login">登录监控</TabsTrigger>
          <TabsTrigger value="threats">威胁检测</TabsTrigger>
          <TabsTrigger value="alerts">告警设置</TabsTrigger>
        </TabsList>

        <TabsContent value="events" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>安全事件日志</CardTitle>
              <CardDescription>系统检测到的安全相关事件</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>时间</TableHead>
                      <TableHead>事件类型</TableHead>
                      <TableHead>用户</TableHead>
                      <TableHead>IP地址</TableHead>
                      <TableHead>风险等级</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow>
                      <TableCell className="text-sm">2025-01-19 14:30:25</TableCell>
                      <TableCell>登录失败</TableCell>
                      <TableCell>张三</TableCell>
                      <TableCell>192.168.1.100</TableCell>
                      <TableCell>
                        <Badge variant="outline">低</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-green-500">已处理</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          查看详情
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell className="text-sm">2025-01-19 13:15:42</TableCell>
                      <TableCell>密码重置</TableCell>
                      <TableCell>李四</TableCell>
                      <TableCell>192.168.1.101</TableCell>
                      <TableCell>
                        <Badge variant="outline">低</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-green-500">已处理</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          查看详情
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell className="text-sm">2025-01-19 12:45:18</TableCell>
                      <TableCell>权限提升</TableCell>
                      <TableCell>管理员</TableCell>
                      <TableCell>192.168.1.102</TableCell>
                      <TableCell>
                        <Badge className="bg-yellow-500">中</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-blue-500">监控中</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          查看详情
                        </Button>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="login" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>登录统计</CardTitle>
                <CardDescription>今日登录活动统计</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Unlock className="h-4 w-4 text-green-500" />
                    <span className="text-sm">成功登录</span>
                  </div>
                  <span className="text-sm font-medium">2,824</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Lock className="h-4 w-4 text-red-500" />
                    <span className="text-sm">失败登录</span>
                  </div>
                  <span className="text-sm font-medium">23</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <User className="h-4 w-4 text-blue-500" />
                    <span className="text-sm">唯一用户</span>
                  </div>
                  <span className="text-sm font-medium">1,156</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Clock className="h-4 w-4 text-purple-500" />
                    <span className="text-sm">平均会话</span>
                  </div>
                  <span className="text-sm font-medium">2.5小时</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>地理分布</CardTitle>
                <CardDescription>登录地理位置分析</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <MapPin className="h-4 w-4 text-green-500" />
                    <span className="text-sm">广东省四会市</span>
                  </div>
                  <Badge>100%</Badge>
                </div>
                <div className="text-sm text-muted-foreground">所有登录均来自本地网络，未检测到异地登录。</div>
                <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
                  <div className="flex items-center space-x-2">
                    <Shield className="h-4 w-4 text-green-600" />
                    <span className="text-sm font-medium text-green-800">地理安全状态良好</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>登录趋势</CardTitle>
              <CardDescription>过去24小时的登录活动趋势</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <Activity className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">登录趋势图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="threats" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">暴力破解检测</CardTitle>
                <CardDescription>检测密码暴力破解尝试</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">检测状态</span>
                    <Badge className="bg-green-500">已启用</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">阈值设置</span>
                    <span className="text-sm">5次/5分钟</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">今日拦截</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">SQL注入检测</CardTitle>
                <CardDescription>检测SQL注入攻击尝试</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">检测状态</span>
                    <Badge className="bg-green-500">已启用</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">规则数量</span>
                    <span className="text-sm">156条</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">今日拦截</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">XSS攻击检测</CardTitle>
                <CardDescription>检测跨站脚本攻击</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">检测状态</span>
                    <Badge className="bg-green-500">已启用</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">过滤规则</span>
                    <span className="text-sm">89条</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">今日拦截</span>
                    <Badge variant="outline">0</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>威胁情报</CardTitle>
              <CardDescription>外部威胁情报和IP黑名单</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-4">
                  <h4 className="font-medium">IP黑名单</h4>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between p-2 border rounded">
                      <span className="text-sm font-mono">192.168.100.1</span>
                      <Badge variant="destructive">已拦截</Badge>
                    </div>
                    <div className="flex items-center justify-between p-2 border rounded">
                      <span className="text-sm font-mono">10.0.0.100</span>
                      <Badge variant="destructive">已拦截</Badge>
                    </div>
                  </div>
                </div>
                <div className="space-y-4">
                  <h4 className="font-medium">威胁统计</h4>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm">恶意IP数量</span>
                      <span className="text-sm font-medium">2</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm">拦截次数</span>
                      <span className="text-sm font-medium">0</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm">最后更新</span>
                      <span className="text-sm font-medium">1小时前</span>
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="alerts" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>告警规则配置</CardTitle>
              <CardDescription>设置安全事件的告警规则和通知方式</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">登录失败告警</h4>
                    <Badge className="bg-green-500">已启用</Badge>
                  </div>
                  <div className="text-sm text-muted-foreground mb-2">当用户登录失败次数超过5次时触发告警</div>
                  <div className="flex items-center space-x-2">
                    <Badge variant="outline">邮件通知</Badge>
                    <Badge variant="outline">短信通知</Badge>
                    <Badge variant="outline">系统通知</Badge>
                  </div>
                </div>

                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">异地登录告警</h4>
                    <Badge className="bg-green-500">已启用</Badge>
                  </div>
                  <div className="text-sm text-muted-foreground mb-2">检测到用户从异地登录时立即告警</div>
                  <div className="flex items-center space-x-2">
                    <Badge variant="outline">邮件通知</Badge>
                    <Badge variant="outline">短信通知</Badge>
                  </div>
                </div>

                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">权限异常告警</h4>
                    <Badge variant="outline">已禁用</Badge>
                  </div>
                  <div className="text-sm text-muted-foreground mb-2">检测到用户权限异常变更时告警</div>
                  <div className="flex items-center space-x-2">
                    <Badge variant="outline">邮件通知</Badge>
                  </div>
                </div>

                <Button>
                  <AlertTriangle className="h-4 w-4 mr-2" />
                  新建告警规则
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
