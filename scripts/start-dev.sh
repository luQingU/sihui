#!/bin/bash

# 四会项目开发环境启动脚本

echo "启动四会项目开发环境..."

# 获取脚本所在目录的父目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "项目根目录: $PROJECT_ROOT"

# 检查依赖
check_dependencies() {
    echo "检查依赖..."
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        echo "错误: Node.js 未安装"
        exit 1
    fi
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        echo "错误: Java 未安装"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo "错误: Maven 未安装"
        exit 1
    fi
    
    echo "依赖检查通过"
}

# 启动后端服务
start_backend() {
    echo "启动后端服务..."
    cd "$PROJECT_ROOT/sihui-backend"
    
    # 后台运行Spring Boot应用
    nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > ../logs/backend.pid
    echo "后端服务已启动，PID: $BACKEND_PID"
}

# 启动前端管理后台
start_admin() {
    echo "启动管理后台..."
    cd "$PROJECT_ROOT/sihui-admin"
    
    # 检查node_modules
    if [ ! -d "node_modules" ]; then
        echo "安装前端依赖..."
        npm install
    fi
    
    # 后台运行开发服务器
    nohup npm run dev > ../logs/admin.log 2>&1 &
    ADMIN_PID=$!
    echo $ADMIN_PID > ../logs/admin.pid
    echo "管理后台已启动，PID: $ADMIN_PID"
}

# 启动小程序开发
start_miniprogram() {
    echo "准备小程序开发环境..."
    cd "$PROJECT_ROOT/sihui-miniprogram"
    
    # 检查node_modules
    if [ ! -d "node_modules" ]; then
        echo "安装小程序依赖..."
        npm install
    fi
    
    echo "小程序开发环境已准备，请在HBuilderX中打开项目"
}

# 创建日志目录
mkdir -p "$PROJECT_ROOT/logs"

# 执行启动流程
check_dependencies
start_backend
sleep 5  # 等待后端启动
start_admin
start_miniprogram

echo ""
echo "================================"
echo "四会项目开发环境启动完成！"
echo "================================"
echo "后端服务: http://localhost:8080"
echo "管理后台: http://localhost:5173"
echo "小程序: 请在HBuilderX中运行"
echo ""
echo "停止服务请运行: ./scripts/stop-dev.sh"
echo "" 