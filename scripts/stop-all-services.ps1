# 四会项目 - 停止所有服务

Write-Host "=== 停止四会项目所有服务 ===" -ForegroundColor Red

# 停止占用指定端口的进程
$ports = @(3000, 5173, 8080)

foreach ($port in $ports) {
    Write-Host "正在停止端口 $port 上的进程..." -ForegroundColor Yellow
    
    try {
        $processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
        
        if ($processes) {
            foreach ($processId in $processes) {
                $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
                if ($process) {
                    Write-Host "停止进程: $($process.ProcessName) (PID: $processId)" -ForegroundColor Cyan
                    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
                }
            }
            Write-Host "端口 $port 已释放" -ForegroundColor Green
        } else {
            Write-Host "端口 $port 未被占用" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "检查端口 $port 时出错: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 额外停止可能的Node.js和Java进程
Write-Host "正在停止相关进程..." -ForegroundColor Yellow

# 停止Node.js进程（可能是前端服务）
Get-Process -Name "node" -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "停止Node.js进程: PID $($_.Id)" -ForegroundColor Cyan
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

# 停止可能的Java进程（Spring Boot）
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -like "*spring*" -or $_.MainWindowTitle -like "*sihui*"
} | ForEach-Object {
    Write-Host "停止Java进程: PID $($_.Id)" -ForegroundColor Cyan
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "=== 所有服务已停止 ===" -ForegroundColor Green
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 