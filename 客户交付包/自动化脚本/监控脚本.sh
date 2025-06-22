#!/bin/bash

# ===========================================
# Sihui 项目系统监控脚本
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

log_status() {
    echo -e "${BLUE}[STATUS]${NC} $1"
}

# 配置阈值
CPU_THRESHOLD=80
MEMORY_THRESHOLD=85
DISK_THRESHOLD=90
LOAD_THRESHOLD=5.0

# 检查系统资源
check_system_resources() {
    log_status "检查系统资源使用情况..."
    
    # CPU使用率
    local cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | awk -F'%' '{print $1}')
    if (( $(echo "$cpu_usage > $CPU_THRESHOLD" | bc -l) )); then
        log_warn "CPU使用率过高: ${cpu_usage}% (阈值: ${CPU_THRESHOLD}%)"
    else
        log_info "CPU使用率正常: ${cpu_usage}%"
    fi
    
    # 内存使用率
    local memory_info=$(free | grep Mem)
    local total_mem=$(echo $memory_info | awk '{print $2}')
    local used_mem=$(echo $memory_info | awk '{print $3}')
    local memory_usage=$(echo "scale=2; $used_mem*100/$total_mem" | bc)
    
    if (( $(echo "$memory_usage > $MEMORY_THRESHOLD" | bc -l) )); then
        log_warn "内存使用率过高: ${memory_usage}% (阈值: ${MEMORY_THRESHOLD}%)"
    else
        log_info "内存使用率正常: ${memory_usage}%"
    fi
    
    # 磁盘使用率
    local disk_usage=$(df -h /opt/sihui | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$disk_usage" -gt "$DISK_THRESHOLD" ]; then
        log_warn "磁盘使用率过高: ${disk_usage}% (阈值: ${DISK_THRESHOLD}%)"
    else
        log_info "磁盘使用率正常: ${disk_usage}%"
    fi
    
    # 系统负载
    local load_avg=$(uptime | awk -F'load average:' '{print $2}' | awk '{print $1}' | sed 's/,//')
    if (( $(echo "$load_avg > $LOAD_THRESHOLD" | bc -l) )); then
        log_warn "系统负载过高: ${load_avg} (阈值: ${LOAD_THRESHOLD})"
    else
        log_info "系统负载正常: ${load_avg}"
    fi
}

# 检查服务状态
check_services() {
    log_status "检查服务状态..."
    
    local services=("mysql" "nginx" "sihui-backend" "sihui-frontend")
    
    for service in "${services[@]}"; do
        if systemctl is-active --quiet $service; then
            log_info "$service 服务运行正常"
        else
            log_error "$service 服务未运行"
        fi
    done
}

# 检查端口监听
check_ports() {
    log_status "检查端口监听状态..."
    
    local ports=("80" "443" "8080" "3000" "3306")
    
    for port in "${ports[@]}"; do
        if ss -tlnp | grep -q ":$port "; then
            log_info "端口 $port 正常监听"
        else
            log_warn "端口 $port 未监听"
        fi
    done
}

# 检查数据库连接
check_database() {
    log_status "检查数据库连接..."
    
    if [ -f "/opt/sihui/backend/.env" ]; then
        source /opt/sihui/backend/.env
        
        if mysql -u ${DB_USERNAME:-sihui_user} -p${DB_PASSWORD} -e "SELECT 1;" ${DB_NAME:-sihui_production} >/dev/null 2>&1; then
            log_info "数据库连接正常"
        else
            log_error "数据库连接失败"
        fi
    else
        log_warn "未找到数据库配置文件"
    fi
}

# 检查API健康状态
check_api_health() {
    log_status "检查API健康状态..."
    
    local api_url="http://localhost:8080/actuator/health"
    
    if curl -s -f $api_url >/dev/null 2>&1; then
        log_info "API健康检查通过"
    else
        log_error "API健康检查失败"
    fi
}

# 检查前端应用
check_frontend() {
    log_status "检查前端应用状态..."
    
    local frontend_url="http://localhost:3000"
    
    if curl -s -f $frontend_url >/dev/null 2>&1; then
        log_info "前端应用运行正常"
    else
        log_error "前端应用无法访问"
    fi
}

# 检查SSL证书
check_ssl_certificate() {
    log_status "检查SSL证书状态..."
    
    local cert_file="/opt/sihui/ssl/*.crt"
    
    if ls $cert_file 1> /dev/null 2>&1; then
        local cert_path=$(ls $cert_file | head -1)
        local expiry_date=$(openssl x509 -in "$cert_path" -noout -enddate | cut -d= -f2)
        local expiry_timestamp=$(date -d "$expiry_date" +%s)
        local current_timestamp=$(date +%s)
        local days_until_expiry=$(( ($expiry_timestamp - $current_timestamp) / 86400 ))
        
        if [ $days_until_expiry -lt 30 ]; then
            log_warn "SSL证书将在 $days_until_expiry 天后过期"
        else
            log_info "SSL证书有效期还有 $days_until_expiry 天"
        fi
    else
        log_warn "未找到SSL证书文件"
    fi
}

# 检查日志文件大小
check_log_sizes() {
    log_status "检查日志文件大小..."
    
    local log_dirs=("/var/log/nginx" "/opt/sihui/logs")
    
    for log_dir in "${log_dirs[@]}"; do
        if [ -d "$log_dir" ]; then
            local large_logs=$(find $log_dir -name "*.log" -size +100M 2>/dev/null)
            if [ ! -z "$large_logs" ]; then
                log_warn "发现大日志文件:"
                echo "$large_logs"
            else
                log_info "$log_dir 目录日志文件大小正常"
            fi
        fi
    done
}

# 生成监控报告
generate_monitoring_report() {
    local report_file="/opt/sihui/logs/monitoring_report_$(date +%Y%m%d_%H%M%S).txt"
    mkdir -p /opt/sihui/logs
    
    cat > $report_file <<EOF
===========================================
Sihui 系统监控报告
===========================================

监控时间: $(date)

系统信息:
- 操作系统: $(uname -a)
- 运行时间: $(uptime)
- 当前用户: $(whoami)

资源使用情况:
$(free -h)

磁盘使用情况:
$(df -h)

网络连接:
$(ss -tlnp | grep -E ':80|:443|:8080|:3000|:3306')

进程状态:
$(ps aux | grep -E 'java|nginx|mysql|node' | grep -v grep)

服务状态:
- MySQL: $(systemctl is-active mysql)
- Nginx: $(systemctl is-active nginx)  
- Backend: $(systemctl is-active sihui-backend)
- Frontend: $(systemctl is-active sihui-frontend)

最近日志:
$(journalctl -u sihui-backend --since "1 hour ago" --no-pager | tail -10)

监控完成时间: $(date)
===========================================
EOF
    
    log_info "监控报告生成: $report_file"
}

# 性能优化建议
performance_recommendations() {
    log_status "性能优化建议..."
    
    echo ""
    echo "📊 性能优化建议:"
    
    # 检查数据库连接数
    local db_connections=$(mysql -u ${DB_USERNAME:-sihui_user} -p${DB_PASSWORD} -e "SHOW STATUS LIKE 'Threads_connected';" 2>/dev/null | awk 'NR==2 {print $2}')
    if [ ! -z "$db_connections" ] && [ "$db_connections" -gt 50 ]; then
        echo "• 数据库连接数较高($db_connections)，建议优化连接池配置"
    fi
    
    # 检查swap使用
    local swap_usage=$(free | grep Swap | awk '{print ($3/$2)*100}')
    if (( $(echo "$swap_usage > 10" | bc -l) )); then
        echo "• Swap使用率过高(${swap_usage}%)，建议增加内存"
    fi
    
    # 检查网络连接数
    local tcp_connections=$(ss -s | grep TCP | awk '{print $2}')
    echo "• 当前TCP连接数: $tcp_connections"
    
    echo ""
}

# 主函数
main() {
    echo "📊 Sihui 系统监控检查..."
    echo "监控时间: $(date)"
    echo ""
    
    check_system_resources
    echo ""
    
    check_services  
    echo ""
    
    check_ports
    echo ""
    
    check_database
    echo ""
    
    check_api_health
    echo ""
    
    check_frontend
    echo ""
    
    check_ssl_certificate
    echo ""
    
    check_log_sizes
    echo ""
    
    performance_recommendations
    
    generate_monitoring_report
    
    echo ""
    log_info "✅ 系统监控检查完成！"
}

# 如果脚本被直接执行
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi