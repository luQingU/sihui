import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { MessageSquare, TrendingUp, Users, Clock, ThumbsUp, AlertCircle, BarChart, PieChart } from "lucide-react"

export default function AIAnalyticsPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">AI分析</h1>
          <p className="text-muted-foreground">AI对话质量和用户满意度分析</p>
        </div>
        <Button>
          <BarChart className="h-4 w-4 mr-2" />
          生成报告
        </Button>
      </div>

      {/* AI分析统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">对话质量</CardTitle>
            <MessageSquare className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">4.6/5</div>
            <p className="text-xs text-muted-foreground">用户评分</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">解决率</CardTitle>
            <ThumbsUp className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">89.3%</div>
            <p className="text-xs text-muted-foreground">问题解决率</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">平均轮次</CardTitle>
            <Clock className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">3.2</div>
            <p className="text-xs text-muted-foreground">对话轮次</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">用户留存</CardTitle>
            <Users className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">76.8%</div>
            <p className="text-xs text-muted-foreground">7日留存率</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="quality" className="space-y-4">
        <TabsList>
          <TabsTrigger value="quality">对话质量</TabsTrigger>
          <TabsTrigger value="satisfaction">用户满意度</TabsTrigger>
          <TabsTrigger value="topics">话题分析</TabsTrigger>
          <TabsTrigger value="improvement">改进建议</TabsTrigger>
        </TabsList>

        <TabsContent value="quality" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>对话质量评估</CardTitle>
                <CardDescription>基于多维度的质量评估</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">回答准确性</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={92} className="w-20" />
                    <span className="text-sm">92%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">回答完整性</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={88} className="w-20" />
                    <span className="text-sm">88%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">语言流畅度</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={95} className="w-20" />
                    <span className="text-sm">95%</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">相关性</span>
                  <div className="flex items-center space-x-2">
                    <Progress value={90} className="w-20" />
                    <span className="text-sm">90%</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>响应时间分析</CardTitle>
                <CardDescription>AI响应速度统计</CardDescription>
              </CardHeader>
              <CardContent className="h-[200px] flex items-center justify-center bg-muted/20 rounded-md">
                <Clock className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">响应时间分布图</span>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>对话流程分析</CardTitle>
              <CardDescription>用户对话路径和流程优化</CardDescription>
            </CardHeader>
            <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
              <TrendingUp className="h-8 w-8 text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">对话流程图表</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="satisfaction" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>满意度评分分布</CardTitle>
                <CardDescription>用户评分统计</CardDescription>
              </CardHeader>
              <CardContent className="h-[250px] flex items-center justify-center bg-muted/20 rounded-md">
                <PieChart className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">满意度分布图</span>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>用户反馈</CardTitle>
                <CardDescription>最新的用户评价</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="p-3 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">张三 - 零售商户</span>
                    <Badge className="bg-green-500">5星</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">AI回答很准确，帮我解决了产品培训的问题。</p>
                </div>
                <div className="p-3 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">李四 - 客户经理</span>
                    <Badge className="bg-blue-500">4星</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">系统操作指导很详细，但希望能更快一些。</p>
                </div>
                <div className="p-3 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">王五 - 零售商户</span>
                    <Badge className="bg-green-500">5星</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">政策解读很清楚，比人工客服还要专业。</p>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="topics" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>热门话题</CardTitle>
                <CardDescription>用户最关心的问题类型</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {[
                  { topic: "产品培训相关", count: 1245, percentage: 35 },
                  { topic: "系统操作指导", count: 892, percentage: 25 },
                  { topic: "政策解读", count: 678, percentage: 19 },
                  { topic: "技术支持", count: 456, percentage: 13 },
                  { topic: "其他问题", count: 289, percentage: 8 },
                ].map((item, index) => (
                  <div key={index} className="flex items-center justify-between">
                    <span className="text-sm">{item.topic}</span>
                    <div className="flex items-center space-x-2">
                      <Progress value={item.percentage} className="w-20" />
                      <span className="text-sm">{item.count}</span>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>话题趋势</CardTitle>
                <CardDescription>不同话题的时间趋势</CardDescription>
              </CardHeader>
              <CardContent className="h-[250px] flex items-center justify-center bg-muted/20 rounded-md">
                <TrendingUp className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">话题趋势图表</span>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>关键词云</CardTitle>
              <CardDescription>用户提问中的高频关键词</CardDescription>
            </CardHeader>
            <CardContent className="h-[200px] flex items-center justify-center bg-muted/20 rounded-md">
              <span className="text-muted-foreground">关键词云图</span>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="improvement" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>AI优化建议</CardTitle>
              <CardDescription>基于数据分析的改进建议</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="p-4 border rounded-lg border-orange-200 bg-orange-50">
                <div className="flex items-center space-x-2 mb-2">
                  <AlertCircle className="h-4 w-4 text-orange-500" />
                  <span className="font-medium text-orange-800">响应速度优化</span>
                </div>
                <p className="text-sm text-orange-700">
                  建议优化知识库检索算法，当前平均响应时间1.2秒，目标优化到0.8秒以下。
                </p>
              </div>

              <div className="p-4 border rounded-lg border-blue-200 bg-blue-50">
                <div className="flex items-center space-x-2 mb-2">
                  <AlertCircle className="h-4 w-4 text-blue-500" />
                  <span className="font-medium text-blue-800">知识库扩充</span>
                </div>
                <p className="text-sm text-blue-700">技术支持类问题的解决率较低(73%)，建议增加相关技术文档和FAQ。</p>
              </div>

              <div className="p-4 border rounded-lg border-green-200 bg-green-50">
                <div className="flex items-center space-x-2 mb-2">
                  <AlertCircle className="h-4 w-4 text-green-500" />
                  <span className="font-medium text-green-800">对话引导优化</span>
                </div>
                <p className="text-sm text-green-700">
                  建议增加主动引导功能，帮助用户更准确地描述问题，提高首次解决率。
                </p>
              </div>

              <div className="p-4 border rounded-lg border-purple-200 bg-purple-50">
                <div className="flex items-center space-x-2 mb-2">
                  <AlertCircle className="h-4 w-4 text-purple-500" />
                  <span className="font-medium text-purple-800">多轮对话优化</span>
                </div>
                <p className="text-sm text-purple-700">当前平均对话轮次3.2轮，建议优化上下文理解能力，减少重复询问。</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>优化进度跟踪</CardTitle>
              <CardDescription>改进措施的实施进度</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm">知识库更新</span>
                <div className="flex items-center space-x-2">
                  <Progress value={75} className="w-32" />
                  <span className="text-sm">75%</span>
                </div>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm">算法优化</span>
                <div className="flex items-center space-x-2">
                  <Progress value={60} className="w-32" />
                  <span className="text-sm">60%</span>
                </div>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm">界面改进</span>
                <div className="flex items-center space-x-2">
                  <Progress value={90} className="w-32" />
                  <span className="text-sm">90%</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
