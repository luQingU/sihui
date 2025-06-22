#!/bin/bash

# 四会培训平台自动化部署脚本
# 使用方法: ./scripts/deploy.sh [环境] [选项]
# 环境: dev|test|prod
# 选项: --backup, --no-build, --force

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV=${1:-dev}
BACKUP_ENABLED=true
BUILD_ENABLED=true
FORCE_DEPLOY=false

# 解析命令行参数
for arg in "$@"; do
    case $arg in
        --backup)
            BACKUP_ENABLED=true
            ;;
        --no-backup)
            BACKUP_ENABLED=false
            ;;
        --no-build)
            BUILD_ENABLED=false
            ;;
        --force)
            FORCE_DEPLOY=true
            ;;
    esac
done

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

# 检查环境
check_environment() {
    log "检查部署环境..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装，请先安装 Docker"
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose 未安装，请先安装 Docker Compose"
    fi
    
    # 检查环境变量文件
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        if [ ! -f "$PROJECT_ROOT/.env.example" ]; then
            error "环境变量文件不存在，请创建 .env 文件"
        else
            warn "未找到 .env 文件，使用 .env.example 作为模板"
            cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
            error "请编辑 .env 文件并配置正确的环境变量，然后重新运行部署脚本"
        fi
    fi
    
    log "环境检查完成"
}

# 备份数据
backup_data() {
    if [ "$BACKUP_ENABLED" = true ]; then
        log "开始备份数据..."
        
        BACKUP_DIR="$PROJECT_ROOT/backup/$(date +%Y%m%d_%H%M%S)"
        mkdir -p "$BACKUP_DIR"
        
        # 备份数据库
        if docker ps | grep -q sihui-mysql; then
            info "备份MySQL数据库..."
            docker exec sihui-mysql mysqldump -u root -p$(grep DB_ROOT_PASSWORD .env | cut -d '=' -f2) --all-databases > "$BACKUP_DIR/mysql_backup.sql"
        fi
        
        # 备份应用配置
        info "备份应用配置..."
        cp -r "$PROJECT_ROOT/config" "$BACKUP_DIR/"
        cp "$PROJECT_ROOT/.env" "$BACKUP_DIR/"
        
        log "数据备份完成: $BACKUP_DIR"
    else
        warn "跳过数据备份"
    fi
}

# 构建镜像
build_images() {
    if [ "$BUILD_ENABLED" = true ]; then
        log "开始构建Docker镜像..."
        
        cd "$PROJECT_ROOT"
        
        # 构建后端镜像
        info "构建后端镜像..."
        docker-compose build backend
        
        # 构建前端镜像
        info "构建前端镜像..."
        docker-compose build frontend
        
        log "镜像构建完成"
    else
        warn "跳过镜像构建"
    fi
}

# 部署服务
deploy_services() {
    log "开始部署服务..."
    
    cd "$PROJECT_ROOT"
    
    # 停止现有服务
    info "停止现有服务..."
    docker-compose down
    
    # 启动数据库服务
    info "启动数据库服务..."
    docker-compose up -d mysql redis
    
    # 等待数据库启动
    info "等待数据库启动..."
    sleep 30
    
    # 运行数据库迁移
    info "运行数据库迁移..."
    docker-compose run --rm backend java -jar app.jar --spring.profiles.active=migration || true
    
    # 启动应用服务
    info "启动应用服务..."
    docker-compose up -d backend frontend nginx
    
    log "服务部署完成"
}

# 健康检查
health_check() {
    log "开始健康检查..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        info "健康检查尝试 $attempt/$max_attempts"
        
        # 检查后端健康状态
        if curl -f -s http://localhost:8080/api/health > /dev/null 2>&1; then
            log "后端服务健康检查通过"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            error "健康检查失败，服务可能未正常启动"
        fi
        
        sleep 10
        ((attempt++))
    done
    
    # 检查前端
    if curl -f -s http://localhost/health > /dev/null 2>&1; then
        log "前端服务健康检查通过"
    else
        warn "前端服务健康检查失败"
    fi
    
    log "健康检查完成"
}

# 显示部署信息
show_deployment_info() {
    log "部署完成！"
    echo ""
    echo "======================================"
    echo "  四会培训平台部署信息"
    echo "======================================"
    echo "环境: $ENV"
    echo "管理后台: http://localhost/admin/"
    echo "API文档: http://localhost/api/swagger-ui.html"
    echo "健康检查: http://localhost/health"
    echo "======================================"
    echo ""
    echo "查看服务状态: docker-compose ps"
    echo "查看日志: docker-compose logs -f [service]"
    echo "停止服务: docker-compose down"
    echo ""
}

# 清理函数
cleanup() {
    if [ $? -ne 0 ]; then
        error "部署失败，请检查错误信息"
    fi
}

# 主函数
main() {
    log "开始部署四会培训平台 (环境: $ENV)"
    
    # 设置错误处理
    trap cleanup EXIT
    
    # 检查环境
    check_environment
    
    # 确认部署
    if [ "$FORCE_DEPLOY" = false ]; then
        echo ""
        read -p "确认要部署到 $ENV 环境吗？(y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            info "部署已取消"
            exit 0
        fi
    fi
    
    # 执行部署步骤
    backup_data
    build_images
    deploy_services
    health_check
    show_deployment_info
    
    log "部署完成！"
}

# 运行主函数
main "$@" 