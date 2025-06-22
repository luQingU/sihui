# 🔧 部署配置文件模板

## 后端环境变量配置 (.env)

将以下内容保存为 `sihui-backend/.env` 文件：

```bash
# ===========================================
# Sihui Backend 生产环境配置文件
# ===========================================

# 应用基本配置
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sihui_production
DB_USERNAME=sihui_user
DB_PASSWORD=请填写您的数据库密码

# 数据库连接池配置
DB_MAXIMUM_POOL_SIZE=20
DB_MINIMUM_IDLE=5
DB_CONNECTION_TIMEOUT=30000

# JWT 安全配置
JWT_SECRET=请填写您的JWT密钥（至少64位字符）
JWT_EXPIRATION=604800000
JWT_ISSUER=sihui-backend

# 文件上传配置
FILE_UPLOAD_PATH=/opt/sihui/uploads
FILE_MAX_SIZE=10MB
FILE_ALLOWED_TYPES=jpg,jpeg,png,pdf,doc,docx,xls,xlsx,ppt,pptx

# 对象存储配置 (三选一)
# ===== 阿里云 OSS =====
OSS_ENDPOINT=请填写OSS端点
OSS_ACCESS_KEY_ID=请填写AccessKeyId
OSS_ACCESS_KEY_SECRET=请填写AccessKeySecret
OSS_BUCKET_NAME=请填写Bucket名称
OSS_DOMAIN=请填写OSS域名

# ===== 腾讯云 COS =====
# COS_REGION=请填写COS地域
# COS_SECRET_ID=请填写SecretId
# COS_SECRET_KEY=请填写SecretKey
# COS_BUCKET_NAME=请填写Bucket名称
# COS_DOMAIN=请填写COS域名

# ===== 七牛云 =====
# QINIU_ACCESS_KEY=请填写AccessKey
# QINIU_SECRET_KEY=请填写SecretKey
# QINIU_BUCKET_NAME=请填写Bucket名称
# QINIU_DOMAIN=请填写七牛云域名

# AI 服务配置
DEEPSEEK_API_KEY=请填写DeepSeek API Key
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat

# OpenAI 配置 (可选)
# OPENAI_API_KEY=请填写OpenAI API Key
# OPENAI_BASE_URL=https://api.openai.com/v1
# OPENAI_MODEL=gpt-3.5-turbo

# 百度文心 配置 (可选)
# BAIDU_API_KEY=请填写百度API Key
# BAIDU_SECRET_KEY=请填写百度Secret Key

# Redis 配置 (如使用缓存)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=请填写Redis密码
REDIS_DATABASE=0
REDIS_TIMEOUT=5000

# 邮件服务配置
MAIL_HOST=请填写SMTP服务器
MAIL_PORT=587
MAIL_USERNAME=请填写邮箱用户名
MAIL_PASSWORD=请填写邮箱密码
MAIL_FROM=请填写发件人邮箱
MAIL_PERSONAL=Sihui系统

# 日志配置
LOG_LEVEL=INFO
LOG_FILE_PATH=/opt/sihui/logs/app.log
LOG_MAX_FILE_SIZE=100MB
LOG_MAX_HISTORY=30

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized

# 安全配置
SECURITY_CORS_ALLOWED_ORIGINS=https://admin.your-domain.com
SECURITY_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
SECURITY_SESSION_TIMEOUT=1800

# 微信小程序配置
WECHAT_APP_ID=请填写微信小程序AppId
WECHAT_APP_SECRET=请填写微信小程序AppSecret

# 系统配置
SYSTEM_NAME=Sihui学习培训系统
SYSTEM_VERSION=1.0.0
SYSTEM_ADMIN_EMAIL=admin@your-domain.com
SYSTEM_TIMEZONE=Asia/Shanghai
```

## 前端环境变量配置 (.env.production)

将以下内容保存为 `admin-dashboard_3/.env.production` 文件：

```bash
# ===========================================
# Sihui Frontend 生产环境配置文件
# ===========================================

# API配置
NEXT_PUBLIC_API_URL=https://api.your-domain.com
NEXT_PUBLIC_WS_URL=wss://api.your-domain.com/ws

# 应用配置
NEXT_PUBLIC_APP_NAME=Sihui管理系统
NEXT_PUBLIC_APP_VERSION=1.0.0
NEXT_PUBLIC_APP_DESCRIPTION=Sihui学习培训系统管理端

# 文件上传配置
NEXT_PUBLIC_MAX_FILE_SIZE=10485760
NEXT_PUBLIC_ALLOWED_FILE_TYPES=.jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx
NEXT_PUBLIC_UPLOAD_URL=https://api.your-domain.com/api/files/upload

# 分页配置
NEXT_PUBLIC_DEFAULT_PAGE_SIZE=20
NEXT_PUBLIC_MAX_PAGE_SIZE=100

# 主题配置
NEXT_PUBLIC_DEFAULT_THEME=light
NEXT_PUBLIC_ENABLE_DARK_MODE=true

# 功能开关
NEXT_PUBLIC_ENABLE_ANALYTICS=true
NEXT_PUBLIC_ENABLE_DEBUG=false
NEXT_PUBLIC_ENABLE_CHAT=true
NEXT_PUBLIC_ENABLE_VIDEO_PREVIEW=true

# 安全配置
NEXT_PUBLIC_SESSION_TIMEOUT=1800000
NEXT_PUBLIC_AUTO_LOGOUT_WARNING=300000

# CDN配置 (可选)
NEXT_PUBLIC_CDN_URL=https://cdn.your-domain.com
NEXT_PUBLIC_STATIC_URL=https://static.your-domain.com

# 第三方服务配置
NEXT_PUBLIC_SENTRY_DSN=请填写Sentry DSN (可选)
NEXT_PUBLIC_GA_TRACKING_ID=请填写Google Analytics ID (可选)
```

## Nginx 配置文件

将以下内容保存为 `/etc/nginx/conf.d/sihui.conf` 文件：

```nginx
# ===========================================
# Sihui 项目 Nginx 配置文件
# ===========================================

# 上游后端服务
upstream sihui_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

# 上游前端服务  
upstream sihui_frontend {
    server 127.0.0.1:3000;
    keepalive 32;
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name api.your-domain.com admin.your-domain.com;
    return 301 https://$server_name$request_uri;
}

# 后端API服务
server {
    listen 443 ssl http2;
    server_name api.your-domain.com;

    # SSL证书配置
    ssl_certificate /opt/sihui/ssl/your-domain.com.crt;
    ssl_certificate_key /opt/sihui/ssl/your-domain.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

    # 日志配置
    access_log /var/log/nginx/api_access.log;
    error_log /var/log/nginx/api_error.log;

    # 客户端配置
    client_max_body_size 10M;
    client_body_timeout 30s;
    client_header_timeout 30s;

    # 代理配置
    location / {
        proxy_pass http://sihui_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }

    # WebSocket支持
    location /ws {
        proxy_pass http://sihui_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 86400;
    }

    # 健康检查
    location /health {
        proxy_pass http://sihui_backend/api/health;
        access_log off;
    }
}

# 管理端前端服务
server {
    listen 443 ssl http2;
    server_name admin.your-domain.com;

    # SSL证书配置
    ssl_certificate /opt/sihui/ssl/your-domain.com.crt;
    ssl_certificate_key /opt/sihui/ssl/your-domain.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

    # 日志配置
    access_log /var/log/nginx/admin_access.log;
    error_log /var/log/nginx/admin_error.log;

    # 静态文件缓存
    location /_next/static/ {
        proxy_pass http://sihui_frontend;
        proxy_cache_valid 200 1y;
        add_header Cache-Control "public, immutable";
    }

    # 其他静态资源
    location /static/ {
        proxy_pass http://sihui_frontend;
        proxy_cache_valid 200 1d;
        add_header Cache-Control "public";
    }

    # 主要代理
    location / {
        proxy_pass http://sihui_frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }
}

# 全局配置
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
```

## 系统服务配置

### 后端服务 (/etc/systemd/system/sihui-backend.service)

```ini
[Unit]
Description=Sihui Backend Service
Documentation=https://github.com/your-repo/sihui
After=mysql.service network.target
Wants=mysql.service

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/sihui/backend
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/sihui/backend/app.jar
ExecReload=/bin/kill -HUP $MAINPID
KillMode=mixed
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# 环境变量
Environment=SPRING_PROFILES_ACTIVE=production
Environment=JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai

# 安全配置
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/opt/sihui/logs /opt/sihui/uploads

[Install]
WantedBy=multi-user.target
```

### 前端服务 (/etc/systemd/system/sihui-frontend.service)

```ini
[Unit]
Description=Sihui Frontend Service
Documentation=https://github.com/your-repo/sihui
After=network.target
Wants=network.target

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/sihui/frontend
ExecStart=/usr/bin/npm start
ExecReload=/bin/kill -HUP $MAINPID
KillMode=mixed
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# 环境变量
Environment=NODE_ENV=production
Environment=PORT=3000

# 安全配置
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ProtectHome=yes

[Install]
WantedBy=multi-user.target
```

## MySQL 数据库初始化脚本

```sql
-- ===========================================
-- Sihui 数据库初始化脚本
-- ===========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sihui_production 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER IF NOT EXISTS 'sihui_user'@'localhost' 
IDENTIFIED BY '请填写您的数据库密码';

-- 授权
GRANT ALL PRIVILEGES ON sihui_production.* TO 'sihui_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON sihui_production.* TO 'sihui_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 切换到目标数据库
USE sihui_production;

-- 显示创建结果
SELECT 
    'Database created successfully' as status,
    DATABASE() as current_database,
    USER() as current_user;
```

## 防火墙配置脚本

```bash
#!/bin/bash
# ===========================================
# Sihui 防火墙配置脚本
# ===========================================

echo "配置防火墙规则..."

# 启用 UFW
sudo ufw --force enable

# 默认策略
sudo ufw default deny incoming
sudo ufw default allow outgoing

# SSH (根据实际端口修改)
sudo ufw allow 22/tcp comment 'SSH'

# HTTP/HTTPS
sudo ufw allow 80/tcp comment 'HTTP'
sudo ufw allow 443/tcp comment 'HTTPS'

# MySQL (仅本地访问)
sudo ufw allow from 127.0.0.1 to any port 3306 comment 'MySQL Local'

# Redis (仅本地访问)
sudo ufw allow from 127.0.0.1 to any port 6379 comment 'Redis Local'

# 显示状态
sudo ufw status numbered

echo "防火墙配置完成！"
```

## 部署脚本

```bash
#!/bin/bash
# ===========================================
# Sihui 一键部署脚本
# ===========================================

set -e

echo "开始部署 Sihui 项目..."

# 项目目录
PROJECT_DIR="/opt/sihui"
BACKEND_DIR="$PROJECT_DIR/Sihui/sihui-backend"
FRONTEND_DIR="$PROJECT_DIR/Sihui/admin-dashboard_3"

# 停止服务
echo "停止现有服务..."
sudo systemctl stop sihui-backend sihui-frontend 2>/dev/null || true

# 构建后端
echo "构建后端..."
cd $BACKEND_DIR
mvn clean package -DskipTests
sudo cp target/sihui-backend-*.jar $PROJECT_DIR/backend/app.jar

# 构建前端
echo "构建前端..."
cd $FRONTEND_DIR
npm install --production
npm run build
sudo cp -r .next $PROJECT_DIR/frontend/
sudo cp -r public $PROJECT_DIR/frontend/
sudo cp package.json $PROJECT_DIR/frontend/
sudo cp next.config.mjs $PROJECT_DIR/frontend/

# 设置权限
sudo chown -R www-data:www-data $PROJECT_DIR
sudo chmod -R 755 $PROJECT_DIR

# 启动服务
echo "启动服务..."
sudo systemctl start sihui-backend sihui-frontend
sudo systemctl enable sihui-backend sihui-frontend

# 重启 Nginx
sudo systemctl reload nginx

# 检查状态
echo "检查服务状态..."
sudo systemctl status sihui-backend --no-pager -l
sudo systemctl status sihui-frontend --no-pager -l

echo "部署完成！"
echo "后端API: https://api.your-domain.com"
echo "管理端: https://admin.your-domain.com"
```

## 备份脚本

```bash
#!/bin/bash
# ===========================================
# Sihui 备份脚本
# ===========================================

BACKUP_DIR="/opt/sihui/backup"
DATE=$(date +%Y%m%d_%H%M%S)

echo "开始备份..."

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
echo "备份数据库..."
mysqldump -u sihui_user -p sihui_production > $BACKUP_DIR/database_$DATE.sql

# 备份上传文件
echo "备份上传文件..."
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /opt/sihui/uploads/

# 备份配置文件
echo "备份配置文件..."
tar -czf $BACKUP_DIR/config_$DATE.tar.gz \
    /opt/sihui/Sihui/sihui-backend/.env \
    /opt/sihui/Sihui/admin-dashboard_3/.env.production \
    /etc/nginx/conf.d/sihui.conf

# 清理旧备份 (保留7天)
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "备份完成！"
echo "备份位置: $BACKUP_DIR"
```

---

## 使用说明

1. **配置环境变量**: 根据上述模板创建对应的 `.env` 文件
2. **配置 Nginx**: 使用提供的 Nginx 配置模板
3. **设置系统服务**: 使用提供的 systemd 服务配置
4. **初始化数据库**: 执行 MySQL 初始化脚本
5. **配置防火墙**: 运行防火墙配置脚本
6. **执行部署**: 使用一键部署脚本完成部署

请根据您的实际环境修改所有 `请填写...` 和 `your-domain.com` 部分！ 