# Script PowerShell para reiniciar contenedores Docker de HCEN

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Reiniciando contenedores Docker de HCEN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Cambiar al directorio del script
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "[1/6] Verificando puerto 5432..." -ForegroundColor Yellow
$portInUse = Get-NetTCPConnection -LocalPort 5432 -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "ADVERTENCIA: El puerto 5432 esta en uso!" -ForegroundColor Red
    Write-Host "Proceso usando el puerto:" -ForegroundColor Yellow
    $portInUse | ForEach-Object {
        $process = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "  - PID: $($_.OwningProcess) - $($process.ProcessName)" -ForegroundColor Yellow
        }
    }
    Write-Host ""
    $response = Read-Host "¿Deseas continuar de todas formas? (S/N)"
    if ($response -ne "S" -and $response -ne "s") {
        Write-Host "Operacion cancelada." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "[2/6] Deteniendo y eliminando contenedores..." -ForegroundColor Yellow
docker-compose down -v
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: No se pudieron detener los contenedores" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host ""
Write-Host "[3/6] Iniciando contenedores..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: No se pudieron iniciar los contenedores" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host ""
Write-Host "[4/6] Esperando 25 segundos para que los servicios se inicialicen..." -ForegroundColor Yellow
Start-Sleep -Seconds 25

Write-Host ""
Write-Host "[5/6] Verificando logs del backend..." -ForegroundColor Yellow
docker logs hcen-backend --tail 30

Write-Host ""
Write-Host "[6/6] Verificando tablas en la base de datos..." -ForegroundColor Yellow
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Proceso completado!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Comandos utiles:" -ForegroundColor Cyan
Write-Host "  Ver logs del backend: docker logs hcen-backend -f" -ForegroundColor White
Write-Host "  Verificar usuarios: docker exec hcen-postgres psql -U hcen_user -d hcen -c `"SELECT id, uid, email, primer_nombre, primer_apellido, rol FROM users;`"" -ForegroundColor White
Write-Host ""
Read-Host "Presiona Enter para salir"


