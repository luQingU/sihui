import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Smartphone, Mail, MessageSquare, Shield, CheckCircle, AlertTriangle, Settings } from "lucide-react"

export default function MFAPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">多因素认证</h1>
          <p className="text-muted-foreground">MFA安全认证管理和配置</p>
        </div>
        <Button>
          <Settings className="h-4 w-4 mr-2" />
          MFA设置
        </Button>
      </div>

      {/* MFA统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">MFA用户</CardTitle>
            <Shield className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">892</div>
            <p className="text-xs text-muted-foreground">71.5% 启用率</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">短信验证</CardTitle>
            <MessageSquare className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">456</div>
            <p className="text-xs text-muted-foreground">最受欢迎</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">邮箱验证</CardTitle>
            <Mail className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">234</div>
            <p className="text-xs text-muted-foreground">备用方式</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">应用验证</CardTitle>
            <Smartphone className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">202</div>
            <p className="text-xs text-muted-foreground">最安全</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">概览</TabsTrigger>
          <TabsTrigger value="methods">认证方式</TabsTrigger>
          <TabsTrigger value="users">用户管理</TabsTrigger>
          <TabsTrigger value="settings">系统设置</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>MFA启用情况</CardTitle>
                <CardDescription>用户多因素认证启用统计</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">已启用MFA</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={71.5} className="w-20" />
                    <span className="text-sm">71.5%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">仅密码登录</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={28.5} className="w-20" />
                    <span className="text-sm">28.5%</span>
                  </div>
                </div>
                <div className="pt-2 border-t">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-600">892</div>
                    <p className="text-sm text-muted-foreground">用户启用MFA</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>认证方式分布</CardTitle>
                <CardDescription>不同MFA方式的使用情况</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <MessageSquare className="h-4 w-4 text-blue-500" />
                    <span className="text-sm">短信验证</span>
                  </div>
                  <Badge>456用户</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Mail className="h-4 w-4 text-purple-500" />
                    <span className="text-sm">邮箱验证</span>
                  </div>
                  <Badge variant="secondary">234用户</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Smartphone className="h-4 w-4 text-orange-500" />
                    <span className="text-sm">应用验证</span>
                  </div>
                  <Badge variant="secondary">202用户</Badge>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>安全事件监控</CardTitle>
              <CardDescription>MFA相关的安全事件</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-3">
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <CheckCircle className="h-4 w-4 text-green-500" />
                    <span className="font-medium">成功验证</span>
                  </div>
                  <div className="text-2xl font-bold text-green-600">2,847</div>
                  <p className="text-sm text-muted-foreground">今日成功次数</p>
                </div>
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <AlertTriangle className="h-4 w-4 text-yellow-500" />
                    <span className="font-medium">验证失败</span>
                  </div>
                  <div className="text-2xl font-bold text-yellow-600">23</div>
                  <p className="text-sm text-muted-foreground">今日失败次数</p>
                </div>
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <AlertTriangle className="h-4 w-4 text-red-500" />
                    <span className="font-medium">异常尝试</span>
                  </div>
                  <div className="text-2xl font-bold text-red-600">0</div>
                  <p className="text-sm text-muted-foreground">可疑活动</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="methods" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <MessageSquare className="h-5 w-5 text-blue-500" />
                  <span>短信验证</span>
                </CardTitle>
                <CardDescription>通过手机短信接收验证码</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">启用状态</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">使用用户</span>
                  <span className="text-sm font-medium">456</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">成功率</span>
                  <span className="text-sm font-medium">98.5%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均延迟</span>
                  <span className="text-sm font-medium">15秒</span>
                </div>
                <Button className="w-full">配置设置</Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Mail className="h-5 w-5 text-purple-500" />
                  <span>邮箱验证</span>
                </CardTitle>
                <CardDescription>通过邮箱接收验证码</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">启用状态</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">使用用户</span>
                  <span className="text-sm font-medium">234</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">成功率</span>
                  <span className="text-sm font-medium">97.2%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均延迟</span>
                  <span className="text-sm font-medium">8秒</span>
                </div>
                <Button className="w-full">配置设置</Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Smartphone className="h-5 w-5 text-orange-500" />
                  <span>应用验证</span>
                </CardTitle>
                <CardDescription>使用认证应用生成验证码</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">启用状态</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">使用用户</span>
                  <span className="text-sm font-medium">202</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">成功率</span>
                  <span className="text-sm font-medium">99.8%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均延迟</span>
                  <span className="text-sm font-medium">即时</span>
                </div>
                <Button className="w-full">配置设置</Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="users" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>用户MFA状态</CardTitle>
              <CardDescription>查看和管理用户的多因素认证设置</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>用户</TableHead>
                      <TableHead>角色</TableHead>
                      <TableHead>MFA状态</TableHead>
                      <TableHead>认证方式</TableHead>
                      <TableHead>最后验证</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow>
                      <TableCell className="font-medium">张三</TableCell>
                      <TableCell>零售商户</TableCell>
                      <TableCell>
                        <Badge className="bg-green-500">已启用</Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-1">
                          <MessageSquare className="h-3 w-3" />
                          <span className="text-sm">短信</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-sm">2025-01-19 14:30</TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          管理
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell className="font-medium">李四</TableCell>
                      <TableCell>客户经理</TableCell>
                      <TableCell>
                        <Badge className="bg-green-500">已启用</Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-1">
                          <Smartphone className="h-3 w-3" />
                          <span className="text-sm">应用</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-sm">2025-01-19 13:15</TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          管理
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell className="font-medium">王五</TableCell>
                      <TableCell>零售商户</TableCell>
                      <TableCell>
                        <Badge variant="outline">未启用</Badge>
                      </TableCell>
                      <TableCell>-</TableCell>
                      <TableCell className="text-sm">-</TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          启用
                        </Button>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="settings" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>全局MFA设置</CardTitle>
                <CardDescription>系统级别的MFA配置</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">强制启用MFA</span>
                  <Badge variant="outline">管理员</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">验证码有效期</span>
                  <span className="text-sm">5分钟</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">最大重试次数</span>
                  <span className="text-sm">3次</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">备用验证方式</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>安全策略</CardTitle>
                <CardDescription>MFA安全相关策略</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">登录失败锁定</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">异地登录检测</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">设备记忆</span>
                  <span className="text-sm">30天</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">风险评估</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
