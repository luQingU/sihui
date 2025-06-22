#!/bin/bash
# ===========================================
# Sihui 项目服务器一键安装脚本
# ===========================================

set -e

echo "🚀 开始安装 Sihui 项目..."
echo "====================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否为root用户
if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}错误: 请以root用户身份运行此脚本${NC}"
   echo "使用: sudo bash server-install.sh"
   exit 1
fi

# 获取当前脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="/opt/sihui"

echo -e "${BLUE}📋 脚本目录: $SCRIPT_DIR${NC}"
echo -e "${BLUE}📁 项目目录: $PROJECT_DIR${NC}"
echo ""

# 步骤1: 系统更新
echo -e "${YELLOW}📦 步骤1: 更新系统...${NC}"
apt update && apt upgrade -y
echo -e "${GREEN}✅ 系统更新完成${NC}"

# 步骤2: 创建目录结构
echo -e "${YELLOW}📁 步骤2: 创建目录结构...${NC}"
mkdir -p $PROJECT_DIR/{backend,frontend,ssl,logs,uploads,backup}
echo -e "${GREEN}✅ 目录结构创建完成${NC}"

# 步骤3: 安装基础软件
echo -e "${YELLOW}⬇️ 步骤3: 安装基础软件...${NC}"

# 安装 Java 8
echo "安装 Java 8..."
apt install openjdk-8-jdk -y

# 安装 Maven
echo "安装 Maven..."
apt install maven -y

# 安装 MySQL 8
echo "安装 MySQL 8..."
apt install mysql-server -y

# 安装 Nginx
echo "安装 Nginx..."
apt install nginx -y

# 安装 Git
echo "安装 Git..."
apt install git -y

# 安装 Node.js 18
echo "安装 Node.js 18..."
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install nodejs -y

echo -e "${GREEN}✅ 基础软件安装完成${NC}"

# 步骤4: 验证安装
echo -e "${YELLOW}🔍 步骤4: 验证软件版本...${NC}"
echo "Java版本: $(java -version 2>&1 | head -n 1)"
echo "Maven版本: $(mvn -v | head -n 1)"
echo "MySQL版本: $(mysql --version)"
echo "Node.js版本: $(node --version)"
echo "NPM版本: $(npm --version)"
echo "Nginx版本: $(nginx -v 2>&1)"
echo -e "${GREEN}✅ 软件版本验证完成${NC}"

# 步骤5: 配置 MySQL 数据库
echo -e "${YELLOW}🗄️ 步骤5: 配置数据库...${NC}"

# 启动 MySQL 服务
systemctl start mysql
systemctl enable mysql

# 检查配置文件是否存在
if [ -f "$SCRIPT_DIR/backend/.env" ]; then
    # 从配置文件读取数据库信息
    DB_PASSWORD=$(grep "DB_PASSWORD=" "$SCRIPT_DIR/backend/.env" | cut -d'=' -f2)
    
    if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "请填写您的数据库密码" ]; then
        echo -e "${RED}错误: 请先在 backend/.env 文件中填写数据库密码${NC}"
        exit 1
    fi
    
    echo "使用配置文件中的数据库密码..."
    
    # 创建数据库和用户
    mysql -u root << EOF
CREATE DATABASE IF NOT EXISTS sihui_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'sihui_user'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON sihui_production.* TO 'sihui_user'@'localhost';
FLUSH PRIVILEGES;
EOF

    echo -e "${GREEN}✅ 数据库配置完成${NC}"
else
    echo -e "${RED}错误: 未找到 backend/.env 配置文件${NC}"
    exit 1
fi

# 步骤6: 复制配置文件
echo -e "${YELLOW}📋 步骤6: 部署配置文件...${NC}"

# 复制环境变量文件
if [ -f "$SCRIPT_DIR/backend/.env" ]; then
    cp "$SCRIPT_DIR/backend/.env" "$PROJECT_DIR/backend/.env"
    echo "✅ 后端环境变量文件已部署"
else
    echo -e "${RED}❌ 后端环境变量文件不存在${NC}"
fi

if [ -f "$SCRIPT_DIR/frontend/.env.production" ]; then
    cp "$SCRIPT_DIR/frontend/.env.production" "$PROJECT_DIR/frontend/.env.production"
    echo "✅ 前端环境变量文件已部署"
else
    echo -e "${RED}❌ 前端环境变量文件不存在${NC}"
fi

# 复制 Nginx 配置
if [ -f "$SCRIPT_DIR/nginx/sihui.conf" ]; then
    cp "$SCRIPT_DIR/nginx/sihui.conf" "/etc/nginx/conf.d/sihui.conf"
    echo "✅ Nginx 配置文件已部署"
else
    echo -e "${RED}❌ Nginx 配置文件不存在${NC}"
fi

# 复制系统服务文件
if [ -f "$SCRIPT_DIR/systemd/sihui-backend.service" ]; then
    cp "$SCRIPT_DIR/systemd/sihui-backend.service" "/etc/systemd/system/"
    echo "✅ 后端服务配置已部署"
else
    echo -e "${RED}❌ 后端服务配置文件不存在${NC}"
fi

if [ -f "$SCRIPT_DIR/systemd/sihui-frontend.service" ]; then
    cp "$SCRIPT_DIR/systemd/sihui-frontend.service" "/etc/systemd/system/"
    echo "✅ 前端服务配置已部署"
else
    echo -e "${RED}❌ 前端服务配置文件不存在${NC}"
fi

# 复制 SSL 证书（如果存在）
if [ -d "$SCRIPT_DIR/ssl" ] && [ "$(ls -A $SCRIPT_DIR/ssl)" ]; then
    cp -r "$SCRIPT_DIR/ssl/"* "$PROJECT_DIR/ssl/"
    chmod 600 "$PROJECT_DIR/ssl/"*
    echo "✅ SSL 证书已部署"
else
    echo "⚠️ 未发现 SSL 证书文件，将使用 HTTP 或稍后配置"
fi

echo -e "${GREEN}✅ 配置文件部署完成${NC}"

# 步骤7: 克隆或准备项目代码
echo -e "${YELLOW}📦 步骤7: 准备项目代码...${NC}"

if [ -d "$SCRIPT_DIR/Sihui" ]; then
    echo "发现项目源码，复制到目标目录..."
    cp -r "$SCRIPT_DIR/Sihui" "$PROJECT_DIR/"
    echo "✅ 项目代码已复制"
elif [ ! -z "$1" ]; then
    echo "从 Git 仓库克隆项目: $1"
    cd $PROJECT_DIR
    git clone "$1" Sihui
    echo "✅ 项目代码已克隆"
else
    echo -e "${YELLOW}⚠️ 项目代码需要手动上传到: $PROJECT_DIR/Sihui${NC}"
    echo "请确保项目结构如下:"
    echo "  $PROJECT_DIR/Sihui/sihui-backend/"
    echo "  $PROJECT_DIR/Sihui/admin-dashboard_3/"
fi

# 步骤8: 构建项目
if [ -d "$PROJECT_DIR/Sihui/sihui-backend" ]; then
    echo -e "${YELLOW}🔨 步骤8: 构建后端项目...${NC}"
    cd "$PROJECT_DIR/Sihui/sihui-backend"
    
    # 复制环境变量文件到项目目录
    if [ -f "$PROJECT_DIR/backend/.env" ]; then
        cp "$PROJECT_DIR/backend/.env" ./.env
    fi
    
    mvn clean package -DskipTests
    cp target/sihui-backend-*.jar "$PROJECT_DIR/backend/app.jar"
    echo -e "${GREEN}✅ 后端构建完成${NC}"
else
    echo -e "${RED}❌ 后端项目目录不存在，请检查项目结构${NC}"
fi

if [ -d "$PROJECT_DIR/Sihui/admin-dashboard_3" ]; then
    echo -e "${YELLOW}🔨 步骤8: 构建前端项目...${NC}"
    cd "$PROJECT_DIR/Sihui/admin-dashboard_3"
    
    # 复制环境变量文件到项目目录
    if [ -f "$PROJECT_DIR/frontend/.env.production" ]; then
        cp "$PROJECT_DIR/frontend/.env.production" ./.env.production
    fi
    
    npm install --production
    npm run build
    cp -r .next "$PROJECT_DIR/frontend/"
    cp -r public "$PROJECT_DIR/frontend/"
    cp package.json "$PROJECT_DIR/frontend/"
    cp next.config.mjs "$PROJECT_DIR/frontend/" 2>/dev/null || echo "next.config.mjs 不存在，跳过"
    echo -e "${GREEN}✅ 前端构建完成${NC}"
else
    echo -e "${RED}❌ 前端项目目录不存在，请检查项目结构${NC}"
fi

# 步骤9: 执行数据库迁移
echo -e "${YELLOW}🗄️ 步骤9: 执行数据库迁移...${NC}"
if [ -d "$PROJECT_DIR/Sihui/sihui-backend/src/main/resources/db/migration" ]; then
    cd "$PROJECT_DIR/Sihui/sihui-backend"
    
    # 读取数据库配置
    DB_PASSWORD=$(grep "DB_PASSWORD=" .env | cut -d'=' -f2)
    
    # 执行迁移脚本
    for sql_file in src/main/resources/db/migration/V*.sql; do
        if [ -f "$sql_file" ]; then
            echo "执行: $sql_file"
            mysql -u sihui_user -p"$DB_PASSWORD" sihui_production < "$sql_file"
        fi
    done
    echo -e "${GREEN}✅ 数据库迁移完成${NC}"
else
    echo -e "${YELLOW}⚠️ 未找到数据库迁移文件${NC}"
fi

# 步骤10: 设置权限
echo -e "${YELLOW}🔐 步骤10: 设置文件权限...${NC}"
chown -R www-data:www-data $PROJECT_DIR
chmod -R 755 $PROJECT_DIR
echo -e "${GREEN}✅ 文件权限设置完成${NC}"

# 步骤11: 配置防火墙
echo -e "${YELLOW}🛡️ 步骤11: 配置防火墙...${NC}"
ufw --force enable
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment 'SSH'
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'
echo -e "${GREEN}✅ 防火墙配置完成${NC}"

# 步骤12: 启动服务
echo -e "${YELLOW}🚀 步骤12: 启动服务...${NC}"

# 重载服务配置
systemctl daemon-reload

# 启动并设置开机自启
systemctl enable mysql nginx sihui-backend sihui-frontend
systemctl start mysql nginx

# 检查 Nginx 配置
nginx -t
if [ $? -eq 0 ]; then
    systemctl start sihui-backend sihui-frontend
    echo -e "${GREEN}✅ 所有服务启动完成${NC}"
else
    echo -e "${RED}❌ Nginx 配置有误，请检查配置文件${NC}"
fi

# 步骤13: 验证部署
echo -e "${YELLOW}🔍 步骤13: 验证部署...${NC}"
sleep 5

echo "检查服务状态..."
systemctl is-active mysql && echo "✅ MySQL: 运行中" || echo "❌ MySQL: 未运行"
systemctl is-active nginx && echo "✅ Nginx: 运行中" || echo "❌ Nginx: 未运行"
systemctl is-active sihui-backend && echo "✅ 后端服务: 运行中" || echo "❌ 后端服务: 未运行"
systemctl is-active sihui-frontend && echo "✅ 前端服务: 运行中" || echo "❌ 前端服务: 未运行"

echo ""
echo "检查端口监听..."
netstat -tlnp | grep -E ':80|:443|:8080|:3000' | head -10

# 获取域名信息
if [ -f "$PROJECT_DIR/frontend/.env.production" ]; then
    API_URL=$(grep "NEXT_PUBLIC_API_URL=" "$PROJECT_DIR/frontend/.env.production" | cut -d'=' -f2)
    ADMIN_URL=$(echo $API_URL | sed 's/api\./admin./')
fi

echo ""
echo -e "${GREEN}🎉 Sihui 项目安装完成！${NC}"
echo "====================================="
echo -e "${BLUE}📋 部署信息:${NC}"
echo "项目目录: $PROJECT_DIR"
echo "配置文件: $PROJECT_DIR/backend/.env"
echo "日志目录: $PROJECT_DIR/logs"
echo ""
echo -e "${BLUE}🌐 访问地址:${NC}"
if [ ! -z "$ADMIN_URL" ]; then
    echo "管理端: $ADMIN_URL"
    echo "API接口: $API_URL"
    echo "API文档: $API_URL/swagger-ui.html"
else
    echo "管理端: https://admin.您的实际域名.com"
    echo "API接口: https://api.您的实际域名.com"
fi
echo ""
echo -e "${BLUE}🔧 常用命令:${NC}"
echo "查看后端日志: journalctl -u sihui-backend -f"
echo "查看前端日志: journalctl -u sihui-frontend -f"
echo "重启后端: systemctl restart sihui-backend"
echo "重启前端: systemctl restart sihui-frontend"
echo "重启Nginx: systemctl restart nginx"
echo ""
echo -e "${YELLOW}⚠️ 注意事项:${NC}"
echo "1. 请确保域名已正确解析到本服务器"
echo "2. 如使用自定义SSL证书，请放置在 $PROJECT_DIR/ssl/ 目录"
echo "3. 定期备份数据库: mysqldump -u sihui_user -p sihui_production > backup.sql"
echo "4. 监控服务器资源使用情况"
echo ""
echo -e "${GREEN}🎊 安装完成，享受您的Sihui系统吧！${NC}" 