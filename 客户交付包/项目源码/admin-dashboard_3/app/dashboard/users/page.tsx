"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Label } from "@/components/ui/label"
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import { Search, Plus, Pencil, Trash2 } from "lucide-react"
import { userService, roleService } from "@/lib/services"
import { withErrorHandling } from "@/lib/error-handler"
import type { User, Role, CreateUserRequest, UpdateUserRequest } from "@/types/api"

// 类型安全的事件处理
type ChangeEvent = React.ChangeEvent<HTMLInputElement>
type SelectValueChange = (value: string) => void

export default function UsersPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [currentPage, setCurrentPage] = useState(1)
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [users, setUsers] = useState<User[]>([])
  const [roles, setRoles] = useState<Role[]>([])
  const [totalUsers, setTotalUsers] = useState(0)
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [roleFilter, setRoleFilter] = useState<string>("all")
  const [formData, setFormData] = useState<CreateUserRequest>({
    username: "",
    email: "",
    phone: "",
    password: "",
    realName: "",
    avatarUrl: "",
    roleIds: [],
  })

  const itemsPerPage = 10
  const totalPages = Math.ceil(totalUsers / itemsPerPage)

  // 获取用户列表
  const fetchUsers = async () => {
    const result = await withErrorHandling(
      () => userService.getUsers({
        page: currentPage - 1, // API 使用 0-based 分页
        size: itemsPerPage,
        keyword: searchTerm || undefined,
        status: statusFilter === 'all' ? undefined : statusFilter,
        role: roleFilter === 'all' ? undefined : roleFilter,
      }),
      { loadingKey: 'fetchUsers' }
    )
    
    if (result) {
      setUsers(result.content)
      setTotalUsers(result.totalElements)
    }
    setLoading(false)
  }

  // 获取角色列表
  const fetchRoles = async () => {
    const result = await withErrorHandling(
      () => roleService.getRoles(),
      { loadingKey: 'fetchRoles' }
    )
    
    if (result) {
      setRoles(result)
    }
  }

  // 初始化数据
  useEffect(() => {
    fetchRoles()
  }, [])

  // 当搜索条件或分页改变时重新获取数据
  useEffect(() => {
    fetchUsers()
  }, [currentPage, searchTerm, statusFilter, roleFilter])

  const handleAddUser = async () => {
    const result = await withErrorHandling(
      () => userService.createUser(formData),
      { loadingKey: 'createUser' }
    )
    
    if (result) {
      setIsAddDialogOpen(false)
      resetFormData()
      fetchUsers() // 刷新用户列表
    }
  }

  const handleEditUser = async () => {
    if (!selectedUser) return
    
    const updateData: UpdateUserRequest = {
      email: formData.email,
      phone: formData.phone,
      realName: formData.realName,
      avatarUrl: formData.avatarUrl,
      roleIds: formData.roleIds,
    }
    
    const result = await withErrorHandling(
      () => userService.updateUser(selectedUser.id, updateData),
      { loadingKey: 'updateUser' }
    )
    
    if (result) {
      setIsEditDialogOpen(false)
      fetchUsers()
    }
  }

  const handleDeleteUser = async () => {
    if (!selectedUser) return
    
    const result = await withErrorHandling(
      () => userService.deleteUser(selectedUser.id),
      { loadingKey: 'deleteUser' }
    )
    
    if (result !== null) {
      setIsDeleteDialogOpen(false)
      fetchUsers()
    }
  }

  const openEditDialog = (user: User) => {
    setSelectedUser(user)
    setFormData({
      username: user.username,
      email: user.email,
      phone: user.phone || "",
      password: "", // 编辑时密码留空
      realName: user.realName || "",
      avatarUrl: user.avatarUrl || "",
      roleIds: user.roles.map(role => role.id),
    })
    setIsEditDialogOpen(true)
  }

  const openDeleteDialog = (user: User) => {
    setSelectedUser(user)
    setIsDeleteDialogOpen(true)
  }

  const resetFormData = () => {
    setFormData({
      username: "",
      email: "",
      phone: "",
      password: "",
      realName: "",
      avatarUrl: "",
      roleIds: [],
    })
  }

  const handleSearch = (e: ChangeEvent) => {
    setSearchTerm(e.target.value)
    setCurrentPage(1) // 搜索时重置到第一页
  }

  const handleStatusFilterChange: SelectValueChange = (value: string) => {
    setStatusFilter(value)
    setCurrentPage(1)
  }

  const handleRoleFilterChange: SelectValueChange = (value: string) => {
    setRoleFilter(value)
    setCurrentPage(1)
  }

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return "bg-green-100 text-green-800"
      case 'INACTIVE':
        return "bg-gray-100 text-gray-800"
      case 'SUSPENDED':
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const getStatusDisplayName = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return "正常"
      case 'INACTIVE':
        return "未激活"
      case 'SUSPENDED':
        return "已禁用"
      default:
        return status
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">用户管理</h1>
        <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              添加用户
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>添加新用户</DialogTitle>
              <DialogDescription>请填写新用户的信息，所有字段都是必填的。</DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="realName" className="text-right">
                  真实姓名
                </Label>
                <Input
                  id="realName"
                  value={formData.realName}
                  onChange={(e) => setFormData({ ...formData, realName: e.target.value })}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="username" className="text-right">
                  用户名 *
                </Label>
                <Input
                  id="username"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="email" className="text-right">
                  邮箱 *
                </Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="phone" className="text-right">
                  手机号
                </Label>
                <Input
                  id="phone"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="password" className="text-right">
                  密码 *
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="roles" className="text-right">
                  角色 *
                </Label>
                <Select 
                  value={formData.roleIds.length > 0 ? formData.roleIds[0].toString() : ""} 
                  onValueChange={(value) => setFormData({ ...formData, roleIds: [parseInt(value)] })}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="选择角色" />
                  </SelectTrigger>
                  <SelectContent>
                    {roles.map((role) => (
                      <SelectItem key={role.id} value={role.id.toString()}>
                        {role.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>
                取消
              </Button>
              <Button onClick={handleAddUser}>保存</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="搜索用户..."
            className="pl-8"
            value={searchTerm}
            onChange={handleSearch}
          />
        </div>
        <Select value={roleFilter} onValueChange={handleRoleFilterChange}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="筛选角色" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">所有角色</SelectItem>
            {roles.map((role) => (
              <SelectItem key={role.id} value={role.name}>
                {role.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={statusFilter} onValueChange={handleStatusFilterChange}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="筛选状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">所有状态</SelectItem>
            <SelectItem value="ACTIVE">正常</SelectItem>
            <SelectItem value="INACTIVE">未激活</SelectItem>
            <SelectItem value="SUSPENDED">已禁用</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>真实姓名</TableHead>
              <TableHead>用户名</TableHead>
              <TableHead>邮箱</TableHead>
              <TableHead>角色</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-4">
                  加载中...
                </TableCell>
              </TableRow>
            ) : users.length > 0 ? (
              users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.realName || '-'}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>
                    {user.roles.map(role => role.name).join(', ') || '-'}
                  </TableCell>
                  <TableCell>
                    <span
                      className={`px-2 py-1 rounded-full text-xs ${getStatusBadgeClass(user.status)}`}
                    >
                      {getStatusDisplayName(user.status)}
                    </span>
                  </TableCell>
                  <TableCell>{new Date(user.createdAt).toLocaleDateString()}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon" onClick={() => openEditDialog(user)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="icon" onClick={() => openDeleteDialog(user)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-4">
                  没有找到匹配的用户
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationPrevious
              href="#"
              onClick={(e) => {
                e.preventDefault()
                if (currentPage > 1) {
                  setCurrentPage(Math.max(1, currentPage - 1))
                }
              }}
              className={currentPage === 1 ? "pointer-events-none opacity-50" : ""}
            />
          </PaginationItem>
          {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
            let pageNumber
            if (totalPages <= 5) {
              pageNumber = i + 1
            } else if (currentPage <= 3) {
              pageNumber = i + 1
            } else if (currentPage >= totalPages - 2) {
              pageNumber = totalPages - 4 + i
            } else {
              pageNumber = currentPage - 2 + i
            }

            return (
              <PaginationItem key={i}>
                <PaginationLink isActive={pageNumber === currentPage} onClick={() => setCurrentPage(pageNumber)}>
                  {pageNumber}
                </PaginationLink>
              </PaginationItem>
            )
          })}
          {totalPages > 5 && currentPage < totalPages - 2 && (
            <PaginationItem>
              <PaginationEllipsis />
            </PaginationItem>
          )}
          <PaginationItem>
            <PaginationNext
              href="#"
              onClick={(e) => {
                e.preventDefault()
                if (currentPage < totalPages) {
                  setCurrentPage(Math.min(totalPages, currentPage + 1))
                }
              }}
              className={currentPage === totalPages ? "pointer-events-none opacity-50" : ""}
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>

      {/* 编辑用户对话框 */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>编辑用户</DialogTitle>
            <DialogDescription>修改用户信息，密码留空表示不修改。</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-realName" className="text-right">
                真实姓名
              </Label>
              <Input
                id="edit-realName"
                value={formData.realName}
                onChange={(e) => setFormData({ ...formData, realName: e.target.value })}
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-username" className="text-right">
                用户名
              </Label>
              <Input
                id="edit-username"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                className="col-span-3"
                disabled
                title="用户名不可修改"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-email" className="text-right">
                邮箱
              </Label>
              <Input
                id="edit-email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-phone" className="text-right">
                手机号
              </Label>
              <Input
                id="edit-phone"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="edit-roles" className="text-right">
                角色
              </Label>
              <Select 
                value={formData.roleIds.length > 0 ? formData.roleIds[0].toString() : ""} 
                onValueChange={(value) => setFormData({ ...formData, roleIds: [parseInt(value)] })}
              >
                <SelectTrigger className="col-span-3">
                  <SelectValue placeholder="选择角色" />
                </SelectTrigger>
                <SelectContent>
                  {roles.map((role) => (
                    <SelectItem key={role.id} value={role.id.toString()}>
                      {role.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              取消
            </Button>
            <Button onClick={handleEditUser}>保存</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 删除用户确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              您确定要删除用户 "{selectedUser?.realName || selectedUser?.username}" 吗？此操作无法撤销。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              取消
            </Button>
            <Button variant="destructive" onClick={handleDeleteUser}>
              删除
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
