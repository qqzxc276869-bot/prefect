@echo off
chcp 65001 >nul
echo =========================================
echo 试剂管理系统数据库初始化脚本
echo =========================================
echo.

:: 设置MySQL连接参数
set MYSQL_HOST=127.0.0.1
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=root

echo 正在连接MySQL数据库...
echo.

:: 执行初始化脚本
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < init_database.sql

if %errorlevel% equ 0 (
    echo.
    echo =========================================
    echo 数据库初始化成功！
    echo =========================================
    echo.
    echo 数据库名称: reagent_management
    echo 初始用户账号:
    echo   管理员: admin / 123456
    echo   教师1: teacher1 / 123456
    echo   教师2: teacher2 / 123456
    echo   学生1: student1 / 123456
    echo   学生2: student2 / 123456
    echo   学生3: student3 / 123456
    echo.
    echo 已插入示例数据:
    echo   - 6个用户
    echo   - 8个试剂分类
    echo   - 15种试剂
    echo   - 10个存放位置
    echo   - 15条库存记录
    echo   - 3个课题组
    echo   - 以及其他相关数据
    echo.
) else (
    echo.
    echo =========================================
    echo 数据库初始化失败！
    echo =========================================
    echo.
    echo 请检查:
    echo   1. MySQL服务是否已启动
    echo   2. 用户名和密码是否正确
    echo   3. 是否有足够的权限
    echo.
)

pause



