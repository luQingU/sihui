import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { Shield, Key, CheckCircle, AlertTriangle, Smartphone } from "lucide-react"

export default function AuthPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">认证管理</h1>
          <p className="text-muted-foreground">JWT + Spring Security 认证体系管理</p>
        </div>
        <Button>
          <Shield className="h-4 w-4 mr-2" />
          安全检查
        </Button>
      </div>

      {/* 认证统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">JWT令牌</CardTitle>
            <Key className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">1,248</div>
            <p className="text-xs text-muted-foreground">活跃令牌数</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">MFA用户</CardTitle>
            <Smartphone className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">892</div>
            <p className="text-xs text-muted-foreground">启用双因素认证</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">登录成功率</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">99.2%</div>
            <p className="text-xs text-muted-foreground">过去24小时</p>
          </CardContent>
        </Card>

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
      </div>

      <Tabs defaultValue="jwt" className="space-y-4">
        <TabsList>
          <TabsTrigger value="jwt">JWT管理</TabsTrigger>
          <TabsTrigger value="sessions">会话管理</TabsTrigger>
          <TabsTrigger value="security">安全策略</TabsTrigger>
        </TabsList>

        <TabsContent value="jwt" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>JWT配置</CardTitle>
                <CardDescription>JSON Web Token 配置信息</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">签名算法</span>
                  <Badge>HS256</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">令牌有效期</span>
                  <span className="text-sm">24小时</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">刷新令牌有效期</span>
                  <span className="text-sm">7天</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">密钥轮换</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>令牌统计</CardTitle>
                <CardDescription>JWT令牌使用情况</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">活跃令牌</span>
                  <span className="text-sm font-medium">1,248</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">今日签发</span>
                  <span className="text-sm font-medium">156</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">今日刷新</span>
                  <span className="text-sm font-medium">89</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">过期令牌</span>
                  <span className="text-sm font-medium">23</span>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>JWT性能监控</CardTitle>
              <CardDescription>令牌生成和验证性能</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-3">
                <div className="text-center">
                  <div className="text-2xl font-bold">2.3ms</div>
                  <p className="text-sm text-muted-foreground">平均生成时间</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold">1.8ms</div>
                  <p className="text-sm text-muted-foreground">平均验证时间</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold">99.9%</div>
                  <p className="text-sm text-muted-foreground">验证成功率</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="sessions" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>会话配置</CardTitle>
                <CardDescription>用户会话管理设置</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">最大并发会话</span>
                  <span className="text-sm">3个/用户</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">会话超时</span>
                  <span className="text-sm">30分钟</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">记住我</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">强制单点登录</span>
                  <Badge variant="outline">已禁用</Badge>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>会话监控</CardTitle>
                <CardDescription>实时会话状态</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">总会话数</span>
                  <span className="text-sm font-medium">1,248</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">活跃会话</span>
                  <span className="text-sm font-medium">892</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">空闲会话</span>
                  <span className="text-sm font-medium">356</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">平均会话时长</span>
                  <span className="text-sm font-medium">2.5小时</span>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>密码策略</CardTitle>
                <CardDescription>用户密码安全要求</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">最小长度</span>
                  <span className="text-sm font-medium">8位</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">复杂度要求</span>
                  <Badge>强</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">密码有效期</span>
                  <span className="text-sm font-medium">90天</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">历史密码检查</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>登录安全</CardTitle>
                <CardDescription>登录保护机制</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">失败锁定阈值</span>
                  <span className="text-sm font-medium">5次</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">锁定时间</span>
                  <span className="text-sm font-medium">15分钟</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">验证码保护</span>
                  <Badge className="bg-green-500">已启用</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">IP白名单</span>
                  <Badge variant="outline">已禁用</Badge>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>安全评估</CardTitle>
              <CardDescription>系统安全状态评估</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">整体安全评分</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={95} className="w-20" />
                    <Badge className="bg-green-500">A+</Badge>
                  </div>
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm">认证强度</span>
                      <Badge className="bg-green-500">优秀</Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm">会话安全</span>
                      <Badge className="bg-green-500">优秀</Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm">密码策略</span>
                      <Badge className="bg-green-500">优秀</Badge>
                    </div>
                  </div>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm">访问控制</span>
                      <Badge className="bg-green-500">优秀</Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm">数据加密</span>
                      <Badge className="bg-green-500">优秀</Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm">审计日志</span>
                      <Badge className="bg-blue-500">良好</Badge>
                    </div>
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
