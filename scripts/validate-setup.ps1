# 简化的数据库设置验证脚本
Write-Host "验证四会项目数据库设置..." -ForegroundColor Green

$passed = 0
$failed = 0

function Test-Feature {
    param($Name, $Test)
    if ($Test) {
        Write-Host "✅ $Name" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host "❌ $Name" -ForegroundColor Red
        $script:failed++
    }
}

# 检查项目结构
Write-Host "`n=== 项目结构检查 ===" -ForegroundColor Cyan
Test-Feature "后端项目目录" (Test-Path "sihui-backend")
Test-Feature "Maven配置文件" (Test-Path "sihui-backend/pom.xml")
Test-Feature "开发环境配置" (Test-Path "sihui-backend/src/main/resources/application-dev.properties")

# 检查迁移脚本
Write-Host "`n=== 迁移脚本检查 ===" -ForegroundColor Cyan
Test-Feature "迁移目录" (Test-Path "sihui-backend/src/main/resources/db/migration")
Test-Feature "V1迁移脚本" (Test-Path "sihui-backend/src/main/resources/db/migration/V1__Create_core_tables.sql")
Test-Feature "V2迁移脚本" (Test-Path "sihui-backend/src/main/resources/db/migration/V2__Insert_initial_data.sql")

# 检查管理脚本
Write-Host "`n=== 管理脚本检查 ===" -ForegroundColor Cyan
Test-Feature "PowerShell设置脚本" (Test-Path "scripts/setup-database.ps1")
Test-Feature "Bash设置脚本" (Test-Path "scripts/setup-database.sh")
Test-Feature "备份脚本" (Test-Path "scripts/backup-database.sh")

# 检查文档
Write-Host "`n=== 文档检查 ===" -ForegroundColor Cyan
Test-Feature "数据库设计文档" (Test-Path "docs/DATABASE_DESIGN.md")
Test-Feature "迁移指南文档" (Test-Path "docs/MIGRATION_GUIDE.md")

# 检查依赖配置
Write-Host "`n=== 依赖配置检查 ===" -ForegroundColor Cyan
if (Test-Path "sihui-backend/pom.xml") {
    $pomContent = Get-Content "sihui-backend/pom.xml" -Raw
    Test-Feature "Flyway依赖" ($pomContent -match "flyway-core")
    Test-Feature "MySQL驱动" ($pomContent -match "mysql-connector")
}

# 输出结果
Write-Host "`n=== 验证结果 ===" -ForegroundColor Cyan
Write-Host "通过: $passed" -ForegroundColor Green
Write-Host "失败: $failed" -ForegroundColor Red
Write-Host "总计: $($passed + $failed)" -ForegroundColor White

if ($failed -eq 0) {
    Write-Host "`n✅ 所有检查通过！数据库设置配置正确。" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ 有$failed 项检查失败，请检查上述问题。" -ForegroundColor Yellow
} 