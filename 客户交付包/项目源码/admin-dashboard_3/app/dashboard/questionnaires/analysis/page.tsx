import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { BarChart, PieChart, LineChart, Download, ArrowLeft } from "lucide-react"
import Link from "next/link"

export default function QuestionnaireAnalysisPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <Button variant="ghost" size="icon" asChild className="mr-2">
            <Link href="/dashboard/questionnaires">
              <ArrowLeft className="h-4 w-4" />
            </Link>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">问卷数据分析</h1>
        </div>
        <Button variant="outline">
          <Download className="h-4 w-4 mr-2" />
          导出报告
        </Button>
      </div>

      <div className="flex items-center space-x-4">
        <Select defaultValue="1">
          <SelectTrigger className="w-[250px]">
            <SelectValue placeholder="选择问卷" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="1">客户满意度调查</SelectItem>
            <SelectItem value="2">产品功能反馈</SelectItem>
            <SelectItem value="5">新功能需求收集</SelectItem>
          </SelectContent>
        </Select>
        <Select defaultValue="all">
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="时间范围" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部时间</SelectItem>
            <SelectItem value="week">最近一周</SelectItem>
            <SelectItem value="month">最近一个月</SelectItem>
            <SelectItem value="quarter">最近三个月</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">总体概览</TabsTrigger>
          <TabsTrigger value="trends">趋势分析</TabsTrigger>
          <TabsTrigger value="comparison">对比分析</TabsTrigger>
          <TabsTrigger value="keywords">关键词分析</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle>满意度分布</CardTitle>
                <CardDescription>客户满意度评分分布</CardDescription>
              </CardHeader>
              <CardContent className="h-[250px] flex items-center justify-center bg-muted/20 rounded-md">
                <PieChart className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">满意度分布图表</span>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>功能使用情况</CardTitle>
                <CardDescription>各功能使用频率</CardDescription>
              </CardHeader>
              <CardContent className="h-[250px] flex items-center justify-center bg-muted/20 rounded-md">
                <BarChart className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">功能使用图表</span>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>客户服务评价</CardTitle>
                <CardDescription>客户服务满意度评分</CardDescription>
              </CardHeader>
              <CardContent className="h-[250px] flex items-center justify-center bg-muted/20 rounded-md">
                <PieChart className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">客户服务评价图表</span>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>AI 分析结果</CardTitle>
              <CardDescription>基于问卷数据的智能分析</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="p-4 bg-muted/20 rounded-md">
                <h3 className="font-medium mb-2">主要发现</h3>
                <ul className="list-disc pl-5 space-y-1">
                  <li>大多数用户对产品总体满意度较高，82.8% 的用户选择"满意"或"非常满意"</li>
                  <li>功能 A 和功能 B 是用户最常使用的功能，分别有 76.6% 和 58.6% 的用户选择</li>
                  <li>客户服务满意度略高于产品满意度，85.9% 的用户表示满意</li>
                  <li>用户反馈中最常提到的改进建议是"自定义功能"和"界面简化"</li>
                </ul>
              </div>
              <div className="p-4 bg-muted/20 rounded-md">
                <h3 className="font-medium mb-2">建议行动</h3>
                <ul className="list-disc pl-5 space-y-1">
                  <li>考虑增强功能 A 和功能 B 的用户体验，这是用户最常使用的功能</li>
                  <li>优先开发更多自定义选项，这是用户最常提到的需求</li>
                  <li>评估界面简化的可能性，这是用户第二常提到的建议</li>
                  <li>保持当前的客户服务水平，这是用户满意度最高的方面</li>
                </ul>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="trends" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>满意度趋势</CardTitle>
              <CardDescription>客户满意度随时间变化</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <LineChart className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">满意度趋势图表</span>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>功能使用趋势</CardTitle>
              <CardDescription>各功能使用频率随时间变化</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <LineChart className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">功能使用趋势图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="comparison" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>不同用户群体对比</CardTitle>
              <CardDescription>不同用户群体的满意度对比</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <BarChart className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">用户群体对比图表</span>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>不同问卷对比</CardTitle>
              <CardDescription>不同问卷结果的对比分析</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <BarChart className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">问卷对比图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="keywords" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>文本回答关键词分析</CardTitle>
              <CardDescription>从文本回答中提取的关键词和主题</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
                  <span className="text-muted-foreground">词云图</span>
                </div>
                <div className="space-y-4">
                  <div className="p-4 bg-muted/20 rounded-md">
                    <h3 className="font-medium mb-2">热门关键词</h3>
                    <div className="flex flex-wrap gap-2">
                      {["自定义功能", "界面", "简洁", "模板", "教程", "集成", "速度", "稳定性", "价格", "支持"].map(
                        (keyword, index) => (
                          <div key={index} className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm">
                            {keyword}
                          </div>
                        ),
                      )}
                    </div>
                  </div>
                  <div className="p-4 bg-muted/20 rounded-md">
                    <h3 className="font-medium mb-2">主要主题</h3>
                    <ul className="list-disc pl-5 space-y-1">
                      <li>用户界面体验 (32%)</li>
                      <li>功能需求 (28%)</li>
                      <li>性能问题 (18%)</li>
                      <li>价格和价值 (12%)</li>
                      <li>客户支持 (10%)</li>
                    </ul>
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
