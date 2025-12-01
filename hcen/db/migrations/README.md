# Database Migrations

Este directorio contiene scripts SQL para migraciones de la base de datos.

## Ejecutar migraciones

### Opción 1: Desde el contenedor Docker

```bash
# Conectarse al contenedor de PostgreSQL
docker exec -i hcen-postgres psql -U hcen_user -d hcen < db/migrations/001_add_tenant_id_to_metadata_documento.sql
```

### Opción 2: Desde el host (si tienes psql instalado)

```bash
psql -h localhost -U hcen_user -d hcen -f db/migrations/001_add_tenant_id_to_metadata_documento.sql
```

### Opción 3: Desde pgAdmin o cualquier cliente SQL

Abre el archivo `001_add_tenant_id_to_metadata_documento.sql` y ejecuta su contenido en la base de datos `hcen`.

## Nota

Hibernate está configurado con `hibernate.hbm2ddl.auto=update`, por lo que esta migración debería ejecutarse automáticamente cuando se despliegue la aplicación. Sin embargo, es recomendable ejecutarla manualmente para tener control sobre cuándo se aplica el cambio.

