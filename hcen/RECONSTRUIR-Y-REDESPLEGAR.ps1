# Script para reconstruir y redesplegar la aplicación con el dialect de PostgreSQL corregido

Write-Host "=== Reconstruyendo y redesplegando HCEN ===" -ForegroundColor Cyan

# 1. Reconstruir la aplicación
Write-Host "`n[1/4] Reconstruyendo la aplicación..." -ForegroundColor Yellow
cd $PSScriptRoot
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: La compilación falló" -ForegroundColor Red
    exit 1
}

# 2. Detener y eliminar contenedores
Write-Host "`n[2/4] Deteniendo contenedores..." -ForegroundColor Yellow
docker-compose down

# 3. Reconstruir y levantar contenedores
Write-Host "`n[3/4] Reconstruyendo y levantando contenedores..." -ForegroundColor Yellow
docker-compose up -d --build

# 4. Esperar a que los servicios estén listos
Write-Host "`n[4/4] Esperando a que los servicios estén listos (30 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 5. Verificar que las tablas se hayan creado
Write-Host "`n=== Verificando tablas en PostgreSQL ===" -ForegroundColor Cyan
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"

Write-Host "`n=== Verificando logs del backend ===" -ForegroundColor Cyan
docker logs hcen-backend --tail 50 | Select-String -Pattern "PostgreSQL|H2|dialect|schema|table" -Context 2

Write-Host "`n=== Proceso completado ===" -ForegroundColor Green
Write-Host "Si las tablas no aparecen, revisa los logs con: docker logs hcen-backend -f" -ForegroundColor Yellow


