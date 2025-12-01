@echo off
REM Script para reiniciar contenedores Docker (CMD/Batch)

echo ========================================
echo Reiniciando contenedores Docker de HCEN
echo ========================================
echo.

REM Cambiar al directorio hcen
cd /d "%~dp0"

echo [1/5] Deteniendo y eliminando contenedores...
docker-compose down -v
if %errorlevel% neq 0 (
    echo ERROR: No se pudieron detener los contenedores
    pause
    exit /b 1
)

echo.
echo [2/5] Verificando puerto 5432...
netstat -ano | findstr :5432
if %errorlevel% equ 0 (
    echo ADVERTENCIA: El puerto 5432 esta en uso!
    echo Por favor, deten el servicio que lo esta usando o cambia el puerto en docker-compose.yml
    echo.
    echo Presiona cualquier tecla para continuar de todas formas...
    pause >nul
)

echo.
echo [3/5] Iniciando contenedores...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ERROR: No se pudieron iniciar los contenedores
    pause
    exit /b 1
)

echo.
echo [4/5] Esperando 25 segundos para que los servicios se inicialicen...
timeout /t 25 /nobreak >nul

echo.
echo [5/5] Verificando tablas en la base de datos...
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"

echo.
echo ========================================
echo Proceso completado!
echo ========================================
echo.
echo Para ver los logs del backend:
echo   docker logs hcen-backend -f
echo.
echo Para verificar usuarios:
echo   docker exec hcen-postgres psql -U hcen_user -d hcen -c "SELECT id, uid, email, primer_nombre, primer_apellido, rol FROM users;"
echo.
pause


