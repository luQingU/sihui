import { api } from '../api'
import type { 
  User, 
  CreateUserRequest, 
  UpdateUserRequest,
  PaginationParams,
  PageResponse,
  Role,
  Permission
} from '../../types/api'

// 用户管理服务
export const userService = {
  // 获取用户列表（分页）
  async getUsers(params: PaginationParams & {
    keyword?: string
    status?: string
    role?: string
  }): Promise<PageResponse<User>> {
    return api.get<PageResponse<User>>('/api/users', {
      page: params.page,
      size: params.size,
      sort: params.sort,
      keyword: params.keyword,
      status: params.status,
      role: params.role
    })
  },

  // 获取指定用户信息
  async getUser(id: number): Promise<User> {
    return api.get<User>(`/api/users/${id}`)
  },

  // 创建用户
  async createUser(data: CreateUserRequest): Promise<User> {
    return api.post<User>('/api/users', data)
  },

  // 更新用户信息
  async updateUser(id: number, data: UpdateUserRequest): Promise<User> {
    return api.put<User>(`/api/users/${id}`, data)
  },

  // 删除用户
  async deleteUser(id: number): Promise<void> {
    return api.delete(`/api/users/${id}`)
  },

  // 批量删除用户
  async batchDeleteUsers(ids: number[]): Promise<void> {
    return api.delete('/api/users/batch', ids)
  },

  // 更新用户状态
  async updateUserStatus(id: number, status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'): Promise<void> {
    return api.patch(`/api/users/${id}/status`, null, { status })
  },

  // 搜索用户
  async searchUsers(params: {
    keyword: string
    page?: number
    size?: number
  }): Promise<PageResponse<User>> {
    return api.get<PageResponse<User>>('/api/users/search', {
      keyword: params.keyword,
      page: params.page || 0,
      size: params.size || 10
    })
  },

  // 按状态获取用户
  async getUsersByStatus(status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED', params?: PaginationParams): Promise<PageResponse<User>> {
    return api.get<PageResponse<User>>(`/api/users/status/${status}`, params)
  },

  // 检查用户名是否存在
  async checkUsername(username: string): Promise<{ exists: boolean }> {
    return api.get<{ exists: boolean }>('/api/users/check-username', { username })
  },

  // 检查邮箱是否存在
  async checkEmail(email: string): Promise<{ exists: boolean }> {
    return api.get<{ exists: boolean }>('/api/users/check-email', { email })
  },

  // 检查手机号是否存在
  async checkPhone(phone: string): Promise<{ exists: boolean }> {
    return api.get<{ exists: boolean }>('/api/users/check-phone', { phone })
  },

  // 为用户分配角色
  async assignRoles(userId: number, roleIds: number[]): Promise<void> {
    return api.post(`/api/users/${userId}/roles`, { roleIds })
  },

  // 移除用户角色
  async removeRoles(userId: number, roleIds: number[]): Promise<void> {
    return api.delete(`/api/users/${userId}/roles`, { roleIds })
  },

  // 获取用户的角色
  async getUserRoles(userId: number): Promise<Role[]> {
    return api.get<Role[]>(`/api/users/${userId}/roles`)
  },

  // 获取用户的权限
  async getUserPermissions(userId: number): Promise<Permission[]> {
    return api.get<Permission[]>(`/api/users/${userId}/permissions`)
  }
}

// 角色管理服务
export const roleService = {
  // 获取所有角色
  async getRoles(): Promise<Role[]> {
    return api.get<Role[]>('/api/roles')
  },

  // 获取角色详情
  async getRole(id: number): Promise<Role> {
    return api.get<Role>(`/api/roles/${id}`)
  },

  // 创建角色
  async createRole(data: Omit<Role, 'id'>): Promise<Role> {
    return api.post<Role>('/api/roles', data)
  },

  // 更新角色
  async updateRole(id: number, data: Partial<Omit<Role, 'id'>>): Promise<Role> {
    return api.put<Role>(`/api/roles/${id}`, data)
  },

  // 删除角色
  async deleteRole(id: number): Promise<void> {
    return api.delete(`/api/roles/${id}`)
  },

  // 为角色分配权限
  async assignPermissions(roleId: number, permissionIds: number[]): Promise<void> {
    return api.post(`/api/roles/${roleId}/permissions`, { permissionIds })
  },

  // 移除角色权限
  async removePermissions(roleId: number, permissionIds: number[]): Promise<void> {
    return api.delete(`/api/roles/${roleId}/permissions`, { permissionIds })
  }
}

// 权限管理服务
export const permissionService = {
  // 获取所有权限
  async getPermissions(): Promise<Permission[]> {
    return api.get<Permission[]>('/api/permissions')
  },

  // 获取权限详情
  async getPermission(id: number): Promise<Permission> {
    return api.get<Permission>(`/api/permissions/${id}`)
  },

  // 创建权限
  async createPermission(data: Omit<Permission, 'id'>): Promise<Permission> {
    return api.post<Permission>('/api/permissions', data)
  },

  // 更新权限
  async updatePermission(id: number, data: Partial<Omit<Permission, 'id'>>): Promise<Permission> {
    return api.put<Permission>(`/api/permissions/${id}`, data)
  },

  // 删除权限
  async deletePermission(id: number): Promise<void> {
    return api.delete(`/api/permissions/${id}`)
  },

  // 检查用户权限
  async checkUserPermission(userId: number, permission: string): Promise<{ hasPermission: boolean }> {
    return api.get<{ hasPermission: boolean }>(`/api/permissions/check`, {
      userId,
      permission
    })
  }
} 