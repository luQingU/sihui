import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Video, Search, Upload, Eye, Clock, Calendar } from "lucide-react"
import Link from "next/link"

// 模拟视频数据
const mockVideos = [
  {
    id: 1,
    title: "产品培训视频 - 基础功能介绍",
    duration: "15:30",
    uploadDate: "2025-04-15",
    views: 245,
    size: "128MB",
  },
  {
    id: 2,
    title: "客户沟通技巧培训",
    duration: "22:45",
    uploadDate: "2025-04-05",
    views: 312,
    size: "215MB",
  },
  {
    id: 3,
    title: "系统操作指南 - 管理员版",
    duration: "18:20",
    uploadDate: "2025-03-20",
    views: 203,
    size: "156MB",
  },
  {
    id: 4,
    title: "销售策略培训 - 2025版",
    duration: "25:10",
    uploadDate: "2025-03-15",
    views: 178,
    size: "230MB",
  },
  {
    id: 5,
    title: "新产品功能介绍",
    duration: "12:40",
    uploadDate: "2025-03-10",
    views: 156,
    size: "110MB",
  },
]

export default function VideosPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">视频管理</h1>
        <Button>
          <Upload className="h-4 w-4 mr-2" />
          上传视频
        </Button>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="搜索视频..." className="pl-8" />
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {mockVideos.map((video) => (
          <Card key={video.id}>
            <CardHeader className="pb-2">
              <div className="flex items-center space-x-2">
                <Video className="h-4 w-4 text-muted-foreground" />
                <CardTitle className="text-sm font-medium">{video.title}</CardTitle>
              </div>
              <CardDescription className="text-xs">上传于 {video.uploadDate}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-32 bg-muted rounded-md flex items-center justify-center mb-4">
                <Video className="h-8 w-8 text-muted-foreground" />
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="flex items-center">
                  <Clock className="h-3 w-3 mr-1 text-muted-foreground" />
                  <span>{video.duration}</span>
                </div>
                <div className="flex items-center">
                  <Eye className="h-3 w-3 mr-1 text-muted-foreground" />
                  <span>{video.views} 次观看</span>
                </div>
                <div className="flex items-center">
                  <Calendar className="h-3 w-3 mr-1 text-muted-foreground" />
                  <span>{video.uploadDate}</span>
                </div>
                <div className="flex items-center">
                  <span>{video.size}</span>
                </div>
              </div>
              <div className="mt-4">
                <Button variant="outline" size="sm" className="w-full" asChild>
                  <Link href={`/dashboard/content/${video.id}`}>查看详情</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
