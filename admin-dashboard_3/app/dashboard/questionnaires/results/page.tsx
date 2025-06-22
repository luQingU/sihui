"use client"

import { useState } from "react"
import { useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { BarChart, PieChart, Download, ArrowLeft, FileText } from "lucide-react"
import Link from "next/link"

// 模拟问卷数据
const mockQuestionnaire = {
  id: 1,
  title: "客户满意度调查",
  description: "了解客户对我们产品和服务的满意度",
  totalResponses: 128,
  questions: [
    {
      id: 1,
      title: "您对我们的产品总体满意度如何？",
      type: "single",
      options: ["非常满意", "满意", "一般", "不满意", "非常不满意"],
      results: [45, 62, 15, 5, 1],
    },
    {
      id: 2,
      title: "您最常使用我们产品的哪些功能？（多选）",
      type: "multiple",
      options: ["功能A", "功能B", "功能C", "功能D", "功能E"],
      results: [98, 75, 62, 45, 30],
    },
    {
      id: 3,
      title: "您对我们的客户服务满意度如何？",
      type: "single",
      options: ["非常满意", "满意", "一般", "不满意", "非常不满意"],
      results: [52, 58, 12, 4, 2],
    },
    {
      id: 4,
      title: "您对产品有什么建议？",
      type: "text",
      responses: [
        "希望能增加更多自定义功能",
        "界面可以更简洁一些",
        "希望提供更多模板",
        "希望能有更详细的使用教程",
        "希望能支持更多第三方集成",
      ],
    },
  ],
}

// 模拟回复数据
const mockResponses = Array.from({ length: 20 }).map((_, i) => ({
  id: i + 1,
  respondent: `用户${i + 1}`,
  date: new Date(Date.now() - Math.floor(Math.random() * 10000000000)).toLocaleDateString(),
  completed: Math.random() > 0.1,
}))

export default function QuestionnaireResultsPage() {
  const searchParams = useSearchParams()
  const questionnaireId = searchParams.get("id") || "1"
  const [activeTab, setActiveTab] = useState("summary")

  return (
    <div className="space-y-6">
      <div className="flex items-center">
        <Button variant="ghost" size="icon" asChild className="mr-2">
          <Link href="/dashboard/questionnaires">
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{mockQuestionnaire.title}</h1>
          <p className="text-muted-foreground">{mockQuestionnaire.description}</p>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总回复数</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockQuestionnaire.totalResponses}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">完成率</CardTitle>
            <PieChart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {Math.round((mockResponses.filter((r) => r.completed).length / mockResponses.length) * 100)}%
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">平均完成时间</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">3分42秒</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">最近回复</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">今天</div>
          </CardContent>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="summary">结果摘要</TabsTrigger>
          <TabsTrigger value="details">详细数据</TabsTrigger>
          <TabsTrigger value="responses">回复列表</TabsTrigger>
        </TabsList>

        <TabsContent value="summary" className="space-y-4">
          {mockQuestionnaire.questions.map((question) => (
            <Card key={question.id}>
              <CardHeader>
                <CardTitle className="text-lg">{question.title}</CardTitle>
                <CardDescription>
                  {question.type === "single" || question.type === "multiple"
                    ? `${question.options.length}个选项，共${
                        question.type === "single"
                          ? question.results.reduce((a, b) => a + b, 0)
                          : mockQuestionnaire.totalResponses
                      }个回答`
                    : `${question.responses?.length || 0}个文本回答`}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {(question.type === "single" || question.type === "multiple") && (
                  <div className="space-y-4">
                    <div className="h-[200px] flex items-center justify-center bg-muted/20 rounded-md">
                      {question.type === "single" ? (
                        <PieChart className="h-8 w-8 text-muted-foreground" />
                      ) : (
                        <BarChart className="h-8 w-8 text-muted-foreground" />
                      )}
                      <span className="ml-2 text-muted-foreground">图表展示区域</span>
                    </div>
                    <div className="space-y-2">
                      {question.options.map((option, index) => (
                        <div key={index} className="grid grid-cols-6 gap-2 items-center">
                          <div className="col-span-2">{option}</div>
                          <div className="col-span-3 h-2 bg-muted rounded-full overflow-hidden">
                            <div
                              className="h-full bg-primary"
                              style={{
                                width: `${
                                  (question.results[index] /
                                    (question.type === "single"
                                      ? question.results.reduce((a, b) => a + b, 0)
                                      : mockQuestionnaire.totalResponses)) *
                                  100
                                }%`,
                              }}
                            ></div>
                          </div>
                          <div className="text-right">
                            {question.results[index]} (
                            {Math.round(
                              (question.results[index] /
                                (question.type === "single"
                                  ? question.results.reduce((a, b) => a + b, 0)
                                  : mockQuestionnaire.totalResponses)) *
                                100,
                            )}
                            %)
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {question.type === "text" && (
                  <div className="space-y-2">
                    {question.responses?.map((response, index) => (
                      <Card key={index}>
                        <CardContent className="p-4">
                          <p className="text-sm">{response}</p>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </TabsContent>

        <TabsContent value="details" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>详细数据</CardTitle>
              <CardDescription>查看问卷的详细统计数据</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[400px] flex items-center justify-center bg-muted/20 rounded-md">
                <BarChart className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">详细数据图表</span>
              </div>
              <div className="flex justify-end mt-4">
                <Button variant="outline">
                  <Download className="h-4 w-4 mr-2" />
                  导出数据
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="responses" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>回复列表</CardTitle>
              <CardDescription>查看所有问卷回复</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>回复者</TableHead>
                      <TableHead>提交日期</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mockResponses.map((response) => (
                      <TableRow key={response.id}>
                        <TableCell>{response.id}</TableCell>
                        <TableCell>{response.respondent}</TableCell>
                        <TableCell>{response.date}</TableCell>
                        <TableCell>
                          <span
                            className={`px-2 py-1 rounded-full text-xs ${
                              response.completed ? "bg-green-100 text-green-800" : "bg-yellow-100 text-yellow-800"
                            }`}
                          >
                            {response.completed ? "已完成" : "未完成"}
                          </span>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button variant="ghost" size="sm">
                            查看详情
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
      </Tabs>
    </div>
  )
}
