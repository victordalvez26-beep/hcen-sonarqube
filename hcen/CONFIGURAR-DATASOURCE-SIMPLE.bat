@echo off
REM Script simple para configurar el datasource

echo ========================================
echo Configurando DataSource en WildFly
echo ========================================
echo.

echo Verificando si el datasource existe...
docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcenDS:read-resource"

if %errorlevel% neq 0 (
    echo.
    echo DataSource no existe. Creando...
    echo.
    
    REM Crear el datasource
    docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcenDS:add(jndi-name=java:/hcen,connection-url=jdbc:postgresql://postgres:5432/hcen,driver-name=postgresql,user-name=hcen_user,password=hcen_password,enabled=true)"
    
    if %errorlevel% equ 0 (
        echo.
        echo DataSource creado exitosamente!
        echo.
        echo Probando conexion...
        docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcenDS:test-connection-in-pool"
        
        echo.
        echo Guardando configuracion...
        docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/:write-configuration"
        
        echo.
        echo Reiniciando backend...
        docker restart hcen-backend
        
        echo.
        echo Esperando 30 segundos para que el backend se inicie...
        timeout /t 30 /nobreak
        
        echo.
        echo Verificando tablas...
        docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"
    ) else (
        echo.
        echo ERROR: No se pudo crear el datasource
        echo Verifica los logs: docker logs hcen-backend
    )
) else (
    echo.
    echo DataSource ya existe. Probando conexion...
    docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcenDS:test-connection-in-pool"
    
    if %errorlevel% equ 0 (
        echo.
        echo Conexion exitosa!
        echo.
        echo Verificando tablas...
        docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"
    ) else (
        echo.
        echo ERROR: La conexion fallo
        echo Verifica que PostgreSQL este corriendo y accesible
    )
)

echo.
pause


