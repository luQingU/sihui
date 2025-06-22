#!/bin/bash

# 四会培训平台数据备份脚本
# 使用方法: ./scripts/backup.sh [选项]
# 选项: --full, --database-only, --config-only, --compress

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_TYPE="full"
COMPRESS=false
RETENTION_DAYS=30

# 解析命令行参数
for arg in "$@"; do
    case $arg in
        --full)
            BACKUP_TYPE="full"
            ;;
        --database-only)
            BACKUP_TYPE="database"
            ;;
        --config-only)
            BACKUP_TYPE="config"
            ;;
        --compress)
            COMPRESS=true
            ;;
        --retention=*)
            RETENTION_DAYS="${arg#*=}"
            ;;
    esac
done

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 日志函数
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# 创建备份目录
create_backup_directory() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    BACKUP_DIR="$PROJECT_ROOT/backup/$timestamp"
    mkdir -p "$BACKUP_DIR"
    log "创建备份目录: $BACKUP_DIR"
}

# 备份数据库
backup_database() {
    log "开始备份数据库..."
    
    # 检查MySQL容器是否运行
    if ! docker ps | grep -q sihui-mysql; then
        error "MySQL容器未运行，无法进行数据库备份"
    fi
    
    # 获取数据库密码
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        error "未找到 .env 文件，无法获取数据库密码"
    fi
    
    local db_password=$(grep DB_ROOT_PASSWORD "$PROJECT_ROOT/.env" | cut -d '=' -f2)
    local db_name=$(grep DB_NAME "$PROJECT_ROOT/.env" | cut -d '=' -f2)
    
    # 备份完整数据库
    log "备份完整数据库..."
    docker exec sihui-mysql mysqldump \
        -u root \
        -p"$db_password" \
        --single-transaction \
        --routines \
        --triggers \
        --all-databases > "$BACKUP_DIR/full_database_backup.sql"
    
    # 备份应用数据库
    log "备份应用数据库: $db_name"
    docker exec sihui-mysql mysqldump \
        -u root \
        -p"$db_password" \
        --single-transaction \
        --routines \
        --triggers \
        "$db_name" > "$BACKUP_DIR/app_database_backup.sql"
    
    # 备份数据库结构
    log "备份数据库结构..."
    docker exec sihui-mysql mysqldump \
        -u root \
        -p"$db_password" \
        --no-data \
        --routines \
        --triggers \
        "$db_name" > "$BACKUP_DIR/database_schema.sql"
    
    log "数据库备份完成"
}

# 备份Redis数据
backup_redis() {
    log "开始备份Redis数据..."
    
    # 检查Redis容器是否运行
    if ! docker ps | grep -q sihui-redis; then
        warn "Redis容器未运行，跳过Redis备份"
        return
    fi
    
    # 创建Redis备份
    docker exec sihui-redis redis-cli BGSAVE
    
    # 等待备份完成
    while [ "$(docker exec sihui-redis redis-cli LASTSAVE)" = "$(docker exec sihui-redis redis-cli LASTSAVE)" ]; do
        sleep 1
    done
    
    # 复制备份文件
    docker cp sihui-redis:/data/dump.rdb "$BACKUP_DIR/redis_dump.rdb"
    
    log "Redis备份完成"
}

# 备份配置文件
backup_configuration() {
    log "开始备份配置文件..."
    
    # 创建配置备份目录
    mkdir -p "$BACKUP_DIR/config"
    
    # 备份环境变量文件
    if [ -f "$PROJECT_ROOT/.env" ]; then
        cp "$PROJECT_ROOT/.env" "$BACKUP_DIR/config/"
        log "备份环境变量文件"
    fi
    
    # 备份Docker配置
    if [ -f "$PROJECT_ROOT/docker-compose.yml" ]; then
        cp "$PROJECT_ROOT/docker-compose.yml" "$BACKUP_DIR/config/"
        log "备份Docker Compose配置"
    fi
    
    # 备份Nginx配置
    if [ -d "$PROJECT_ROOT/config/nginx" ]; then
        cp -r "$PROJECT_ROOT/config/nginx" "$BACKUP_DIR/config/"
        log "备份Nginx配置"
    fi
    
    # 备份MySQL配置
    if [ -d "$PROJECT_ROOT/config/mysql" ]; then
        cp -r "$PROJECT_ROOT/config/mysql" "$BACKUP_DIR/config/"
        log "备份MySQL配置"
    fi
    
    # 备份应用配置
    if [ -f "$PROJECT_ROOT/sihui-backend/src/main/resources/application.properties" ]; then
        mkdir -p "$BACKUP_DIR/config/application"
        cp "$PROJECT_ROOT/sihui-backend/src/main/resources/application"*.properties "$BACKUP_DIR/config/application/" 2>/dev/null || true
        log "备份应用配置文件"
    fi
    
    log "配置文件备份完成"
}

# 备份应用文件
backup_application_files() {
    log "开始备份应用文件..."
    
    # 备份关键应用文件
    mkdir -p "$BACKUP_DIR/application"
    
    # 备份后端JAR文件
    if [ -f "$PROJECT_ROOT/sihui-backend/target/"*.jar ]; then
        cp "$PROJECT_ROOT/sihui-backend/target/"*.jar "$BACKUP_DIR/application/" 2>/dev/null || true
        log "备份后端JAR文件"
    fi
    
    # 备份前端构建文件
    if [ -d "$PROJECT_ROOT/sihui-admin/dist" ]; then
        cp -r "$PROJECT_ROOT/sihui-admin/dist" "$BACKUP_DIR/application/frontend-dist"
        log "备份前端构建文件"
    fi
    
    # 备份脚本文件
    if [ -d "$PROJECT_ROOT/scripts" ]; then
        cp -r "$PROJECT_ROOT/scripts" "$BACKUP_DIR/application/"
        log "备份脚本文件"
    fi
    
    log "应用文件备份完成"
}

# 备份日志文件
backup_logs() {
    log "开始备份日志文件..."
    
    mkdir -p "$BACKUP_DIR/logs"
    
    # 备份应用日志
    if [ -d "$PROJECT_ROOT/logs" ]; then
        cp -r "$PROJECT_ROOT/logs" "$BACKUP_DIR/"
        log "备份应用日志"
    fi
    
    # 导出Docker容器日志
    local containers=("sihui-backend" "sihui-frontend" "sihui-nginx" "sihui-mysql" "sihui-redis")
    for container in "${containers[@]}"; do
        if docker ps -a --format "{{.Names}}" | grep -q "^$container$"; then
            docker logs "$container" > "$BACKUP_DIR/logs/${container}.log" 2>&1
            log "备份 $container 容器日志"
        fi
    done
    
    log "日志文件备份完成"
}

# 压缩备份文件
compress_backup() {
    if [ "$COMPRESS" = true ]; then
        log "开始压缩备份文件..."
        
        local backup_name=$(basename "$BACKUP_DIR")
        local backup_parent=$(dirname "$BACKUP_DIR")
        
        cd "$backup_parent"
        tar -czf "${backup_name}.tar.gz" "$backup_name"
        
        # 删除原始备份目录
        rm -rf "$backup_name"
        
        log "备份文件已压缩: ${backup_name}.tar.gz"
        BACKUP_DIR="${backup_parent}/${backup_name}.tar.gz"
    fi
}

# 清理旧备份
cleanup_old_backups() {
    log "清理超过 $RETENTION_DAYS 天的旧备份..."
    
    local backup_root="$PROJECT_ROOT/backup"
    if [ -d "$backup_root" ]; then
        # 删除超过指定天数的备份文件和目录
        find "$backup_root" -type f -name "*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true
        find "$backup_root" -type d -mtime +$RETENTION_DAYS -empty -delete 2>/dev/null || true
        
        local deleted_count=$(find "$backup_root" -type f -name "*.tar.gz" -mtime +$RETENTION_DAYS | wc -l)
        if [ "$deleted_count" -gt 0 ]; then
            log "已删除 $deleted_count 个旧备份文件"
        fi
    fi
}

# 生成备份报告
generate_backup_report() {
    log "生成备份报告..."
    
    local report_file="$BACKUP_DIR/backup_report.txt"
    if [ "$COMPRESS" = true ]; then
        report_file="$PROJECT_ROOT/backup/backup_report_$(date +%Y%m%d_%H%M%S).txt"
    fi
    
    cat > "$report_file" << EOF
四会培训平台备份报告
==========================================
备份时间: $(date)
备份类型: $BACKUP_TYPE
备份位置: $BACKUP_DIR
压缩状态: $COMPRESS
保留天数: $RETENTION_DAYS

备份内容:
$(if [ "$BACKUP_TYPE" = "full" ] || [ "$BACKUP_TYPE" = "database" ]; then echo "✓ 数据库备份"; fi)
$(if [ "$BACKUP_TYPE" = "full" ] || [ "$BACKUP_TYPE" = "config" ]; then echo "✓ 配置文件备份"; fi)
$(if [ "$BACKUP_TYPE" = "full" ]; then echo "✓ 应用文件备份"; echo "✓ 日志文件备份"; echo "✓ Redis数据备份"; fi)

备份文件大小:
$(if [ "$COMPRESS" = true ]; then ls -lh "$BACKUP_DIR" 2>/dev/null || echo "压缩文件信息"; else du -sh "$BACKUP_DIR" 2>/dev/null || echo "目录大小信息"; fi)

==========================================
EOF
    
    log "备份报告已生成: $report_file"
}

# 主函数
main() {
    log "开始四会培训平台数据备份 (类型: $BACKUP_TYPE)"
    
    # 创建备份目录
    create_backup_directory
    
    # 根据备份类型执行相应操作
    case $BACKUP_TYPE in
        "full")
            backup_database
            backup_redis
            backup_configuration
            backup_application_files
            backup_logs
            ;;
        "database")
            backup_database
            backup_redis
            ;;
        "config")
            backup_configuration
            ;;
    esac
    
    # 压缩备份
    compress_backup
    
    # 生成报告
    generate_backup_report
    
    # 清理旧备份
    cleanup_old_backups
    
    log "备份完成！备份位置: $BACKUP_DIR"
    
    # 显示备份大小
    if [ "$COMPRESS" = true ]; then
        local size=$(ls -lh "$BACKUP_DIR" | awk '{print $5}')
        log "备份文件大小: $size"
    else
        local size=$(du -sh "$BACKUP_DIR" | cut -f1)
        log "备份目录大小: $size"
    fi
}

# 运行主函数
main "$@" 