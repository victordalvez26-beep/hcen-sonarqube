# Script para liberar puertos 5432, 5433 o 5434

param(
    [int]$Puerto = 5433
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Liberando puerto $Puerto" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Buscar conexiones en el puerto
$connections = Get-NetTCPConnection -LocalPort $Puerto -ErrorAction SilentlyContinue

if (-not $connections) {
    Write-Host "El puerto $Puerto no esta en uso." -ForegroundColor Green
    exit 0
}

Write-Host "Procesos usando el puerto $Puerto:" -ForegroundColor Yellow
$processes = @()

foreach ($conn in $connections) {
    $process = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
    if ($process) {
        $processes += $process
        Write-Host "  - PID: $($process.Id) - $($process.ProcessName) - $($process.Path)" -ForegroundColor Yellow
    }
}

if ($processes.Count -eq 0) {
    Write-Host "No se pudieron identificar los procesos." -ForegroundColor Red
    exit 1
}

Write-Host ""
$response = Read-Host "¿Deseas detener estos procesos? (S/N)"

if ($response -eq "S" -or $response -eq "s") {
    foreach ($proc in $processes) {
        try {
            Write-Host "Deteniendo proceso $($proc.ProcessName) (PID: $($proc.Id))..." -ForegroundColor Yellow
            Stop-Process -Id $proc.Id -Force -ErrorAction Stop
            Write-Host "  ✓ Proceso detenido." -ForegroundColor Green
        } catch {
            Write-Host "  ✗ Error al detener proceso: $_" -ForegroundColor Red
        }
    }
    
    Start-Sleep -Seconds 2
    
    # Verificar que el puerto esté libre
    $stillInUse = Get-NetTCPConnection -LocalPort $Puerto -ErrorAction SilentlyContinue
    if (-not $stillInUse) {
        Write-Host ""
        Write-Host "✓ Puerto $Puerto liberado exitosamente!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "⚠ El puerto $Puerto aun esta en uso. Puede requerir permisos de administrador." -ForegroundColor Yellow
    }
} else {
    Write-Host "Operacion cancelada." -ForegroundColor Yellow
}

Write-Host ""
Read-Host "Presiona Enter para salir"


