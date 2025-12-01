# HCEN — Guía rápida y pruebas E2E

Este README explica los archivos clave en el módulo `hcen`, su utilidad, y cómo probar los casos de uso (Crear/Actualizar/Baja de clínicas) junto con la integración con el nodo periférico (mensajería RabbitMQ y callbacks HTTP).


## Archivos y componentes importantes

- `ejb/src/main/java/uy/edu/tse/hcen/messaging/NodoRegistrationConsumer.java`
  - Consumer RabbitMQ que escucha la cola `nodo_config_queue` (bindings: `alta.clinica`, `update.clinica`, `delete.clinica`).
  - Procesa mensajes y realiza llamadas HTTP al `nodoPerifericoUrlBase` del nodo para `init`, `update` o `delete`.
  - Maneja ACK/NACK, reintentos y logging en DLQ si el mensaje no puede procesarse.

- `ejb/src/main/java/uy/edu/tse/hcen/service/NodoService.java`
  - Lógica transaccional de negocio: crear, actualizar, borrar nodos.
  - Publica eventos en Rabbit (alta/update/delete) — o en la versión simplificada actual puede persistir y exponer métodos para que el caller publique.

- `ejb/src/main/java/uy/edu/tse/hcen/repository/NodoPerifericoRepository.java`
  - Acceso a datos JPA para `NodoPeriferico`. `create()` establece `fechaAlta` si no está presente.

- `web/src/main/java/uy/edu/tse/hcen/rest/NodoPerifericoResource.java`
  - Endpoints REST para CRUD de nodos:
    - `POST /hcen-web/api/nodos` — crea un nodo (valida campos obligatorios: `RUT`, `departamento`).
  - `PUT /hcen-web/api/nodos/{rut}` — actualiza un nodo (usar RUT como clave pública).
  - `GET /hcen-web/api/nodos/{rut}` — retorna el DTO del nodo.

- `rest/dto/NodoPerifericoDTO.java` — DTO con validaciones (`@NotBlank`, `@Size`) y campo `fechaAlta`.

## Flujo E2E explicado

1. Usuario crea un nodo (HCEN): `POST /hcen-web/api/nodos` con payload similar a `smoke-temp/create-nodo.json`.
   - La API valida y persiste el nodo; `fechaAlta` se setea automáticamente.
   - En entornos reales, HCEN publica el evento `alta.clinica` a RabbitMQ.

2. Consumer (`NodoRegistrationConsumer`) recibe el mensaje `alta.clinica` y hace `POST` a `nodoPerifericoUrlBase + '/config/init'` con `{"id":<id>}`.
   - Si la llamada responde 2xx, se actualiza el `estado` del nodo a `ACTIVO`.
   - Si falla, el consumer hace reintentos y eventualmente NACK/reenvío a DLQ.

3. Para update/baja, se publican eventos `update.clinica` y `delete.clinica` respectivamente; el consumer llama `/config/update` o `/config/delete`.

## Cómo probar localmente (paso a paso)

Prerequisitos:
- WildFly corriendo localmente (puerto 8080 y management 9990).
- RabbitMQ con management plugin activo en `http://localhost:15672`.

1) Construir y desplegar HCEN EAR/WAR:
```powershell
cd hcen
mvn clean package
# copiar el ear/war a WildFly standalone/deployments o usar la consola admin
```

2) Crear un nodo (ejemplo):
```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/hcen-web/api/nodos' -Method Post -ContentType 'application/json' -InFile 'smoke-temp/create-nodo.json' -UseBasicParsing
```

3) Publicar manualmente el evento `alta.clinica` (si HCEN no publica automáticamente):
- Usa la UI de RabbitMQ (Exchange `clinica_config_exchange`) -> Publish message


4) Verificar estado del nodo:
```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/hcen-web/api/nodos/{rut}' -Method Get -UseBasicParsing
```
- Debería mostrar `estado: ACTIVO` si el periphery respondió correctamente.

5) Logs y debugging
- Revisa `standalone/log/server.log` en WildFly para ver traza del consumer (`NodoRegistrationConsumer`) y errores JSON-B o problemas en la llamada HTTP.

## Scripts y utilidades en el repo
- `smoke-test.ps1` — script PowerShell para pruebas automáticas (envía init, create-nodo, etc.).
- `update-nodo-url.ps1` — helper para actualizar `nodoPerifericoUrlBase` de un nodo ya creado.
- `get-nodo-status.ps1` — consulta y muestra `GET /nodos/1`.

## Notas de configuración
- Asegúrate de que `nodoPerifericoUrlBase` apunte a la URL correcta donde el periphery está desplegado. En nuestros despliegues de ejemplo, suele ser `http://localhost:8080/nodo-periferico/api`.
- Payloads JSON deben ser enviados tal cual (usar `-InFile` o `curl --data-binary @file` para evitar problemas de quoting).

"