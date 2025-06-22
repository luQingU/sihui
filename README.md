# 四会学习培训管理系统

一个完整的企业级学习培训管理平台，包含管理后台、API后端和微信小程序。

## 🚀 项目概览

### 核心功能
- 📊 **问卷调研系统** - 创建、发布、统计问卷调研
- 🤖 **AI智能聊天** - 集成DeepSeek AI的智能问答系统
- 📚 **学习内容管理** - 文档、视频等培训资料管理
- 👥 **用户权限管理** - 多角色权限控制系统
- 📱 **微信小程序** - 移动端学习和答题应用
- 📈 **数据分析报表** - 学习进度和问卷数据分析

### 技术栈
- **前端管理后台**: Next.js 15 + React 19 + TypeScript + Tailwind CSS
- **后端API**: Spring Boot 2.7 + Spring Security + JPA + MySQL
- **微信小程序**: uni-app + Vue 3
- **部署**: Docker + Nginx + 系统服务

## 📁 项目结构

```
Sihui/
├── admin-dashboard_3/          # 前端管理后台
├── sihui-backend/              # 后端API服务
├── sihui-wx/                   # 微信小程序
├── config/                     # 配置文件
├── docs/                       # 项目文档
├── scripts/                    # 部署脚本
└── 完整部署包/                  # 生产部署包
```

## 🔧 快速开始

### 开发环境设置

1. **克隆项目**
```bash
git clone <repository-url>
cd Sihui
```

2. **后端设置**
```bash
cd sihui-backend
# 复制环境配置
cp .env.example .env
# 修改.env文件中的配置信息
# 启动后端服务
mvn spring-boot:run
```

3. **前端设置**
```bash
cd admin-dashboard_3
# 安装依赖
npm install
# 启动开发服务器
npm run dev
```

4. **微信小程序设置**
```bash
cd sihui-wx
# 安装依赖
npm install
# 启动开发模式
npm run dev:mp-weixin
```

### 生产环境部署

详细部署说明请参考：[`完整部署包/🚀快速部署流程.md`](完整部署包/🚀快速部署流程.md)

**一键部署流程：**
1. 配置 `完整部署包/backend/.env` 文件
2. 配置 `完整部署包/frontend/.env.production` 文件
3. 配置 `完整部署包/nginx/sihui.conf` 文件
4. 上传部署包到服务器
5. 执行 `sudo bash server-install.sh`

## 📋 环境要求

### 开发环境
- Node.js 18+
- Java 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 生产环境
- Ubuntu 20.04+ / CentOS 7+
- 4GB+ RAM
- 20GB+ 存储空间
- 已备案的域名
- SSL证书

## 🔑 配置说明

### 必需配置项
- **数据库**: MySQL连接信息
- **JWT密钥**: 64位随机字符串
- **阿里云OSS**: 文件存储服务
- **DeepSeek API**: AI聊天功能
- **微信小程序**: AppID和AppSecret

### 可选配置项
- **Redis**: 缓存和会话存储
- **邮件服务**: 用户通知功能
- **短信服务**: 手机验证功能

## 📖 详细文档

- [API文档](docs/) - 后端API接口说明
- [部署指南](完整部署包/本地配置指南.md) - 详细部署步骤
- [开发文档](docs/) - 开发相关说明
- [用户手册](docs/) - 系统使用说明

## 🛠️ 技术支持

### 常见问题
1. **数据库连接失败** - 检查数据库配置和服务状态
2. **JWT认证错误** - 验证JWT密钥配置
3. **文件上传失败** - 检查OSS配置
4. **AI聊天不工作** - 验证DeepSeek API密钥

### 获取支持
- 📧 技术支持邮箱: [support@example.com]
- 📞 技术支持电话: [电话号码]
- 💬 在线支持: [支持链接]

## 📄 许可证

本项目为商业项目，版权归 [公司名称] 所有。

## 🔄 更新日志

### v1.0.0 (2024-01-XX)
- ✅ 完成基础功能开发
- ✅ 完成部署脚本和文档
- ✅ 完成测试和优化

---

**开发团队**: [团队信息]  
**最后更新**: 2024年1月  
**版本**: v1.0.0 