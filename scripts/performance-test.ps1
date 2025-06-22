# 四会项目性能测试脚本
# 用于测试系统关键性能指标和优化效果

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$ConcurrentUsers = 10,
    [int]$TestDuration = 60,
    [string]$OutputFile = "performance-test-report.json"
)

Write-Host "=== 四会项目性能测试 ===" -ForegroundColor Green
Write-Host "测试目标: $BaseUrl" -ForegroundColor Yellow
Write-Host "并发用户: $ConcurrentUsers" -ForegroundColor Yellow
Write-Host "测试时长: $TestDuration 秒" -ForegroundColor Yellow

# 测试结果存储
$TestResults = @{
    timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    baseUrl = $BaseUrl
    concurrentUsers = $ConcurrentUsers
    testDuration = $TestDuration
    tests = @()
    summary = @{}
}

# 测试配置
$TestToken = ""
$TestUsername = "testuser"
$TestPassword = "password123"

# 获取认证Token
function Get-AuthToken {
    param([string]$Username, [string]$Password)
    
    try {
        $loginData = @{
            username = $Username
            password = $Password
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST `
            -ContentType "application/json" -Body $loginData -TimeoutSec 10
        
        return $response.token
    }
    catch {
        Write-Warning "无法获取认证Token: $($_.Exception.Message)"
        return $null
    }
}

# 性能测试函数
function Test-ApiPerformance {
    param(
        [string]$TestName,
        [string]$Endpoint,
        [string]$Method = "GET",
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [int]$Iterations = 100
    )
    
    Write-Host "测试: $TestName" -ForegroundColor Cyan
    
    $results = @{
        testName = $TestName
        endpoint = $Endpoint
        method = $Method
        iterations = $Iterations
        responseTimes = @()
        successCount = 0
        errorCount = 0
        averageResponseTime = 0
        p95ResponseTime = 0
        p99ResponseTime = 0
        throughput = 0
        errors = @()
    }
    
    # 添加认证头
    if ($TestToken) {
        $Headers["Authorization"] = "Bearer $TestToken"
    }
    
    $startTime = Get-Date
    
    for ($i = 1; $i -le $Iterations; $i++) {
        try {
            $testStart = Get-Date
            
            if ($Method -eq "GET") {
                $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method $Method `
                    -Headers $Headers -TimeoutSec 30
            }
            else {
                $jsonBody = $Body | ConvertTo-Json
                $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method $Method `
                    -Headers $Headers -ContentType "application/json" -Body $jsonBody -TimeoutSec 30
            }
            
            $testEnd = Get-Date
            $responseTime = ($testEnd - $testStart).TotalMilliseconds
            $results.responseTimes += $responseTime
            $results.successCount++
            
            Write-Progress -Activity "测试进行中" -Status "$TestName" `
                -PercentComplete (($i / $Iterations) * 100)
        }
        catch {
            $results.errorCount++
            $results.errors += $_.Exception.Message
            Write-Verbose "请求失败: $($_.Exception.Message)"
        }
    }
    
    $endTime = Get-Date
    $totalTime = ($endTime - $startTime).TotalSeconds
    
    # 计算统计数据
    if ($results.responseTimes.Count -gt 0) {
        $sortedTimes = $results.responseTimes | Sort-Object
        $results.averageResponseTime = ($results.responseTimes | Measure-Object -Average).Average
        $results.p95ResponseTime = $sortedTimes[[math]::Floor($sortedTimes.Count * 0.95)]
        $results.p99ResponseTime = $sortedTimes[[math]::Floor($sortedTimes.Count * 0.99)]
        $results.throughput = $results.successCount / $totalTime
    }
    
    Write-Host "  成功请求: $($results.successCount)/$Iterations" -ForegroundColor Green
    Write-Host "  平均响应时间: $([math]::Round($results.averageResponseTime, 2))ms" -ForegroundColor Yellow
    Write-Host "  P95响应时间: $([math]::Round($results.p95ResponseTime, 2))ms" -ForegroundColor Yellow
    Write-Host "  P99响应时间: $([math]::Round($results.p99ResponseTime, 2))ms" -ForegroundColor Yellow
    Write-Host "  吞吐量: $([math]::Round($results.throughput, 2)) TPS" -ForegroundColor Yellow
    
    return $results
}

# 并发测试函数
function Test-ConcurrentLoad {
    param(
        [string]$TestName,
        [string]$Endpoint,
        [int]$Users = 10,
        [int]$Duration = 30
    )
    
    Write-Host "并发测试: $TestName ($Users 用户, $Duration 秒)" -ForegroundColor Cyan
    
    $jobs = @()
    $startTime = Get-Date
    
    # 启动并发作业
    for ($i = 1; $i -le $Users; $i++) {
        $job = Start-Job -ScriptBlock {
            param($BaseUrl, $Endpoint, $Duration, $Token)
            
            $headers = @{}
            if ($Token) {
                $headers["Authorization"] = "Bearer $Token"
            }
            
            $results = @{
                requests = 0
                errors = 0
                responseTimes = @()
            }
            
            $endTime = (Get-Date).AddSeconds($Duration)
            
            while ((Get-Date) -lt $endTime) {
                try {
                    $testStart = Get-Date
                    $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method GET `
                        -Headers $headers -TimeoutSec 10
                    $testEnd = Get-Date
                    
                    $responseTime = ($testEnd - $testStart).TotalMilliseconds
                    $results.responseTimes += $responseTime
                    $results.requests++
                }
                catch {
                    $results.errors++
                }
                
                Start-Sleep -Milliseconds 100
            }
            
            return $results
        } -ArgumentList $BaseUrl, $Endpoint, $Duration, $TestToken
        
        $jobs += $job
    }
    
    # 等待所有作业完成
    Write-Host "等待并发测试完成..." -ForegroundColor Yellow
    $jobResults = $jobs | Wait-Job | Receive-Job
    $jobs | Remove-Job
    
    # 汇总结果
    $totalRequests = ($jobResults | Measure-Object -Property requests -Sum).Sum
    $totalErrors = ($jobResults | Measure-Object -Property errors -Sum).Sum
    $allResponseTimes = $jobResults | ForEach-Object { $_.responseTimes } | Where-Object { $_ }
    
    $results = @{
        testName = $TestName
        concurrentUsers = $Users
        duration = $Duration
        totalRequests = $totalRequests
        totalErrors = $totalErrors
        successRate = if ($totalRequests -gt 0) { (($totalRequests - $totalErrors) / $totalRequests) * 100 } else { 0 }
        averageResponseTime = if ($allResponseTimes.Count -gt 0) { ($allResponseTimes | Measure-Object -Average).Average } else { 0 }
        throughput = $totalRequests / $Duration
    }
    
    Write-Host "  总请求数: $($results.totalRequests)" -ForegroundColor Green
    Write-Host "  成功率: $([math]::Round($results.successRate, 2))%" -ForegroundColor Yellow
    Write-Host "  平均响应时间: $([math]::Round($results.averageResponseTime, 2))ms" -ForegroundColor Yellow
    Write-Host "  整体吞吐量: $([math]::Round($results.throughput, 2)) TPS" -ForegroundColor Yellow
    
    return $results
}

# 数据库性能测试
function Test-DatabasePerformance {
    Write-Host "数据库性能测试" -ForegroundColor Cyan
    
    $dbTests = @()
    
    # 测试用户查询性能
    $userQueryTest = Test-ApiPerformance -TestName "用户列表查询" -Endpoint "/api/admin/users?page=0&size=20" -Iterations 50
    $dbTests += $userQueryTest
    
    # 测试文档查询性能
    $docQueryTest = Test-ApiPerformance -TestName "文档列表查询" -Endpoint "/api/documents?page=0&size=20" -Iterations 50
    $dbTests += $docQueryTest
    
    # 测试问卷查询性能
    $questionnaireTest = Test-ApiPerformance -TestName "问卷列表查询" -Endpoint "/api/questionnaires?page=0&size=20" -Iterations 50
    $dbTests += $questionnaireTest
    
    return $dbTests
}

# 缓存性能测试
function Test-CachePerformance {
    Write-Host "缓存性能测试" -ForegroundColor Cyan
    
    $cacheTests = @()
    
    # 第一次查询（缓存未命中）
    Write-Host "第一次查询（缓存预热）..." -ForegroundColor Yellow
    $warmupTest = Test-ApiPerformance -TestName "缓存预热查询" -Endpoint "/api/admin/users/1" -Iterations 5
    
    # 第二次查询（缓存命中）
    Write-Host "第二次查询（缓存命中）..." -ForegroundColor Yellow
    $cacheHitTest = Test-ApiPerformance -TestName "缓存命中查询" -Endpoint "/api/admin/users/1" -Iterations 50
    
    $cacheTests += $warmupTest
    $cacheTests += $cacheHitTest
    
    # 计算缓存效果
    if ($warmupTest.averageResponseTime -gt 0 -and $cacheHitTest.averageResponseTime -gt 0) {
        $cacheImprovement = (($warmupTest.averageResponseTime - $cacheHitTest.averageResponseTime) / $warmupTest.averageResponseTime) * 100
        Write-Host "  缓存性能提升: $([math]::Round($cacheImprovement, 2))%" -ForegroundColor Green
    }
    
    return $cacheTests
}

# 开始性能测试
try {
    Write-Host "连接到服务器..." -ForegroundColor Yellow
    
    # 健康检查
    try {
        $healthCheck = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method GET -TimeoutSec 10
        Write-Host "服务器状态: $($healthCheck.status)" -ForegroundColor Green
    }
    catch {
        Write-Warning "无法连接到服务器或获取健康状态"
    }
    
    # 获取认证Token
    Write-Host "获取认证Token..." -ForegroundColor Yellow
    $TestToken = Get-AuthToken -Username $TestUsername -Password $TestPassword
    
    # 1. API响应时间测试
    Write-Host "`n=== API响应时间测试 ===" -ForegroundColor Green
    
    $apiTests = @()
    $apiTests += Test-ApiPerformance -TestName "健康检查" -Endpoint "/actuator/health" -Iterations 50
    $apiTests += Test-ApiPerformance -TestName "系统信息" -Endpoint "/actuator/info" -Iterations 30
    
    if ($TestToken) {
        $apiTests += Test-ApiPerformance -TestName "用户资料" -Endpoint "/api/auth/profile" -Iterations 50
    }
    
    $TestResults.tests += $apiTests
    
    # 2. 数据库性能测试
    Write-Host "`n=== 数据库性能测试 ===" -ForegroundColor Green
    $dbTests = Test-DatabasePerformance
    $TestResults.tests += $dbTests
    
    # 3. 缓存性能测试
    Write-Host "`n=== 缓存性能测试 ===" -ForegroundColor Green
    $cacheTests = Test-CachePerformance
    $TestResults.tests += $cacheTests
    
    # 4. 并发负载测试
    Write-Host "`n=== 并发负载测试 ===" -ForegroundColor Green
    $concurrentTests = @()
    $concurrentTests += Test-ConcurrentLoad -TestName "用户列表并发访问" -Endpoint "/api/admin/users" -Users $ConcurrentUsers -Duration 30
    $concurrentTests += Test-ConcurrentLoad -TestName "文档列表并发访问" -Endpoint "/api/documents" -Users $ConcurrentUsers -Duration 30
    
    $TestResults.tests += $concurrentTests
    
    # 5. 生成测试摘要
    Write-Host "`n=== 测试摘要 ===" -ForegroundColor Green
    
    $allTests = $TestResults.tests | Where-Object { $_.averageResponseTime -gt 0 }
    $avgResponseTime = ($allTests | Measure-Object -Property averageResponseTime -Average).Average
    $maxResponseTime = ($allTests | Measure-Object -Property averageResponseTime -Maximum).Maximum
    $avgThroughput = ($allTests | Measure-Object -Property throughput -Average).Average
    
    $TestResults.summary = @{
        totalTests = $TestResults.tests.Count
        averageResponseTime = [math]::Round($avgResponseTime, 2)
        maxResponseTime = [math]::Round($maxResponseTime, 2)
        averageThroughput = [math]::Round($avgThroughput, 2)
        performanceGrade = ""
    }
    
    # 性能评级
    if ($avgResponseTime -lt 100) {
        $TestResults.summary.performanceGrade = "优秀"
        $gradeColor = "Green"
    }
    elseif ($avgResponseTime -lt 300) {
        $TestResults.summary.performanceGrade = "良好"
        $gradeColor = "Yellow"
    }
    elseif ($avgResponseTime -lt 500) {
        $TestResults.summary.performanceGrade = "一般"
        $gradeColor = "DarkYellow"
    }
    else {
        $TestResults.summary.performanceGrade = "需要优化"
        $gradeColor = "Red"
    }
    
    Write-Host "总测试数: $($TestResults.summary.totalTests)" -ForegroundColor Cyan
    Write-Host "平均响应时间: $($TestResults.summary.averageResponseTime)ms" -ForegroundColor Cyan
    Write-Host "最大响应时间: $($TestResults.summary.maxResponseTime)ms" -ForegroundColor Cyan
    Write-Host "平均吞吐量: $($TestResults.summary.averageThroughput) TPS" -ForegroundColor Cyan
    Write-Host "性能评级: $($TestResults.summary.performanceGrade)" -ForegroundColor $gradeColor
    
    # 6. 保存测试报告
    $reportJson = $TestResults | ConvertTo-Json -Depth 10
    $reportJson | Out-File -FilePath $OutputFile -Encoding UTF8
    Write-Host "`n测试报告已保存到: $OutputFile" -ForegroundColor Green
    
    # 7. 性能建议
    Write-Host "`n=== 性能优化建议 ===" -ForegroundColor Green
    
    if ($avgResponseTime -gt 200) {
        Write-Host "• 建议检查数据库查询优化和索引配置" -ForegroundColor Yellow
    }
    
    if ($avgThroughput -lt 100) {
        Write-Host "• 建议增加服务器线程池大小" -ForegroundColor Yellow
    }
    
    $cacheHitTest = $TestResults.tests | Where-Object { $_.testName -eq "缓存命中查询" }
    if ($cacheHitTest -and $cacheHitTest.averageResponseTime -gt 50) {
        Write-Host "• 建议检查Redis缓存配置和连接" -ForegroundColor Yellow
    }
    
    Write-Host "• 定期运行此性能测试脚本监控系统性能" -ForegroundColor Green
    Write-Host "• 建议在生产环境部署前进行更大规模的负载测试" -ForegroundColor Green
    
}
catch {
    Write-Error "性能测试执行失败: $($_.Exception.Message)"
    Write-Error $_.ScriptStackTrace
}

Write-Host "`n=== 性能测试完成 ===" -ForegroundColor Green 