@echo off
REM Script para reconstruir y redesplegar la aplicación con el dialect de PostgreSQL corregido

echo === Reconstruyendo y redesplegando HCEN ===

REM 1. Reconstruir la aplicación
echo.
echo [1/4] Reconstruyendo la aplicación...
cd /d %~dp0
call mvn clean package -DskipTests

if errorlevel 1 (
    echo ERROR: La compilación falló
    exit /b 1
)

REM 2. Detener y eliminar contenedores
echo.
echo [2/4] Deteniendo contenedores...
docker-compose down

REM 3. Reconstruir y levantar contenedores
echo.
echo [3/4] Reconstruyendo y levantando contenedores...
docker-compose up -d --build

REM 4. Esperar a que los servicios estén listos
echo.
echo [4/4] Esperando a que los servicios estén listos (30 segundos)...
timeout /t 30 /nobreak

REM 5. Verificar que las tablas se hayan creado
echo.
echo === Verificando tablas en PostgreSQL ===
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"

echo.
echo === Verificando logs del backend ===
docker logs hcen-backend --tail 50 | findstr /i "PostgreSQL H2 dialect schema table"

echo.
echo === Proceso completado ===
echo Si las tablas no aparecen, revisa los logs con: docker logs hcen-backend -f


