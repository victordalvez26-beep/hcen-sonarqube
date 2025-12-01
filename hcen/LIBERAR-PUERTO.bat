@echo off
REM Script para liberar puerto PostgreSQL

set PUERTO=5433
if not "%1"=="" set PUERTO=%1

echo ========================================
echo Liberando puerto %PUERTO%
echo ========================================
echo.

echo Buscando procesos usando el puerto %PUERTO%...
netstat -ano | findstr :%PUERTO%

echo.
echo Para detener un proceso, usa:
echo   taskkill /PID ^<NUMERO_PID^> /F
echo.
echo Ejemplo:
echo   taskkill /PID 12345 /F
echo.

pause


