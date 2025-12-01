#!/bin/bash
# Script para verificar metadata de documentos en la base de datos

echo "=== Verificando metadata de documentos ==="
echo ""

# Contar total de registros
echo "Total de documentos en metadata_documento:"
docker exec hcen-postgres psql -U hcen_user -d hcen -c "SELECT COUNT(*) as total FROM metadata_documento;" 2>&1 | grep -A 1 "total"

echo ""
echo "=== Últimos 5 documentos registrados ==="
docker exec hcen-postgres psql -U hcen_user -d hcen -c "
SELECT 
    id,
    cod_docum as ci_paciente,
    nombre_paciente || ' ' || COALESCE(apellido_paciente, '') as paciente,
    tipo_documento,
    formato_documento,
    fecha_creacion,
    clinica_origen,
    profesional_salud,
    uri_documento
FROM metadata_documento 
ORDER BY id DESC 
LIMIT 5;
" 2>&1 | grep -v "rows\|row\|^$" | head -10

echo ""
echo "=== Buscar por CI específico (ejemplo: 12345678) ==="
echo "Para buscar por un CI específico, ejecuta:"
echo "docker exec hcen-postgres psql -U hcen_user -d hcen -c \"SELECT * FROM metadata_documento WHERE cod_docum = 'TU_CI_AQUI' ORDER BY fecha_creacion DESC;\""

