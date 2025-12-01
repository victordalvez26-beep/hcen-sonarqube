# Documentación PDI

Esta carpeta contiene la documentación técnica de la API del PDI (Plataforma de Interoperabilidad).

## Contenido

- **`Documentación de Endpoints REST API.md`** — Documentación detallada de todos los endpoints REST con ejemplos de uso en PowerShell.
- **`openapi-pdi.yaml`** — Especificación OpenAPI 3.0.3 de la API REST del PDI.

## Cómo usar

### Visualizar OpenAPI

Para visualizar la especificación OpenAPI:

1. **Swagger UI:**
   - Abre https://editor.swagger.io/
   - Copia el contenido de `openapi-pdi.yaml`
   - O importa el archivo directamente

2. **Redoc:**
   - Usa https://redocly.com/docs/redoc/quickstart/
   - O instala Redoc localmente: `npm install -g redoc-cli`
   - Ejecuta: `redoc-cli serve openapi-pdi.yaml`

3. **Postman:**
   - Importa el archivo `openapi-pdi.yaml` en Postman
   - Se generarán automáticamente las colecciones de endpoints

### Documentación Markdown

La documentación en Markdown (`Documentación de Endpoints REST API.md`) incluye:
- Descripción detallada de cada endpoint
- Ejemplos de request/response
- Ejemplos de uso con PowerShell
- Información sobre el servicio SOAP

## Notas

- El servicio SOAP no está incluido en OpenAPI (OpenAPI solo soporta REST)
- Para el servicio SOAP, consulta el WSDL en: `http://localhost:8083/ws/dnic.wsdl`
- Todos los endpoints REST están documentados en ambos formatos

## Referencias

- **Arquitectura de Gobierno AGESIC:** https://arquitecturadegobierno.agesic.gub.uy/docs/salud/modelos-referencia/arquitectura-negocio/servicios-hcen
- **Documentación completa del proyecto:** Ver `README.md` en la raíz del proyecto PDI

