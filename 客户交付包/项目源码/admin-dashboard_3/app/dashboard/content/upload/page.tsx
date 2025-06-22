"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Upload,
  FileText,
  Video,
  ImageIcon,
  File,
  CheckCircle,
  AlertCircle,
  Clock,
  Trash2,
  FolderPlus,
} from "lucide-react"

// 模拟上传文件数据
const mockUploadFiles = [
  {
    id: 1,
    name: "产品培训视频.mp4",
    type: "video",
    size: "125.6 MB",
    progress: 100,
    status: "completed",
    category: "培训视频",
    tags: ["产品", "培训", "基础"],
  },
  {
    id: 2,
    name: "销售手册.pdf",
    type: "document",
    size: "2.3 MB",
    progress: 75,
    status: "uploading",
    category: "文档资料",
    tags: ["销售", "手册"],
  },
  {
    id: 3,
    name: "产品图片.jpg",
    type: "image",
    size: "1.8 MB",
    progress: 0,
    status: "pending",
    category: "图片素材",
    tags: ["产品", "图片"],
  },
]

export default function UploadPage() {
  const [uploadFiles, setUploadFiles] = useState(mockUploadFiles)
  const [selectedCategory, setSelectedCategory] = useState("")
  const [tags, setTags] = useState("")
  const [folder, setFolder] = useState("")

  const getFileIcon = (type: string) => {
    switch (type) {
      case "video":
        return <Video className="h-5 w-5 text-blue-500" />
      case "document":
        return <FileText className="h-5 w-5 text-green-500" />
      case "image":
        return <ImageIcon className="h-5 w-5 text-purple-500" />
      default:
        return <File className="h-5 w-5 text-gray-500" />
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed":
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case "uploading":
        return <Clock className="h-4 w-4 text-blue-500" />
      case "error":
        return <AlertCircle className="h-4 w-4 text-red-500" />
      default:
        return <Clock className="h-4 w-4 text-gray-500" />
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "completed":
        return <Badge className="bg-green-500">已完成</Badge>
      case "uploading":
        return <Badge className="bg-blue-500">上传中</Badge>
      case "error":
        return <Badge variant="destructive">失败</Badge>
      default:
        return <Badge variant="outline">等待中</Badge>
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">批量上传</h1>
          <p className="text-muted-foreground">上传培训视频、文档和图片到阿里云OSS</p>
        </div>
        <Button>
          <FolderPlus className="h-4 w-4 mr-2" />
          新建文件夹
        </Button>
      </div>

      {/* 上传统计 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总文件数</CardTitle>
            <Upload className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{uploadFiles.length}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">已完成</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {uploadFiles.filter((f) => f.status === "completed").length}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">上传中</CardTitle>
            <Clock className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {uploadFiles.filter((f) => f.status === "uploading").length}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总大小</CardTitle>
            <File className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">129.7 MB</div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="upload" className="space-y-4">
        <TabsList>
          <TabsTrigger value="upload">文件上传</TabsTrigger>
          <TabsTrigger value="queue">上传队列</TabsTrigger>
          <TabsTrigger value="settings">上传设置</TabsTrigger>
        </TabsList>

        <TabsContent value="upload" className="space-y-4">
          <div className="grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>选择文件</CardTitle>
                <CardDescription>支持视频、文档、图片等多种格式</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="border-2 border-dashed border-muted-foreground/25 rounded-lg p-8 text-center">
                  <Upload className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                  <div className="text-lg font-medium mb-2">拖拽文件到此处</div>
                  <div className="text-sm text-muted-foreground mb-4">或者点击选择文件</div>
                  <Button>选择文件</Button>
                </div>

                <div className="text-sm text-muted-foreground">
                  <p>支持的文件类型：</p>
                  <ul className="list-disc list-inside mt-1 space-y-1">
                    <li>视频：MP4, AVI, MOV, WMV (最大 500MB)</li>
                    <li>文档：PDF, DOC, DOCX, PPT, PPTX (最大 50MB)</li>
                    <li>图片：JPG, PNG, GIF, BMP (最大 10MB)</li>
                  </ul>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>文件信息</CardTitle>
                <CardDescription>设置文件的分类和标签</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="category">文件分类</Label>
                  <Select value={selectedCategory} onValueChange={setSelectedCategory}>
                    <SelectTrigger>
                      <SelectValue placeholder="选择分类" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="training-video">培训视频</SelectItem>
                      <SelectItem value="document">文档资料</SelectItem>
                      <SelectItem value="image">图片素材</SelectItem>
                      <SelectItem value="other">其他</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="folder">存储文件夹</Label>
                  <Input
                    id="folder"
                    placeholder="例如：training/2025/products"
                    value={folder}
                    onChange={(e) => setFolder(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="tags">标签</Label>
                  <Input
                    id="tags"
                    placeholder="用逗号分隔，例如：产品,培训,基础"
                    value={tags}
                    onChange={(e) => setTags(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">描述</Label>
                  <Textarea id="description" placeholder="文件描述（可选）" rows={3} />
                </div>

                <Button className="w-full">
                  <Upload className="h-4 w-4 mr-2" />
                  开始上传
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="queue" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>上传队列</CardTitle>
              <CardDescription>查看当前上传任务的进度</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {uploadFiles.map((file) => (
                  <div key={file.id} className="border rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center space-x-3">
                        {getFileIcon(file.type)}
                        <div>
                          <div className="font-medium">{file.name}</div>
                          <div className="text-sm text-muted-foreground">{file.size}</div>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        {getStatusIcon(file.status)}
                        {getStatusBadge(file.status)}
                        <Button variant="ghost" size="icon">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>

                    {file.status === "uploading" && (
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span>上传进度</span>
                          <span>{file.progress}%</span>
                        </div>
                        <Progress value={file.progress} />
                      </div>
                    )}

                    <div className="mt-2 flex flex-wrap gap-1">
                      <Badge variant="outline">{file.category}</Badge>
                      {file.tags.map((tag, index) => (
                        <Badge key={index} variant="secondary" className="text-xs">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="settings" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>上传配置</CardTitle>
                <CardDescription>设置上传的默认参数</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>默认存储桶</Label>
                  <Input value="sihui-training-platform" disabled />
                </div>
                <div className="space-y-2">
                  <Label>CDN加速</Label>
                  <div className="flex items-center space-x-2">
                    <input type="checkbox" defaultChecked />
                    <span className="text-sm">启用CDN加速</span>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label>访问权限</Label>
                  <Select defaultValue="private">
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="private">私有</SelectItem>
                      <SelectItem value="public">公开</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>阿里云OSS状态</CardTitle>
                <CardDescription>云存储服务连接状态</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm">连接状态</span>
                  <Badge className="bg-green-500">正常</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">存储用量</span>
                  <span className="text-sm">2.3 GB / 100 GB</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">本月流量</span>
                  <span className="text-sm">156 GB / 1 TB</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">API调用次数</span>
                  <span className="text-sm">12,456 / 100,000</span>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
