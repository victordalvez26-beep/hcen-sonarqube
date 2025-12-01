# Script para configurar el datasource en WildFly

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Configurando DataSource en WildFly" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que el backend esté corriendo
$backend = docker ps --filter "name=hcen-backend" --format "{{.Names}}" 2>$null
if (-not $backend) {
    Write-Host "ERROR: El backend no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta primero: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Backend encontrado: $backend" -ForegroundColor Green
Write-Host ""

# Verificar que PostgreSQL esté corriendo
$postgres = docker ps --filter "name=hcen-postgres" --format "{{.Names}}" 2>$null
if (-not $postgres) {
    Write-Host "ERROR: PostgreSQL no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta primero: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "PostgreSQL encontrado: $postgres" -ForegroundColor Green
Write-Host ""

# Crear script CLI temporal
$cliScript = @"
embed-server --server-config=standalone-full.xml --std-out=echo

# Verificar si el driver ya existe
try
    /subsystem=datasources/jdbc-driver=postgresql:read-resource
catch
    # Agregar el driver PostgreSQL
    module add --name=org.postgresql --resources=/opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql-42.6.0.jar --dependencies=javax.api,javax.transaction.api
    /subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)
end-try

# Eliminar datasource si existe
try
    /subsystem=datasources/data-source=hcenDS:remove()
catch
end-try

# Crear el datasource
/subsystem=datasources/data-source=hcenDS:add(
    jndi-name=java:/hcen,
    connection-url=jdbc:postgresql://postgres:5432/hcen,
    driver-name=postgresql,
    user-name=hcen_user,
    password=hcen_password,
    enabled=true,
    max-pool-size=20,
    min-pool-size=5
)

# Verificar que se creó correctamente
/subsystem=datasources/data-source=hcenDS:read-resource

# Probar la conexión
/subsystem=datasources/data-source=hcenDS:test-connection-in-pool

/:write-configuration
stop-embedded-server
"@

$tempFile = [System.IO.Path]::GetTempFileName() + ".cli"
$cliScript | Out-File -FilePath $tempFile -Encoding UTF8

Write-Host "Ejecutando configuracion del datasource..." -ForegroundColor Yellow
Write-Host ""

# Ejecutar el script CLI
docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --file=$tempFile 2>&1

$result = $LASTEXITCODE

# Limpiar archivo temporal
Remove-Item $tempFile -ErrorAction SilentlyContinue

if ($result -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "DataSource configurado exitosamente!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Reiniciando el backend para aplicar cambios..." -ForegroundColor Yellow
    docker restart hcen-backend
    
    Write-Host ""
    Write-Host "Esperando 30 segundos para que el backend se inicie..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    Write-Host ""
    Write-Host "Verificando que las tablas se hayan creado..." -ForegroundColor Yellow
    docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Error al configurar el datasource" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifica los logs del backend:" -ForegroundColor Yellow
    Write-Host "  docker logs hcen-backend -f" -ForegroundColor Gray
}

Write-Host ""
Read-Host "Presiona Enter para salir"


