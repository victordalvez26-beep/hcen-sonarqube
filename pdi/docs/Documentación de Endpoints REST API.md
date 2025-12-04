# Documentación de Endpoints REST API - PDI

## Base URL

```
http://localhost:8083
```

**Nota**: El servicio PDI corre en el puerto 8083 por defecto.

## Endpoints Disponibles

### 1. Servicios de Prueba (Test)

#### Agregar Persona
**POST** `/api/test/personas`

Agrega una nueva persona al repositorio en memoria para pruebas.

**Request Body:**
```json
{
  "tipoDocumento": "CI",
  "numeroDocumento": "12345678",
  "primerNombre": "Test",
  "segundoNombre": "Usuario",
  "primerApellido": "Prueba",
  "segundoApellido": "Test",
  "fechaNacimiento": "1990-01-01",
  "sexo": "M",
  "nacionalidad": "UY",
  "departamento": "Montevideo",
  "localidad": "Montevideo"
}
```

**Campos requeridos:**
- `tipoDocumento` (obligatorio)
- `numeroDocumento` (obligatorio)
- `primerNombre` (obligatorio)
- `primerApellido` (obligatorio)

**Respuesta:**
- Status: `201 Created`
- Body: Objeto con `success`, `mensaje`, `persona` y `totalPersonas`

**Ejemplo:**
```powershell
$body = '{"tipoDocumento":"CI","numeroDocumento":"12345678","primerNombre":"Test","primerApellido":"Prueba","fechaNacimiento":"1990-01-01","sexo":"M","nacionalidad":"UY"}'
Invoke-RestMethod -Uri "http://localhost:8083/api/test/personas" -Method POST -Body $body -ContentType "application/json"
```

---

#### Listar Personas
**GET** `/api/test/personas`

Lista todas las personas en el repositorio en memoria.

**Respuesta:**
- Status: `200 OK`
- Body: Objeto con `total` y `personas` (array)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/test/personas" -Method GET
```

---

### 2. Servicios INUS - Gestión de Usuarios

#### Obtener Usuario por UID
**GET** `/api/inus/usuarios/{uid}`

Obtiene los datos patronímicos de un usuario por su UID.

**Parámetros:**
- `uid` (path parameter) - UID del usuario

**Respuesta:**
- Status: `200 OK` - Usuario encontrado
- Status: `404 Not Found` - Usuario no existe
- Body: Objeto con `success`, `usuario` (InusUsuario)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/uid-victor-50830691" -Method GET
```

---

#### Obtener Usuario por Documento
**GET** `/api/inus/usuarios/documento?tipDocum={tipo}&codDocum={numero}`

Obtiene los datos patronímicos de un usuario por tipo y número de documento.

**Parámetros:**
- `tipDocum` (query parameter) - Tipo de documento (CI, PASAPORTE, etc.)
- `codDocum` (query parameter) - Número de documento

**Respuesta:**
- Status: `200 OK` - Usuario encontrado
- Status: `404 Not Found` - Usuario no existe
- Body: Objeto con `success`, `usuario` (InusUsuario)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/documento?tipDocum=CI&codDocum=50830691" -Method GET
```

---

#### Crear Usuario
**POST** `/api/inus/usuarios`

Crea un nuevo usuario con datos patronímicos.

**Request Body:**
```json
{
  "uid": "uid-nuevo-usuario",
  "email": "usuario@example.com",
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "González",
  "tipDocum": "CI",
  "codDocum": "12345678",
  "nacionalidad": "UY",
  "fechaNacimiento": "1990-01-01",
  "departamento": "MONTEVIDEO",
  "localidad": "Montevideo",
  "direccion": "Av. 18 de Julio 1234",
  "telefono": "099123456",
  "codigoPostal": "11200",
  "profileCompleted": true,
  "rol": "USUARIO_SALUD"
}
```

**Campos requeridos:**
- `uid` o `tipDocum` + `codDocum` (al menos uno)

**Respuesta:**
- Status: `201 Created` - Usuario creado
- Status: `409 Conflict` - Usuario ya existe
- Body: Objeto con `success`, `mensaje`, `usuario` y `totalUsuarios`

**Ejemplo:**
```powershell
$body = '{"uid":"uid-nuevo","email":"test@example.com","primerNombre":"Test","primerApellido":"Usuario","tipDocum":"CI","codDocum":"12345678","nacionalidad":"UY","rol":"USUARIO_SALUD"}'
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios" -Method POST -Body $body -ContentType "application/json"
```

---

#### Actualizar Usuario
**PUT** `/api/inus/usuarios/{uid}`

Actualiza los datos patronímicos de un usuario existente.

**Parámetros:**
- `uid` (path parameter) - UID del usuario

**Request Body:**
```json
{
  "email": "nuevo-email@example.com",
  "telefono": "099999999",
  "direccion": "Nueva dirección 567"
}
```

**Nota:** Solo se actualizan los campos que se envían (no nulos).

**Respuesta:**
- Status: `200 OK` - Usuario actualizado
- Status: `404 Not Found` - Usuario no existe
- Body: Objeto con `success`, `mensaje` y `usuario`

**Ejemplo:**
```powershell
$body = '{"email":"nuevo@example.com","telefono":"099999999"}'
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/uid-victor-50830691" -Method PUT -Body $body -ContentType "application/json"
```

---

#### Listar Todos los Usuarios
**GET** `/api/inus/usuarios`

Lista todos los usuarios registrados (para testing/administración).

**Respuesta:**
- Status: `200 OK`
- Body: Objeto con `success`, `total` y `usuarios` (array)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios" -Method GET
```

---

### 3. Servicios INUS - Búsqueda por Prestador

#### Obtener Usuarios por Prestador (ID)
**GET** `/api/inus/usuarios/prestador/{prestadorId}`

Obtiene todos los usuarios asociados a un prestador de salud por su ID.

**Parámetros:**
- `prestadorId` (path parameter) - ID del prestador

**Respuesta:**
- Status: `200 OK`
- Body: Objeto con `success`, `prestadorId`, `total` y `usuarios` (array)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/prestador/1" -Method GET
```

---

#### Obtener Usuarios por Prestador (RUT)
**GET** `/api/inus/usuarios/prestador/rut/{prestadorRut}`

Obtiene todos los usuarios asociados a un prestador de salud por su RUT.

**Parámetros:**
- `prestadorRut` (path parameter) - RUT del prestador

**Respuesta:**
- Status: `200 OK`
- Body: Objeto con `success`, `prestadorRut`, `total` y `usuarios` (array)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/prestador/rut/12345678" -Method GET
```

---

#### Obtener Prestadores de un Usuario
**GET** `/api/inus/usuarios/{uid}/prestadores`

Obtiene todos los prestadores de salud asociados a un usuario.

**Parámetros:**
- `uid` (path parameter) - UID del usuario

**Respuesta:**
- Status: `200 OK` - Usuario encontrado
- Status: `404 Not Found` - Usuario no existe
- Body: Objeto con `success`, `usuarioUid`, `totalPrestadores`, `prestadoresIds` y `asociaciones`

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/uid-victor-50830691/prestadores" -Method GET
```

---

#### Asociar Usuario con Prestador
**POST** `/api/inus/usuarios/{uid}/prestadores`

Crea una asociación entre un usuario y un prestador de salud.

**Parámetros:**
- `uid` (path parameter) - UID del usuario

**Request Body:**
```json
{
  "prestadorId": 1,
  "prestadorRut": "12345678"
}
```

**Campos requeridos:**
- `prestadorId` o `prestadorRut` (al menos uno)

**Respuesta:**
- Status: `201 Created` - Asociación creada
- Status: `404 Not Found` - Usuario no existe
- Body: Objeto con `success`, `mensaje` y `asociacion`

**Ejemplo:**
```powershell
$body = '{"prestadorId":1,"prestadorRut":"12345678"}'
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/uid-victor-50830691/prestadores" -Method POST -Body $body -ContentType "application/json"
```

---

#### Eliminar Asociación Usuario-Prestador
**DELETE** `/api/inus/usuarios/{uid}/prestadores/{prestadorId}`

Elimina la asociación entre un usuario y un prestador de salud.

**Parámetros:**
- `uid` (path parameter) - UID del usuario
- `prestadorId` (path parameter) - ID del prestador

**Respuesta:**
- Status: `200 OK` - Asociación eliminada
- Status: `404 Not Found` - Asociación no existe
- Body: Objeto con `success` y `mensaje`

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/usuarios/uid-victor-50830691/prestadores/1" -Method DELETE
```

---

#### Listar Todas las Asociaciones
**GET** `/api/inus/asociaciones`

Lista todas las asociaciones usuario-prestador (para administración/testing).

**Respuesta:**
- Status: `200 OK`
- Body: Objeto con `success`, `total` y `asociaciones` (array)

**Ejemplo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8083/api/inus/asociaciones" -Method GET
```

---

## Servicio SOAP

El PDI también expone un servicio SOAP para consultar información de personas por documento.

### WSDL
```
http://localhost:8083/ws/dnic.wsdl
```

### Endpoint SOAP
```
http://localhost:8083/ws
```

### Operación: ObtPersonaPorDoc

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:dnic="http://agesic.gub.uy/pdi/services/dnic/1.0">
   <soapenv:Header/>
   <soapenv:Body>
      <dnic:ObtPersonaPorDocRequest>
         <dnic:tipoDocumento>CI</dnic:tipoDocumento>
         <dnic:numeroDocumento>50830691</dnic:numeroDocumento>
      </dnic:ObtPersonaPorDocRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Ejemplo con PowerShell:**
```powershell
$xml = @"
<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:dnic='http://agesic.gub.uy/pdi/services/dnic/1.0'>
   <soapenv:Header/>
   <soapenv:Body>
      <dnic:ObtPersonaPorDocRequest>
         <dnic:tipoDocumento>CI</dnic:tipoDocumento>
         <dnic:numeroDocumento>50830691</dnic:numeroDocumento>
      </dnic:ObtPersonaPorDocRequest>
   </soapenv:Body>
</soapenv:Envelope>
"@

Invoke-RestMethod -Uri "http://localhost:8083/ws" -Method POST -Body $xml -ContentType "text/xml"
```

---

## Script de Prueba

Para probar los endpoints SOAP, usa el script incluido:

```powershell
cd pdi
.\test-soap.ps1
```

---

## Referencias

- **Arquitectura de Gobierno AGESIC:** https://arquitecturadegobierno.agesic.gub.uy/docs/salud/modelos-referencia/arquitectura-negocio/servicios-hcen
- **Documentación completa:** Ver `README.md` en la raíz del proyecto PDI

