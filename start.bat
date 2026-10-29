@echo off
setlocal
cd /d "%~dp0"

echo.
echo Accountant - choose launch mode
echo 1. Local:    http://localhost:8080
echo 2. Public:   http://89.109.17.6:4242
echo.
set /p "MODE=Enter 1 or 2: "

if "%MODE%"=="1" goto local
if "%MODE%"=="2" goto public

echo Invalid choice. Run start.bat again and choose 1 or 2.
exit /b 1

:local
cd /d "%~dp0backend"
call gradlew.bat bootRun --args="--server.port=8080 --spring.web.resources.static-locations=file:../frontend/"
exit /b %ERRORLEVEL%

:public
cd /d "%~dp0backend"
call gradlew.bat bootRun --args="--server.port=4242 --spring.web.resources.static-locations=file:../frontend/ --spring.h2.console.enabled=false"
exit /b %ERRORLEVEL%
