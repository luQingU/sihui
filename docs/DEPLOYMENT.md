# 部署指南

## 概览

四会项目的生产环境部署指南。

## 环境要求

### 服务器环境
- **操作系统**: Ubuntu 20.04+ / CentOS 8+
- **内存**: 最低 4GB，推荐 8GB+
- **存储**: 最低 50GB，推荐 100GB+
- **网络**: 带宽建议 10Mbps+

### 软件依赖
- **Java**: OpenJDK 17+
- **Node.js**: 18.x+
- **Nginx**: 1.18+
- **MySQL**: 8.0+
- **Redis**: 6.0+

## 部署步骤

### 1. 环境准备
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装Java
sudo apt install openjdk-17-jdk -y

# 安装Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# 安装Nginx
sudo apt install nginx -y

# 安装MySQL
sudo apt install mysql-server -y

# 安装Redis
sudo apt install redis-server -y
```

### 2. 数据库初始化
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE sihui_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sihui_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON sihui_db.* TO 'sihui_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 后端部署
```bash
# 构建项目
cd sihui-backend
mvn clean package -DskipTests

# 复制jar文件到部署目录
sudo mkdir -p /opt/sihui
sudo cp target/sihui-backend-*.jar /opt/sihui/app.jar

# 创建服务配置
sudo vim /etc/systemd/system/sihui-backend.service
```

### 4. 前端部署
```bash
# 构建管理后台
cd sihui-admin
npm install
npm run build

# 部署到Nginx
sudo cp -r dist/* /var/www/html/admin/

# 配置Nginx
sudo vim /etc/nginx/sites-available/sihui
```

### 5. 启动服务
```bash
# 启动后端服务
sudo systemctl enable sihui-backend
sudo systemctl start sihui-backend

# 重启Nginx
sudo systemctl restart nginx
```

## 监控与维护

### 日志查看
```bash
# 查看后端日志
sudo journalctl -u sihui-backend -f

# 查看Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 备份策略
- 数据库备份：每日自动备份
- 文件备份：每周完整备份
- 日志轮转：保留30天

## 故障排查

### 常见问题
1. **服务无法启动**: 检查端口占用和配置文件
2. **数据库连接失败**: 验证数据库配置和网络连接
3. **前端404错误**: 检查Nginx配置和文件路径

### 联系方式
- 技术支持邮箱: support@sihui.com
- 紧急联系电话: xxx-xxxx-xxxx 