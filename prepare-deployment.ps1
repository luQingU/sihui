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

# 4. 创建系统服务文件 (后端)
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
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# 环境变量
Environment=SPRING_PROFILES_ACTIVE=production
Environment=JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai

[Install]
WantedBy=multi-user.target
"@

$BackendServicePath = Join-Path $OutputDir "systemd\sihui-backend.service"
Set-Content -Path $BackendServicePath -Value $BackendServiceContent -Encoding UTF8
Write-Host "✅ 创建后端系统服务文件: $BackendServicePath" -ForegroundColor Green

# 5. 创建系统服务文件 (前端)
$FrontendServiceContent = @"
[Unit]
Description=Sihui Frontend Service
Documentation=https://github.com/your-repo/sihui
After=network.target

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/sihui/frontend
ExecStart=/usr/bin/npm start
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# 环境变量
Environment=NODE_ENV=production
Environment=PORT=3000

[Install]
WantedBy=multi-user.target
"@

$FrontendServicePath = Join-Path $OutputDir "systemd\sihui-frontend.service"
Set-Content -Path $FrontendServicePath -Value $FrontendServiceContent -Encoding UTF8
Write-Host "✅ 创建前端系统服务文件: $FrontendServicePath" -ForegroundColor Green

# 创建部署指令文档
$DeployInstructionsContent = @"
# 🚀 Sihui 项目服务器部署指令

## 📋 快速部署步骤

### 1. 准备工作
- [ ] 服务器已安装 Ubuntu 20.04+ 或 CentOS 8+
- [ ] 域名已解析到服务器IP: $DomainName
- [ ] 已填写所有配置文件中的 "请填写..." 项目

### 2. 连接服务器并初始化
```bash
# 连接服务器
ssh root@您的服务器IP

# 更新系统
apt update && apt upgrade -y

# 创建部署目录
mkdir -p /opt/sihui/{backend,frontend,ssl,logs,uploads,backup}

# 安装基础软件
apt install openjdk-8-jdk maven mysql-server nginx git -y

# 安装 Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install nodejs -y
```

### 3. 上传项目和配置文件
使用 XFTP 或其他工具上传：

**项目源码：**
- 上传整个项目到: `/opt/sihui/Sihui/`

**配置文件：**
- `backend/.env` → `/opt/sihui/Sihui/sihui-backend/.env`
- `frontend/.env.production` → `/opt/sihui/Sihui/admin-dashboard_3/.env.production`
- `nginx/sihui.conf` → `/etc/nginx/conf.d/sihui.conf`
- `systemd/*.service` → `/etc/systemd/system/`

**SSL证书（如有）：**
- `$DomainName.crt` → `/opt/sihui/ssl/`
- `$DomainName.key` → `/opt/sihui/ssl/`

### 4. 数据库初始化
```bash
# 配置 MySQL
mysql_secure_installation

# 登录 MySQL
mysql -u root -p

# 执行初始化
CREATE DATABASE sihui_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sihui_user'@'localhost' IDENTIFIED BY '您的数据库密码';
GRANT ALL PRIVILEGES ON sihui_production.* TO 'sihui_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 执行数据库迁移脚本
cd /opt/sihui/Sihui/sihui-backend
mysql -u sihui_user -p sihui_production < src/main/resources/db/migration/V1__Create_core_tables.sql
mysql -u sihui_user -p sihui_production < src/main/resources/db/migration/V2__Insert_initial_data.sql
mysql -u sihui_user -p sihui_production < src/main/resources/db/migration/V3__Create_chat_tables.sql
```

### 5. 构建和部署项目
```bash
# 构建后端
cd /opt/sihui/Sihui/sihui-backend
mvn clean package -DskipTests
cp target/sihui-backend-*.jar /opt/sihui/backend/app.jar

# 构建前端
cd /opt/sihui/Sihui/admin-dashboard_3
npm install --production
npm run build
cp -r .next /opt/sihui/frontend/
cp -r public /opt/sihui/frontend/
cp package.json /opt/sihui/frontend/
cp next.config.mjs /opt/sihui/frontend/

# 设置权限
chown -R www-data:www-data /opt/sihui
chmod -R 755 /opt/sihui
```

### 6. 启动服务
```bash
# 重载系统服务配置
systemctl daemon-reload

# 启动并设置开机自启
systemctl enable mysql nginx sihui-backend sihui-frontend
systemctl start mysql nginx sihui-backend sihui-frontend

# 检查服务状态
systemctl status sihui-backend sihui-frontend nginx
```

### 7. 配置防火墙
```bash
# 启用防火墙
ufw enable

# 开放端口
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS

# 查看状态
ufw status
```

## ✅ 验证部署

### 检查服务状态
```bash
# 检查端口监听
netstat -tlnp | grep -E ':80|:443|:8080|:3000'

# 检查日志
journalctl -u sihui-backend -f
journalctl -u sihui-frontend -f
```

### 访问测试
- 后端API: https://api.$DomainName/health
- 管理端: https://admin.$DomainName

## 🔧 常用管理命令

```bash
# 重启服务
systemctl restart sihui-backend sihui-frontend

# 查看实时日志
journalctl -u sihui-backend -f
journalctl -u sihui-frontend -f

# 重新加载 Nginx 配置
nginx -t && systemctl reload nginx

# 检查系统资源
top
df -h
free -h
```

## 🆘 问题排查

1. **服务启动失败**: 
   ```bash
   journalctl -u sihui-backend -l
   journalctl -u sihui-frontend -l
   ```

2. **数据库连接失败**: 检查数据库服务和 .env 文件中的密码

3. **端口被占用**: 
   ```bash
   netstat -tlnp | grep :端口号
   ```

4. **文件权限问题**: 
   ```bash
   chown -R www-data:www-data /opt/sihui
   chmod -R 755 /opt/sihui
   ```

---

## 🎉 部署完成

部署成功后，您可以访问：
- 🌐 管理端: https://admin.$DomainName
- 🔗 API接口: https://api.$DomainName
- 📚 API文档: https://api.$DomainName/swagger-ui.html

**注意事项：**
1. 定期备份数据库和上传文件
2. 监控服务器资源使用情况
3. 及时更新系统和依赖包
4. 配置 SSL 证书自动续期
"@

$DeployInstructionsPath = Join-Path $OutputDir "部署指令.md"
Set-Content -Path $DeployInstructionsPath -Value $DeployInstructionsContent -Encoding UTF8
Write-Host "✅ 创建部署指令文档: $DeployInstructionsPath" -ForegroundColor Green

Write-Host ""
Write-Host "🎉 部署文件准备完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📁 输出目录: $OutputDir" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚡ 下一步操作：" -ForegroundColor Yellow
Write-Host "1. 编辑 backend\.env 文件，填写数据库密码、JWT密钥、API密钥等" -ForegroundColor White
Write-Host "2. 编辑 frontend\.env.production 文件，确认API地址" -ForegroundColor White
Write-Host "3. 准备SSL证书文件（可选，也可以使用Let's Encrypt）" -ForegroundColor White
Write-Host "4. 使用 XFTP 将配置文件和项目上传到服务器" -ForegroundColor White
Write-Host "5. 按照 '部署指令.md' 中的步骤完成部署" -ForegroundColor White
Write-Host ""
Write-Host "🌐 预计访问地址：" -ForegroundColor Yellow
Write-Host "   管理端: https://admin.$DomainName" -ForegroundColor White
Write-Host "   API: https://api.$DomainName" -ForegroundColor White 