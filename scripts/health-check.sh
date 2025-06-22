#!/bin/bash

# 四会培训平台健康检查脚本
# 使用方法: ./scripts/health-check.sh [选项]
# 选项: --verbose, --json, --alert

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
VERBOSE=false
JSON_OUTPUT=false
ALERT_MODE=false

# 解析命令行参数
for arg in "$@"; do
    case $arg in
        --verbose|-v)
            VERBOSE=true
            ;;
        --json|-j)
            JSON_OUTPUT=true
            ;;
        --alert|-a)
            ALERT_MODE=true
            ;;
    esac
done

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 健康检查结果
HEALTH_STATUS="healthy"
HEALTH_DETAILS=()
FAILED_CHECKS=()

# 日志函数
log() {
    if [ "$VERBOSE" = true ] || [ "$JSON_OUTPUT" = false ]; then
        echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
    fi
}

warn() {
    if [ "$VERBOSE" = true ] || [ "$JSON_OUTPUT" = false ]; then
        echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
    fi
}

error() {
    if [ "$VERBOSE" = true ] || [ "$JSON_OUTPUT" = false ]; then
        echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    fi
    HEALTH_STATUS="unhealthy"
    FAILED_CHECKS+=("$1")
}

info() {
    if [ "$VERBOSE" = true ]; then
        echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
    fi
}

# 检查Docker服务状态
check_docker_services() {
    log "检查Docker服务状态..."
    
    local services=("sihui-mysql" "sihui-redis" "sihui-backend" "sihui-frontend" "sihui-nginx")
    local all_running=true
    
    for service in "${services[@]}"; do
        if docker ps --format "table {{.Names}}" | grep -q "^$service$"; then
            info "✓ $service 正在运行"
            HEALTH_DETAILS+=("docker_$service:running")
        else
            error "✗ $service 未运行"
            all_running=false
            HEALTH_DETAILS+=("docker_$service:stopped")
        fi
    done
    
    if [ "$all_running" = true ]; then
        log "所有Docker服务正常运行"
    fi
}

# 检查应用健康状态
check_application_health() {
    log "检查应用健康状态..."
    
    # 检查后端API健康状态
    if curl -f -s -m 10 http://localhost:8080/api/health > /dev/null 2>&1; then
        local backend_response=$(curl -s -m 10 http://localhost:8080/api/health)
        info "✓ 后端API健康检查通过"
        HEALTH_DETAILS+=("backend_api:healthy")
        
        # 解析详细健康信息
        if command -v jq &> /dev/null; then
            local db_status=$(echo "$backend_response" | jq -r '.database // "unknown"')
            local redis_status=$(echo "$backend_response" | jq -r '.redis // "unknown"')
            HEALTH_DETAILS+=("database:$db_status" "redis:$redis_status")
        fi
    else
        error "✗ 后端API健康检查失败"
        HEALTH_DETAILS+=("backend_api:unhealthy")
    fi
    
    # 检查前端服务
    if curl -f -s -m 10 http://localhost/health > /dev/null 2>&1; then
        info "✓ 前端服务健康检查通过"
        HEALTH_DETAILS+=("frontend:healthy")
    else
        error "✗ 前端服务健康检查失败"
        HEALTH_DETAILS+=("frontend:unhealthy")
    fi
}

# 检查数据库连接
check_database_connection() {
    log "检查数据库连接..."
    
    if docker exec sihui-mysql mysql -u root -p$(grep DB_ROOT_PASSWORD "$PROJECT_ROOT/.env" | cut -d '=' -f2) -e "SELECT 1;" > /dev/null 2>&1; then
        info "✓ MySQL数据库连接正常"
        HEALTH_DETAILS+=("mysql_connection:healthy")
        
        # 检查数据库大小
        local db_size=$(docker exec sihui-mysql mysql -u root -p$(grep DB_ROOT_PASSWORD "$PROJECT_ROOT/.env" | cut -d '=' -f2) -e "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) AS 'DB Size in MB' FROM information_schema.tables WHERE table_schema='sihui_db';" | tail -n 1)
        HEALTH_DETAILS+=("database_size_mb:$db_size")
    else
        error "✗ MySQL数据库连接失败"
        HEALTH_DETAILS+=("mysql_connection:unhealthy")
    fi
}

# 检查Redis连接
check_redis_connection() {
    log "检查Redis连接..."
    
    if docker exec sihui-redis redis-cli -a $(grep REDIS_PASSWORD "$PROJECT_ROOT/.env" | cut -d '=' -f2) ping | grep -q PONG; then
        info "✓ Redis连接正常"
        HEALTH_DETAILS+=("redis_connection:healthy")
        
        # 检查Redis内存使用
        local redis_memory=$(docker exec sihui-redis redis-cli -a $(grep REDIS_PASSWORD "$PROJECT_ROOT/.env" | cut -d '=' -f2) info memory | grep used_memory_human | cut -d ':' -f2 | tr -d '\r')
        HEALTH_DETAILS+=("redis_memory:$redis_memory")
    else
        error "✗ Redis连接失败"
        HEALTH_DETAILS+=("redis_connection:unhealthy")
    fi
}

# 检查系统资源
check_system_resources() {
    log "检查系统资源..."
    
    # 检查磁盘空间
    local disk_usage=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$disk_usage" -lt 80 ]; then
        info "✓ 磁盘空间充足 (已使用: ${disk_usage}%)"
        HEALTH_DETAILS+=("disk_usage_percent:$disk_usage")
    else
        warn "⚠ 磁盘空间不足 (已使用: ${disk_usage}%)"
        HEALTH_DETAILS+=("disk_usage_percent:$disk_usage")
    fi
    
    # 检查内存使用
    local memory_usage=$(free | grep Mem | awk '{printf "%.1f", $3/$2 * 100.0}')
    if [ "${memory_usage%.*}" -lt 80 ]; then
        info "✓ 内存使用正常 (已使用: ${memory_usage}%)"
        HEALTH_DETAILS+=("memory_usage_percent:$memory_usage")
    else
        warn "⚠ 内存使用较高 (已使用: ${memory_usage}%)"
        HEALTH_DETAILS+=("memory_usage_percent:$memory_usage")
    fi
    
    # 检查CPU负载
    local cpu_load=$(uptime | awk -F'load average:' '{ print $2 }' | cut -d, -f1 | xargs)
    HEALTH_DETAILS+=("cpu_load_1min:$cpu_load")
    info "CPU 1分钟负载: $cpu_load"
}

# 检查日志错误
check_logs_for_errors() {
    log "检查应用日志错误..."
    
    # 检查后端日志中的错误
    local backend_errors=$(docker logs sihui-backend --since=1h 2>&1 | grep -i error | wc -l || echo "0")
    if [ "$backend_errors" -eq 0 ]; then
        info "✓ 后端日志无错误"
        HEALTH_DETAILS+=("backend_errors_1h:0")
    else
        warn "⚠ 后端日志发现 $backend_errors 个错误(最近1小时)"
        HEALTH_DETAILS+=("backend_errors_1h:$backend_errors")
    fi
    
    # 检查Nginx日志中的错误
    local nginx_errors=$(docker logs sihui-nginx --since=1h 2>&1 | grep -i error | wc -l || echo "0")
    if [ "$nginx_errors" -eq 0 ]; then
        info "✓ Nginx日志无错误"
        HEALTH_DETAILS+=("nginx_errors_1h:0")
    else
        warn "⚠ Nginx日志发现 $nginx_errors 个错误(最近1小时)"
        HEALTH_DETAILS+=("nginx_errors_1h:$nginx_errors")
    fi
}

# 生成JSON报告
generate_json_report() {
    local timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    local details_json=""
    
    for detail in "${HEALTH_DETAILS[@]}"; do
        local key=$(echo "$detail" | cut -d ':' -f1)
        local value=$(echo "$detail" | cut -d ':' -f2-)
        if [ -n "$details_json" ]; then
            details_json="$details_json,"
        fi
        details_json="$details_json\"$key\":\"$value\""
    done
    
    local failed_json=""
    for failed in "${FAILED_CHECKS[@]}"; do
        if [ -n "$failed_json" ]; then
            failed_json="$failed_json,"
        fi
        failed_json="$failed_json\"$failed\""
    done
    
    echo "{"
    echo "  \"status\": \"$HEALTH_STATUS\","
    echo "  \"timestamp\": \"$timestamp\","
    echo "  \"details\": {$details_json},"
    echo "  \"failed_checks\": [$failed_json]"
    echo "}"
}

# 发送告警
send_alert() {
    if [ "$ALERT_MODE" = true ] && [ "$HEALTH_STATUS" = "unhealthy" ]; then
        local alert_message="四会培训平台健康检查失败！\n失败的检查项:\n"
        for failed in "${FAILED_CHECKS[@]}"; do
            alert_message="$alert_message- $failed\n"
        done
        
        # 这里可以集成邮件、Slack、微信等告警方式
        warn "告警: $alert_message"
    fi
}

# 主函数
main() {
    if [ "$JSON_OUTPUT" = false ]; then
        log "开始四会培训平台健康检查..."
    fi
    
    # 执行各项检查
    check_docker_services
    check_application_health
    check_database_connection
    check_redis_connection
    check_system_resources
    check_logs_for_errors
    
    # 生成报告
    if [ "$JSON_OUTPUT" = true ]; then
        generate_json_report
    else
        echo ""
        echo "======================================"
        echo "  健康检查报告"
        echo "======================================"
        echo "总体状态: $HEALTH_STATUS"
        echo "检查时间: $(date)"
        echo "失败检查: ${#FAILED_CHECKS[@]}"
        echo "======================================"
        
        if [ ${#FAILED_CHECKS[@]} -gt 0 ]; then
            echo "失败的检查项:"
            for failed in "${FAILED_CHECKS[@]}"; do
                echo "  - $failed"
            done
        fi
    fi
    
    # 发送告警
    send_alert
    
    # 设置退出代码
    if [ "$HEALTH_STATUS" = "unhealthy" ]; then
        exit 1
    else
        exit 0
    fi
}

# 运行主函数
main "$@" 