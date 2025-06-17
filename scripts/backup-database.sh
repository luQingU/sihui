#!/bin/bash

# 四会项目数据库备份脚本
echo "开始备份四会项目数据库..."

# 配置变量
DB_NAME="sihui_dev"
DB_USER="sihui_user"
DB_PASSWORD="sihui_password"
BACKUP_DIR="./backups"
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/sihui_backup_${DATE}.sql"

# 创建备份目录
mkdir -p "$BACKUP_DIR"

# 检查mysqldump命令
if ! command -v mysqldump &> /dev/null; then
    echo "❌ mysqldump 命令未找到！请确保MySQL客户端工具已安装。"
    exit 1
fi

# 执行备份
echo "正在备份数据库 $DB_NAME 到 $BACKUP_FILE ..."

mysqldump -u "$DB_USER" -p"$DB_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --hex-blob \
    --add-drop-table \
    --add-locks \
    --create-options \
    --disable-keys \
    --extended-insert \
    --lock-tables=false \
    --quick \
    --set-charset \
    --comments \
    "$DB_NAME" > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    # 压缩备份文件
    gzip "$BACKUP_FILE"
    BACKUP_FILE="${BACKUP_FILE}.gz"
    
    echo "✅ 数据库备份成功！"
    echo "备份文件: $BACKUP_FILE"
    echo "文件大小: $(du -h "$BACKUP_FILE" | cut -f1)"
    
    # 清理7天前的备份文件
    find "$BACKUP_DIR" -name "sihui_backup_*.sql.gz" -mtime +7 -delete
    echo "已清理7天前的备份文件"
else
    echo "❌ 数据库备份失败！"
    exit 1
fi 