"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import { Search, Plus, Shield, Users, Settings, Eye, Pencil, Trash2 } from "lucide-react"

// 模拟角色数据
const mockRoles = [
  {
    id: 1,
    name: "超级管理员",
    description: "拥有系统所有权限",
    userCount: 2,
    permissions: 50,
    status: "active",
    createdAt: "2025-01-01",
  },
  {
    id: 2,
    name: "管理员",
    description: "拥有大部分管理权限",
    userCount: 5,
    permissions: 35,
    status: "active",
    createdAt: "2025-01-01",
  },
  {
    id: 3,
    name: "客户经理",
    description: "负责客户管理和培训",
    userCount: 25,
    permissions: 20,
    status: "active",
    createdAt: "2025-01-01",
  },
  {
    id: 4,
    name: "零售商户",
    description: "普通用户权限",
    userCount: 1216,
    permissions: 8,
    status: "active",
    createdAt: "2025-01-01",
  },
]

// 模拟权限数据
const mockPermissions = [
  { id: "USER_CREATE", name: "创建用户", category: "用户管理" },
  { id: "USER_READ", name: "查看用户", category: "用户管理" },
  { id: "USER_UPDATE", name: "编辑用户", category: "用户管理" },
  { id: "USER_DELETE", name: "删除用户", category: "用户管理" },
  { id: "CONTENT_CREATE", name: "创建内容", category: "内容管理" },
  { id: "CONTENT_READ", name: "查看内容", category: "内容管理" },
  { id: "CONTENT_UPDATE", name: "编辑内容", category: "内容管理" },
  { id: "CONTENT_DELETE", name: "删除内容", category: "内容管理" },
  { id: "POLL_CREATE", name: "创建问卷", category: "问卷管理" },
  { id: "POLL_READ", name: "查看问卷", category: "问卷管理" },
  { id: "POLL_UPDATE", name: "编辑问卷", category: "问卷管理" },
  { id: "POLL_DELETE", name: "删除问卷", category: "问卷管理" },
  { id: "AI_CHAT", name: "AI对话", category: "AI功能" },
  { id: "AI_KNOWLEDGE", name: "知识库管理", category: "AI功能" },
  { id: "SYSTEM_MONITOR", name: "系统监控", category: "系统管理" },
  { id: "SYSTEM_CONFIG", name: "系统配置", category: "系统管理" },
]

export default function RolesPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [selectedPermissions, setSelectedPermissions] = useState<string[]>([])

  const filteredRoles = mockRoles.filter((role) => role.name.toLowerCase().includes(searchTerm.toLowerCase()))

  const permissionsByCategory = mockPermissions.reduce(
    (acc, permission) => {
      if (!acc[permission.category]) {
        acc[permission.category] = []
      }
      acc[permission.category].push(permission)
      return acc
    },
    {} as Record<string, typeof mockPermissions>,
  )

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">角色权限管理</h1>
          <p className="text-muted-foreground">管理系统角色和权限分配</p>
        </div>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              创建角色
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>创建新角色</DialogTitle>
              <DialogDescription>设置角色名称、描述和权限</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="role-name">角色名称</Label>
                  <Input id="role-name" placeholder="输入角色名称" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="role-status">状态</Label>
                  <Input id="role-status" placeholder="激活" />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="role-description">角色描述</Label>
                <Input id="role-description" placeholder="输入角色描述" />
              </div>
              <div className="space-y-4">
                <Label>权限设置</Label>
                <div className="max-h-60 overflow-y-auto border rounded-md p-4">
                  {Object.entries(permissionsByCategory).map(([category, permissions]) => (
                    <div key={category} className="mb-4">
                      <h4 className="font-medium mb-2">{category}</h4>
                      <div className="grid grid-cols-2 gap-2">
                        {permissions.map((permission) => (
                          <div key={permission.id} className="flex items-center space-x-2">
                            <Checkbox
                              id={permission.id}
                              checked={selectedPermissions.includes(permission.id)}
                              onCheckedChange={(checked) => {
                                if (checked) {
                                  setSelectedPermissions([...selectedPermissions, permission.id])
                                } else {
                                  setSelectedPermissions(selectedPermissions.filter((p) => p !== permission.id))
                                }
                              }}
                            />
                            <Label htmlFor={permission.id} className="text-sm">
                              {permission.name}
                            </Label>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsCreateDialogOpen(false)}>
                取消
              </Button>
              <Button onClick={() => setIsCreateDialogOpen(false)}>创建角色</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* 权限统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总角色数</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockRoles.length}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">权限点数</CardTitle>
            <Settings className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">50+</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">分配用户</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockRoles.reduce((sum, role) => sum + role.userCount, 0)}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">活跃角色</CardTitle>
            <Eye className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockRoles.filter((role) => role.status === "active").length}</div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="roles" className="space-y-4">
        <TabsList>
          <TabsTrigger value="roles">角色管理</TabsTrigger>
          <TabsTrigger value="permissions">权限详情</TabsTrigger>
        </TabsList>

        <TabsContent value="roles" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>角色列表</CardTitle>
              <CardDescription>管理系统中的所有角色</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2 mb-4">
                <div className="relative flex-1 max-w-sm">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="搜索角色..."
                    className="pl-8"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
              </div>

              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>角色名称</TableHead>
                      <TableHead>描述</TableHead>
                      <TableHead>用户数量</TableHead>
                      <TableHead>权限数量</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>创建时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredRoles.map((role) => (
                      <TableRow key={role.id}>
                        <TableCell className="font-medium">{role.name}</TableCell>
                        <TableCell>{role.description}</TableCell>
                        <TableCell>{role.userCount}</TableCell>
                        <TableCell>{role.permissions}</TableCell>
                        <TableCell>
                          <Badge variant={role.status === "active" ? "default" : "secondary"}>
                            {role.status === "active" ? "激活" : "禁用"}
                          </Badge>
                        </TableCell>
                        <TableCell>{role.createdAt}</TableCell>
                        <TableCell className="text-right">
                          <Button variant="ghost" size="icon">
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button variant="ghost" size="icon">
                            <Trash2 className="h-4 w-4" />
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

        <TabsContent value="permissions" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>权限详情</CardTitle>
              <CardDescription>系统中所有可用的权限点</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {Object.entries(permissionsByCategory).map(([category, permissions]) => (
                  <div key={category}>
                    <h3 className="text-lg font-semibold mb-3">{category}</h3>
                    <div className="grid gap-2 md:grid-cols-2 lg:grid-cols-3">
                      {permissions.map((permission) => (
                        <div key={permission.id} className="p-3 border rounded-lg">
                          <div className="font-medium">{permission.name}</div>
                          <div className="text-sm text-muted-foreground font-mono">{permission.id}</div>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
