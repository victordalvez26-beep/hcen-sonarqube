# Script para verificar y configurar el datasource

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verificando DataSource en WildFly" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que el backend esté corriendo
$backend = docker ps --filter "name=hcen-backend" --format "{{.Names}}" 2>$null
if (-not $backend) {
    Write-Host "ERROR: El backend no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta primero: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Verificando si el datasource existe..." -ForegroundColor Yellow
$result = docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcenDS:read-resource" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ DataSource 'hcenDS' existe" -ForegroundColor Green
    Write-Host ""
    Write-Host "Probando conexion..." -ForegroundColor Yellow
    $testResult = docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcenDS:test-connection-in-pool" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Conexion exitosa" -ForegroundColor Green
    } else {
        Write-Host "✗ Error en la conexion" -ForegroundColor Red
        Write-Host $testResult -ForegroundColor Red
    }
} else {
    Write-Host "✗ DataSource 'hcenDS' NO existe" -ForegroundColor Red
    Write-Host ""
    Write-Host "Configurando datasource..." -ForegroundColor Yellow
    
    # Script para crear el datasource
    $cliScript = @"
try
    /subsystem=datasources/jdbc-driver=postgresql:read-resource
catch
    module add --name=org.postgresql --resources=/opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql-42.6.0.jar --dependencies=javax.api,javax.transaction.api
    /subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)
end-try

try
    /subsystem=datasources/data-source=hcenDS:remove()
catch
end-try

/subsystem=datasources/data-source=hcenDS:add(
    jndi-name=java:/hcen,
    connection-url=jdbc:postgresql://postgres:5432/hcen,
    driver-name=postgresql,
    user-name=hcen_user,
    password=hcen_password,
    enabled=true,
    max-pool-size=20
)

/subsystem=datasources/data-source=hcenDS:test-connection-in-pool
/:write-configuration
"@
    
    $tempFile = [System.IO.Path]::GetTempFileName() + ".cli"
    $cliScript | Out-File -FilePath $tempFile -Encoding UTF8
    
    Write-Host "Ejecutando configuracion..." -ForegroundColor Yellow
    docker exec -i hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect < $tempFile
    
    Remove-Item $tempFile -ErrorAction SilentlyContinue
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ DataSource configurado exitosamente" -ForegroundColor Green
        Write-Host ""
        Write-Host "Reiniciando backend para que Hibernate cree las tablas..." -ForegroundColor Yellow
        docker restart hcen-backend
        Write-Host "Esperando 30 segundos..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30
    } else {
        Write-Host ""
        Write-Host "✗ Error al configurar el datasource" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Verificando tablas en la base de datos..." -ForegroundColor Yellow
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"

Write-Host ""
Read-Host "Presiona Enter para salir"


