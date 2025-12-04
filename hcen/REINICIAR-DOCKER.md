# Comandos para Reiniciar los Contenedores Docker

## Importante: Esto eliminará todos los datos de la base de datos

## Paso 1: Detener y eliminar contenedores y volúmenes

```powershell
# Ir al directorio hcen
cd C:\Users\CaroH\Documents\fing\TSE\lab\hcen

# Detener y eliminar contenedores
docker-compose down -v

# Verificar que los contenedores se hayan detenido
docker ps -a | Select-String "hcen"
```

## Paso 2: Reiniciar los contenedores

```powershell
# Levantar los contenedores (esto creará la base de datos vacía)
docker-compose up -d

# Esperar a que PostgreSQL esté listo (puede tardar unos segundos)
Start-Sleep -Seconds 10

# Verificar que los contenedores estén corriendo
docker ps | Select-String "hcen"
```

## Paso 3: Verificar que el backend se haya desplegado

```powershell
# Ver logs del backend para verificar que se haya desplegado correctamente
docker logs hcen-backend --tail 50

# Si ves errores, espera un poco más y vuelve a verificar
Start-Sleep -Seconds 30
docker logs hcen-backend --tail 100
```

## Paso 4: Verificar que las tablas se hayan creado

```powershell
# Verificar que la tabla users existe
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"

# Si la tabla users no aparece, el backend aún no se ha conectado.
# Espera un poco más y vuelve a verificar.
```

## Paso 5: Si las tablas no se crean automáticamente

Si después de esperar las tablas no se crean, el backend puede no estar conectándose correctamente. Verifica:

1. **Verificar que el datasource esté configurado en WildFly:**
```powershell
docker exec hcen-backend /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=hcen:read-resource"
```

2. **Ver logs del backend para errores de conexión:**
```powershell
docker logs hcen-backend | Select-String -Pattern "error|exception|datasource" -Context 2
```

## Paso 6: Reiniciar el frontend (si es necesario)

```powershell
# Ir al directorio del frontend
cd C:\Users\CaroH\Documents\fing\TSE\lab\hcen-frontend

# Detener y reiniciar
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d

# Ver logs
docker-compose -f docker-compose.dev.yml logs -f
```

## Comandos Útiles de Diagnóstico

### Ver todos los contenedores
```powershell
docker ps -a
```

### Ver logs en tiempo real
```powershell
# Backend
docker logs hcen-backend -f

# PostgreSQL
docker logs hcen-postgres -f

# Frontend
docker logs hcen-frontend-frontend-dev-1 -f
```

### Conectarse a PostgreSQL
```powershell
docker exec -it hcen-postgres psql -U hcen_user -d hcen
```

### Ver tablas en PostgreSQL
```powershell
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"
```

### Ver usuarios
```powershell
docker exec hcen-postgres psql -U hcen_user -d hcen -c "SELECT id, uid, email, primer_nombre, primer_apellido, rol, nacionalidad, profile_completed FROM users ORDER BY id;"
```

## ⚡ Comando Rápido (Todo en uno)

```powershell
# Desde el directorio hcen
cd C:\Users\CaroH\Documents\fing\TSE\lab\hcen
docker-compose down -v
docker-compose up -d
Start-Sleep -Seconds 15
docker logs hcen-backend --tail 50
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"
```


