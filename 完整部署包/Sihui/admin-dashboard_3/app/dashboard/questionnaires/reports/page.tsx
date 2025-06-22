import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  FileText,
  Download,
  Search,
  Calendar,
  BarChart,
  PieChart,
  TrendingUp,
  Users,
  Clock,
  CheckCircle,
} from "lucide-react"

// 模拟报告数据
const mockReports = [
  {
    id: 1,
    title: "2025年Q1客户满意度报告",
    questionnaire: "客户满意度调查",
    period: "2025-01-01 至 2025-03-31",
    responses: 1248,
    status: "已完成",
    createdAt: "2025-01-19",
    format: "PDF",
  },
  {
    id: 2,
    title: "产品功能反馈分析报告",
    questionnaire: "产品功能反馈",
    period: "2025-01-01 至 2025-01-31",
    responses: 856,
    status: "生成中",
    createdAt: "2025-01-18",
    format: "Excel",
  },
  {
    id: 3,
    title: "培训效果评估报告",
    questionnaire: "培训效果评估",
    period: "2024-12-01 至 2024-12-31",
    responses: 432,
    status: "已完成",
    createdAt: "2025-01-15",
    format: "PDF",
  },
]

export default function ReportsPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">报告生成</h1>
          <p className="text-muted-foreground">自动生成问卷分析报告和数据导出</p>
        </div>
        <Button>
          <FileText className="h-4 w-4 mr-2" />
          新建报告
        </Button>
      </div>

      {/* 报告统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总报告数</CardTitle>
            <FileText className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockReports.length}</div>
            <p className="text-xs text-muted-foreground">本月生成</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">已完成</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {mockReports.filter((r) => r.status === "已完成").length}
            </div>
            <p className="text-xs text-muted-foreground">可下载报告</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总响应数</CardTitle>
            <Users className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">
              {mockReports.reduce((sum, r) => sum + r.responses, 0)}
            </div>
            <p className="text-xs text-muted-foreground">数据样本</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">生成中</CardTitle>
            <Clock className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {mockReports.filter((r) => r.status === "生成中").length}
            </div>
            <p className="text-xs text-muted-foreground">处理中报告</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="reports" className="space-y-4">
        <TabsList>
          <TabsTrigger value="reports">报告列表</TabsTrigger>
          <TabsTrigger value="templates">报告模板</TabsTrigger>
          <TabsTrigger value="schedule">定时报告</TabsTrigger>
        </TabsList>

        <TabsContent value="reports" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>报告列表</CardTitle>
              <CardDescription>查看和下载生成的分析报告</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2 mb-4">
                <div className="relative flex-1 max-w-sm">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input type="search" placeholder="搜索报告..." className="pl-8" />
                </div>
                <Button variant="outline">
                  <Calendar className="h-4 w-4 mr-2" />
                  时间筛选
                </Button>
              </div>

              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>报告标题</TableHead>
                      <TableHead>问卷名称</TableHead>
                      <TableHead>时间范围</TableHead>
                      <TableHead>响应数</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>格式</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mockReports.map((report) => (
                      <TableRow key={report.id}>
                        <TableCell className="font-medium">{report.title}</TableCell>
                        <TableCell>{report.questionnaire}</TableCell>
                        <TableCell className="text-sm">{report.period}</TableCell>
                        <TableCell>{report.responses.toLocaleString()}</TableCell>
                        <TableCell>
                          <Badge variant={report.status === "已完成" ? "default" : "secondary"}>{report.status}</Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">{report.format}</Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button variant="ghost" size="sm" disabled={report.status !== "已完成"}>
                            <Download className="h-4 w-4 mr-1" />
                            下载
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

        <TabsContent value="templates" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">标准分析报告</CardTitle>
                <CardDescription>包含基础统计和图表分析</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center space-x-2">
                    <BarChart className="h-4 w-4 text-blue-500" />
                    <span className="text-sm">基础统计图表</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <PieChart className="h-4 w-4 text-green-500" />
                    <span className="text-sm">分布饼图</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <TrendingUp className="h-4 w-4 text-purple-500" />
                    <span className="text-sm">趋势分析</span>
                  </div>
                  <Button className="w-full mt-4">使用模板</Button>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">详细分析报告</CardTitle>
                <CardDescription>深度数据分析和洞察</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center space-x-2">
                    <BarChart className="h-4 w-4 text-blue-500" />
                    <span className="text-sm">多维度分析</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <PieChart className="h-4 w-4 text-green-500" />
                    <span className="text-sm">交叉分析</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <TrendingUp className="h-4 w-4 text-purple-500" />
                    <span className="text-sm">预测分析</span>
                  </div>
                  <Button className="w-full mt-4">使用模板</Button>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">执行摘要报告</CardTitle>
                <CardDescription>高管层面的简要总结</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center space-x-2">
                    <BarChart className="h-4 w-4 text-blue-500" />
                    <span className="text-sm">关键指标</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <PieChart className="h-4 w-4 text-green-500" />
                    <span className="text-sm">核心洞察</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <TrendingUp className="h-4 w-4 text-purple-500" />
                    <span className="text-sm">行动建议</span>
                  </div>
                  <Button className="w-full mt-4">使用模板</Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="schedule" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>定时报告设置</CardTitle>
              <CardDescription>配置自动生成的定期报告</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">月度客户满意度报告</h4>
                    <Badge className="bg-green-500">已启用</Badge>
                  </div>
                  <div className="text-sm text-muted-foreground mb-2">每月1日自动生成上月的客户满意度分析报告</div>
                  <div className="flex items-center space-x-2">
                    <Badge variant="outline">PDF格式</Badge>
                    <Badge variant="outline">邮件发送</Badge>
                    <Badge variant="outline">自动存档</Badge>
                  </div>
                </div>

                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">周度培训效果报告</h4>
                    <Badge variant="outline">已暂停</Badge>
                  </div>
                  <div className="text-sm text-muted-foreground mb-2">每周一生成上周的培训效果评估报告</div>
                  <div className="flex items-center space-x-2">
                    <Badge variant="outline">Excel格式</Badge>
                    <Badge variant="outline">系统通知</Badge>
                  </div>
                </div>

                <Button>
                  <FileText className="h-4 w-4 mr-2" />
                  新建定时报告
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
