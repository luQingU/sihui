#!/bin/bash

# ===========================================
# Sihui 项目数据备份脚本
# ===========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# 配置信息
BACKUP_DIR="/opt/sihui/backup"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# 读取数据库配置
if [ -f "/opt/sihui/backend/.env" ]; then
    source /opt/sihui/backend/.env
else
    log_error "未找到配置文件 /opt/sihui/backend/.env"
    exit 1
fi

# 创建备份目录
mkdir -p $BACKUP_DIR

log_info "开始执行备份任务..."

# 备份数据库
backup_database() {
    log_info "备份数据库..."
    
    local db_backup_file="$BACKUP_DIR/database_$DATE.sql"
    
    if mysqldump -u ${DB_USERNAME:-sihui_user} -p${DB_PASSWORD} ${DB_NAME:-sihui_production} > $db_backup_file; then
        log_info "数据库备份完成: $db_backup_file"
        
        # 压缩数据库备份
        gzip $db_backup_file
        log_info "数据库备份已压缩: ${db_backup_file}.gz"
    else
        log_error "数据库备份失败"
        exit 1
    fi
}

# 备份上传文件
backup_uploads() {
    log_info "备份上传文件..."
    
    local uploads_backup_file="$BACKUP_DIR/uploads_$DATE.tar.gz"
    
    if [ -d "/opt/sihui/uploads" ] && [ "$(ls -A /opt/sihui/uploads)" ]; then
        tar -czf $uploads_backup_file /opt/sihui/uploads/
        log_info "上传文件备份完成: $uploads_backup_file"
    else
        log_warn "上传目录为空或不存在，跳过文件备份"
    fi
}

# 备份配置文件
backup_configs() {
    log_info "备份配置文件..."
    
    local config_backup_file="$BACKUP_DIR/configs_$DATE.tar.gz"
    
    tar -czf $config_backup_file \
        /opt/sihui/backend/.env \
        /opt/sihui/frontend/.env.production \
        /etc/nginx/sites-available/sihui.conf \
        /etc/systemd/system/sihui-*.service \
        2>/dev/null || true
    
    log_info "配置文件备份完成: $config_backup_file"
}

# 备份应用程序
backup_applications() {
    log_info "备份应用程序..."
    
    local app_backup_file="$BACKUP_DIR/applications_$DATE.tar.gz"
    
    tar -czf $app_backup_file \
        /opt/sihui/backend/app.jar \
        /opt/sihui/frontend/.next \
        2>/dev/null || true
    
    log_info "应用程序备份完成: $app_backup_file"
}

# 清理过期备份
cleanup_old_backups() {
    log_info "清理${RETENTION_DAYS}天前的备份文件..."
    
    # 删除过期的备份文件
    find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true
    find $BACKUP_DIR -name "*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true
    
    log_info "过期备份清理完成"
}

# 生成备份报告
generate_backup_report() {
    local report_file="$BACKUP_DIR/backup_report_$DATE.txt"
    
    cat > $report_file <<EOF
===========================================
Sihui 项目备份报告
===========================================

备份时间: $(date)
备份目录: $BACKUP_DIR

备份文件列表:
$(ls -lh $BACKUP_DIR/*$DATE* 2>/dev/null || echo "无备份文件")

磁盘使用情况:
$(df -h $BACKUP_DIR)

系统状态:
- 数据库状态: $(systemctl is-active mysql)
- 后端服务状态: $(systemctl is-active sihui-backend)
- 前端服务状态: $(systemctl is-active sihui-frontend)
- Nginx状态: $(systemctl is-active nginx)

备份完成时间: $(date)
===========================================
EOF
    
    log_info "备份报告生成: $report_file"
}

# 发送备份通知（可选）
send_notification() {
    # 如果配置了邮件服务，可以发送通知
    if [ ! -z "$MAIL_FROM" ] && [ ! -z "$MAIL_USERNAME" ]; then
        local subject="Sihui 系统备份完成 - $DATE"
        local body="备份任务已完成，请检查备份文件。备份目录: $BACKUP_DIR"
        
        # 这里可以添加邮件发送逻辑
        log_info "备份通知已准备（需要配置邮件服务）"
    fi
}

# 主函数
main() {
    echo "🚀 开始 Sihui 系统备份..."
    echo ""
    
    # 检查权限
    if [[ $EUID -ne 0 ]]; then
        log_error "此脚本需要root权限运行"
        exit 1
    fi
    
    # 检查服务状态
    if ! systemctl is-active --quiet mysql; then
        log_error "MySQL服务未运行，无法进行备份"
        exit 1
    fi
    
    # 执行备份
    backup_database
    backup_uploads
    backup_configs
    backup_applications
    cleanup_old_backups
    generate_backup_report
    send_notification
    
    echo ""
    log_info "✅ 备份任务完成！"
    log_info "备份文件保存在: $BACKUP_DIR"
    log_info "备份报告: $BACKUP_DIR/backup_report_$DATE.txt"
    
    # 显示备份文件大小
    echo ""
    echo "📁 备份文件详情:"
    ls -lh $BACKUP_DIR/*$DATE* 2>/dev/null || echo "无备份文件"
    
    echo ""
    echo "💾 磁盘使用情况:"
    df -h $BACKUP_DIR
}

# 如果脚本被直接执行
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi