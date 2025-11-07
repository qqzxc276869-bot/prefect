# 试剂管理系统数据库初始化脚本 (PowerShell)
# 编码：UTF-8

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "试剂管理系统数据库初始化脚本" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 设置MySQL连接参数
$MYSQL_HOST = "127.0.0.1"
$MYSQL_PORT = "3306"
$MYSQL_USER = "root"
$MYSQL_PASSWORD = "root"

Write-Host "正在连接MySQL数据库..." -ForegroundColor Yellow
Write-Host ""

# 获取当前脚本目录
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SqlFile = Join-Path $ScriptDir "init_database.sql"

# 检查SQL文件是否存在
if (-not (Test-Path $SqlFile)) {
    Write-Host "错误：找不到初始化脚本文件 init_database.sql" -ForegroundColor Red
    Write-Host "请确保该文件与此脚本在同一目录下" -ForegroundColor Red
    pause
    exit 1
}

# 执行MySQL命令
try {
    $command = "mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD"
    Get-Content $SqlFile | & mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=========================================" -ForegroundColor Green
        Write-Host "数据库初始化成功！" -ForegroundColor Green
        Write-Host "=========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "数据库名称: reagent_management" -ForegroundColor White
        Write-Host ""
        Write-Host "初始用户账号:" -ForegroundColor Yellow
        Write-Host "  管理员: admin / 123456" -ForegroundColor White
        Write-Host "  教师1: teacher1 / 123456" -ForegroundColor White
        Write-Host "  教师2: teacher2 / 123456" -ForegroundColor White
        Write-Host "  学生1: student1 / 123456" -ForegroundColor White
        Write-Host "  学生2: student2 / 123456" -ForegroundColor White
        Write-Host "  学生3: student3 / 123456" -ForegroundColor White
        Write-Host ""
        Write-Host "已插入示例数据:" -ForegroundColor Yellow
        Write-Host "  - 6个用户" -ForegroundColor White
        Write-Host "  - 8个试剂分类" -ForegroundColor White
        Write-Host "  - 15种试剂" -ForegroundColor White
        Write-Host "  - 10个存放位置" -ForegroundColor White
        Write-Host "  - 15条库存记录" -ForegroundColor White
        Write-Host "  - 3个课题组" -ForegroundColor White
        Write-Host "  - 以及其他相关数据" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "=========================================" -ForegroundColor Red
        Write-Host "数据库初始化失败！" -ForegroundColor Red
        Write-Host "=========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "请检查:" -ForegroundColor Yellow
        Write-Host "  1. MySQL服务是否已启动" -ForegroundColor White
        Write-Host "  2. 用户名和密码是否正确" -ForegroundColor White
        Write-Host "  3. 是否有足够的权限" -ForegroundColor White
        Write-Host "  4. MySQL是否已添加到系统PATH环境变量" -ForegroundColor White
        Write-Host ""
    }
} catch {
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Red
    Write-Host "执行脚本时发生错误！" -ForegroundColor Red
    Write-Host "=========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的原因:" -ForegroundColor Yellow
    Write-Host "  1. MySQL未安装或未添加到PATH环境变量" -ForegroundColor White
    Write-Host "  2. MySQL服务未启动" -ForegroundColor White
    Write-Host "  3. 连接参数不正确" -ForegroundColor White
    Write-Host ""
}

Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")



