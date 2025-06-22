import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { FileText, Search, Upload, Download } from "lucide-react"
import Link from "next/link"

// 模拟文档数据
const mockDocuments = [
  {
    id: 1,
    title: "销售手册 - 2025版",
    type: "PDF",
    uploadDate: "2025-04-10",
    views: 189,
    size: "2.5MB",
  },
  {
    id: 2,
    title: "产品规格说明书",
    type: "DOCX",
    uploadDate: "2025-03-28",
    views: 156,
    size: "1.8MB",
  },
  {
    id: 3,
    title: "常见问题解答",
    type: "PDF",
    uploadDate: "2025-03-15",
    views: 278,
    size: "3.2MB",
  },
  {
    id: 4,
    title: "市场分析报告",
    type: "PPTX",
    uploadDate: "2025-03-05",
    views: 145,
    size: "5.7MB",
  },
  {
    id: 5,
    title: "用户手册",
    type: "PDF",
    uploadDate: "2025-02-20",
    views: 312,
    size: "4.1MB",
  },
]

export default function DocumentsPage() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">文档管理</h1>
        <Button>
          <Upload className="h-4 w-4 mr-2" />
          上传文档
        </Button>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="搜索文档..." className="pl-8" />
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>文档名称</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>大小</TableHead>
              <TableHead>上传日期</TableHead>
              <TableHead>查看次数</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {mockDocuments.map((doc) => (
              <TableRow key={doc.id}>
                <TableCell className="font-medium">
                  <div className="flex items-center">
                    <FileText className="h-4 w-4 mr-2 text-muted-foreground" />
                    {doc.title}
                  </div>
                </TableCell>
                <TableCell>{doc.type}</TableCell>
                <TableCell>{doc.size}</TableCell>
                <TableCell>{doc.uploadDate}</TableCell>
                <TableCell>{doc.views}</TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="sm" asChild className="mr-2">
                    <Link href={`/dashboard/content/documents/${doc.id}`}>查看</Link>
                  </Button>
                  <Button variant="outline" size="sm">
                    <Download className="h-4 w-4 mr-1" />
                    下载
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}
