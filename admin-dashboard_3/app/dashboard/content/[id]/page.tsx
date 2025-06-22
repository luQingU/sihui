"use client"

import { useParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ArrowLeft, Download, Share, Edit, Trash2, Eye } from "lucide-react"
import Link from "next/link"

export default function ContentDetailPage() {
  const params = useParams()
  const contentId = params.id

  // 模拟内容数据
  const content = {
    id: contentId,
    title: `内容标题 #${contentId}`,
    type: Number(contentId) % 2 === 0 ? "document" : "video",
    description: "这是一段详细的内容描述，介绍了该内容的主要信息和用途。",
    uploadDate: "2025-04-15",
    views: 245,
    size: Number(contentId) % 2 === 0 ? "2.5MB" : "128MB",
    duration: "15:30",
    author: "管理员",
    tags: ["培训", "产品", "基础知识"],
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <Button variant="ghost" size="icon" asChild className="mr-2">
            <Link href={content.type === "video" ? "/dashboard/content/videos" : "/dashboard/content/documents"}>
              <ArrowLeft className="h-4 w-4" />
            </Link>
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">{content.title}</h1>
            <p className="text-muted-foreground">
              {content.type === "video" ? "视频" : "文档"} • 上传于 {content.uploadDate}
            </p>
          </div>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" size="sm">
            <Download className="h-4 w-4 mr-2" />
            下载
          </Button>
          <Button variant="outline" size="sm">
            <Share className="h-4 w-4 mr-2" />
            分享
          </Button>
          <Button variant="outline" size="sm">
            <Edit className="h-4 w-4 mr-2" />
            编辑
          </Button>
          <Button variant="destructive" size="sm">
            <Trash2 className="h-4 w-4 mr-2" />
            删除
          </Button>
        </div>
      </div>

      <Tabs defaultValue="preview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="preview">预览</TabsTrigger>
          <TabsTrigger value="details">详细信息</TabsTrigger>
          <TabsTrigger value="analytics">访问统计</TabsTrigger>
        </TabsList>

        <TabsContent value="preview" className="space-y-4">
          <Card>
            <CardContent className="p-6">
              {content.type === "video" ? (
                <div className="aspect-video bg-muted rounded-md flex items-center justify-center">
                  <div className="text-center">
                    <Eye className="h-12 w-12 mx-auto text-muted-foreground" />
                    <p className="mt-2 text-muted-foreground">视频预览</p>
                  </div>
                </div>
              ) : (
                <div className="aspect-[3/4] max-h-[600px] bg-muted rounded-md flex items-center justify-center">
                  <div className="text-center">
                    <Eye className="h-12 w-12 mx-auto text-muted-foreground" />
                    <p className="mt-2 text-muted-foreground">文档预览</p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="details" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>内容详情</CardTitle>
              <CardDescription>查看内容的详细信息</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">标题</h3>
                  <p>{content.title}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">类型</h3>
                  <p>{content.type === "video" ? "视频" : "文档"}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">上传日期</h3>
                  <p>{content.uploadDate}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">大小</h3>
                  <p>{content.size}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">查看次数</h3>
                  <p>{content.views} 次</p>
                </div>
                {content.type === "video" && (
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">时长</h3>
                    <p>{content.duration}</p>
                  </div>
                )}
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">上传者</h3>
                  <p>{content.author}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">标签</h3>
                  <div className="flex flex-wrap gap-2 mt-1">
                    {content.tags.map((tag, index) => (
                      <span key={index} className="px-2 py-1 bg-muted rounded-md text-xs">
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-sm font-medium text-muted-foreground">描述</h3>
                <p className="mt-1">{content.description}</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>访问统计</CardTitle>
              <CardDescription>查看内容的访问数据</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex items-center justify-center bg-muted/20 rounded-md">
                <span className="text-muted-foreground">访问统计图表</span>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
