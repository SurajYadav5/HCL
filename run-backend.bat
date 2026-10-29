@echo off
echo ===================================================
echo   PortfolioPro - Spring Boot Backend
echo ===================================================
set "PATH=%LOCALAPPDATA%\Programs\apache-maven\bin;%PATH%"
cd portfoliopro-backend
call mvn spring-boot:run
pause
