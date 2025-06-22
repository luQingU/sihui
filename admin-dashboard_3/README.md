# 四会培训管理平台 - PC管理端

这是基于 Next.js 14 开发的现代化管理后台，为四会培训管理系统提供完整的管理功能。

## 🚀 功能特性

### ✅ 已完成功能

- **认证管理**
  - 用户登录/登出
  - JWT Token 管理
  - 多因素认证 (MFA)
  - 会话管理

- **用户管理**
  - 用户 CRUD 操作
  - 角色权限管理
  - 用户状态管理
  - 批量操作

- **AI智能问答**
  - 聊天会话管理
  - AI 统计分析
  - 知识库管理
  - 对话记录查看

- **问卷系统**
  - 可视化问卷设计器
  - 问卷发布管理
  - 数据统计分析
  - 多格式报告导出

- **内容管理**
  - 文件上传/下载
  - 视频管理
  - 文档管理
  - OSS 云存储

- **系统监控**
  - 性能监控
  - 健康检查
  - 错误日志
  - 系统优化

## 🛠 技术栈

- **框架**: Next.js 14 (App Router)
- **语言**: TypeScript
- **UI组件**: Radix UI + Tailwind CSS
- **状态管理**: React Hooks
- **HTTP客户端**: Fetch API (自定义封装)
- **图标**: Lucide React
- **主题**: next-themes

## 📦 安装依赖

```bash
# 使用 pnpm (推荐)
pnpm install

# 或使用 npm
npm install

# 或使用 yarn
yarn install
```

## 🚀 启动项目

```bash
# 开发模式
pnpm dev

# 构建生产版本
pnpm build

# 启动生产版本
pnpm start

# 代码检查
pnpm lint
```

项目将在 [http://localhost:3000](http://localhost:3000) 启动。

## ⚙️ 环境配置

### 环境变量

创建 `.env.local` 文件并配置以下环境变量：

```env
# API 配置
NEXT_PUBLIC_API_URL=http://localhost:8080

# 应用配置
NEXT_PUBLIC_APP_NAME=四会培训管理平台
NEXT_PUBLIC_APP_VERSION=1.0.0
NEXT_PUBLIC_ENV=development

# 分页配置
NEXT_PUBLIC_DEFAULT_PAGE_SIZE=10
NEXT_PUBLIC_MAX_PAGE_SIZE=100

# 文件上传配置
NEXT_PUBLIC_MAX_FILE_SIZE=10485760
NEXT_PUBLIC_ALLOWED_FILE_TYPES=jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,mp4,avi,mov

# 开发配置
NEXT_PUBLIC_DEBUG=false
NEXT_PUBLIC_LOG_LEVEL=info
```

### 后端API配置

确保后端服务正在运行在指定的端口（默认: 8080），并且包含以下功能：

- 用户认证与权限管理
- 问卷系统 API
- AI 聊天 API
- 内容管理 API
- 系统监控 API

## 📱 页面结构

```
/login                     # 登录页面
/dashboard                 # 仪表板首页
├── /ai
│   ├── /chat             # AI聊天管理
│   ├── /analytics        # AI分析报告
│   └── /knowledge        # 知识库管理
├── /users                # 用户管理
│   ├── /roles           # 角色管理
│   └── /sessions        # 会话管理
├── /questionnaires       # 问卷管理
│   ├── /create          # 创建问卷
│   ├── /designer        # 问卷设计器
│   ├── /analysis        # 数据分析
│   ├── /reports         # 报告生成
│   └── /results         # 结果查看
├── /content              # 内容管理
│   ├── /upload          # 文件上传
│   ├── /documents       # 文档管理
│   └── /videos          # 视频管理
├── /monitoring           # 系统监控
│   └── /performance     # 性能监控
└── /security             # 安全管理
    ├── /auth            # 认证管理
    ├── /mfa             # 多因素认证
    └── /monitoring      # 安全监控
```

## 🔧 API 服务层

项目包含完整的 API 服务层，位于 `lib/services/` 目录：

- `auth.ts` - 认证相关服务
- `users.ts` - 用户管理服务
- `ai.ts` - AI 功能服务
- `questionnaires.ts` - 问卷系统服务
- `content.ts` - 内容管理服务
- `monitoring.ts` - 系统监控服务

## 🎨 UI 组件

基于 Radix UI 和 Tailwind CSS 构建的现代化组件库：

- 完整的表单组件
- 数据表格与分页
- 模态框与对话框
- 导航与菜单
- 图表与统计卡片
- 加载状态与错误处理

## 🔐 认证与权限

- JWT Token 认证
- 自动 Token 刷新
- 路由守卫保护
- 基于角色的权限控制
- 多因素认证支持

## 📊 状态管理

- React Hooks 进行状态管理
- 自定义 Hook 封装业务逻辑
- 统一的错误处理机制
- 加载状态管理

## 🚢 部署

### 生产构建

```bash
pnpm build
```

### Docker 部署

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production && npm cache clean --force
COPY . .
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app
COPY --from=builder /app/next.config.mjs ./
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json

EXPOSE 3000
CMD ["npm", "start"]
```

## 🐛 问题排查

### 常见问题

1. **API 连接失败**
   - 检查 `NEXT_PUBLIC_API_URL` 配置
   - 确认后端服务正在运行
   - 检查网络连接和防火墙设置

2. **认证问题**
   - 清除浏览器 localStorage
   - 检查 JWT Token 是否过期
   - 确认后端认证接口正常

3. **页面加载错误**
   - 检查控制台错误信息
   - 确认所有依赖正确安装
   - 检查环境变量配置

## 📞 技术支持

如有问题，请联系开发团队或查看：

- 后端 API 文档
- 系统架构设计文档
- 部署指南文档

## 📄 许可证

 