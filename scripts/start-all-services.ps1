# 四会项目 - 启动所有服务
# 管理端: 5173端口
# 后端: 8080端口  
# 小程序端: 3000端口

Write-Host "=== 四会项目启动脚本 ===" -ForegroundColor Green

# 检查是否在项目根目录
if (-not (Test-Path "admin-dashboard_3") -or -not (Test-Path "sihui-backend") -or -not (Test-Path "sihui-wx")) {
    Write-Host "请在项目根目录运行此脚本！" -ForegroundColor Red
    exit 1
}

Write-Host "正在启动所有服务..." -ForegroundColor Yellow

# 启动后端 (8080端口)
Write-Host "1. 启动后端服务 (端口: 8080)" -ForegroundColor Cyan
Start-Process PowerShell -ArgumentList "-NoExit", "-Command", "cd '$PWD\sihui-backend'; .\mvnw.cmd spring-boot:run"

# 等待2秒
Start-Sleep -Seconds 2

# 启动管理端 (5173端口) 
Write-Host "2. 启动管理端 (端口: 5173)" -ForegroundColor Cyan
Start-Process PowerShell -ArgumentList "-NoExit", "-Command", "cd '$PWD\admin-dashboard_3'; npm run dev"

# 等待2秒
Start-Sleep -Seconds 2

# 启动小程序端 (3000端口)
Write-Host "3. 启动小程序端 (端口: 3000)" -ForegroundColor Cyan  
Start-Process PowerShell -ArgumentList "-NoExit", "-Command", "cd '$PWD\sihui-wx'; npm run dev:h5"

Write-Host ""
Write-Host "=== 服务启动完成 ===" -ForegroundColor Green
Write-Host "管理端: http://localhost:5173" -ForegroundColor White
Write-Host "后端API: http://localhost:8080" -ForegroundColor White  
Write-Host "小程序端: http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 