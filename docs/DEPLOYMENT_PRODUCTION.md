# 四会培训平台 - 生产环境部署指南

## 部署架构概览

四会培训平台采用前后端分离架构，支持容器化部署：

```
Internet
    ↓
Nginx (反向代理/负载均衡)
    ↓
┌─────────────────┬─────────────────┐
│   前端服务       │    后端服务      │
│ (Vue3 Admin)    │ (Spring Boot)   │
│ (UniApp Mini)   │                │
└─────────────────┴─────────────────┘
    ↓                    ↓
┌─────────────────┬─────────────────┐
│   数据存储       │    文件存储      │
│ (MySQL/Redis)   │ (Aliyun OSS)    │
└─────────────────┴─────────────────┘
```

## 环境要求

### 硬件要求
- **CPU**: 4核心以上
- **内存**: 8GB以上（推荐16GB）
- **存储**: 100GB以上SSD
- **网络**: 10Mbps以上带宽

### 软件要求
- **操作系统**: Ubuntu 20.04+ / CentOS 8+ / Docker
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

## 快速部署（推荐）

### 1. 使用Docker Compose部署

```bash
# 克隆项目
git clone <your-repo-url> sihui-platform
cd sihui-platform

# 配置环境变量
cp .env.example .env
vim .env  # 修改配置

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 2. 初始化数据库

```bash
# 等待MySQL启动完成
docker-compose exec mysql mysql -u root -p sihui_db

# 运行数据库迁移
docker-compose exec backend java -jar app.jar --spring.profiles.active=migration
```

### 3. 验证部署

```bash
# 健康检查
curl http://localhost/api/health
curl http://localhost/admin/

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

## 详细配置

### 环境变量配置 (.env)

```bash
# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=sihui_db
DB_USERNAME=sihui_user
DB_PASSWORD=your_secure_password

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT配置
JWT_SECRET=your_jwt_secret_key_at_least_256_bits
JWT_EXPIRATION=86400

# 阿里云OSS配置
ALIYUN_OSS_ENDPOINT=your_oss_endpoint
ALIYUN_OSS_BUCKET=your_bucket_name
ALIYUN_OSS_ACCESS_KEY=your_access_key
ALIYUN_OSS_SECRET_KEY=your_secret_key

# DeepSeek API配置
DEEPSEEK_API_KEY=your_deepseek_api_key
DEEPSEEK_API_URL=https://api.deepseek.com/v1

# 应用配置
APP_ENV=production
APP_DEBUG=false
APP_LOG_LEVEL=INFO
APP_PORT=8080

# Nginx配置
NGINX_PORT=80
NGINX_SSL_PORT=443
```

### SSL证书配置

```bash
# 创建证书目录
mkdir -p ./nginx/ssl

# 使用Let's Encrypt获取证书
certbot certonly --standalone -d your-domain.com

# 复制证书文件
cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ./nginx/ssl/
cp /etc/letsencrypt/live/your-domain.com/privkey.pem ./nginx/ssl/
```

## 监控与日志

### 健康检查端点

```bash
# 后端健康检查
GET /api/health
# 返回: {"status": "UP", "timestamp": "2024-01-01T00:00:00Z"}

# 数据库连接检查
GET /api/health/db
# 返回: {"status": "UP", "database": "connected"}

# Redis连接检查
GET /api/health/redis
# 返回: {"status": "UP", "redis": "connected"}

# 外部服务检查
GET /api/health/external
# 返回: {"deepseek": "UP", "oss": "UP"}
```

### 日志管理

```bash
# 查看实时日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f nginx

# 日志轮转配置（在docker-compose.yml中）
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### 性能监控

```bash
# 查看容器资源使用情况
docker stats

# 查看系统资源
htop
df -h
free -h

# 数据库性能监控
docker-compose exec mysql mysqladmin -u root -p processlist
docker-compose exec mysql mysqladmin -u root -p status
```

## 备份策略

### 数据库备份

```bash
# 自动备份脚本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
mkdir -p $BACKUP_DIR

docker-compose exec mysql mysqldump -u root -p$DB_PASSWORD sihui_db > $BACKUP_DIR/sihui_db_$DATE.sql

# 保留最近30天的备份
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

### 文件备份

```bash
# 应用文件备份
tar -czf /backup/app_$(date +%Y%m%d).tar.gz \
  --exclude=node_modules \
  --exclude=target \
  --exclude=.git \
  ./sihui-platform/

# OSS文件备份（如需要）
# 使用阿里云OSS同步工具或API
```

## 扩展与优化

### 水平扩展

```yaml
# docker-compose.scale.yml
services:
  backend:
    deploy:
      replicas: 3
  
  nginx:
    depends_on:
      - backend
    # 负载均衡配置
```

### 缓存优化

```bash
# Redis集群配置
# 在生产环境中考虑Redis哨兵或集群模式

# 应用层缓存
# 已实现的增强缓存系统将自动优化性能
```

### 数据库优化

```sql
-- 生产环境MySQL优化配置
-- 在my.cnf中添加：

[mysqld]
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
query_cache_size = 128M
max_connections = 300
```

## 安全配置

### 防火墙设置

```bash
# UFW防火墙配置
sudo ufw enable
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 3306/tcp  # 禁止外部访问MySQL
sudo ufw deny 6379/tcp  # 禁止外部访问Redis
```

### 应用安全

```bash
# 定期更新依赖
docker-compose pull
docker-compose up -d

# 安全扫描
docker scan sihui-backend:latest
docker scan sihui-frontend:latest
```

## 故障排查

### 常见问题

1. **容器启动失败**
   ```bash
   docker-compose logs container_name
   docker-compose ps
   ```

2. **数据库连接问题**
   ```bash
   docker-compose exec backend ping mysql
   docker-compose exec mysql mysql -u root -p
   ```

3. **OSS连接问题**
   ```bash
   # 检查网络连接
   curl -I https://your-bucket.oss-region.aliyuncs.com
   
   # 检查API密钥配置
   docker-compose exec backend env | grep ALIYUN
   ```

4. **性能问题**
   ```bash
   # 查看系统负载
   docker stats
   
   # 查看应用性能指标
   curl http://localhost/api/monitoring/performance
   ```

### 紧急恢复

```bash
# 快速回滚
docker-compose down
git checkout previous_stable_tag
docker-compose up -d

# 数据库恢复
docker-compose exec mysql mysql -u root -p sihui_db < backup_file.sql
```

## 更新与维护

### 应用更新

```bash
# 1. 备份当前版本
./scripts/backup.sh

# 2. 拉取新版本
git pull origin main

# 3. 构建新镜像
docker-compose build

# 4. 滚动更新
docker-compose up -d --no-deps backend
docker-compose up -d --no-deps frontend

# 5. 验证更新
./scripts/health-check.sh
```

### 数据库迁移

```bash
# 运行数据库迁移
docker-compose exec backend java -jar app.jar --spring.profiles.active=migration

# 验证迁移结果
docker-compose exec mysql mysql -u root -p -e "SHOW TABLES;" sihui_db
```

## 联系信息

- **技术支持**: tech-support@sihui.com
- **运维团队**: ops@sihui.com
- **紧急联系**: +86-xxx-xxxx-xxxx
- **文档更新**: 2024年1月

---

**注意**: 请根据实际部署环境调整配置参数，确保所有敏感信息都通过环境变量管理，不要在代码中硬编码。 