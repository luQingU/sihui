#!/bin/bash

# 四会项目开发环境停止脚本

echo "停止四会项目开发环境..."

# 获取脚本所在目录的父目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "项目根目录: $PROJECT_ROOT"

# 停止后端服务
stop_backend() {
    if [ -f "$PROJECT_ROOT/logs/backend.pid" ]; then
        BACKEND_PID=$(cat "$PROJECT_ROOT/logs/backend.pid")
        if ps -p $BACKEND_PID > /dev/null; then
            echo "停止后端服务 (PID: $BACKEND_PID)..."
            kill $BACKEND_PID
            rm "$PROJECT_ROOT/logs/backend.pid"
            echo "后端服务已停止"
        else
            echo "后端服务进程不存在"
            rm "$PROJECT_ROOT/logs/backend.pid"
        fi
    else
        echo "未找到后端服务PID文件"
    fi
}

# 停止前端管理后台
stop_admin() {
    if [ -f "$PROJECT_ROOT/logs/admin.pid" ]; then
        ADMIN_PID=$(cat "$PROJECT_ROOT/logs/admin.pid")
        if ps -p $ADMIN_PID > /dev/null; then
            echo "停止管理后台 (PID: $ADMIN_PID)..."
            kill $ADMIN_PID
            rm "$PROJECT_ROOT/logs/admin.pid"
            echo "管理后台已停止"
        else
            echo "管理后台进程不存在"
            rm "$PROJECT_ROOT/logs/admin.pid"
        fi
    else
        echo "未找到管理后台PID文件"
    fi
}

# 清理日志文件
cleanup_logs() {
    echo "清理日志文件..."
    if [ -d "$PROJECT_ROOT/logs" ]; then
        # 保留最近的日志，删除旧的
        find "$PROJECT_ROOT/logs" -name "*.log" -mtime +7 -delete
        echo "日志清理完成"
    fi
}

# 执行停止流程
stop_backend
stop_admin
cleanup_logs

echo ""
echo "================================"
echo "四会项目开发环境已停止！"
echo "================================"
echo "" 