# 四会培训管理平台 - 前端部署脚本
# PowerShell Script for Windows

param(
    [string]$Environment = "development",
    [switch]$Build = $false,
    [switch]$Install = $false,
    [switch]$Start = $false,
    [switch]$Help = $false
)

# 显示帮助信息
if ($Help) {
    Write-Host "四会培训管理平台 - 前端部署脚本" -ForegroundColor Green
    Write-Host ""
    Write-Host "使用方法:"
    Write-Host "  .\scripts\deploy.ps1 [参数]"
    Write-Host ""
    Write-Host "参数:"
    Write-Host "  -Environment <env>   指定环境 (development, production)"
    Write-Host "  -Install             安装依赖"
    Write-Host "  -Build               构建项目"
    Write-Host "  -Start               启动项目"
    Write-Host "  -Help                显示帮助信息"
    Write-Host ""
    Write-Host "示例:"
    Write-Host "  .\scripts\deploy.ps1 -Install -Build -Start"
    Write-Host "  .\scripts\deploy.ps1 -Environment production -Build"
    Write-Host ""
    exit 0
}

Write-Host "=== 四会培训管理平台 - 前端部署 ===" -ForegroundColor Green
Write-Host "环境: $Environment" -ForegroundColor Yellow
Write-Host ""

# 检查 Node.js
Write-Host "检查 Node.js..." -ForegroundColor Blue
try {
    $nodeVersion = node --version
    Write-Host "Node.js 版本: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到 Node.js，请先安装 Node.js" -ForegroundColor Red
    exit 1
}

# 检查 pnpm
Write-Host "检查 pnpm..." -ForegroundColor Blue
try {
    $pnpmVersion = pnpm --version
    Write-Host "pnpm 版本: $pnpmVersion" -ForegroundColor Green
} catch {
    Write-Host "警告: 未找到 pnpm，将使用 npm" -ForegroundColor Yellow
    $useNpm = $true
}

# 安装依赖
if ($Install) {
    Write-Host "安装依赖..." -ForegroundColor Blue
    
    if ($useNpm) {
        npm install
    } else {
        pnpm install
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "错误: 依赖安装失败" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "依赖安装完成" -ForegroundColor Green
}

# 环境变量检查
Write-Host "检查环境配置..." -ForegroundColor Blue

$envFile = ".env.local"
if (-not (Test-Path $envFile)) {
    Write-Host "警告: 未找到 .env.local 文件" -ForegroundColor Yellow
    Write-Host "请根据 README.md 创建环境配置文件" -ForegroundColor Yellow
    
    # 创建示例环境文件
    $exampleEnv = @"
# API 配置
NEXT_PUBLIC_API_URL=http://localhost:8080

# 应用配置
NEXT_PUBLIC_APP_NAME=四会培训管理平台
NEXT_PUBLIC_APP_VERSION=1.0.0
NEXT_PUBLIC_ENV=$Environment

# 分页配置
NEXT_PUBLIC_DEFAULT_PAGE_SIZE=10
NEXT_PUBLIC_MAX_PAGE_SIZE=100
"@
    
    $exampleEnv | Out-File -FilePath ".env.local.example" -Encoding UTF8
    Write-Host "已创建 .env.local.example 示例文件" -ForegroundColor Green
}

# 构建项目
if ($Build) {
    Write-Host "构建项目..." -ForegroundColor Blue
    
    if ($Environment -eq "production") {
        $env:NODE_ENV = "production"
    }
    
    if ($useNpm) {
        npm run build
    } else {
        pnpm build
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "错误: 项目构建失败" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "项目构建完成" -ForegroundColor Green
}

# 启动项目
if ($Start) {
    Write-Host "启动项目..." -ForegroundColor Blue
    
    if ($Environment -eq "production") {
        Write-Host "以生产模式启动..." -ForegroundColor Yellow
        if ($useNpm) {
            npm start
        } else {
            pnpm start
        }
    } else {
        Write-Host "以开发模式启动..." -ForegroundColor Yellow
        if ($useNpm) {
            npm run dev
        } else {
            pnpm dev
        }
    }
}

Write-Host ""
Write-Host "=== 部署完成 ===" -ForegroundColor Green

if (-not $Start) {
    Write-Host ""
    Write-Host "手动启动命令:" -ForegroundColor Yellow
    Write-Host "  开发模式: pnpm dev" -ForegroundColor White
    Write-Host "  生产模式: pnpm start" -ForegroundColor White
    Write-Host ""
    Write-Host "访问地址: http://localhost:3000" -ForegroundColor Cyan
} 