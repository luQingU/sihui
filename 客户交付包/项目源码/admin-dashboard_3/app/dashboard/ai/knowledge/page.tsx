import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { FileText, Search, Upload, TrendingUp, Zap, BookOpen } from "lucide-react"

export default function KnowledgePage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">知识库管理</h1>
          <p className="text-muted-foreground">四会知识库 - TF-IDF智能检索系统</p>
        </div>
        <Button>
          <Upload className="h-4 w-4 mr-2" />
          上传文档
        </Button>
      </div>

      {/* 知识库统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">文档总数</CardTitle>
            <FileText className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">1,456</div>
            <p className="text-xs text-muted-foreground">已向量化: 1,398</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">检索准确率</CardTitle>
            <Zap className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">92.8%</div>
            <p className="text-xs text-muted-foreground">TF-IDF算法</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">今日查询</CardTitle>
            <Search className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">2,847</div>
            <p className="text-xs text-muted-foreground">平均响应: 0.3s</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">知识覆盖</CardTitle>
            <BookOpen className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">95.6%</div>
            <p className="text-xs text-muted-foreground">四会业务覆盖率</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="documents" className="space-y-4">
        <TabsList>
          <TabsTrigger value="documents">文档管理</TabsTrigger>
          <TabsTrigger value="search">智能检索</TabsTrigger>
          <TabsTrigger value="analytics">分析统计</TabsTrigger>
        </TabsList>

        <TabsContent value="documents" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>知识文档列表</CardTitle>
              <CardDescription>管理四会培训相关的知识文档</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2 mb-4">
                <div className="relative flex-1 max-w-sm">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input type="search" placeholder="搜索文档..." className="pl-8" />
                </div>
              </div>

              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>文档标题</TableHead>
                      <TableHead>分类</TableHead>
                      <TableHead>关键词</TableHead>
                      <TableHead>向量化状态</TableHead>
                      <TableHead>上传时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow>
                      <TableCell className="font-medium">四会市零售商户管理办法</TableCell>
                      <TableCell>
                        <Badge variant="outline">政策文件</Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1">
                          <Badge variant="secondary" className="text-xs">
                            零售商户
                          </Badge>
                          <Badge variant="secondary" className="text-xs">
                            管理办法
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-green-500">已完成</Badge>
                      </TableCell>
                      <TableCell>2025-01-15</TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          查看
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell className="font-medium">产品培训手册 - 2025版</TableCell>
                      <TableCell>
                        <Badge variant="outline">培训资料</Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1">
                          <Badge variant="secondary" className="text-xs">
                            产品培训
                          </Badge>
                          <Badge variant="secondary" className="text-xs">
                            操作指南
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-green-500">已完成</Badge>
                      </TableCell>
                      <TableCell>2025-01-12</TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">
                          查看
                        </Button>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="search" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>智能检索测试</CardTitle>
              <CardDescription>测试TF-IDF算法的检索效果</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex space-x-2">
                <Input placeholder="输入查询内容..." className="flex-1" />
                <Button>
                  <Search className="h-4 w-4 mr-2" />
                  检索
                </Button>
              </div>

              <div className="space-y-4">
                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">四会市零售商户管理办法</h4>
                    <Badge>相关度: 95%</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground mb-2">
                    本办法适用于四会市范围内的零售商户管理，包括准入条件、经营规范、监督管理等内容...
                  </p>
                  <div className="flex space-x-2">
                    <Badge variant="secondary" className="text-xs">
                      零售商户
                    </Badge>
                    <Badge variant="secondary" className="text-xs">
                      管理办法
                    </Badge>
                  </div>
                </div>

                <div className="p-4 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium">商户培训指导手册</h4>
                    <Badge>相关度: 87%</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground mb-2">
                    为帮助零售商户更好地理解和执行相关政策，特制定本培训指导手册...
                  </p>
                  <div className="flex space-x-2">
                    <Badge variant="secondary" className="text-xs">
                      培训指导
                    </Badge>
                    <Badge variant="secondary" className="text-xs">
                      商户管理
                    </Badge>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>检索统计</CardTitle>
                <CardDescription>知识库使用情况分析</CardDescription>
              </CardHeader>
              <CardContent className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
                <TrendingUp className="h-8 w-8 text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">检索统计图表</span>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>热门文档</CardTitle>
                <CardDescription>最常被检索的文档</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">零售商户管理办法</span>
                  <Badge>1,245次</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">产品培训手册</span>
                  <Badge>892次</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">系统操作指南</span>
                  <Badge>678次</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">常见问题解答</span>
                  <Badge>456次</Badge>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
