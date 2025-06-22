import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Switch } from "@/components/ui/switch"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Save, RefreshCw, Database, Shield, Bell } from "lucide-react"

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">系统设置</h1>
        <p className="text-muted-foreground">管理系统的全局设置和配置</p>
      </div>

      <Tabs defaultValue="general" className="space-y-4">
        <TabsList>
          <TabsTrigger value="general">基本设置</TabsTrigger>
          <TabsTrigger value="appearance">外观</TabsTrigger>
          <TabsTrigger value="notifications">通知</TabsTrigger>
          <TabsTrigger value="security">安全</TabsTrigger>
          <TabsTrigger value="database">数据库</TabsTrigger>
        </TabsList>

        <TabsContent value="general" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>基本信息</CardTitle>
              <CardDescription>设置系统的基本信息</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="system-name">系统名称</Label>
                <Input id="system-name" defaultValue="管理系统" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="admin-email">管理员邮箱</Label>
                <Input id="admin-email" defaultValue="admin@example.com" type="email" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="timezone">时区</Label>
                <Select defaultValue="Asia/Shanghai">
                  <SelectTrigger id="timezone">
                    <SelectValue placeholder="选择时区" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Asia/Shanghai">中国标准时间 (UTC+8)</SelectItem>
                    <SelectItem value="America/New_York">美国东部时间 (UTC-5)</SelectItem>
                    <SelectItem value="Europe/London">格林威治标准时间 (UTC+0)</SelectItem>
                    <SelectItem value="Asia/Tokyo">日本标准时间 (UTC+9)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="language">系统语言</Label>
                <Select defaultValue="zh-CN">
                  <SelectTrigger id="language">
                    <SelectValue placeholder="选择语言" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="zh-CN">简体中文</SelectItem>
                    <SelectItem value="en-US">English (US)</SelectItem>
                    <SelectItem value="ja-JP">日本語</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button>
                <Save className="h-4 w-4 mr-2" />
                保存设置
              </Button>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="appearance" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>外观设置</CardTitle>
              <CardDescription>自定义系统的外观和主题</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="dark-mode">深色模式</Label>
                  <p className="text-sm text-muted-foreground">启用系统深色模式</p>
                </div>
                <Switch id="dark-mode" />
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="auto-theme">自动切换主题</Label>
                  <p className="text-sm text-muted-foreground">根据系统设置自动切换主题</p>
                </div>
                <Switch id="auto-theme" defaultChecked />
              </div>
              <div className="space-y-2">
                <Label htmlFor="primary-color">主题色</Label>
                <div className="grid grid-cols-6 gap-2">
                  {["bg-blue-600", "bg-green-600", "bg-red-600", "bg-purple-600", "bg-orange-600", "bg-gray-600"].map(
                    (color, index) => (
                      <div
                        key={index}
                        className={`h-8 rounded-md cursor-pointer ${color} ${
                          index === 0 ? "ring-2 ring-offset-2 ring-blue-600" : ""
                        }`}
                      ></div>
                    ),
                  )}
                </div>
              </div>
              <Button>
                <Save className="h-4 w-4 mr-2" />
                保存设置
              </Button>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="notifications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>通知设置</CardTitle>
              <CardDescription>配置系统通知和提醒</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="email-notifications">邮件通知</Label>
                  <p className="text-sm text-muted-foreground">接收系统更新和重要事件的邮件通知</p>
                </div>
                <Switch id="email-notifications" defaultChecked />
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="browser-notifications">浏览器通知</Label>
                  <p className="text-sm text-muted-foreground">在浏览器中接收实时通知</p>
                </div>
                <Switch id="browser-notifications" />
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="login-alerts">登录提醒</Label>
                  <p className="text-sm text-muted-foreground">当有新的登录活动时接收提醒</p>
                </div>
                <Switch id="login-alerts" defaultChecked />
              </div>
              <Button>
                <Bell className="h-4 w-4 mr-2" />
                测试通知
              </Button>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>安全设置</CardTitle>
              <CardDescription>管理系统的安全选项</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="two-factor">两步验证</Label>
                  <p className="text-sm text-muted-foreground">启用两步验证以增强账户安全</p>
                </div>
                <Switch id="two-factor" />
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="session-timeout">会话超时</Label>
                  <p className="text-sm text-muted-foreground">设置自动登出时间</p>
                </div>
                <Select defaultValue="30">
                  <SelectTrigger className="w-[180px]">
                    <SelectValue placeholder="选择时间" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="15">15 分钟</SelectItem>
                    <SelectItem value="30">30 分钟</SelectItem>
                    <SelectItem value="60">1 小时</SelectItem>
                    <SelectItem value="120">2 小时</SelectItem>
                    <SelectItem value="never">永不</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="password-policy">密码策略</Label>
                <Select defaultValue="strong">
                  <SelectTrigger id="password-policy">
                    <SelectValue placeholder="选择密码策略" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="basic">基本 (最少 8 个字符)</SelectItem>
                    <SelectItem value="medium">中等 (最少 10 个字符，包含数字)</SelectItem>
                    <SelectItem value="strong">强 (最少 12 个字符，包含大小写字母、数字和符号)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button>
                <Shield className="h-4 w-4 mr-2" />
                更新安全设置
              </Button>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="database" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>数据库设置</CardTitle>
              <CardDescription>管理数据库连接和备份</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="db-connection">数据库连接</Label>
                <Input id="db-connection" defaultValue="mysql://user:****@localhost:3306/dbname" type="text" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="backup-frequency">备份频率</Label>
                <Select defaultValue="daily">
                  <SelectTrigger id="backup-frequency">
                    <SelectValue placeholder="选择备份频率" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="hourly">每小时</SelectItem>
                    <SelectItem value="daily">每天</SelectItem>
                    <SelectItem value="weekly">每周</SelectItem>
                    <SelectItem value="monthly">每月</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="backup-retention">备份保留期</Label>
                <Select defaultValue="30">
                  <SelectTrigger id="backup-retention">
                    <SelectValue placeholder="选择保留期" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="7">7 天</SelectItem>
                    <SelectItem value="30">30 天</SelectItem>
                    <SelectItem value="90">90 天</SelectItem>
                    <SelectItem value="365">365 天</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex space-x-2">
                <Button>
                  <Database className="h-4 w-4 mr-2" />
                  立即备份
                </Button>
                <Button variant="outline">
                  <RefreshCw className="h-4 w-4 mr-2" />
                  测试连接
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
