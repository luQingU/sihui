# 四会培训平台 - 快速启动指南

## 🚀 5分钟快速部署

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 4GB+ 可用内存
- 10GB+ 可用磁盘空间

### 1. 克隆项目

```bash
git clone <your-repo-url> sihui-platform
cd sihui-platform
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量（必须修改的项目）
vim .env
```

**必须修改的配置项：**
```bash
# 数据库密码（强烈建议修改）
DB_PASSWORD=your_secure_password_here
DB_ROOT_PASSWORD=your_root_password_here

# Redis密码（强烈建议修改）
REDIS_PASSWORD=your_redis_password_here

# JWT密钥（必须修改，至少32字符）
JWT_SECRET=your_jwt_secret_key_at_least_256_bits_long

# 阿里云OSS配置（必须配置）
ALIYUN_OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_BUCKET=your-bucket-name
ALIYUN_OSS_ACCESS_KEY=your_access_key_id
ALIYUN_OSS_SECRET_KEY=your_access_key_secret

# DeepSeek API密钥（必须配置）
DEEPSEEK_API_KEY=your_deepseek_api_key_here
```

### 3. 一键部署

```bash
# 使用部署脚本（推荐）
./scripts/deploy.sh prod --force

# 或者手动部署
docker-compose up -d
```

### 4. 验证部署

```bash
# 检查服务状态
docker-compose ps

# 健康检查
./scripts/health-check.sh

# 或者手动检查
curl http://localhost/health
curl http://localhost/api/health
```

### 5. 访问系统

- **管理后台**: http://localhost/admin/
- **API文档**: http://localhost/api/swagger-ui.html
- **健康检查**: http://localhost/health

## 📋 默认账户

**管理员账户**（首次登录后请立即修改密码）：
- 用户名: `admin`
- 密码: `admin123`

## 🔧 常用操作

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f nginx
```

### 数据备份
```bash
# 完整备份
./scripts/backup.sh --full --compress

# 仅备份数据库
./scripts/backup.sh --database-only

# 仅备份配置
./scripts/backup.sh --config-only
```

### 停止服务
```bash
# 停止所有服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

### 更新系统
```bash
# 拉取最新代码
git pull origin main

# 重新部署
./scripts/deploy.sh prod --force
```

## 🛠️ 故障排查

### 1. 服务无法启动
```bash
# 检查Docker服务
docker --version
docker-compose --version

# 检查端口占用
netstat -tulpn | grep :80
netstat -tulpn | grep :8080
netstat -tulpn | grep :3306

# 查看详细错误
docker-compose logs [service_name]
```

### 2. 数据库连接失败
```bash
# 检查MySQL容器
docker-compose ps mysql

# 进入MySQL容器
docker-compose exec mysql mysql -u root -p

# 检查数据库配置
cat .env | grep DB_
```

### 3. 前端404错误
```bash
# 检查Nginx配置
docker-compose exec nginx nginx -t

# 重启Nginx
docker-compose restart nginx

# 检查前端构建
docker-compose logs frontend
```

### 4. API调用失败
```bash
# 检查后端服务
curl http://localhost:8080/api/health

# 查看后端日志
docker-compose logs backend

# 检查环境变量
docker-compose exec backend env | grep SPRING_
```

## 📊 监控与维护

### 系统监控
```bash
# 查看资源使用情况
docker stats

# 健康检查
./scripts/health-check.sh --verbose

# 系统状态
df -h
free -h
top
```

### 定期维护
```bash
# 每日备份（建议设置定时任务）
0 2 * * * /path/to/sihui-platform/scripts/backup.sh --full --compress

# 每周健康检查
0 9 * * 1 /path/to/sihui-platform/scripts/health-check.sh --alert

# 清理Docker镜像
docker system prune -f
```

## 🔒 安全建议

### 1. 密码安全
- 修改所有默认密码
- 使用强密码（包含大小写字母、数字、特殊字符）
- 定期更换密码

### 2. 网络安全
- 配置防火墙规则
- 仅开放必要端口（80, 443）
- 禁止直接访问数据库端口

### 3. 数据安全
- 定期备份数据
- 启用HTTPS（生产环境）
- 监控异常访问

## 📞 技术支持

- **文档**: [DEPLOYMENT_PRODUCTION.md](./DEPLOYMENT_PRODUCTION.md)
- **问题反馈**: 请创建GitHub Issue
- **紧急联系**: support@sihui.com

---

**注意**: 这是快速启动指南，生产环境部署请参考 [DEPLOYMENT_PRODUCTION.md](./DEPLOYMENT_PRODUCTION.md) 获取详细配置说明。