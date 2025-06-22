# PC端管理平台 API 文档

## 概述
本文档描述了四会培训管理平台PC端管理界面所使用的API接口。PC端主要为管理员提供系统管理、用户管理、培训内容管理、问卷管理、数据分析等功能。

**Base URL**: `http://localhost:8080`

## 认证管理

### 用户登录
- **接口**: `POST /api/auth/login`
- **描述**: 管理员用户通过用户名或邮箱进行登录
- **请求体**:
```json
{
  "usernameOrEmail": "string", // 用户名或邮箱 (3-50字符)
  "password": "string"         // 密码 (6-100字符)
}
```
- **响应**: 
  - `200`: 登录成功，返回JWT token
  - `400`: 请求参数错误
  - `401`: 用户名或密码错误

### 刷新访问令牌
- **接口**: `POST /api/auth/refresh`
- **描述**: 使用刷新令牌获取新的访问令牌
- **请求体**:
```json
{
  "refreshToken": "string"
}
```

### 用户登出
- **接口**: `POST /api/auth/logout`
- **描述**: 用户登出（客户端应清除本地令牌）

## 用户管理

### 获取用户列表
- **接口**: `GET /api/users`
- **描述**: 分页获取所有用户列表
- **参数**: 
  - `pageable`: 分页参数 (必填)
    - `page`: 页码 (≥0)
    - `size`: 每页大小 (≥1)
    - `sort`: 排序字段数组

### 创建用户
- **接口**: `POST /api/users`
- **描述**: 创建新用户
- **请求体**:
```json
{
  "username": "string",      // 用户名 (3-50字符，字母数字下划线)
  "email": "string",         // 邮箱 (必填，最大100字符)
  "phone": "string",         // 手机号 (1[3-9]开头的11位数字)
  "password": "string",      // 密码 (必填，6-50字符)
  "realName": "string",      // 真实姓名 (最大100字符)
  "avatarUrl": "string",     // 头像URL (最大500字符)
  "roleIds": [1, 2, 3]       // 角色ID数组
}
```

### 获取指定用户信息
- **接口**: `GET /api/users/{id}`
- **描述**: 根据用户ID获取用户详细信息
- **路径参数**: `id` (用户ID)

### 更新用户信息
- **接口**: `PUT /api/users/{id}`
- **描述**: 更新指定用户信息
- **路径参数**: `id` (用户ID)
- **请求体**:
```json
{
  "email": "string",
  "phone": "string",
  "realName": "string",
  "avatarUrl": "string",
  "status": "ACTIVE|INACTIVE|SUSPENDED",
  "emailVerified": boolean,
  "phoneVerified": boolean,
  "roleIds": [1, 2, 3]
}
```

### 删除用户
- **接口**: `DELETE /api/users/{id}`
- **描述**: 删除指定用户

### 批量删除用户
- **接口**: `DELETE /api/users/batch`
- **描述**: 批量删除用户
- **请求体**: `[1, 2, 3]` (用户ID数组)

### 更新用户状态
- **接口**: `PATCH /api/users/{id}/status`
- **描述**: 更新用户状态
- **参数**: 
  - `id`: 用户ID
  - `status`: 状态 (`ACTIVE|INACTIVE|SUSPENDED`)

### 搜索用户
- **接口**: `GET /api/users/search`
- **描述**: 根据关键词搜索用户
- **参数**:
  - `keyword`: 搜索关键词
  - `pageable`: 分页参数

### 按状态获取用户
- **接口**: `GET /api/users/status/{status}`
- **描述**: 获取指定状态的用户列表
- **路径参数**: `status` (`ACTIVE|INACTIVE|SUSPENDED`)

### 用户信息检查
- **接口**: `GET /api/users/check-username` - 检查用户名是否存在
- **接口**: `GET /api/users/check-email` - 检查邮箱是否存在
- **接口**: `GET /api/users/check-phone` - 检查手机号是否存在

### 用户角色管理
- **接口**: `POST /api/users/{userId}/roles` - 为用户分配角色
- **接口**: `DELETE /api/users/{userId}/roles` - 移除用户角色

## 培训内容管理

### 上传单个文件
- **接口**: `POST /api/contents/upload`
- **描述**: 上传单个培训内容文件到OSS
- **请求类型**: `multipart/form-data`
- **参数**:
  - `file`: 要上传的文件 (必填)
  - `category`: 文件分类
  - `description`: 文件描述
  - `folder`: 文件夹路径
  - `isPublic`: 是否公开访问 (默认false)
  - `tags`: 标签（逗号分隔）

### 批量上传文件
- **接口**: `POST /api/contents/upload/batch`
- **描述**: 批量上传培训内容文件到OSS
- **参数**: 同单个文件上传，但`files`为文件数组

### 删除文件
- **接口**: `DELETE /api/contents/{fileName}`
- **描述**: 从OSS删除指定的文件
- **路径参数**: `fileName` (文件名，包含路径)

### 检查文件是否存在
- **接口**: `GET /api/contents/exists/{fileName}`
- **描述**: 检查OSS中是否存在指定文件

### 获取文件签名URL
- **接口**: `GET /api/contents/signed-url/{fileName}`
- **描述**: 获取文件的临时访问URL
- **参数**:
  - `fileName`: 文件名（包含路径）
  - `expiredInSeconds`: 过期时间（秒），默认3600秒

## 知识文档管理

### 上传知识文档
- **接口**: `POST /api/knowledge/documents/upload`
- **描述**: 上传Markdown或TXT格式的四会知识文档
- **请求类型**: `multipart/form-data`
- **参数**:
  - `file`: 要上传的文档文件 (必填)
  - `title`: 文档标题
  - `category`: 文档分类 (默认"四会文档")
  - `keywords`: 关键词（逗号分隔）
  - `isPublic`: 是否公开访问 (默认false)

### 获取文档详情
- **接口**: `GET /api/knowledge/documents/{id}`
- **描述**: 根据ID获取知识文档的详细信息

### 智能搜索文档
- **接口**: `GET /api/knowledge/documents/search`
- **描述**: 使用TF-IDF算法进行智能文档检索
- **参数**:
  - `keyword`: 搜索关键词 (必填)
  - `limit`: 返回结果数量限制 (默认20)

### 获取相似文档
- **接口**: `GET /api/knowledge/documents/{id}/similar`
- **描述**: 根据文档ID获取相似的文档
- **参数**:
  - `id`: 文档ID
  - `limit`: 返回结果数量限制 (默认10)

## 问卷管理

### 创建问卷
- **接口**: `POST /api/questionnaires`
- **描述**: 创建新问卷
- **请求体**:
```json
{
  "request": {
    "title": "string",           // 问卷标题 (必填，最大200字符)
    "description": "string",     // 问卷描述 (最大1000字符)
    "type": "string",           // 问卷类型 (必填)
    "version": "string",        // 版本号 (最大20字符)
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-12-31T23:59:59",
    "settings": {
      "isAnonymous": boolean,
      "allowMultipleSubmissions": boolean,
      "themeColor": "string",
      "layout": "string",
      "requirePassword": boolean,
      "accessPassword": "string"
    },
    "questions": [
      {
        "title": "string",       // 题目标题 (必填)
        "description": "string", // 题目描述
        "type": "string",        // 题目类型 (必填)
        "required": boolean,     // 是否必填
        "sortOrder": number,     // 排序
        "placeholder": "string", // 占位符
        "ratingStyle": "string", // 评分样式
        "fileTypes": ["string"], // 文件类型
        "validationRules": {},   // 验证规则
        "options": [             // 选项
          {
            "text": "string",    // 选项文本 (必填)
            "image": "string",   // 选项图片
            "value": "string",   // 选项值
            "sortOrder": number  // 排序
          }
        ]
      }
    ]
  },
  "currentUser": {
    // 当前用户信息
  }
}
```

### 获取问卷详情
- **接口**: `GET /api/questionnaires/{id}`
- **描述**: 获取指定问卷的详细信息

### 发布问卷
- **接口**: `POST /api/questionnaires/{id}/publish`
- **描述**: 发布问卷

### 验证问卷访问权限
- **接口**: `POST /api/questionnaires/{id}/validate-access`
- **描述**: 验证用户是否有权限访问问卷

### 获取问卷统计
- **接口**: `GET /api/questionnaires/{id}/stats`
- **描述**: 获取问卷统计信息
- **参数**: `currentUser` (当前用户信息)

### 获取用户问卷列表
- **接口**: `GET /api/questionnaires/my`
- **描述**: 获取当前用户创建的问卷列表
- **参数**: 
  - `currentUser`: 当前用户信息
  - `pageable`: 分页参数

## 问卷分析

### 获取问卷统计
- **接口**: `GET /api/questionnaires/{questionnaireId}/analysis/stats`
- **描述**: 获取问卷详细统计分析

### 获取回答时间分布
- **接口**: `GET /api/questionnaires/{questionnaireId}/analysis/time-distribution`
- **描述**: 获取问卷回答时间分布统计

### 导出分析报告
- **接口**: `GET /api/questionnaires/{questionnaireId}/analysis/export/csv` - 导出CSV格式
- **接口**: `GET /api/questionnaires/{questionnaireId}/analysis/export/pdf` - 导出PDF格式

## 问卷报告

### 获取报告模板
- **接口**: `GET /api/questionnaires/{questionnaireId}/reports/templates`
- **描述**: 获取可用的报告模板列表

### 生成汇总报告
- **接口**: `GET /api/questionnaires/{questionnaireId}/reports/summary`
- **描述**: 生成问卷汇总报告

### 预览报告
- **接口**: `GET /api/questionnaires/{questionnaireId}/reports/preview`
- **描述**: 预览报告内容
- **参数**: `template` (模板类型，默认"summary")

### 生成报告文件
- **接口**: `GET /api/questionnaires/{questionnaireId}/reports/html` - 生成HTML报告
- **接口**: `GET /api/questionnaires/{questionnaireId}/reports/pdf` - 生成PDF报告
- **接口**: `GET /api/questionnaires/{questionnaireId}/reports/excel` - 生成Excel报告

### 生成对比报告
- **接口**: `POST /api/questionnaires/{questionnaireId}/reports/comparison`
- **描述**: 生成多个问卷的对比报告
- **请求体**:
```json
{
  "questionnaireIds": [1, 2, 3],
  "currentUser": {}
}
```

## 系统监控

### 系统健康检查
- **接口**: `GET /api/monitoring/health`
- **描述**: 检查系统各组件的健康状态

### 获取系统指标
- **接口**: `GET /api/monitoring/metrics`
- **描述**: 获取系统运行时指标

### JWT性能统计
- **接口**: `GET /api/monitoring/jwt/performance` - 获取JWT性能统计
- **接口**: `POST /api/monitoring/jwt/performance/reset` - 重置JWT性能统计
- **接口**: `POST /api/monitoring/jwt/performance/report` - 生成性能报告

## 性能监控

### 性能概览
- **接口**: `GET /api/performance/overview`
- **描述**: 获取性能概览信息

### 性能统计
- **接口**: `GET /api/performance/stats` - 获取性能统计
- **接口**: `DELETE /api/performance/stats` - 清除性能统计

### 系统资源
- **接口**: `GET /api/performance/resources`
- **描述**: 获取系统资源使用情况

### 性能健康检查
- **接口**: `GET /api/performance/health`
- **描述**: 获取性能健康状态

### 慢查询日志
- **接口**: `GET /api/performance/slow-queries`
- **描述**: 获取慢查询日志

## 性能测试

### 系统健康检查
- **接口**: `GET /api/performance/test-health`
- **描述**: 系统健康检查

### 获取性能指标
- **接口**: `GET /api/performance/metrics`
- **描述**: 获取系统性能指标

### 负载测试
- **接口**: `POST /api/performance/load-test/cpu` - CPU负载测试
- **接口**: `POST /api/performance/load-test/memory` - 内存负载测试
- **接口**: `POST /api/performance/load-test/database` - 数据库连接池测试
- **接口**: `POST /api/performance/load-test/cache` - 缓存性能测试

### 缓存管理
- **接口**: `POST /api/performance/cache/warmup` - 预热缓存
- **接口**: `DELETE /api/performance/cache/clear` - 清理所有缓存

## 性能优化

### 性能分析
- **接口**: `GET /api/performance/analysis/api` - API性能分析
- **接口**: `GET /api/performance/analysis/query` - 查询性能分析
- **接口**: `GET /api/performance/analysis/memory` - 内存使用分析
- **接口**: `GET /api/performance/analysis/caching` - 缓存分析

### 性能审查
- **接口**: `GET /api/performance/review/best-practices` - 最佳实践审查
- **接口**: `GET /api/performance/review/database-indexes` - 数据库索引审查
- **接口**: `POST /api/performance/review/comprehensive` - 综合性能审查

### 性能优化
- **接口**: `POST /api/performance/optimization/user-queries` - 优化用户查询
- **接口**: `POST /api/performance/benchmark` - 执行基准测试
- **接口**: `POST /api/performance/report/optimization` - 生成优化报告

## 安全测试

### 安全测试
- **接口**: `GET /api/security/test/api` - API安全测试
- **接口**: `GET /api/security/test/jwt` - JWT安全测试
- **接口**: `GET /api/security/test/xss` - XSS防护测试
- **接口**: `GET /api/security/test/csrf` - CSRF防护测试
- **接口**: `GET /api/security/test/sql-injection` - SQL注入防护测试
- **接口**: `GET /api/security/test/session` - 会话管理测试
- **接口**: `GET /api/security/test/password` - 密码加密测试
- **接口**: `GET /api/security/test/access-control` - 访问控制测试
- **接口**: `GET /api/security/test/data-protection` - 敏感数据保护测试

### 安全评估
- **接口**: `POST /api/security/assessment` - 执行漏洞评估
- **接口**: `POST /api/security/full-test` - 执行完整安全测试
- **接口**: `GET /api/security/report` - 生成安全报告

## 数据加密管理

### 加密状态查询
- **接口**: `GET /api/admin/encryption/status` - 获取加密状态
- **接口**: `GET /api/admin/encryption/config-summary` - 获取配置摘要

### 加密功能测试
- **接口**: `POST /api/admin/encryption/test` - 测试加密功能
- **接口**: `POST /api/admin/encryption/test-hash` - 测试哈希功能

### 密钥管理
- **接口**: `POST /api/admin/encryption/rotate-keys` - 密钥轮换
- **接口**: `POST /api/admin/encryption/generate-field-key` - 生成字段密钥

## 多因素认证管理

### MFA配置
- **接口**: `GET /api/auth/mfa/config` - 获取MFA配置
- **接口**: `PUT /api/auth/mfa/config` - 更新MFA配置

### TOTP管理
- **接口**: `POST /api/auth/mfa/totp/generate` - 生成TOTP密钥
- **接口**: `POST /api/auth/mfa/totp/enable` - 启用TOTP
- **接口**: `POST /api/auth/mfa/totp/disable` - 禁用TOTP

### 验证码管理
- **接口**: `POST /api/auth/mfa/sms/send` - 发送短信验证码
- **接口**: `POST /api/auth/mfa/email/send` - 发送邮箱验证码
- **接口**: `POST /api/auth/mfa/verify` - 验证MFA代码

### 恢复代码
- **接口**: `POST /api/auth/mfa/recovery/generate` - 生成恢复代码

## 增强认证管理

### 会话管理
- **接口**: `GET /api/auth/enhanced/sessions` - 获取用户活跃会话
- **接口**: `GET /api/auth/enhanced/sessions/stats` - 获取会话统计
- **接口**: `DELETE /api/auth/enhanced/sessions/{sessionId}` - 注销指定会话
- **接口**: `DELETE /api/auth/enhanced/sessions/all` - 注销所有会话

### 管理员会话控制
- **接口**: `DELETE /api/auth/enhanced/admin/users/{userId}/sessions` - 管理员强制注销用户会话

## 数据库集成测试

### 数据库测试
- **接口**: `GET /api/integration/database/connection` - 测试数据库连接
- **接口**: `GET /api/integration/data/validate` - 验证数据一致性
- **接口**: `DELETE /api/integration/data/cleanup` - 清理测试数据

### 基础数据管理
- **接口**: `POST /api/integration/data/initialize` - 初始化基础数据

### 用户管理测试
- **接口**: `POST /api/integration/user/test-create` - 创建测试用户
- **接口**: `POST /api/integration/user-management/test` - 测试用户管理操作

### 权限系统测试
- **接口**: `POST /api/integration/rbac/test` - 测试角色权限系统
- **接口**: `GET /api/integration/permission/test/{userId}/{permission}` - 测试权限检查

### 完整测试
- **接口**: `POST /api/integration/complete-test` - 运行完整集成测试

## 通用响应格式

### 成功响应
```json
{
  "success": true,
  "data": {},
  "message": "操作成功",
  "code": "200"
}
```

### 错误响应
```json
{
  "success": false,
  "data": null,
  "message": "错误信息",
  "code": "错误代码"
}
```

## 权限说明

所有API接口都需要进行身份验证，请在请求头中携带JWT token：
```
Authorization: Bearer <your-jwt-token>
```

不同的API接口需要不同的权限级别，请确保当前用户具有相应的权限。 