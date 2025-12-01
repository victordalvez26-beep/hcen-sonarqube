@echo off
REM Script de diagnóstico para problemas de login (CMD)

echo ========================================
echo Diagnostico de Login - Sistema HCEN
echo ========================================
echo.

echo [1/5] Verificando contenedores...
docker ps --filter "name=hcen-backend" --format "{{.Names}}"
if %errorlevel% equ 0 (
    echo   Backend esta corriendo
) else (
    echo   Backend NO esta corriendo
)

docker ps --filter "name=hcen-postgres" --format "{{.Names}}"
if %errorlevel% equ 0 (
    echo   PostgreSQL esta corriendo
) else (
    echo   PostgreSQL NO esta corriendo
)

echo.
echo [2/5] Verificando endpoint del backend...
curl -s http://localhost:8080/api/auth/session
if %errorlevel% equ 0 (
    echo   Backend responde correctamente
) else (
    echo   Backend NO responde
)

echo.
echo [3/5] Verificando endpoint del frontend...
curl -s http://localhost:3000 >nul
if %errorlevel% equ 0 (
    echo   Frontend responde correctamente
) else (
    echo   Frontend NO responde
)

echo.
echo [4/5] Verificando tablas en la base de datos...
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt" 2>nul
if %errorlevel% equ 0 (
    echo   Base de datos accesible
) else (
    echo   No se pudo acceder a la base de datos
)

echo.
echo [5/5] Ultimos logs del backend (buscar errores)...
docker logs hcen-backend --tail 20 2>&1 | findstr /i "error exception"

echo.
echo ========================================
echo Diagnostico completado
echo ========================================
echo.
echo Para ver logs completos del backend:
echo   docker logs hcen-backend -f
echo.
echo Para iniciar el frontend:
echo   cd hcen-frontend
echo   npm start
echo.
pause


