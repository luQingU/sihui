#!/bin/bash

# ===========================================
# Sihui 项目自动化部署安装脚本
# ===========================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查root权限
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "此脚本需要root权限运行"
        exit 1
    fi
}

# 检测操作系统
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
    else
        log_error "无法检测操作系统"
        exit 1
    fi
    
    log_info "检测到操作系统: $OS $VER"
}

# 更新系统
update_system() {
    log_step "更新系统包..."
    
    if [[ "$OS" == *"Ubuntu"* ]] || [[ "$OS" == *"Debian"* ]]; then
        apt update && apt upgrade -y
    elif [[ "$OS" == *"CentOS"* ]] || [[ "$OS" == *"Red Hat"* ]]; then
        yum update -y
    else
        log_warn "未知的操作系统，跳过系统更新"
    fi
}

# 安装基础软件
install_base_packages() {
    log_step "安装基础软件包..."
    
    if [[ "$OS" == *"Ubuntu"* ]] || [[ "$OS" == *"Debian"* ]]; then
        apt install -y curl wget git unzip nginx mysql-server openjdk-8-jdk maven
        
        # 安装Node.js 18
        curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
        apt install -y nodejs
        
    elif [[ "$OS" == *"CentOS"* ]] || [[ "$OS" == *"Red Hat"* ]]; then
        yum install -y curl wget git unzip nginx mysql-server java-1.8.0-openjdk maven
        
        # 安装Node.js 18
        curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
        yum install -y nodejs
    fi
    
    log_info "基础软件安装完成"
}

# 创建部署目录
create_directories() {
    log_step "创建部署目录结构..."
    
    mkdir -p /opt/sihui/{backend,frontend,nginx,ssl,logs,uploads,backup}
    
    # 设置权限
    chown -R www-data:www-data /opt/sihui 2>/dev/null || chown -R nginx:nginx /opt/sihui
    chmod -R 755 /opt/sihui
    
    log_info "目录结构创建完成"
}

# 配置MySQL
setup_mysql() {
    log_step "配置MySQL数据库..."
    
    # 启动MySQL服务
    systemctl start mysql 2>/dev/null || systemctl start mysqld
    systemctl enable mysql 2>/dev/null || systemctl enable mysqld
    
    # 检查.env文件是否存在
    if [ ! -f "./backend/.env" ]; then
        log_error "未找到backend/.env配置文件，请先配置环境变量"
        exit 1
    fi
    
    # 从.env文件读取数据库配置
    source ./backend/.env
    
    if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "请填写数据库密码" ]; then
        log_error "请在backend/.env文件中设置数据库密码"
        exit 1
    fi
    
    # 设置MySQL root密码并创建数据库
    mysql -u root <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${DB_PASSWORD}';
CREATE DATABASE IF NOT EXISTS ${DB_NAME:-sihui_production};
CREATE USER IF NOT EXISTS '${DB_USERNAME:-sihui_user}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ${DB_NAME:-sihui_production}.* TO '${DB_USERNAME:-sihui_user}'@'localhost';
FLUSH PRIVILEGES;
EOF
    
    log_info "MySQL配置完成"
}

# 部署后端服务
deploy_backend() {
    log_step "部署后端服务..."
    
    if [ ! -d "./Sihui/sihui-backend" ]; then
        log_error "未找到后端项目源码，请确保Sihui项目已复制到部署包中"
        exit 1
    fi
    
    # 复制后端项目到部署目录
    cp -r ./Sihui/sihui-backend/* /opt/sihui/backend/
    
    # 复制环境变量文件
    cp ./backend/.env /opt/sihui/backend/
    
    # 构建后端项目
    cd /opt/sihui/backend
    mvn clean package -DskipTests
    
    # 检查构建结果
    if [ ! -f "target/sihui-backend-*.jar" ]; then
        log_error "后端项目构建失败"
        exit 1
    fi
    
    # 复制jar文件
    cp target/sihui-backend-*.jar /opt/sihui/backend/app.jar
    
    log_info "后端部署完成"
}

# 部署前端服务
deploy_frontend() {
    log_step "部署前端服务..."
    
    if [ ! -d "./Sihui/admin-dashboard_3" ]; then
        log_error "未找到前端项目源码"
        exit 1
    fi
    
    # 复制前端项目到部署目录
    cp -r ./Sihui/admin-dashboard_3/* /opt/sihui/frontend/
    
    # 复制生产环境配置
    if [ -f "./frontend/.env.production" ]; then
        cp ./frontend/.env.production /opt/sihui/frontend/
    fi
    
    # 构建前端项目
    cd /opt/sihui/frontend
    npm ci --production
    npm run build
    
    log_info "前端部署完成"
}

# 配置Nginx
setup_nginx() {
    log_step "配置Nginx..."
    
    # 复制Nginx配置
    if [ -f "./nginx/sihui.conf" ]; then
        cp ./nginx/sihui.conf /etc/nginx/sites-available/ 2>/dev/null || cp ./nginx/sihui.conf /etc/nginx/conf.d/
        
        # 启用站点 (Ubuntu/Debian)
        if [ -d "/etc/nginx/sites-enabled" ]; then
            ln -sf /etc/nginx/sites-available/sihui.conf /etc/nginx/sites-enabled/
        fi
    else
        log_error "未找到Nginx配置文件"
        exit 1
    fi
    
    # 复制SSL证书
    if [ -d "./ssl" ]; then
        cp ./ssl/* /opt/sihui/ssl/
        chmod 600 /opt/sihui/ssl/*
    else
        log_warn "未找到SSL证书文件"
    fi
    
    # 测试Nginx配置
    nginx -t
    
    log_info "Nginx配置完成"
}

# 创建系统服务
create_systemd_services() {
    log_step "创建系统服务..."
    
    # 后端服务
    cat > /etc/systemd/system/sihui-backend.service <<EOF
[Unit]
Description=Sihui Backend Service
After=mysql.service network.target

[Service]
Type=simple
User=www-data
Group=www-data
ExecStart=/usr/bin/java -jar /opt/sihui/backend/app.jar
WorkingDirectory=/opt/sihui/backend
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=production
StandardOutput=journal
StandardError=journal
SyslogIdentifier=sihui-backend

[Install]
WantedBy=multi-user.target
EOF

    # 前端服务
    cat > /etc/systemd/system/sihui-frontend.service <<EOF
[Unit]
Description=Sihui Frontend Service
After=network.target

[Service]
Type=simple
User=www-data
Group=www-data
ExecStart=/usr/bin/npm start
WorkingDirectory=/opt/sihui/frontend
Restart=always
RestartSec=10
Environment=NODE_ENV=production
StandardOutput=journal
StandardError=journal
SyslogIdentifier=sihui-frontend

[Install]
WantedBy=multi-user.target
EOF

    # 重新加载systemd
    systemctl daemon-reload
    
    log_info "系统服务创建完成"
}

# 启动服务
start_services() {
    log_step "启动服务..."
    
    # 启动并设置开机自启
    systemctl enable --now mysql nginx sihui-backend sihui-frontend
    
    # 等待服务启动
    sleep 10
    
    # 检查服务状态
    if systemctl is-active --quiet sihui-backend; then
        log_info "后端服务启动成功"
    else
        log_error "后端服务启动失败"
        systemctl status sihui-backend
    fi
    
    if systemctl is-active --quiet sihui-frontend; then
        log_info "前端服务启动成功"
    else
        log_error "前端服务启动失败"
        systemctl status sihui-frontend
    fi
    
    if systemctl is-active --quiet nginx; then
        log_info "Nginx服务启动成功"
    else
        log_error "Nginx服务启动失败"
        systemctl status nginx
    fi
}

# 配置防火墙
setup_firewall() {
    log_step "配置防火墙..."
    
    # Ubuntu/Debian
    if command -v ufw >/dev/null 2>&1; then
        ufw --force enable
        ufw allow ssh
        ufw allow 80/tcp
        ufw allow 443/tcp
        log_info "UFW防火墙配置完成"
    
    # CentOS/RHEL
    elif command -v firewall-cmd >/dev/null 2>&1; then
        systemctl enable --now firewalld
        firewall-cmd --permanent --add-service=ssh
        firewall-cmd --permanent --add-service=http
        firewall-cmd --permanent --add-service=https
        firewall-cmd --reload
        log_info "Firewall防火墙配置完成"
    else
        log_warn "未找到防火墙工具，请手动配置"
    fi
}

# 创建备份脚本
create_backup_script() {
    log_step "创建备份脚本..."
    
    cat > /opt/sihui/backup/backup.sh <<'EOF'
#!/bin/bash

# 备份配置
BACKUP_DIR="/opt/sihui/backup"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
source /opt/sihui/backend/.env
mysqldump -u ${DB_USERNAME:-sihui_user} -p${DB_PASSWORD} ${DB_NAME:-sihui_production} > $BACKUP_DIR/database_$DATE.sql

# 备份上传文件
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /opt/sihui/uploads/

# 清理7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "备份完成: $DATE"
EOF

    chmod +x /opt/sihui/backup/backup.sh
    
    # 添加到crontab (每天2点备份)
    (crontab -l 2>/dev/null; echo "0 2 * * * /opt/sihui/backup/backup.sh") | crontab -
    
    log_info "备份脚本创建完成"
}

# 部署验证
verify_deployment() {
    log_step "验证部署..."
    
    # 检查端口
    if ss -tlnp | grep -q ":80\|:443\|:8080\|:3000"; then
        log_info "服务端口正常监听"
    else
        log_warn "部分服务端口未监听，请检查服务状态"
    fi
    
    # 检查健康状态
    sleep 5
    if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_info "后端健康检查通过"
    else
        log_warn "后端健康检查失败，服务可能需要更多时间启动"
    fi
    
    log_info "部署验证完成"
}

# 显示部署结果
show_deployment_result() {
    log_step "部署结果总结"
    
    echo ""
    echo "🎉 Sihui项目部署完成！"
    echo ""
    echo "📋 服务信息:"
    echo "   后端API: http://your-domain.com:8080"
    echo "   前端管理: http://your-domain.com:3000"
    echo "   Swagger文档: http://your-domain.com:8080/swagger-ui.html"
    echo ""
    echo "🔧 管理命令:"
    echo "   查看服务状态: systemctl status sihui-backend sihui-frontend nginx"
    echo "   查看实时日志: journalctl -u sihui-backend -f"
    echo "   重启服务: systemctl restart sihui-backend"
    echo "   运行备份: /opt/sihui/backup/backup.sh"
    echo ""
    echo "📁 重要目录:"
    echo "   部署目录: /opt/sihui/"
    echo "   日志目录: /opt/sihui/logs/"
    echo "   备份目录: /opt/sihui/backup/"
    echo "   上传目录: /opt/sihui/uploads/"
    echo ""
    echo "⚠️ 下一步操作:"
    echo "   1. 配置域名DNS解析到此服务器IP"
    echo "   2. 检查SSL证书配置"
    echo "   3. 测试访问系统功能"
    echo ""
}

# 主函数
main() {
    echo "🚀 开始Sihui项目自动化部署..."
    echo ""
    
    check_root
    detect_os
    update_system
    install_base_packages
    create_directories
    setup_mysql
    deploy_backend
    deploy_frontend
    setup_nginx
    create_systemd_services
    start_services
    setup_firewall
    create_backup_script
    verify_deployment
    show_deployment_result
    
    log_info "部署完成！请检查上述信息并测试系统功能。"
}

# 运行主函数
main "$@"