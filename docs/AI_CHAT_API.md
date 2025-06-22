# AI聊天功能API文档

## 概述

四会培训平台集成了DeepSeek AI聊天功能，为用户提供智能问答服务。系统支持简单聊天、带历史记录的对话以及智能记忆管理。

## 技术架构

### 核心组件
- **DeepSeek API**: 基于DeepSeek的deepseek-chat模型
- **Apache HttpClient**: HTTP客户端，用于API调用
- **Spring Boot**: 后端框架
- **MySQL**: 数据存储
- **JWT**: 用户认证（可选）

### 数据模型
- **ChatSession**: 聊天会话管理
- **ChatMessage**: 消息存储和检索

## API端点

### 1. 简单聊天接口

**端点**: `POST /api/ai/chat`

**描述**: 发送单条消息给AI，获取回复

**请求参数**:
```
Content-Type: application/x-www-form-urlencoded
message: string (必需) - 用户消息内容
```

**响应格式**:
```json
{
    "success": true,
    "message": "AI回复内容",
    "timestamp": "2025-06-18T08:30:00.000Z"
}
```

**示例**:
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "message=你好，请介绍一下四会培训平台"
```

### 2. 带对话历史的聊天接口

**端点**: `POST /api/ai/chat/conversation`

**描述**: 发送消息并包含对话历史上下文

**请求参数**:
```json
{
    "message": "string (必需) - 当前消息",
    "history": [
        {
            "role": "user|assistant",
            "content": "消息内容"
        }
    ]
}
```

**响应格式**:
```json
{
    "success": true,
    "message": "AI回复内容",
    "timestamp": "2025-06-18T08:30:00.000Z"
}
```

**示例**:
```bash
curl -X POST http://localhost:8080/api/ai/chat/conversation \
  -H "Content-Type: application/json" \
  -d '{
    "message": "那培训内容有哪些？",
    "history": [
        {"role": "user", "content": "你好，请介绍一下四会培训平台"},
        {"role": "assistant", "content": "四会培训平台是一个专业的在线培训系统..."}
    ]
  }'
```

### 3. 智能记忆聊天接口

**端点**: `POST /api/ai/chat/memory`

**描述**: 使用智能记忆管理的聊天接口，自动维护用户会话历史

**请求参数**:
```json
{
    "userId": "number (必需) - 用户ID",
    "message": "string (必需) - 消息内容",
    "sessionId": "string (可选) - 会话ID，不提供则自动创建"
}
```

**响应格式**:
```json
{
    "success": true,
    "message": "AI回复内容",
    "sessionId": "会话ID",
    "timestamp": "2025-06-18T08:30:00.000Z"
}
```

**示例**:
```bash
curl -X POST http://localhost:8080/api/ai/chat/memory \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "message": "你好，我想了解培训课程"
  }'
```

## 错误处理

### 常见错误码

| 状态码 | 错误信息 | 描述 |
|--------|----------|------|
| 400 | 消息内容不能为空 | 请求参数验证失败 |
| 400 | 用户ID不能为空 | 记忆聊天缺少用户ID |
| 500 | 系统异常，请稍后重试 | 服务器内部错误 |
| 500 | AI服务暂时不可用 | DeepSeek API调用失败 |

### 错误响应格式
```json
{
    "success": false,
    "message": "错误描述",
    "timestamp": "2025-06-18T08:30:00.000Z"
}
```

## 配置说明

### 环境变量
```properties
# DeepSeek API配置
deepseek.api.key=your_api_key_here
deepseek.api.url=https://api.deepseek.com/v1/chat/completions
deepseek.api.model=deepseek-chat
deepseek.api.max-tokens=2000
deepseek.api.temperature=0.7

# HTTP客户端配置
deepseek.http.connect-timeout=30000
deepseek.http.socket-timeout=60000
deepseek.http.connection-request-timeout=10000
```

### 数据库表结构

#### chat_sessions表
```sql
CREATE TABLE chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    title VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
);
```

#### chat_messages表
```sql
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    role ENUM('user', 'assistant') NOT NULL,
    content TEXT NOT NULL,
    sequence_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_sequence (session_id, sequence_number),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE
);
```

## 性能优化

### 上下文窗口管理
- 智能记忆聊天自动保留最近10条消息
- 超出限制的历史消息自动截断
- 支持自定义上下文窗口大小

### 并发处理
- 支持多用户并发聊天
- 数据库连接池优化
- 异步处理机制

### 缓存策略
- 会话信息缓存
- API响应缓存（可选）
- 数据库查询优化

## 安全性

### 输入验证
- 消息长度限制
- 特殊字符过滤
- SQL注入防护
- XSS攻击防护

### 认证授权
- JWT令牌验证（可选）
- 用户权限检查
- 接口访问限制

### 数据保护
- 敏感信息脱敏
- 会话数据加密
- 审计日志记录

## 监控和日志

### 关键指标
- API响应时间
- 成功/失败率
- 并发用户数
- 消息处理量

### 日志记录
- 请求/响应日志
- 错误日志
- 性能日志
- 安全审计日志

## 故障排除

### 常见问题

**1. API调用超时**
- 检查网络连接
- 验证API密钥
- 调整超时配置

**2. 数据库连接失败**
- 检查数据库配置
- 验证连接池设置
- 查看数据库状态

**3. 内存不足**
- 监控JVM内存使用
- 调整堆内存大小
- 优化对象创建

### 调试步骤
1. 检查应用日志
2. 验证配置参数
3. 测试API连通性
4. 查看数据库状态
5. 监控系统资源

## 版本历史

### v1.0.0 (2025-06-18)
- 初始版本发布
- 支持基础聊天功能
- 集成DeepSeek API
- 实现智能记忆管理
- 添加安全性和性能测试

## 联系支持

如有问题或建议，请联系开发团队：
- 邮箱：support@sihui-platform.com
- 文档：https://docs.sihui-platform.com
- GitHub：https://github.com/sihui-platform/backend 