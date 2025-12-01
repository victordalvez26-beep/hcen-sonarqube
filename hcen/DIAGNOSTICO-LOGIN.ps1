# Script de diagnóstico para problemas de login

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnostico de Login - Sistema HCEN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar contenedores
Write-Host "[1/6] Verificando contenedores..." -ForegroundColor Yellow
$backend = docker ps --filter "name=hcen-backend" --format "{{.Names}}" 2>$null
$postgres = docker ps --filter "name=hcen-postgres" --format "{{.Names}}" 2>$null
$frontend = docker ps --filter "name=frontend" --format "{{.Names}}" 2>$null

if ($backend) {
    Write-Host "  ✓ Backend corriendo: $backend" -ForegroundColor Green
} else {
    Write-Host "  ✗ Backend NO esta corriendo" -ForegroundColor Red
}

if ($postgres) {
    Write-Host "  ✓ PostgreSQL corriendo: $postgres" -ForegroundColor Green
} else {
    Write-Host "  ✗ PostgreSQL NO esta corriendo" -ForegroundColor Red
}

if ($frontend) {
    Write-Host "  ✓ Frontend corriendo: $frontend" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Frontend puede estar corriendo con npm (no en Docker)" -ForegroundColor Yellow
}

Write-Host ""

# 2. Verificar endpoints
Write-Host "[2/6] Verificando endpoints..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/session" -Method GET -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✓ Backend responde en http://localhost:8080" -ForegroundColor Green
    Write-Host "    Respuesta: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ Backend NO responde en http://localhost:8080" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✓ Frontend responde en http://localhost:3000" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Frontend NO responde en http://localhost:3000" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 3. Verificar base de datos
Write-Host "[3/6] Verificando base de datos..." -ForegroundColor Yellow
try {
    $tables = docker exec hcen-postgres psql -U hcen_user -d hcen -t -c "\dt" 2>$null
    if ($tables -match "users") {
        Write-Host "  ✓ Tabla 'users' existe" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Tabla 'users' NO existe" -ForegroundColor Red
        Write-Host "    Las tablas se crean automaticamente cuando el backend se conecta" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ No se pudo conectar a la base de datos" -ForegroundColor Red
}

Write-Host ""

# 4. Verificar logs del backend
Write-Host "[4/6] Ultimos logs del backend (errores)..." -ForegroundColor Yellow
$logs = docker logs hcen-backend --tail 30 2>&1 | Select-String -Pattern "error|Error|ERROR|exception|Exception|EXCEPTION" -Context 1
if ($logs) {
    Write-Host "  Errores encontrados:" -ForegroundColor Red
    $logs | ForEach-Object { Write-Host "    $_" -ForegroundColor Red }
} else {
    Write-Host "  ✓ No se encontraron errores recientes" -ForegroundColor Green
}

Write-Host ""

# 5. Verificar callback servlet
Write-Host "[5/6] Verificando callback servlet..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -Method GET -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($response.Content -match "Backend HCEN API") {
        Write-Host "  ✓ Callback servlet esta funcionando" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Callback servlet responde pero con contenido inesperado" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Callback servlet NO responde" -ForegroundColor Red
}

Write-Host ""

# 6. Verificar configuración
Write-Host "[6/6] Verificando configuracion..." -ForegroundColor Yellow
Write-Host "  Client ID: 890192" -ForegroundColor Gray
Write-Host "  Redirect URI: http://localhost:8080" -ForegroundColor Gray
Write-Host "  Frontend URL: http://localhost:3000" -ForegroundColor Gray
Write-Host "  ID Uruguay Testing: https://auth-testing.iduruguay.gub.uy" -ForegroundColor Gray

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnostico completado" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pasos siguientes:" -ForegroundColor Yellow
Write-Host "  1. Si el backend no responde, verifica los logs:" -ForegroundColor White
Write-Host "     docker logs hcen-backend -f" -ForegroundColor Gray
Write-Host ""
Write-Host "  2. Si el frontend no responde, inicia el servidor:" -ForegroundColor White
Write-Host "     cd hcen-frontend" -ForegroundColor Gray
Write-Host "     npm start" -ForegroundColor Gray
Write-Host ""
Write-Host "  3. Prueba el login en modo incognito:" -ForegroundColor White
Write-Host "     Abre http://localhost:3000 en modo incognito" -ForegroundColor Gray
Write-Host "     Haz clic en 'Iniciar Sesion con gub.uy'" -ForegroundColor Gray
Write-Host ""
Write-Host "  4. Si hay errores, revisa la consola del navegador (F12)" -ForegroundColor White
Write-Host ""
Read-Host "Presiona Enter para salir"


