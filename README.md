# 四会项目 (Sihui Project)

## 项目简介

四会项目是一个全栈应用，包含Web管理后台、后端API服务和小程序前端。

## 项目结构

```
Sihui/
├── sihui-admin/          # Vue.js 管理后台
├── sihui-backend/        # Spring Boot 后端服务
├── sihui-miniprogram/    # 小程序前端
└── .taskmaster/          # 任务管理配置
```

## 技术栈

- **前端管理后台**: Vue 3 + TypeScript + Vite
- **后端服务**: Java Spring Boot + Maven
- **小程序**: uni-app框架
- **项目管理**: Taskmaster

## 开发环境设置

### 前置要求
- Node.js 16+
- Java 17+
- Maven 3.6+

### 安装依赖

#### 管理后台
```bash
cd sihui-admin
npm install
```

#### 后端服务
```bash
cd sihui-backend
mvn clean install
```

#### 小程序
```bash
cd sihui-miniprogram
npm install
```

## 启动项目

### 开发环境
```bash
# 启动管理后台
cd sihui-admin && npm run dev

# 启动后端服务
cd sihui-backend && mvn spring-boot:run

# 启动小程序开发
cd sihui-miniprogram && npm run dev
```

## 项目状态

项目当前处于初始开发阶段，正在进行基础架构搭建。

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。 