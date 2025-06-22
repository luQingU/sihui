# 微信小程序端 API 文档

## 概述
本文档描述了四会培训管理平台微信小程序端所使用的API接口。小程序端主要为客户经理和零售商户提供培训内容学习、AI智能问答、问卷填写等功能。

**Base URL**: `http://localhost:8080`

## 认证管理

### 微信小程序登录
- **接口**: `POST /api/auth/enhanced/login`
- **描述**: 支持会话管理的用户登录（包括微信授权登录）
- **请求体**:
```json
{
  "usernameOrEmail": "string", // 微信授权码或用户名
  "password": "string"         // 密码或微信授权信息
}
```
- **响应**: 
  - `200`: 登录成功，返回JWT token和用户信息

### 刷新访问令牌
- **接口**: `POST /api/auth/enhanced/refresh`
- **描述**: 使用刷新令牌获取新的访问令牌
- **请求体**:
```json
{
  "refreshToken": "string"
}
```

### 用户登出
- **接口**: `POST /api/auth/enhanced/logout`
- **描述**: 注销用户当前会话

### 获取用户会话信息
- **接口**: `GET /api/auth/enhanced/sessions`
- **描述**: 获取当前用户所有活跃会话列表

### 注销指定会话
- **接口**: `DELETE /api/auth/enhanced/sessions/{sessionId}`
- **描述**: 注销用户指定的会话
- **路径参数**: `sessionId` (会话ID)

### 注销所有会话
- **接口**: `DELETE /api/auth/enhanced/sessions/all`
- **描述**: 强制注销用户所有会话

## 培训内容访问

### 获取文件签名URL
- **接口**: `GET /api/contents/signed-url/{fileName}`
- **描述**: 获取培训文件的临时访问URL，用于视频播放或文档查看
- **路径参数**: `fileName` (文件名，包含路径)
- **查询参数**:
  - `expiredInSeconds`: 过期时间（秒），默认3600秒（1小时）
- **用途**: 小程序端播放视频或查看文档时获取临时URL

### 检查文件是否存在
- **接口**: `GET /api/contents/exists/{fileName}`
- **描述**: 检查OSS中是否存在指定的培训文件
- **路径参数**: `fileName` (文件名，包含路径)
- **响应**: `boolean` - 文件是否存在

## AI智能问答

### 基础聊天
- **接口**: `POST /api/ai/chat`
- **描述**: 向AI发送单条消息并获取回复
- **查询参数**:
  - `message`: 用户消息 (必填)
- **用途**: 简单的一次性问答

### 对话聊天
- **接口**: `POST /api/ai/chat/conversation`
- **描述**: 向AI发送消息并包含对话历史
- **请求体**:
```json
{
  "message": "string",           // 当前用户消息
  "history": [                   // 对话历史
    {
      "role": "user",
      "content": "string"
    },
    {
      "role": "assistant", 
      "content": "string"
    }
  ]
}
```
- **用途**: 支持上下文的连续对话

### 智能记忆聊天
- **接口**: `POST /api/ai/chat/memory`
- **描述**: 自动管理对话历史的智能聊天
- **请求体**:
```json
{
  "userId": 123,                 // 用户ID
  "message": "string",           // 用户消息
  "sessionId": "string"          // 会话ID（可选，用于区分不同对话）
}
```
- **用途**: 系统自动记忆对话历史，无需客户端管理

### 知识增强智能聊天
- **接口**: `POST /api/ai/chat/knowledge`
- **描述**: 基于四会文档知识库的智能问答
- **请求体**:
```json
{
  "userId": 123,                 // 用户ID
  "message": "string",           // 用户消息
  "sessionId": "string"          // 会话ID（可选）
}
```
- **用途**: 基于上传的四会文档进行专业问答

## 知识文档检索

### 智能搜索文档
- **接口**: `GET /api/knowledge/documents/search`
- **描述**: 使用TF-IDF算法进行智能文档检索
- **查询参数**:
  - `keyword`: 搜索关键词 (必填)
  - `limit`: 返回结果数量限制 (默认20)
- **用途**: 用户主动搜索相关培训文档

### 获取文档详情
- **接口**: `GET /api/knowledge/documents/{id}`
- **描述**: 根据ID获取知识文档的详细信息
- **路径参数**: `id` (文档ID)
- **用途**: 查看具体文档内容

### 获取相似文档
- **接口**: `GET /api/knowledge/documents/{id}/similar`
- **描述**: 根据文档ID获取相似的文档
- **路径参数**: `id` (文档ID)
- **查询参数**:
  - `limit`: 返回结果数量限制 (默认10)
- **用途**: 推荐相关文档

## 问卷参与

### 获取可用问卷列表
- **接口**: `GET /api/questionnaires/available`
- **描述**: 获取当前用户可以参与的问卷列表
- **查询参数**:
  - `currentUser`: 当前用户信息 (必填)

### 获取公开问卷列表
- **接口**: `GET /api/public/questionnaires`
- **描述**: 获取所有公开的问卷列表
- **查询参数**:
  - `currentUser`: 当前用户信息 (必填)

### 获取问卷详情
- **接口**: `GET /api/questionnaires/{id}`
- **描述**: 获取指定问卷的详细信息
- **路径参数**: `id` (问卷ID)

### 获取公开问卷详情
- **接口**: `GET /api/public/questionnaires/{id}`
- **描述**: 获取指定公开问卷的详细信息
- **路径参数**: `id` (问卷ID)

### 验证问卷访问权限
- **接口**: `POST /api/questionnaires/{id}/validate-access`
- **描述**: 验证用户是否有权限访问问卷
- **路径参数**: `id` (问卷ID)

### 验证公开问卷访问权限
- **接口**: `POST /api/public/questionnaires/{id}/validate-access`
- **描述**: 验证用户是否有权限访问公开问卷
- **路径参数**: `id` (问卷ID)

### 提交问卷回答
- **接口**: `POST /api/questionnaires/submit`
- **描述**: 提交问卷答案
- **请求体**:
```json
{
  "request": {
    "questionnaireId": 123,      // 问卷ID
    "accessPassword": "string",  // 访问密码（如果问卷设置了密码）
    "answers": [                 // 答案数组
      {
        "questionId": 456,       // 题目ID
        "textAnswer": "string",  // 文本答案
        "numberAnswer": "string", // 数字答案
        "dateAnswer": "string",  // 日期答案
        "selectedOptions": [1, 2, 3], // 选中的选项ID数组
        "fileUrl": "string"      // 文件URL
      }
    ]
  },
  "currentUser": {
    // 当前用户信息
  }
}
```

### 提交公开问卷回答
- **接口**: `POST /api/public/questionnaires/{id}/submit`
- **描述**: 提交公开问卷答案
- **路径参数**: `id` (问卷ID)
- **请求体**: 同上，但路径参数中已包含问卷ID

### 获取问卷完成状态
- **接口**: `GET /api/questionnaires/{id}/completion-status`
- **描述**: 检查用户是否已完成指定问卷
- **路径参数**: `id` (问卷ID)
- **查询参数**:
  - `currentUser`: 当前用户信息 (必填)

### 获取公开问卷完成状态
- **接口**: `GET /api/public/questionnaires/{id}/completion-status`
- **描述**: 检查用户是否已完成指定公开问卷
- **路径参数**: `id` (问卷ID)
- **查询参数**:
  - `currentUser`: 当前用户信息 (必填)

## 多因素认证（可选功能）

### 获取MFA配置
- **接口**: `GET /api/auth/mfa/config`
- **描述**: 获取当前用户的多因素认证配置

### 更新MFA配置
- **接口**: `PUT /api/auth/mfa/config`
- **描述**: 更新用户的多因素认证配置
- **请求体**:
```json
{
  "smsEnabled": boolean,         // 是否启用短信验证
  "emailEnabled": boolean,       // 是否启用邮箱验证
  "phoneNumber": "string",       // 手机号
  "email": "string",            // 邮箱
  "requireMfaForLogin": boolean, // 登录时是否需要MFA
  "requireMfaForSensitiveOperations": boolean // 敏感操作时是否需要MFA
}
```

### 生成TOTP密钥
- **接口**: `POST /api/auth/mfa/totp/generate`
- **描述**: 为用户生成TOTP认证器密钥和二维码

### 启用TOTP
- **接口**: `POST /api/auth/mfa/totp/enable`
- **描述**: 验证TOTP代码并启用TOTP认证
- **请求体**:
```json
{
  "totpCode": "string"           // TOTP验证码
}
```

### 禁用TOTP
- **接口**: `POST /api/auth/mfa/totp/disable`
- **描述**: 验证TOTP代码并禁用TOTP认证
- **请求体**:
```json
{
  "totpCode": "string"           // TOTP验证码
}
```

### 发送短信验证码
- **接口**: `POST /api/auth/mfa/sms/send`
- **描述**: 向指定手机号发送MFA验证码
- **请求体**:
```json
{
  "phoneNumber": "string"        // 手机号
}
```

### 发送邮箱验证码
- **接口**: `POST /api/auth/mfa/email/send`
- **描述**: 向指定邮箱发送MFA验证码
- **请求体**:
```json
{
  "email": "string"              // 邮箱地址
}
```

### 验证MFA代码
- **接口**: `POST /api/auth/mfa/verify`
- **描述**: 验证多因素认证代码
- **请求体**:
```json
{
  "verificationCode": "string",  // 验证码
  "verificationType": "sms|email|totp" // 验证类型
}
```

### 生成恢复代码
- **接口**: `POST /api/auth/mfa/recovery/generate`
- **描述**: 生成MFA恢复代码（用于在无法使用MFA设备时恢复账户）
- **请求体**:
```json
{
  "password": "string"           // 用户密码确认
}
```

## 数据模型

### 问卷回答响应格式
```json
{
  "responseId": 789,             // 回答记录ID
  "questionnaireId": 123,        // 问卷ID
  "questionnaireTitle": "string", // 问卷标题
  "isCompleted": true,           // 是否完成
  "startedAt": "2024-01-01T10:00:00", // 开始时间
  "completedAt": "2024-01-01T10:15:00", // 完成时间
  "durationSeconds": 900,        // 耗时（秒）
  "message": "提交成功"          // 提示信息
}
```

### 问卷详情格式
```json
{
  "id": 123,
  "title": "四会培训效果调研",
  "description": "了解培训效果和改进建议",
  "type": "survey",
  "status": "published",
  "version": "1.0",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "isAnonymous": false,
  "allowMultipleSubmissions": false,
  "themeColor": "#007AFF",
  "layout": "card",
  "requirePassword": false,
  "publishUrl": "https://example.com/survey/123",
  "questionCount": 10,
  "responseCount": 156,
  "estimatedTime": 5,            // 预计完成时间（分钟）
  "userCompleted": false,        // 当前用户是否已完成
  "questions": [
    {
      "id": 456,
      "type": "single_choice",
      "title": "您对本次培训的整体满意度如何？",
      "description": "请选择最符合您感受的选项",
      "required": true,
      "sortOrder": 1,
      "options": [
        {
          "id": 1,
          "text": "非常满意",
          "value": "5",
          "sortOrder": 1
        },
        {
          "id": 2,
          "text": "满意",
          "value": "4", 
          "sortOrder": 2
        }
      ]
    }
  ]
}
```

### AI聊天响应格式
```json
{
  "message": "根据四会培训内容，我来为您解答...", // AI回复内容
  "sessionId": "session_123",                    // 会话ID
  "timestamp": "2024-01-01T10:00:00",           // 时间戳
  "sources": [                                   // 知识来源（仅知识增强聊天）
    {
      "documentId": 789,
      "title": "四会基础知识",
      "relevanceScore": 0.95
    }
  ]
}
```

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

## 权限和安全

### 身份验证
所有API接口都需要进行身份验证，请在请求头中携带JWT token：
```
Authorization: Bearer <your-jwt-token>
```

### 微信小程序特殊处理
- 小程序端通过微信授权获取用户信息
- 服务端验证微信授权码的有效性
- 自动创建或关联用户账户
- 返回适用于小程序的JWT token

### 错误码说明
- `401`: 未授权，需要重新登录
- `403`: 权限不足，无法访问该资源
- `404`: 资源不存在
- `429`: 请求频率过高，需要稍后重试
- `500`: 服务器内部错误

## 使用建议

### AI聊天使用策略
1. **基础问答**: 使用 `/api/ai/chat` 进行简单问答
2. **连续对话**: 使用 `/api/ai/chat/conversation` 维护对话上下文
3. **智能记忆**: 使用 `/api/ai/chat/memory` 让系统自动管理对话历史
4. **专业问答**: 使用 `/api/ai/chat/knowledge` 获取基于四会文档的专业回答

### 文件访问优化
- 使用 `/api/contents/signed-url/{fileName}` 获取临时URL
- 缓存签名URL直到过期时间
- 大文件建议设置较长的过期时间减少请求频率

### 问卷填写体验
- 先调用 `/validate-access` 检查访问权限
- 使用 `/completion-status` 检查是否已完成避免重复提交
- 支持草稿保存功能（本地存储）

### 性能优化建议
- 合理使用分页参数控制数据量
- 重要数据可以本地缓存减少网络请求
- 图片和视频文件使用CDN加速
- 适当的请求频率控制避免被限流 