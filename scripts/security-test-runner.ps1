# 四会培训平台安全测试自动化脚本
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminUsername = "admin", 
    [string]$AdminPassword = "admin123",
    [string]$OutputDir = "security-test-results",
    [switch]$FullTest = $false
)

# 创建输出目录
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# 日志文件
$LogFile = Join-Path $OutputDir "security-test.log"

# 日志函数
function Write-TestLog {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    Write-Host $logMessage
    Add-Content -Path $LogFile -Value $logMessage
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
    Write-TestLog $Message "SUCCESS"
}

function Write-TestError {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
    Write-TestLog $Message "ERROR"
}

# 获取管理员Token
function Get-AdminToken {
    Write-Host "正在获取管理员访问令牌..." -ForegroundColor Cyan
    
    $loginData = @{
        username = $AdminUsername
        password = $AdminPassword
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
        if ($response.token) {
            Write-Success "管理员令牌获取成功"
            return $response.token
        } else {
            Write-TestError "管理员令牌获取失败"
            return $null
        }
    } catch {
        Write-TestError "管理员令牌获取失败: $($_.Exception.Message)"
        return $null
    }
}

# 执行安全测试
function Invoke-SecurityTest {
    param([string]$TestName, [string]$Endpoint, [string]$Token)
    
    Write-Host "执行测试: $TestName" -ForegroundColor Yellow
    
    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method GET -Headers $headers
        
        if ($response.success -eq $true) {
            Write-Success "$TestName - 通过"
            if ($response.issueCount -gt 0) {
                Write-Host "  发现 $($response.issueCount) 个问题" -ForegroundColor Yellow
            }
        } else {
            Write-TestError "$TestName - 失败"
        }
        
        return $response
    } catch {
        Write-TestError "$TestName - 异常: $($_.Exception.Message)"
        return $null
    }
}

# 主函数
Write-Host "四会培训平台安全测试工具" -ForegroundColor Cyan
Write-Host "测试目标: $BaseUrl" -ForegroundColor Gray
Write-Host ""

# 获取Token
$adminToken = Get-AdminToken
if (-not $adminToken) {
    Write-TestError "无法获取管理员Token，测试终止"
    exit 1
}

# 执行测试套件
Write-Host "`n开始执行安全测试套件..." -ForegroundColor Cyan

$testResults = @()

# 执行各项测试
$testResults += Invoke-SecurityTest "JWT安全性测试" "/api/security-test/test/jwt" $adminToken
$testResults += Invoke-SecurityTest "密码加密测试" "/api/security-test/test/password" $adminToken
$testResults += Invoke-SecurityTest "SQL注入防护测试" "/api/security-test/test/sql-injection" $adminToken
$testResults += Invoke-SecurityTest "XSS防护测试" "/api/security-test/test/xss" $adminToken
$testResults += Invoke-SecurityTest "CSRF防护测试" "/api/security-test/test/csrf" $adminToken
$testResults += Invoke-SecurityTest "访问控制测试" "/api/security-test/test/access-control" $adminToken
$testResults += Invoke-SecurityTest "会话管理测试" "/api/security-test/test/session" $adminToken
$testResults += Invoke-SecurityTest "API安全测试" "/api/security-test/test/api" $adminToken
$testResults += Invoke-SecurityTest "敏感数据保护测试" "/api/security-test/test/data-protection" $adminToken

if ($FullTest) {
    Write-Host "`n执行完整测试..." -ForegroundColor Cyan
    $testResults += Invoke-SecurityTest "完整安全测试" "/api/security-test/full-test" $adminToken
    $testResults += Invoke-SecurityTest "漏洞评估" "/api/security-test/assessment" $adminToken
}

# 生成报告
Write-Host "`n生成测试报告..." -ForegroundColor Cyan
$reportPath = Join-Path $OutputDir "security-report.json"
$testResults | ConvertTo-Json -Depth 5 | Out-File -FilePath $reportPath -Encoding UTF8

Write-Host "`n测试完成！" -ForegroundColor Green
Write-Host "报告保存至: $reportPath" -ForegroundColor Gray 