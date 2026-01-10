@echo off
chcp 65001 > nul
echo ====================================
echo   重新编译后端代码
echo ====================================
echo.

cd /d "%~dp0backend"

echo 正在编译...
call mvn clean compile -DskipTests

if %errorlevel% == 0 (
    echo.
    echo ====================================
    echo   编译成功！
    echo ====================================
    echo.
    echo 请重启后端服务以应用更改
) else (
    echo.
    echo ====================================
    echo   编译失败，请检查错误信息
    echo ====================================
)

pause

