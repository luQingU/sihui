#!/bin/bash

# 四会项目数据库设置脚本
echo "设置四会项目数据库..."

# 配置变量
DB_NAME="sihui_dev"
DB_USER="sihui_user"
DB_PASSWORD="sihui_password"
ROOT_PASSWORD=""

# 检查MySQL是否运行
if ! systemctl is-active --quiet mysql; then
    echo "启动MySQL服务..."
    sudo systemctl start mysql
fi

# 提示输入MySQL root密码
if [ -z "$ROOT_PASSWORD" ]; then
    read -s -p "请输入MySQL root密码: " ROOT_PASSWORD
    echo
fi

# 创建数据库和用户
mysql -u root -p"$ROOT_PASSWORD" << EOF
-- 创建开发数据库
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';

-- 授权
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;

-- 显示创建结果
SHOW DATABASES LIKE '${DB_NAME}';
SELECT User, Host FROM mysql.user WHERE User = '${DB_USER}';
EOF

if [ $? -eq 0 ]; then
    echo "✅ 数据库设置完成！"
    echo "数据库名: $DB_NAME"
    echo "用户名: $DB_USER"
    echo "密码: $DB_PASSWORD"
    echo ""
    echo "请更新 application-dev.properties 中的数据库配置："
    echo "spring.datasource.url=jdbc:mysql://localhost:3306/${DB_NAME}?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai"
    echo "spring.datasource.username=${DB_USER}"
    echo "spring.datasource.password=${DB_PASSWORD}"
else
    echo "❌ 数据库设置失败！"
    exit 1
fi 