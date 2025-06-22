# 四会培训平台安全测试自动化脚本
# 作者: 安全测试团队
# 日期: 2025-06-18
# 版本: 1.0

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminUsername = "admin", 
    [string]$AdminPassword = "admin123",
    [string]$OutputDir = "security-test-results",
    [switch]$FullTest = $false,
    [switch]$Verbose = $false
)

# 设置错误处理
$ErrorActionPreference = "Continue"

# 创建输出目录
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# 日志文件
$LogFile = Join-Path $OutputDir "security-test.log"
$ReportFile = Join-Path $OutputDir "security-report.json"

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    Write-Host $logMessage
    Add-Content -Path $LogFile -Value $logMessage
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
    Write-Log $Message "SUCCESS"
}

function Write-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
    Write-Log $Message "ERROR"
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
    Write-Log $Message "WARNING"
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
    Write-Log $Message "INFO"
}

# 测试结果存储
$TestResults = @{
    TotalTests = 0
    PassedTests = 0
    FailedTests = 0
    SecurityScore = 0
    Vulnerabilities = @()
    TestDetails = @{}
    StartTime = Get-Date
    EndTime = $null
}

# 获取管理员Token
function Get-AdminToken {
    Write-Info "正在获取管理员访问令牌..."
    
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
            Write-Error "管理员令牌获取失败: 响应中未包含token"
            return $null
        }
    } catch {
        Write-Error "管理员令牌获取失败: $($_.Exception.Message)"
        return $null
    }
}

# 执行单个安全测试
function Invoke-SecurityTest {
    param(
        [string]$TestName,
        [string]$Endpoint,
        [string]$Token,
        [string]$Method = "GET",
        [hashtable]$Body = @{},
        [string]$Description
    )
    
    $TestResults.TotalTests++
    Write-Info "执行安全测试: $TestName"
    if ($Verbose) { Write-Info "测试描述: $Description" }
    
    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }
    
    try {
        $url = "$BaseUrl$Endpoint"
        $bodyJson = if ($Body.Count -gt 0) { $Body | ConvertTo-Json } else { $null }
        
        if ($Verbose) { Write-Info "请求URL: $url" }
        
        if ($Method -eq "POST" -and $bodyJson) {
            $response = Invoke-RestMethod -Uri $url -Method $Method -Headers $headers -Body $bodyJson
        } else {
            $response = Invoke-RestMethod -Uri $url -Method $Method -Headers $headers
        }
        
        # 分析响应结果
        if ($response.success -eq $true) {
            Write-Success "$TestName - 通过"
            $TestResults.PassedTests++
            
            # 记录测试详情
            $TestResults.TestDetails[$TestName] = @{
                Status = "PASSED"
                Response = $response
                Issues = @()
            }
            
            # 检查是否有发现的问题
            if ($response.issueCount -and $response.issueCount -gt 0) {
                Write-Warning "$TestName - 发现 $($response.issueCount) 个问题"
                if ($response.issues) {
                    foreach ($issue in $response.issues) {
                        $TestResults.Vulnerabilities += @{
                            TestName = $TestName
                            Issue = $issue
                            Severity = if ($issue.severity) { $issue.severity } else { "Medium" }
                            Timestamp = Get-Date
                        }
                    }
                }
            }
        } else {
            Write-Error "$TestName - 失败: $($response.message)"
            $TestResults.FailedTests++
            $TestResults.TestDetails[$TestName] = @{
                Status = "FAILED"
                Response = $response
                Error = $response.message
            }
        }
        
        return $response
        
    } catch {
        Write-Error "$TestName - 异常: $($_.Exception.Message)"
        $TestResults.FailedTests++
        $TestResults.TestDetails[$TestName] = @{
            Status = "ERROR"
            Error = $_.Exception.Message
        }
        return $null
    }
}

# 主要安全测试套件
function Invoke-SecurityTestSuite {
    param([string]$Token)
    
    Write-Info "开始执行安全测试套件..."
    
    # 1. JWT安全性测试
    Invoke-SecurityTest -TestName "JWT_Security" -Endpoint "/api/security-test/test/jwt" -Token $Token -Description "JWT令牌安全性验证"
    
    # 2. 密码加密测试
    Invoke-SecurityTest -TestName "Password_Encryption" -Endpoint "/api/security-test/test/password" -Token $Token -Description "密码加密强度验证"
    
    # 3. SQL注入防护测试
    Invoke-SecurityTest -TestName "SQL_Injection_Protection" -Endpoint "/api/security-test/test/sql-injection" -Token $Token -Description "SQL注入攻击防护验证"
    
    # 4. XSS防护测试
    Invoke-SecurityTest -TestName "XSS_Protection" -Endpoint "/api/security-test/test/xss" -Token $Token -Description "跨站脚本攻击防护验证"
    
    # 5. CSRF防护测试
    Invoke-SecurityTest -TestName "CSRF_Protection" -Endpoint "/api/security-test/test/csrf" -Token $Token -Description "跨站请求伪造防护验证"
    
    # 6. 访问控制测试
    Invoke-SecurityTest -TestName "Access_Control" -Endpoint "/api/security-test/test/access-control" -Token $Token -Description "基于角色的访问控制验证"
    
    # 7. 会话管理测试
    Invoke-SecurityTest -TestName "Session_Management" -Endpoint "/api/security-test/test/session" -Token $Token -Description "会话管理安全性验证"
    
    # 8. API安全测试
    Invoke-SecurityTest -TestName "API_Security" -Endpoint "/api/security-test/test/api" -Token $Token -Description "API安全机制验证"
    
    # 9. 敏感数据保护测试
    Invoke-SecurityTest -TestName "Data_Protection" -Endpoint "/api/security-test/test/data-protection" -Token $Token -Description "敏感数据保护机制验证"
    
    if ($FullTest) {
        Write-Info "执行完整安全测试..."
        # 10. 完整安全测试套件
        Invoke-SecurityTest -TestName "Full_Security_Test" -Endpoint "/api/security-test/full-test" -Token $Token -Method "POST" -Description "完整安全测试套件执行"
        
        # 11. 安全漏洞评估
        Invoke-SecurityTest -TestName "Vulnerability_Assessment" -Endpoint "/api/security-test/assessment" -Token $Token -Method "POST" -Description "安全漏洞评估分析"
        
        # 12. 生成安全报告
        Invoke-SecurityTest -TestName "Security_Report" -Endpoint "/api/security-test/report" -Token $Token -Description "安全测试报告生成"
    }
}

# 手动渗透测试
function Invoke-PenetrationTests {
    param([string]$Token)
    
    Write-Info "开始执行手动渗透测试..."
    
    # 测试弱密码
    Write-Info "测试弱密码攻击..."
    $weakPasswords = @("123456", "password", "admin", "123456789", "qwerty")
    foreach ($password in $weakPasswords) {
        try {
            $loginData = @{
                username = "admin"
                password = $password
            } | ConvertTo-Json
            
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginData -ContentType "application/json" -ErrorAction SilentlyContinue
            if ($response.token) {
                Write-Error "弱密码漏洞: 能够使用密码 '$password' 登录"
                $TestResults.Vulnerabilities += @{
                    TestName = "Weak_Password_Test"
                    Issue = "系统接受弱密码: $password"
                    Severity = "High"
                    Timestamp = Get-Date
                }
            }
        } catch {
            # 预期的失败，说明密码被正确拒绝
        }
    }
    
    # 测试SQL注入
    Write-Info "测试SQL注入攻击..."
    $sqlPayloads = @(
        "admin' OR '1'='1",
        "admin'; DROP TABLE users; --",
        "admin' UNION SELECT * FROM users --"
    )
    
    foreach ($payload in $sqlPayloads) {
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/users?username=$payload" -Headers @{"Authorization" = "Bearer $Token"} -ErrorAction SilentlyContinue
            if ($response -and $response.Count -gt 0) {
                Write-Error "SQL注入漏洞: payload '$payload' 返回了数据"
                $TestResults.Vulnerabilities += @{
                    TestName = "SQL_Injection_Manual"
                    Issue = "SQL注入payload成功: $payload"
                    Severity = "Critical"
                    Timestamp = Get-Date
                }
            }
        } catch {
            # 预期的失败，说明SQL注入被正确阻止
        }
    }
    
    # 测试XSS攻击
    Write-Info "测试XSS攻击..."
    $xssPayloads = @(
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "javascript:alert('XSS')"
    )
    
    foreach ($payload in $xssPayloads) {
        try {
            $testData = @{
                title = $payload
                content = "test content"
            } | ConvertTo-Json
            
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/content" -Method POST -Headers @{"Authorization" = "Bearer $Token"; "Content-Type" = "application/json"} -Body $testData -ErrorAction SilentlyContinue
            if ($response -and $response.title -eq $payload) {
                Write-Error "XSS漏洞: payload '$payload' 未被过滤"
                $TestResults.Vulnerabilities += @{
                    TestName = "XSS_Manual_Test"
                    Issue = "XSS payload未被过滤: $payload"
                    Severity = "High"
                    Timestamp = Get-Date
                }
            }
        } catch {
            # 可能是正常的验证失败
        }
    }
    
    # 测试越权访问
    Write-Info "测试越权访问..."
    try {
        # 创建普通用户token（如果可能）
        $userLoginData = @{
            username = "user"
            password = "user123"
        } | ConvertTo-Json
        
        $userResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $userLoginData -ContentType "application/json" -ErrorAction SilentlyContinue
        if ($userResponse.token) {
            # 尝试使用普通用户token访问管理员功能
            $adminResponse = Invoke-RestMethod -Uri "$BaseUrl/api/admin/users" -Headers @{"Authorization" = "Bearer $($userResponse.token)"} -ErrorAction SilentlyContinue
            if ($adminResponse) {
                Write-Error "越权访问漏洞: 普通用户能够访问管理员功能"
                $TestResults.Vulnerabilities += @{
                    TestName = "Privilege_Escalation"
                    Issue = "普通用户能够访问管理员API"
                    Severity = "Critical"
                    Timestamp = Get-Date
                }
            }
        }
    } catch {
        # 预期的失败，说明越权访问被正确阻止
    }
}

# 依赖项漏洞扫描
function Invoke-DependencyCheck {
    Write-Info "开始依赖项安全扫描..."
    
    try {
        # 检查Maven是否可用
        $mvnVersion = mvn -version 2>$null
        if ($mvnVersion) {
            Write-Info "执行Maven依赖项安全检查..."
            $dependencyCheckOutput = Join-Path $OutputDir "dependency-check-report.html"
            
            # 切换到后端目录
            Push-Location "sihui-backend"
            
            # 运行OWASP依赖项检查
            mvn org.owasp:dependency-check-maven:check -Dformat=HTML -DoutputDirectory=$OutputDir
            
            Pop-Location
            
            if (Test-Path $dependencyCheckOutput) {
                Write-Success "依赖项安全检查完成，报告已生成: $dependencyCheckOutput"
            } else {
                Write-Warning "依赖项安全检查完成，但未找到报告文件"
            }
        } else {
            Write-Warning "Maven未安装或不在PATH中，跳过依赖项扫描"
        }
    } catch {
        Write-Error "依赖项扫描失败: $($_.Exception.Message)"
    }
}

# 网络端口扫描
function Invoke-PortScan {
    Write-Info "开始网络端口扫描..."
    
    $commonPorts = @(21, 22, 23, 25, 53, 80, 110, 143, 443, 993, 995, 3306, 5432, 6379, 8080, 8443, 9000)
    $openPorts = @()
    
    foreach ($port in $commonPorts) {
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $tcpClient.ConnectAsync("localhost", $port).Wait(1000)
            if ($tcpClient.Connected) {
                $openPorts += $port
                $tcpClient.Close()
            }
        } catch {
            # 端口关闭，这是预期的
        }
    }
    
    if ($openPorts.Count -gt 0) {
        Write-Info "发现开放端口: $($openPorts -join ', ')"
        
        # 检查不应该开放的危险端口
        $dangerousPorts = @(21, 22, 23, 25, 53, 110, 143)
        $exposedDangerousPorts = $openPorts | Where-Object { $_ -in $dangerousPorts }
        
        if ($exposedDangerousPorts.Count -gt 0) {
            Write-Warning "发现潜在危险的开放端口: $($exposedDangerousPorts -join ', ')"
            $TestResults.Vulnerabilities += @{
                TestName = "Port_Scan"
                Issue = "发现开放的危险端口: $($exposedDangerousPorts -join ', ')"
                Severity = "Medium"
                Timestamp = Get-Date
            }
        }
    } else {
        Write-Info "未发现开放端口"
    }
}

# 生成安全测试报告
function Generate-SecurityReport {
    Write-Info "生成安全测试报告..."
    
    $TestResults.EndTime = Get-Date
    $duration = $TestResults.EndTime - $TestResults.StartTime
    
    # 计算安全得分
    $passRate = if ($TestResults.TotalTests -gt 0) { ($TestResults.PassedTests / $TestResults.TotalTests) * 100 } else { 0 }
    $vulnerabilityPenalty = ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Critical" }).Count * 20 +
                           ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "High" }).Count * 10 +
                           ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Medium" }).Count * 5 +
                           ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Low" }).Count * 1
    
    $TestResults.SecurityScore = [Math]::Max(0, $passRate - $vulnerabilityPenalty)
    
    # 安全等级评估
    $securityGrade = switch ($TestResults.SecurityScore) {
        { $_ -ge 90 } { "A - 优秀" }
        { $_ -ge 80 } { "B - 良好" }
        { $_ -ge 70 } { "C - 一般" }
        { $_ -ge 60 } { "D - 较差" }
        default { "F - 严重不足" }
    }
    
    # 创建报告对象
    $report = @{
        Summary = @{
            SecurityScore = $TestResults.SecurityScore
            SecurityGrade = $securityGrade
            TotalTests = $TestResults.TotalTests
            PassedTests = $TestResults.PassedTests
            FailedTests = $TestResults.FailedTests
            TotalVulnerabilities = $TestResults.Vulnerabilities.Count
            CriticalVulnerabilities = ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Critical" }).Count
            HighVulnerabilities = ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "High" }).Count
            MediumVulnerabilities = ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Medium" }).Count
            LowVulnerabilities = ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Low" }).Count
            TestDuration = $duration.ToString()
            StartTime = $TestResults.StartTime
            EndTime = $TestResults.EndTime
        }
        TestDetails = $TestResults.TestDetails
        Vulnerabilities = $TestResults.Vulnerabilities
        Recommendations = @()
    }
    
    # 添加建议
    if ($TestResults.Vulnerabilities.Count -gt 0) {
        $report.Recommendations += "立即修复发现的 $($TestResults.Vulnerabilities.Count) 个安全漏洞"
    }
    
    if ($TestResults.SecurityScore -lt 80) {
        $report.Recommendations += "安全得分偏低，建议进行全面的安全加固"
    }
    
    $report.Recommendations += "定期进行安全测试和漏洞扫描"
    $report.Recommendations += "建立安全监控和事件响应机制"
    $report.Recommendations += "对开发团队进行安全培训"
    
    # 保存报告
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $ReportFile -Encoding UTF8
    
    # 显示测试结果摘要
    Write-Host "`n" -NoNewline
    Write-Host "======================== 安全测试结果摘要 ========================" -ForegroundColor Cyan
    Write-Host "安全得分: " -NoNewline
    $scoreColor = if ($TestResults.SecurityScore -ge 80) { "Green" } elseif ($TestResults.SecurityScore -ge 60) { "Yellow" } else { "Red" }
    Write-Host "$($TestResults.SecurityScore.ToString('F1')) 分 ($securityGrade)" -ForegroundColor $scoreColor
    Write-Host "测试总数: $($TestResults.TotalTests)"
    Write-Host "通过测试: " -NoNewline; Write-Host $TestResults.PassedTests -ForegroundColor Green
    Write-Host "失败测试: " -NoNewline; Write-Host $TestResults.FailedTests -ForegroundColor Red
    Write-Host "发现漏洞: " -NoNewline; Write-Host $TestResults.Vulnerabilities.Count -ForegroundColor $(if ($TestResults.Vulnerabilities.Count -eq 0) { "Green" } else { "Red" })
    
    if ($TestResults.Vulnerabilities.Count -gt 0) {
        Write-Host "  - 严重: " -NoNewline; Write-Host ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Critical" }).Count -ForegroundColor Red
        Write-Host "  - 高危: " -NoNewline; Write-Host ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "High" }).Count -ForegroundColor Red
        Write-Host "  - 中危: " -NoNewline; Write-Host ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Medium" }).Count -ForegroundColor Yellow
        Write-Host "  - 低危: " -NoNewline; Write-Host ($TestResults.Vulnerabilities | Where-Object { $_.Severity -eq "Low" }).Count -ForegroundColor Yellow
    }
    
    Write-Host "测试时长: $($duration.ToString('hh\:mm\:ss'))"
    Write-Host "详细报告: $ReportFile"
    Write-Host "================================================================" -ForegroundColor Cyan
}

# 主函数
function Main {
    Write-Host "四会培训平台安全测试工具 v1.0" -ForegroundColor Cyan
    Write-Host "开始时间: $(Get-Date)" -ForegroundColor Gray
    Write-Host "测试目标: $BaseUrl" -ForegroundColor Gray
    Write-Host "输出目录: $OutputDir" -ForegroundColor Gray
    Write-Host ""
    
    # 清空日志文件
    if (Test-Path $LogFile) { Remove-Item $LogFile }
    
    Write-Log "开始安全测试" "INFO"
    
    # 1. 获取管理员Token
    $adminToken = Get-AdminToken
    if (-not $adminToken) {
        Write-Error "无法获取管理员访问令牌，测试终止"
        return
    }
    
    # 2. 执行自动化安全测试套件
    Invoke-SecurityTestSuite -Token $adminToken
    
    # 3. 执行手动渗透测试
    if ($FullTest) {
        Invoke-PenetrationTests -Token $adminToken
        
        # 4. 依赖项漏洞扫描
        Invoke-DependencyCheck
        
        # 5. 网络端口扫描
        Invoke-PortScan
    }
    
    # 6. 生成测试报告
    Generate-SecurityReport
    
    Write-Log "安全测试完成" "INFO"
    
    # 返回结果
    return $TestResults.SecurityScore -ge 80
}

# 执行主函数
try {
    $success = Main
    if ($success) {
        exit 0
    } else {
        exit 1
    }
} catch {
    Write-Error "安全测试执行失败: $($_.Exception.Message)"
    Write-Log "安全测试执行失败: $($_.Exception.Message)" "ERROR"
    exit 2
} 