# PDI - Plataforma de Interoperabilidad

Simulador del **Servicio Básico de Información de DNIC** (Dirección Nacional de Identificación Civil) para el proyecto TSE 2025.

---

## Descripción

Esta aplicación simula los servicios SOAP de la PDI que son consumidos por HCEN para obtener información de los usuarios de salud, específicamente para verificar mayoría de edad mediante la fecha de nacimiento.

## Tecnologías

- **Spring Boot 3.1.5**
- **Spring Web Services** (SOAP)
- **Java 17**
- **Maven**
- **Persistencia en memoria** (ArrayList en PersonaRepository)

## Levantar el Servicio

### Opción 1: Con Maven

```powershell
cd pdi
mvn clean package -DskipTests
mvn spring-boot:run
```

### Opción 2: Con Docker

```powershell
cd pdi
docker-compose up -d
```

### Opción 3: JAR compilado

```powershell
cd pdi
mvn clean package -DskipTests
java -jar target/pdi-dnic-simulator-1.0.0.jar
```

**El servicio estará disponible en:** `http://localhost:8083`

---

## Endpoints

### SOAP Web Service

| Endpoint | URL |
|----------|-----|
| **WSDL** | http://localhost:8083/ws/dnic.wsdl |
| **SOAP Service** | http://localhost:8083/ws |
| **Namespace** | http://agesic.gub.uy/pdi/services/dnic/1.0 |

### REST API - Testing (PDI)

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/test/personas` | POST | Agregar nueva persona |
| `/api/test/personas` | GET | Listar todas las personas |

### REST API - INUS (Datos Patronímicos)

#### Servicios de Consulta y Gestión de Usuarios

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/inus/usuarios/{uid}` | GET | Obtener usuario por UID |
| `/api/inus/usuarios/documento?tipDocum={tipo}&codDocum={numero}` | GET | Obtener usuario por documento |
| `/api/inus/usuarios` | POST | Crear nuevo usuario |
| `/api/inus/usuarios/{uid}` | PUT | Actualizar usuario existente |
| `/api/inus/usuarios` | GET | Listar todos los usuarios |

#### Servicios de Búsqueda por Prestador de Salud

**Basados en la arquitectura de AGESIC:** Similar a `UserClinicAssociation` en HCEN, pero para prestadores externos.

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/inus/usuarios/prestador/{prestadorId}` | GET | Obtener usuarios asociados a un prestador (por ID) |
| `/api/inus/usuarios/prestador/rut/{prestadorRut}` | GET | Obtener usuarios asociados a un prestador (por RUT) |
| `/api/inus/usuarios/{uid}/prestadores` | GET | Obtener prestadores asociados a un usuario |
| `/api/inus/usuarios/{uid}/prestadores` | POST | Asociar usuario con prestador |
| `/api/inus/usuarios/{uid}/prestadores/{prestadorId}` | DELETE | Eliminar asociación usuario-prestador |
| `/api/inus/asociaciones` | GET | Listar todas las asociaciones (administración) |

---

## Servicio SOAP: ObtPersonaPorDoc

### Descripción

Obtiene información de una persona por su tipo y número de documento.

### Request

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

### Response (Persona Encontrada)

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:ObtPersonaPorDocResponse xmlns:ns2="http://agesic.gub.uy/pdi/services/dnic/1.0">
         <ns2:persona>
            <ns2:tipoDocumento>CI</ns2:tipoDocumento>
            <ns2:numeroDocumento>50830691</ns2:numeroDocumento>
            <ns2:primerNombre>Victor</ns2:primerNombre>
            <ns2:segundoNombre>David</ns2:segundoNombre>
            <ns2:primerApellido>Alvez</ns2:primerApellido>
            <ns2:segundoApellido>González</ns2:segundoApellido>
            <ns2:fechaNacimiento>2000-12-26</ns2:fechaNacimiento>
            <ns2:sexo>M</ns2:sexo>
            <ns2:nacionalidad>UY</ns2:nacionalidad>
            <ns2:departamento>Montevideo</ns2:departamento>
            <ns2:localidad>Montevideo</ns2:localidad>
         </ns2:persona>
      </ns2:ObtPersonaPorDocResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### Response (Persona No Encontrada)

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:ObtPersonaPorDocResponse xmlns:ns2="http://agesic.gub.uy/pdi/services/dnic/1.0">
         <ns2:error>
            <ns2:codigo>PERSONA_NO_ENCONTRADA</ns2:codigo>
            <ns2:mensaje>No se encontró una persona con el documento CI 99999999</ns2:mensaje>
         </ns2:error>
      </ns2:ObtPersonaPorDocResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

## Datos de Prueba

El sistema viene pre-cargado con las siguientes personas:

### Personas Uruguayas (CI)

| Documento | Nombre | Fecha Nacimiento | Nacionalidad | Mayor Edad |
|-----------|--------|------------------|--------------|------------|
| **50830691** | Victor David Alvez González | 2000-12-26 | UY | Sí |
| **41500075** | Juan Carlos Pérez González | 1985-03-15 | UY | Sí |
| **25850303** | María Laura López García | 1990-07-22 | UY | Sí |
| **58076354** | Carlos Alberto Rodríguez Martínez | 1978-11-08 | UY | Sí |
| **38974671** | Ana Sofía Fernández Suárez | 1995-05-30 | UY | Sí |
| **39178531** | Pedro José González Díaz | 2010-09-12 | UY | No (Menor) |

### Personas Extranjeras (PASAPORTE)

| Documento | Nombre | Fecha Nacimiento | Nacionalidad | Mayor Edad |
|-----------|--------|------------------|--------------|------------|
| **26347848** | Roberto Silva Santos | 1982-04-18 | BR | Sí |
| **AB789456** | Martín Eduardo Rodríguez López | 1988-06-10 | AR | Sí |
| **CH456123** | Carla Isabel González Muñoz | 1992-11-25 | CL | Sí |

---

## Probar el Servicio

### Con cURL (PowerShell)

```powershell
# 1. Ver el WSDL
curl http://localhost:8083/ws/dnic.wsdl

# 2. Probar SOAP con archivo XML
curl -X POST http://localhost:8083/ws `
  -H "Content-Type: text/xml" `
  -d @ejemplos/request-persona-mayor.xml

# 3. Agregar una nueva persona (REST)
curl -X POST http://localhost:8083/api/test/personas `
  -H "Content-Type: application/json" `
  -d '{\"tipoDocumento\":\"CI\",\"numeroDocumento\":\"12345678\",\"primerNombre\":\"Test\",\"primerApellido\":\"Usuario\",\"fechaNacimiento\":\"1990-01-01\",\"sexo\":\"M\",\"nacionalidad\":\"UY\"}'

# 4. Listar todas las personas
curl http://localhost:8083/api/test/personas
```

### Con Script PowerShell

Usa el script de pruebas incluido:

```powershell
cd pdi
.\test-soap.ps1
```

---

## Servicios INUS - Ejemplos de Uso

### 1. Consultar Usuarios por Prestador (por ID)

```bash
# Obtener todos los usuarios asociados al prestador ID 1
curl http://localhost:8083/api/inus/usuarios/prestador/1
```

**Response:**
```json
{
  "success": true,
  "prestadorId": 1,
  "total": 1,
  "usuarios": [
    {
      "id": 1,
      "uid": "uid-victor-50830691",
      "email": "victor.alvez@example.com",
      "primerNombre": "Victor",
      ...
    }
  ]
}
```

### 2. Consultar Usuarios por Prestador (por RUT)

```bash
# Obtener todos los usuarios asociados al prestador con RUT 12345678
curl http://localhost:8083/api/inus/usuarios/prestador/rut/12345678
```

### 3. Consultar Prestadores de un Usuario

```bash
# Obtener todos los prestadores asociados a un usuario
curl http://localhost:8083/api/inus/usuarios/uid-victor-50830691/prestadores
```

**Response:**
```json
{
  "success": true,
  "usuarioUid": "uid-victor-50830691",
  "totalPrestadores": 1,
  "prestadoresIds": [1],
  "asociaciones": [
    {
      "id": 1,
      "usuarioUid": "uid-victor-50830691",
      "prestadorId": 1,
      "prestadorRut": "12345678",
      "fechaAlta": "2025-01-15T10:30:00"
    }
  ]
}
```

### 4. Asociar Usuario con Prestador

```bash
curl -X POST http://localhost:8083/api/inus/usuarios/uid-victor-50830691/prestadores \
  -H "Content-Type: application/json" \
  -d '{
    "prestadorId": 1,
    "prestadorRut": "12345678"
  }'
```

### 5. Listar Todas las Asociaciones

```bash
curl http://localhost:8083/api/inus/asociaciones
```

---

## Modelo de Asociación Usuario-Prestador

La asociación `UsuarioPrestadorAssociation` replica la funcionalidad de `UserClinicAssociation` en HCEN, pero para prestadores externos:

- **usuarioUid**: UID del usuario (formato: uy-ci-XXXXXXXX o PACIENTE_XXXXXXXX)
- **prestadorId**: ID del prestador de salud (referencia al PrestadorSalud en HCEN)
- **prestadorRut**: RUT del prestador (para facilitar búsquedas)
- **fechaAlta**: Fecha de creación de la asociación

**Referencia:** https://arquitecturadegobierno.agesic.gub.uy/docs/salud/modelos-referencia/arquitectura-negocio/servicios-hcen

### Con SoapUI

1. Crear nuevo proyecto SOAP
2. WSDL: `http://localhost:8083/ws/dnic.wsdl`
3. Operación: `ObtPersonaPorDoc`
4. Llenar request con documento de prueba
5. Enviar

### Con Postman

**Para SOAP:**
1. New Request → SOAP
2. URL: `http://localhost:8083/ws`
3. Body → raw → XML
4. Pegar el XML del request
5. Send

**Para agregar persona (REST):**
1. New Request → POST
2. URL: `http://localhost:8083/api/test/personas`
3. Body → raw → JSON
4. Ejemplo:
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
5. Send

---

## Integración con HCEN

El componente central de HCEN debe consumir este servicio para:

1. Obtener fecha de nacimiento del usuario
2. Calcular edad
3. Verificar mayoría de edad (>= 18 años)

### Ejemplo de consumo desde HCEN (Java):

```java
// Cliente SOAP en HCEN
String wsdlUrl = "http://localhost:8083/ws/dnic.wsdl";
// ... configurar cliente SOAP
// Llamar ObtPersonaPorDoc con CI del usuario
// Verificar fechaNacimiento y calcular si es mayor de edad
```

---

## Estructura del Proyecto

```
pdi/
├── src/
│   └── main/
│       ├── java/
│       │   ├── uy/gub/agesic/pdi/              # PDI - DNIC
│       │   │   ├── PdiApplication.java          # Main
│       │   │   ├── config/
│       │   │   │   └── WebServiceConfig.java    # Config SOAP
│       │   │   ├── endpoint/
│       │   │   │   └── DnicServicioBasicoEndpoint.java  # Endpoint SOAP
│       │   │   ├── controller/
│       │   │   │   └── TestController.java      # REST para testing
│       │   │   ├── model/
│       │   │   │   └── PersonaData.java         # Modelo
│       │   │   └── repository/
│       │   │       └── PersonaRepository.java   # Datos en memoria
│       │   └── uy/gub/agesic/inus/             # INUS - Datos Patronímicos
│       │       ├── controller/
│       │       │   └── InusController.java      # REST Controller
│       │       ├── model/
│       │       │   ├── InusUsuario.java         # Modelo Usuario
│       │       │   ├── Nacionalidad.java        # Enum
│       │       │   ├── Departamento.java        # Enum
│       │       │   └── Rol.java                 # Enum
│       │       └── repository/
│       │           └── InusRepository.java      # Datos en memoria
│       └── resources/
│           ├── xsd/
│           │   └── dnic-servicio-basico.xsd     # Contrato SOAP
│           ├── application.properties            # Configuración
│           └── banner.txt                        # Banner
├── ejemplos/
│   ├── request-persona-mayor.xml               # Ejemplo persona mayor
│   ├── request-persona-menor.xml               # Ejemplo persona menor
│   ├── request-persona-no-existe.xml           # Ejemplo no encontrada
│   ├── request-persona-brasilera.xml           # Ejemplo Brasil
│   ├── request-persona-argentina.xml           # Ejemplo Argentina
│   └── request-persona-chilena.xml             # Ejemplo Chile
├── pom.xml                                      # Maven
├── Dockerfile                                   # Docker image
├── docker-compose.yml                           # Docker compose
├── test-soap.ps1                                # Script de pruebas
└── README.md                                    # Este archivo
```

---

## Datos Importantes

### Códigos de Nacionalidad

El campo `nacionalidad` utiliza códigos ISO en lugar de nombres completos:

| Código | País |
|--------|------|
| **UY** | Uruguay |
| **AR** | Argentina |
| **BR** | Brasil |
| **CL** | Chile |
| **OT** | Otros |

**Ejemplo:** En lugar de devolver "Uruguaya", el servicio devuelve "UY"

Esto está alineado con el componente `SelectNacionalidad.js` del frontend:

```javascript
{ codigo: 'UY', nombre: 'Uruguay' }
{ codigo: 'AR', nombre: 'Argentina' }
{ codigo: 'BR', nombre: 'Brasil' }
{ codigo: 'CL', nombre: 'Chile' }
{ codigo: 'OT', nombre: 'Otros' }
```

### Verificación de Mayoría de Edad

El servicio calcula automáticamente:
- **Edad actual** (basada en fecha de nacimiento)
- **Es mayor de edad** (>= 18 años)

**Persona mayor:** CI 50830691 → 24 años → Puede usar HCEN
**Persona menor:** CI 39178531 → 14 años → No puede usar HCEN

---

## Desarrollo

### Hot Reload

Spring Boot DevTools está activado. Los cambios en código se recargan automáticamente.

### Agregar más personas

Edita `PersonaRepository.java` y agrega más datos en el método `init()`.

### Ver logs

```powershell
# Si corre con Maven
# Ver en la consola

# Si corre con Docker
docker logs pdi-dnic-service -f
```

---

## Puertos

| Puerto | Servicio |
|--------|----------|
| **8083** | PDI SOAP Service |

**No conflicta con:**
- 8080 - HCEN Backend
- 8081 - Periférico Backend
- 8082 - Clínica Backend
- 3000 - Frontends

---

## Verificar Funcionamiento

```powershell
# 1. Ver WSDL
curl http://localhost:8083/ws/dnic.wsdl

# 2. Probar SOAP persona uruguaya
curl -X POST http://localhost:8083/ws `
  -H "Content-Type: text/xml" `
  -d @ejemplos/request-persona-mayor.xml

# 3. Probar SOAP persona brasileña
curl -X POST http://localhost:8083/ws `
  -H "Content-Type: text/xml" `
  -d @ejemplos/request-persona-brasilera.xml

# 4. Ejecutar todos los tests automatizados
.\test-soap.ps1

# 5. Agregar una persona nueva
curl -X POST http://localhost:8083/api/test/personas `
  -H "Content-Type: application/json" `
  -d '{\"tipoDocumento\":\"CI\",\"numeroDocumento\":\"11111111\",\"primerNombre\":\"Nuevo\",\"primerApellido\":\"Usuario\",\"fechaNacimiento\":\"1995-06-15\",\"sexo\":\"F\",\"nacionalidad\":\"UY\",\"departamento\":\"Canelones\",\"localidad\":\"Ciudad de la Costa\"}'

# 6. Listar personas
curl http://localhost:8083/api/test/personas

# 7. Obtener usuario de INUS por UID
curl http://localhost:8083/api/inus/usuarios/uid-victor-50830691

# 8. Crear nuevo usuario en INUS
curl -X POST http://localhost:8083/api/inus/usuarios `
  -H "Content-Type: application/json" `
  -d '{\"uid\":\"uid-test-12345\",\"email\":\"test@example.com\",\"primerNombre\":\"Test\",\"primerApellido\":\"Usuario\",\"tipDocum\":\"CI\",\"codDocum\":\"12345678\",\"nacionalidad\":\"UY\"}'

# 9. Actualizar usuario en INUS
curl -X PUT http://localhost:8083/api/inus/usuarios/uid-test-12345 `
  -H "Content-Type: application/json" `
  -d '{\"telefono\":\"099999999\",\"direccion\":\"Nueva Dirección 123\"}'

# 10. Listar todos los usuarios de INUS
curl http://localhost:8083/api/inus/usuarios
```

---

## Próximos Pasos

1. Servicio SOAP ObtPersonaPorDoc implementado
2. Códigos de nacionalidad alineados con frontend
3. Consumir desde HCEN para verificar mayoría de edad
4. Agregar más servicios SOAP según necesidad
5. Agregar seguridad (WS-Security) si es necesario

---

**Servicio PDI listo para usar**

Ahora con códigos de nacionalidad (UY, AR, BR, CL, OT) compatibles con el frontend.
