@echo off
echo ===================================================
echo   PortfolioPro — Starting Full-Stack Application
echo ===================================================
echo.

echo Starting Spring Boot Backend (Port 8080)...
start "PortfolioPro Backend" cmd /k "cd portfoliopro-backend && mvn spring-boot:run"

echo.
echo Starting Angular Frontend (Port 4200)...
start "PortfolioPro Frontend" cmd /k "cd portfoliopro-frontend && npm install && npm start"

echo.
echo ===================================================
echo Applications are launching!
echo - Backend API:  http://localhost:8080/api/swagger-ui.html
echo - Frontend UI:  http://localhost:4200
echo - Demo User:    trader_demo / Trader123! ($100,000 buying power)
echo ===================================================
pause
