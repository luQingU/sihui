#!/bin/bash
# 项目完整性快速检查脚本

echo "🔍 四会学习培训系统 - 项目完整性检查"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查计数器
total_checks=0
passed_checks=0

check_file() {
    total_checks=$((total_checks + 1))
    if [ -f "$1" ]; then
        echo -e "${GREEN}✅ $1${NC}"
        passed_checks=$((passed_checks + 1))
    else
        echo -e "${RED}❌ $1 (缺失)${NC}"
    fi
}

check_dir() {
    total_checks=$((total_checks + 1))
    if [ -d "$1" ]; then
        echo -e "${GREEN}✅ $1/${NC}"
        passed_checks=$((passed_checks + 1))
    else
        echo -e "${RED}❌ $1/ (缺失)${NC}"
    fi
}

echo -e "${YELLOW}📁 检查核心项目结构...${NC}"
check_dir "admin-dashboard_3"
check_dir "sihui-backend" 
check_dir "sihui-wx"
check_dir "完整部署包"

echo ""
echo -e "${YELLOW}📋 检查核心配置文件...${NC}"
check_file "admin-dashboard_3/package.json"
check_file "admin-dashboard_3/.env.local"
check_file "admin-dashboard_3/.env.production"
check_file "admin-dashboard_3/server.js"
check_file "sihui-backend/pom.xml"
check_file "sihui-backend/.env"
check_file "sihui-wx/package.json"

echo ""
echo -e "${YELLOW}🚀 检查部署文件...${NC}"
check_file "完整部署包/server-install.sh"
check_file "完整部署包/backend/.env"
check_file "完整部署包/frontend/.env.production"
check_file "完整部署包/systemd/sihui-backend.service"
check_file "完整部署包/systemd/sihui-frontend.service"

echo ""
echo -e "${YELLOW}📖 检查文档文件...${NC}"
check_file "README.md"
check_file "完整部署包/🚀快速部署流程.md"
check_file "完整部署包/📋 客户配置指南.md"
check_file "📋 项目交付清单.md"

echo ""
echo -e "${YELLOW}🗄️ 检查数据库文件...${NC}"
check_file "sihui-backend/src/main/resources/db/migration/V1__Create_core_tables.sql"
check_file "sihui-backend/src/main/resources/db/migration/V2__Insert_initial_data.sql"

echo ""
echo -e "${YELLOW}⚙️ 检查关键源码文件...${NC}"
check_file "sihui-backend/src/main/java/com/vote/sihuibackend/SihuiBackendApplication.java"
check_file "admin-dashboard_3/lib/api.ts"
check_file "sihui-wx/src/config/index.js"

echo ""
echo "=========================================="
echo -e "${YELLOW}📊 检查结果汇总${NC}"
echo "总检查项: $total_checks"
echo -e "通过检查: ${GREEN}$passed_checks${NC}"
echo -e "缺失文件: ${RED}$((total_checks - passed_checks))${NC}"

if [ $passed_checks -eq $total_checks ]; then
    echo ""
    echo -e "${GREEN}🎉 恭喜！项目文件完整，可以交付给客户！${NC}"
    echo ""
    echo "接下来的步骤："
    echo "1. 复制整个Sihui目录到 完整部署包/Sihui/"
    echo "2. 客户根据配置指南填写配置文件"
    echo "3. 上传部署包到服务器执行安装"
    echo ""
else
    echo ""
    echo -e "${RED}⚠️ 项目文件不完整，请检查缺失的文件后再交付${NC}"
    echo ""
fi

echo "详细部署说明请查看: 完整部署包/🚀快速部署流程.md"
echo ""