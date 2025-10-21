@echo off
echo ======================================
echo 启动实验室试剂管理系统 - 后端
echo ======================================
echo.

cd backend
echo 正在启动后端服务...
mvn spring-boot:run -Dmaven.test.skip=true

pause


