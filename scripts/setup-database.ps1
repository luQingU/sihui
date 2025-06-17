# 四会项目数据库设置脚本 (PowerShell)
Write-Host "设置四会项目数据库..." -ForegroundColor Green

# 配置变量
$DB_NAME = "sihui_dev"
$DB_USER = "sihui_user"
$DB_PASSWORD = "sihui_password"

# 检查MySQL是否安装
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlPath) {
    Write-Host "❌ 未找到MySQL命令行工具！请确保MySQL已安装并添加到PATH环境变量中。" -ForegroundColor Red
    exit 1
}

# 提示输入MySQL root密码
$ROOT_PASSWORD = Read-Host "请输入MySQL root密码" -AsSecureString
$ROOT_PASSWORD_TEXT = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($ROOT_PASSWORD))

# 创建SQL脚本内容
$sqlScript = @"
-- 创建开发数据库
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';

-- 授权
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;

-- 显示创建结果
SHOW DATABASES LIKE '$DB_NAME';
SELECT User, Host FROM mysql.user WHERE User = '$DB_USER';
"@

# 写入临时SQL文件
$tempSqlFile = [System.IO.Path]::GetTempFileName() + ".sql"
$sqlScript | Out-File -FilePath $tempSqlFile -Encoding UTF8

try {
    # 执行SQL脚本
    Write-Host "执行数据库设置脚本..."
    $process = Start-Process -FilePath "mysql" -ArgumentList "-u", "root", "-p$ROOT_PASSWORD_TEXT", "-e", "source $tempSqlFile" -Wait -PassThru -WindowStyle Hidden
    
    if ($process.ExitCode -eq 0) {
        Write-Host "✅ 数据库设置完成！" -ForegroundColor Green
        Write-Host "数据库名: $DB_NAME" -ForegroundColor Yellow
        Write-Host "用户名: $DB_USER" -ForegroundColor Yellow
        Write-Host "密码: $DB_PASSWORD" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "请更新 application-dev.properties 中的数据库配置：" -ForegroundColor Cyan
        Write-Host "spring.datasource.url=jdbc:mysql://localhost:3306/$DB_NAME`?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai" -ForegroundColor Gray
        Write-Host "spring.datasource.username=$DB_USER" -ForegroundColor Gray
        Write-Host "spring.datasource.password=$DB_PASSWORD" -ForegroundColor Gray
    } else {
        Write-Host "❌ 数据库设置失败！退出码: $($process.ExitCode)" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ 执行过程中发生错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
finally {
    # 清理临时文件
    if (Test-Path $tempSqlFile) {
        Remove-Item $tempSqlFile
    }
}

Write-Host "`n下一步：" -ForegroundColor Green
Write-Host "1. 确保MySQL服务正在运行" -ForegroundColor White
Write-Host "2. 更新application-dev.properties配置文件" -ForegroundColor White
Write-Host "3. 运行 'mvn spring-boot:run' 启动项目" -ForegroundColor White
Write-Host "4. 访问 http://localhost:8080/swagger-ui.html 查看API文档" -ForegroundColor White 