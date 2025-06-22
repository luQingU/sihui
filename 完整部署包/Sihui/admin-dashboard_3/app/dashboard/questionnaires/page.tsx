"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Badge } from "@/components/ui/badge"
import { Search, Plus, BarChart, FileText, Eye, Pencil, Trash2 } from "lucide-react"
import Link from "next/link"

// 模拟问卷数据
const mockQuestionnaires = [
  {
    id: 1,
    title: "客户满意度调查",
    status: "已发布",
    responses: 128,
    createdAt: "2025-04-10",
    endDate: "2025-05-10",
  },
  {
    id: 2,
    title: "产品功能反馈",
    status: "已发布",
    responses: 85,
    createdAt: "2025-04-05",
    endDate: "2025-05-05",
  },
  {
    id: 3,
    title: "培训效果评估",
    status: "草稿",
    responses: 0,
    createdAt: "2025-04-15",
    endDate: null,
  },
  {
    id: 4,
    title: "用户体验调研",
    status: "已结束",
    responses: 203,
    createdAt: "2025-03-01",
    endDate: "2025-04-01",
  },
  {
    id: 5,
    title: "新功能需求收集",
    status: "已发布",
    responses: 42,
    createdAt: "2025-04-12",
    endDate: "2025-05-12",
  },
]

export default function QuestionnairesPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [selectedQuestionnaire, setSelectedQuestionnaire] = useState<any>(null)

  // 过滤问卷
  const filteredQuestionnaires = mockQuestionnaires.filter((questionnaire) =>
    questionnaire.title.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const handleDeleteQuestionnaire = () => {
    // 实际项目中，这里应该调用删除问卷的 API
    console.log("删除问卷:", selectedQuestionnaire?.id)
    setIsDeleteDialogOpen(false)
  }

  const openDeleteDialog = (questionnaire: any) => {
    setSelectedQuestionnaire(questionnaire)
    setIsDeleteDialogOpen(true)
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">问卷管理</h1>
        <Button asChild>
          <Link href="/dashboard/questionnaires/create">
            <Plus className="h-4 w-4 mr-2" />
            创建问卷
          </Link>
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总问卷数</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockQuestionnaires.length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">已发布</CardTitle>
            <Badge>活跃</Badge>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockQuestionnaires.filter((q) => q.status === "已发布").length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总回复数</CardTitle>
            <Eye className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockQuestionnaires.reduce((sum, q) => sum + q.responses, 0)}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">平均回复率</CardTitle>
            <BarChart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {Math.round(
                (mockQuestionnaires.reduce((sum, q) => sum + q.responses, 0) /
                  (mockQuestionnaires.filter((q) => q.status !== "草稿").length * 100)) *
                  100,
              )}
              %
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="搜索问卷..."
            className="pl-8"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select defaultValue="all">
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="筛选状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">所有状态</SelectItem>
            <SelectItem value="published">已发布</SelectItem>
            <SelectItem value="draft">草稿</SelectItem>
            <SelectItem value="ended">已结束</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>标题</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>回复数</TableHead>
              <TableHead>创建日期</TableHead>
              <TableHead>截止日期</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredQuestionnaires.length > 0 ? (
              filteredQuestionnaires.map((questionnaire) => (
                <TableRow key={questionnaire.id}>
                  <TableCell>{questionnaire.id}</TableCell>
                  <TableCell>{questionnaire.title}</TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        questionnaire.status === "已发布"
                          ? "default"
                          : questionnaire.status === "草稿"
                            ? "outline"
                            : "secondary"
                      }
                    >
                      {questionnaire.status}
                    </Badge>
                  </TableCell>
                  <TableCell>{questionnaire.responses}</TableCell>
                  <TableCell>{questionnaire.createdAt}</TableCell>
                  <TableCell>{questionnaire.endDate || "未设置"}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon" asChild>
                      <Link href={`/dashboard/questionnaires/results?id=${questionnaire.id}`}>
                        <BarChart className="h-4 w-4" />
                      </Link>
                    </Button>
                    <Button variant="ghost" size="icon" asChild>
                      <Link href={`/dashboard/questionnaires/edit?id=${questionnaire.id}`}>
                        <Pencil className="h-4 w-4" />
                      </Link>
                    </Button>
                    <Button variant="ghost" size="icon" onClick={() => openDeleteDialog(questionnaire)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-4">
                  没有找到匹配的问卷
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 删除问卷确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              您确定要删除问卷 "{selectedQuestionnaire?.title}" 吗？此操作无法撤销。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              取消
            </Button>
            <Button variant="destructive" onClick={handleDeleteQuestionnaire}>
              删除
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
