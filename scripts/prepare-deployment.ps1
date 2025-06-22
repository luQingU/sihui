# ===========================================
# Sihui 项目部署准备脚本 (Windows PowerShell)
# ===========================================

param(
    [string]$DomainName = "your-domain.com",
    [string]$OutputDir = ".\deployment-files",
    [switch]$Help
)

if ($Help) {
    Write-Host @"
Sihui 项目部署准备脚本

用法:
    .\prepare-deployment.ps1 -DomainName "yourdomain.com" -OutputDir ".\deploy"

参数:
    -DomainName     您的域名 (默认: your-domain.com)
    -OutputDir      输出目录 (默认: .\deployment-files)
    -Help           显示此帮助信息

该脚本将创建部署所需的所有配置文件模板，您可以填写后上传到服务器。
"@
    exit 0
}

Write-Host "🚀 开始准备 Sihui 项目部署文件..." -ForegroundColor Green
Write-Host "域名: $DomainName" -ForegroundColor Cyan
Write-Host "输出目录: $OutputDir" -ForegroundColor Cyan

# 创建输出目录
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    Write-Host "✅ 创建输出目录: $OutputDir" -ForegroundColor Green
}

# 创建子目录
$ConfigDirs = @(
    "backend",
    "frontend", 
    "nginx",
    "systemd",
    "scripts",
    "ssl"
)

foreach ($dir in $ConfigDirs) {
    $fullPath = Join-Path $OutputDir $dir
    if (!(Test-Path $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
    }
}

Write-Host "📁 创建目录结构完成" -ForegroundColor Green

# 1. 创建后端环境变量文件
$BackendEnvContent = @"
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

# AI 服务配置
DEEPSEEK_API_KEY=请填写DeepSeek API Key
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=请填写Redis密码
REDIS_DATABASE=0

# 邮件服务配置
MAIL_HOST=请填写SMTP服务器
MAIL_PORT=587
MAIL_USERNAME=请填写邮箱用户名
MAIL_PASSWORD=请填写邮箱密码
MAIL_FROM=请填写发件人邮箱

# 微信小程序配置
WECHAT_APP_ID=请填写微信小程序AppId
WECHAT_APP_SECRET=请填写微信小程序AppSecret

# 系统配置
SYSTEM_NAME=Sihui学习培训系统
SYSTEM_VERSION=1.0.0
SYSTEM_ADMIN_EMAIL=admin@$DomainName
SYSTEM_TIMEZONE=Asia/Shanghai
"@

$BackendEnvPath = Join-Path $OutputDir "backend\.env"
Set-Content -Path $BackendEnvPath -Value $BackendEnvContent -Encoding UTF8
Write-Host "✅ 创建后端环境变量文件: $BackendEnvPath" -ForegroundColor Green

# 2. 创建前端环境变量文件
$FrontendEnvContent = @"
# ===========================================
# Sihui Frontend 生产环境配置文件
# ===========================================

# API配置
NEXT_PUBLIC_API_URL=https://api.$DomainName
NEXT_PUBLIC_WS_URL=wss://api.$DomainName/ws

# 应用配置
NEXT_PUBLIC_APP_NAME=Sihui管理系统
NEXT_PUBLIC_APP_VERSION=1.0.0
NEXT_PUBLIC_APP_DESCRIPTION=Sihui学习培训系统管理端

# 文件上传配置
NEXT_PUBLIC_MAX_FILE_SIZE=10485760
NEXT_PUBLIC_ALLOWED_FILE_TYPES=.jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx
NEXT_PUBLIC_UPLOAD_URL=https://api.$DomainName/api/files/upload

# 功能开关
NEXT_PUBLIC_ENABLE_ANALYTICS=true
NEXT_PUBLIC_ENABLE_DEBUG=false
NEXT_PUBLIC_ENABLE_CHAT=true
NEXT_PUBLIC_ENABLE_VIDEO_PREVIEW=true

# 安全配置
NEXT_PUBLIC_SESSION_TIMEOUT=1800000
NEXT_PUBLIC_AUTO_LOGOUT_WARNING=300000
"@

$FrontendEnvPath = Join-Path $OutputDir "frontend\.env.production"
Set-Content -Path $FrontendEnvPath -Value $FrontendEnvContent -Encoding UTF8
Write-Host "✅ 创建前端环境变量文件: $FrontendEnvPath" -ForegroundColor Green

# 3. 创建 Nginx 配置文件
$NginxContent = @"
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
    server_name api.$DomainName admin.$DomainName;
    return 301 https://`$server_name`$request_uri;
}

# 后端API服务
server {
    listen 443 ssl http2;
    server_name api.$DomainName;

    # SSL证书配置
    ssl_certificate /opt/sihui/ssl/$DomainName.crt;
    ssl_certificate_key /opt/sihui/ssl/$DomainName.key;
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

    # 代理配置
    location / {
        proxy_pass http://sihui_backend;
        proxy_set_header Host `$host;
        proxy_set_header X-Real-IP `$remote_addr;
        proxy_set_header X-Forwarded-For `$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto `$scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    # WebSocket支持
    location /ws {
        proxy_pass http://sihui_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade `$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host `$host;
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
    server_name admin.$DomainName;

    # SSL证书配置
    ssl_certificate /opt/sihui/ssl/$DomainName.crt;
    ssl_certificate_key /opt/sihui/ssl/$DomainName.key;
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

    # 主要代理
    location / {
        proxy_pass http://sihui_frontend;
        proxy_set_header Host `$host;
        proxy_set_header X-Real-IP `$remote_addr;
        proxy_set_header X-Forwarded-For `$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto `$scheme;
    }
}

# 全局配置
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
"@

$NginxPath = Join-Path $OutputDir "nginx\sihui.conf"
Set-Content -Path $NginxPath -Value $NginxContent -Encoding UTF8
Write-Host "✅ 创建 Nginx 配置文件: $NginxPath" -ForegroundColor Green

# 4. 创建后端系统服务文件
$BackendServiceContent = @"
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
ExecReload=/bin/kill -HUP `$MAINPID
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
"@

$BackendServicePath = Join-Path $OutputDir "systemd\sihui-backend.service"
Set-Content -Path $BackendServicePath -Value $BackendServiceContent -Encoding UTF8
Write-Host "✅ 创建后端系统服务文件: $BackendServicePath" -ForegroundColor Green

# 5. 创建前端系统服务文件
$FrontendServiceContent = @"
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
ExecReload=/bin/kill -HUP `$MAINPID
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
"@

$FrontendServicePath = Join-Path $OutputDir "systemd\sihui-frontend.service"
Set-Content -Path $FrontendServicePath -Value $FrontendServiceContent -Encoding UTF8
Write-Host "✅ 创建前端系统服务文件: $FrontendServicePath" -ForegroundColor Green

# 6. 创建数据库初始化脚本
$DatabaseInitContent = @"
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
"@

$DatabaseInitPath = Join-Path $OutputDir "scripts\init-database.sql"
Set-Content -Path $DatabaseInitPath -Value $DatabaseInitContent -Encoding UTF8
Write-Host "✅ 创建数据库初始化脚本: $DatabaseInitPath" -ForegroundColor Green

# 7. 创建一键部署脚本
$DeployScriptContent = @"
#!/bin/bash
# ===========================================
# Sihui 一键部署脚本
# ===========================================

set -e

echo "🚀 开始部署 Sihui 项目..."

# 项目目录
PROJECT_DIR="/opt/sihui"
BACKEND_DIR="`$PROJECT_DIR/Sihui/sihui-backend"
FRONTEND_DIR="`$PROJECT_DIR/Sihui/admin-dashboard_3"

# 停止服务
echo "⏹️ 停止现有服务..."
sudo systemctl stop sihui-backend sihui-frontend 2>/dev/null || true

# 构建后端
echo "🔨 构建后端..."
cd `$BACKEND_DIR
mvn clean package -DskipTests
sudo cp target/sihui-backend-*.jar `$PROJECT_DIR/backend/app.jar

# 构建前端
echo "🔨 构建前端..."
cd `$FRONTEND_DIR
npm install --production
npm run build
sudo cp -r .next `$PROJECT_DIR/frontend/
sudo cp -r public `$PROJECT_DIR/frontend/
sudo cp package.json `$PROJECT_DIR/frontend/
sudo cp next.config.mjs `$PROJECT_DIR/frontend/

# 设置权限
sudo chown -R www-data:www-data `$PROJECT_DIR
sudo chmod -R 755 `$PROJECT_DIR

# 启动服务
echo "▶️ 启动服务..."
sudo systemctl start sihui-backend sihui-frontend
sudo systemctl enable sihui-backend sihui-frontend

# 重启 Nginx
sudo systemctl reload nginx

# 检查状态
echo "✅ 检查服务状态..."
sudo systemctl status sihui-backend --no-pager -l
sudo systemctl status sihui-frontend --no-pager -l

echo "🎉 部署完成！"
echo "后端API: https://api.$DomainName"
echo "管理端: https://admin.$DomainName"
"@

$DeployScriptPath = Join-Path $OutputDir "scripts\deploy.sh"
Set-Content -Path $DeployScriptPath -Value $DeployScriptContent -Encoding UTF8
Write-Host "✅ 创建一键部署脚本: $DeployScriptPath" -ForegroundColor Green

# 8. 创建备份脚本
$BackupScriptContent = @"
#!/bin/bash
# ===========================================
# Sihui 备份脚本
# ===========================================

BACKUP_DIR="/opt/sihui/backup"
DATE=`$(date +%Y%m%d_%H%M%S)

echo "📦 开始备份..."

# 创建备份目录
mkdir -p `$BACKUP_DIR

# 备份数据库
echo "🗄️ 备份数据库..."
mysqldump -u sihui_user -p sihui_production > `$BACKUP_DIR/database_`$DATE.sql

# 备份上传文件
echo "📁 备份上传文件..."
tar -czf `$BACKUP_DIR/uploads_`$DATE.tar.gz /opt/sihui/uploads/

# 备份配置文件
echo "⚙️ 备份配置文件..."
tar -czf `$BACKUP_DIR/config_`$DATE.tar.gz \
    /opt/sihui/Sihui/sihui-backend/.env \
    /opt/sihui/Sihui/admin-dashboard_3/.env.production \
    /etc/nginx/conf.d/sihui.conf

# 清理旧备份 (保留7天)
find `$BACKUP_DIR -name "*.sql" -mtime +7 -delete
find `$BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "✅ 备份完成！"
echo "备份位置: `$BACKUP_DIR"
"@

$BackupScriptPath = Join-Path $OutputDir "scripts\backup.sh"
Set-Content -Path $BackupScriptPath -Value $BackupScriptContent -Encoding UTF8
Write-Host "✅ 创建备份脚本: $BackupScriptPath" -ForegroundColor Green

# 9. 创建SSL证书说明文件
$SSLInstructionContent = @"
# SSL 证书配置说明

## 证书文件要求

请将以下SSL证书文件放置在此目录下：

1. **$DomainName.crt** - 证书文件
2. **$DomainName.key** - 私钥文件
3. **ca-bundle.crt** - 证书链文件（如有）

## 免费SSL证书获取方式

### 1. Let's Encrypt（推荐）
```bash
# 安装 certbot
sudo apt install certbot python3-certbot-nginx

# 自动获取证书并配置 Nginx
sudo certbot --nginx -d api.$DomainName -d admin.$DomainName

# 自动续期
sudo crontab -e
# 添加：0 12 * * * /usr/bin/certbot renew --quiet
```

### 2. 阿里云免费SSL证书
1. 登录阿里云控制台
2. 搜索"SSL证书"
3. 购买免费证书（个人DV版）
4. 申请并验证域名
5. 下载Nginx格式证书

### 3. 腾讯云免费SSL证书
1. 登录腾讯云控制台
2. 搜索"SSL证书"
3. 申请免费证书
4. 完成域名验证
5. 下载Nginx格式证书

## 证书安装到服务器

```bash
# 创建SSL目录
sudo mkdir -p /opt/sihui/ssl

# 复制证书文件
sudo cp $DomainName.crt /opt/sihui/ssl/
sudo cp $DomainName.key /opt/sihui/ssl/

# 设置权限
sudo chmod 600 /opt/sihui/ssl/*
sudo chown root:root /opt/sihui/ssl/*
```

## 验证证书
```bash
# 检查证书有效性
openssl x509 -in /opt/sihui/ssl/$DomainName.crt -text -noout

# 检查证书和私钥匹配
openssl x509 -noout -modulus -in /opt/sihui/ssl/$DomainName.crt | openssl md5
openssl rsa -noout -modulus -in /opt/sihui/ssl/$DomainName.key | openssl md5
```
"@

$SSLInstructionPath = Join-Path $OutputDir "ssl\README.md"
Set-Content -Path $SSLInstructionPath -Value $SSLInstructionContent -Encoding UTF8
Write-Host "✅ 创建SSL证书说明: $SSLInstructionPath" -ForegroundColor Green

# 10. 创建部署指令文件
$DeployInstructionsContent = @"
# 🚀 Sihui 项目部署指令

## 📋 部署前准备清单

### 1. 服务器要求
- [ ] Ubuntu 20.04+ 或 CentOS 8+
- [ ] 2核CPU + 4GB内存 + 50GB硬盘
- [ ] 已开放端口 22, 80, 443

### 2. 域名配置
- [ ] 域名已解析到服务器IP
- [ ] API域名: api.$DomainName
- [ ] 管理端域名: admin.$DomainName

### 3. 必填配置项
请编辑以下文件并填写实际值：

#### backend/.env
- [ ] DB_PASSWORD（数据库密码）
- [ ] JWT_SECRET（JWT密钥，64位以上）
- [ ] OSS相关配置（选择一个云存储）
- [ ] DEEPSEEK_API_KEY（AI服务密钥）
- [ ] WECHAT_APP_ID 和 WECHAT_APP_SECRET

#### frontend/.env.production
- [ ] 检查API地址是否正确
- [ ] 其他配置一般无需修改

## 🔧 服务器端部署步骤

### 1. 连接服务器
```bash
ssh root@your-server-ip
```

### 2. 系统初始化
```bash
# 更新系统
apt update && apt upgrade -y

# 创建部署目录
mkdir -p /opt/sihui/{backend,frontend,nginx,ssl,logs,uploads,backup}

# 安装基础软件
apt install openjdk-8-jdk maven mysql-server nginx git -y

# 安装 Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install nodejs -y
```

### 3. 上传项目文件
使用 XFTP 或 scp 上传以下内容到服务器：

```bash
# 方式1: 使用 git 克隆
cd /opt/sihui
git clone https://github.com/your-username/Sihui.git

# 方式2: 使用 XFTP 上传本地项目到 /opt/sihui/Sihui
```

### 4. 上传配置文件
将本地生成的配置文件上传到服务器对应位置：

```bash
# 环境变量文件
/opt/sihui/Sihui/sihui-backend/.env
/opt/sihui/Sihui/admin-dashboard_3/.env.production

# Nginx配置
/etc/nginx/conf.d/sihui.conf

# 系统服务
/etc/systemd/system/sihui-backend.service
/etc/systemd/system/sihui-frontend.service

# SSL证书（如有）
/opt/sihui/ssl/$DomainName.crt
/opt/sihui/ssl/$DomainName.key
```

### 5. 数据库初始化
```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/init-database.sql

# 执行项目数据库迁移
cd /opt/sihui/Sihui/sihui-backend
mysql -u sihui_user -p sihui_production < src/main/resources/db/migration/V1__Create_core_tables.sql
mysql -u sihui_user -p sihui_production < src/main/resources/db/migration/V2__Insert_initial_data.sql
mysql -u sihui_user -p sihui_production < src/main/resources/db/migration/V3__Create_chat_tables.sql
```

### 6. 执行部署
```bash
# 上传并执行部署脚本
chmod +x /path/to/deploy.sh
./deploy.sh
```

### 7. 配置防火墙
```bash
# 启用防火墙
ufw enable

# 开放必要端口
ufw allow 22    # SSH
ufw allow 80    # HTTP  
ufw allow 443   # HTTPS

# 查看状态
ufw status
```

### 8. 启动服务
```bash
# 重载系统服务
systemctl daemon-reload

# 启动并设置开机自启
systemctl enable mysql nginx sihui-backend sihui-frontend
systemctl start mysql nginx sihui-backend sihui-frontend

# 检查服务状态
systemctl status sihui-backend sihui-frontend nginx
```

## ✅ 部署验证

### 检查服务状态
```bash
# 检查端口监听
netstat -tlnp | grep -E ':80|:443|:8080|:3000'

# 检查日志
journalctl -u sihui-backend -f
journalctl -u sihui-frontend -f

# 测试 Nginx 配置
nginx -t
```

### 访问测试
- [ ] 后端API: https://api.$DomainName/health
- [ ] 管理端: https://admin.$DomainName
- [ ] SSL证书: 检查浏览器是否显示安全锁

## 🔧 常用管理命令

### 服务管理
```bash
# 重启服务
systemctl restart sihui-backend sihui-frontend

# 查看日志
journalctl -u sihui-backend --since "1 hour ago"
journalctl -u sihui-frontend --since "1 hour ago"

# 重新加载 Nginx 配置
nginx -t && systemctl reload nginx
```

### 备份
```bash
# 手动执行备份
/path/to/backup.sh

# 设置定时备份（每天凌晨2点）
crontab -e
# 添加：0 2 * * * /path/to/backup.sh
```

## 🆘 问题排查

### 常见问题
1. **服务启动失败**: 检查日志 `journalctl -u service-name`
2. **数据库连接失败**: 检查数据库服务和密码
3. **文件权限问题**: 确保 www-data 用户有相应权限
4. **端口被占用**: 使用 `netstat -tlnp` 检查端口占用
5. **SSL证书问题**: 检查证书文件路径和权限

### 日志位置
- Nginx: `/var/log/nginx/`
- 后端: `journalctl -u sihui-backend`
- 前端: `journalctl -u sihui-frontend`
- 系统: `/var/log/syslog`

---

## 📞 联系支持

如有问题，请联系开发团队或查看项目文档。

**部署完成后，请访问：**
- 🌐 管理端: https://admin.$DomainName
- 🔗 API文档: https://api.$DomainName/swagger-ui.html
"@

$DeployInstructionsPath = Join-Path $OutputDir "部署指令.md"
Set-Content -Path $DeployInstructionsPath -Value $DeployInstructionsContent -Encoding UTF8
Write-Host "✅ 创建部署指令文档: $DeployInstructionsPath" -ForegroundColor Green

# 创建压缩包
Write-Host "📦 创建部署文件压缩包..." -ForegroundColor Yellow

$ZipPath = "$OutputDir.zip"
if (Test-Path $ZipPath) {
    Remove-Item $ZipPath -Force
}

try {
    Compress-Archive -Path "$OutputDir\*" -DestinationPath $ZipPath -Force
    Write-Host "✅ 部署文件已打包为: $ZipPath" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 打包失败，但文件已生成在: $OutputDir" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 部署文件准备完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📁 输出目录: $OutputDir" -ForegroundColor Cyan
Write-Host "📦 压缩包: $ZipPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚡ 下一步操作：" -ForegroundColor Yellow
Write-Host "1. 编辑配置文件，填写实际的域名、密码、API密钥等信息" -ForegroundColor White
Write-Host "2. 使用 XFTP 将文件上传到服务器" -ForegroundColor White
Write-Host "3. 按照 '部署指令.md' 中的步骤完成部署" -ForegroundColor White
Write-Host ""
Write-Host "🌐 预计访问地址：" -ForegroundColor Yellow
Write-Host "   管理端: https://admin.$DomainName" -ForegroundColor White
Write-Host "   API: https://api.$DomainName" -ForegroundColor White 