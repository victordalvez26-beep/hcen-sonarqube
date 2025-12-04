# Componente Prestador de Salud - Mockup

Aplicación Jakarta EE que simula el software de un prestador de salud para consumir los servicios de HCEN.

## 🎯 Propósito

Este proyecto es un mockup sencillo que permite a los prestadores de salud:
- Configurar su API Key y URL de origen
- Dar de alta usuarios en el INUS
- Registrar metadatos de documentos clínicos en el RNDC
- Consultar y descargar documentos clínicos

## Características

- **Interfaz Web Simple**: HTML/CSS/JavaScript para una experiencia de usuario amigable
- **Cliente REST**: Consumo de servicios HCEN mediante HTTP
- **Autenticación**: Validación de API Key y origen de peticiones
- **Servicios Disponibles**:
  - Alta de usuarios en INUS
  - Registro de metadatos en RNDC
  - Listado de documentos por paciente
  - Consulta de documento específico
  - Descarga de documentos

## Requisitos

- Java 17+
- Maven 3.8+
- Jakarta EE 10 compatible server (WildFly, Payara, etc.)
- **O Docker y Docker Compose** (recomendado para despliegue local)

## 🐳 Despliegue con Docker Compose (Recomendado)

### Despliegue Rápido

```bash
# 1. Compilar el proyecto
mvn clean package

# 2. Construir y levantar el contenedor
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

La aplicación estará disponible en: **http://localhost:8085/prestador-salud-mockup/**

### Configuración

- **Puerto**: 8085 (aplicación) y 9995 (consola de administración)
- **URL de HCEN**: Por defecto apunta a `http://host.docker.internal:8080` (HCEN en el host)
- **Variable de entorno**: `HCEN_API_URL` puede modificarse en `docker-compose.yml`

## 🔧 Configuración Manual (Sin Docker)

### 1. Variables de Entorno

El cliente REST usa la siguiente variable de entorno (opcional):
- `HCEN_API_URL`: URL base de la API HCEN (default: `http://localhost:8080/api/prestador-salud/services`)

### 2. Compilar y Desplegar

```bash
# Compilar (genera el EAR en ear/target/)
mvn clean package

# Desplegar el EAR en tu servidor Jakarta EE
# Copiar ear/target/prestador-salud-mockup.ear a deployments/
```

### 3. Configurar API Key

1. Acceder a la aplicación:
   - Con Docker: `http://localhost:8085/prestador-salud-mockup/`
   - Sin Docker: `http://localhost:8080/prestador-salud-mockup/`
2. Ir a "Configuración"
3. Ingresar:
   - **API Key**: Obtenida al completar el registro del prestador en HCEN
   - **URL de Origen**: URL base del prestador (debe coincidir con la registrada)
     - Con Docker: `http://localhost:8085` o la URL pública del prestador
     - Sin Docker: `http://localhost:8080` o la URL pública del prestador

## 📖 Uso

### Alta de Usuario en INUS

1. Ir a "Alta de Usuario"
2. Completar los datos del paciente
3. Hacer clic en "Dar de Alta"

### Registrar Metadatos de Documento

1. Ir a "Registrar Metadatos"
2. Completar información del documento
3. Hacer clic en "Registrar Metadatos"

### Listar Documentos de Paciente

1. Ir a "Listar Documentos"
2. Ingresar CI del paciente
3. Hacer clic en "Buscar Documentos"

### Consultar/Descargar Documento

1. Ir a "Consultar Documento" o "Descargar Documento"
2. Ingresar ID del documento
3. Hacer clic en "Consultar" o "Descargar"

## 🏗️ Estructura del Proyecto

Proyecto multi-módulo Jakarta EE con estructura EAR:

```
componente-prestador-salud/
├── pom.xml                          # POM padre
├── README.md
├── Dockerfile
├── docker-compose.yml
├── ejb/                             # Módulo EJB
│   ├── pom.xml
│   └── src/main/java/
│       └── uy/edu/tse/hcen/prestador/
│           └── client/
│               └── HcenApiClient.java  # Bean CDI ApplicationScoped
├── web/                             # Módulo WAR
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── uy/edu/tse/hcen/prestador/
│       │       └── servlet/
│       │           ├── ConfigServlet.java
│       │           └── PrestadorSaludServlet.java  # Usa @Inject para HcenApiClient
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml
│           │   └── beans.xml        # Configuración CDI
│           ├── index.html
│           ├── config.html
│           ├── alta-usuario.html
│           ├── registrar-metadatos.html
│           ├── listar-documentos.html
│           ├── consultar-documento.html
│           └── descargar-documento.html
└── ear/                             # Módulo EAR
    ├── pom.xml
    └── src/main/application/
        └── META-INF/
            └── jboss-app.xml        # Configuración de despliegue
```

## 🔐 Seguridad

- La API Key se almacena en la sesión HTTP (no persistente)
- Todas las peticiones incluyen el header `X-API-Key`
- El origen de las peticiones se valida en el servidor HCEN

## 🏛️ Arquitectura Jakarta EE

Este proyecto utiliza una arquitectura completa Jakarta EE:

- **EAR (Enterprise Application Archive)**: Empaqueta todos los módulos
- **EJB Module**: Contiene beans CDI (`@ApplicationScoped`) para lógica de negocio
- **WAR Module**: Contiene servlets y páginas web que inyectan los beans EJB
- **CDI (Contexts and Dependency Injection)**: Para inyección de dependencias
- **Beans.xml**: Configuración de CDI con `bean-discovery-mode="all"`

El cliente REST (`HcenApiClient`) es un bean CDI que se inyecta en los servlets usando `@Inject`.

## 🐳 Comandos Docker Útiles

```bash
# Compilar proyecto
mvn clean package

# Construir imagen
docker-compose build

# Levantar en segundo plano
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down

# Reconstruir después de cambios
mvn clean package && docker-compose up -d --build
```

## 📝 Notas

- Este es un mockup de demostración
- En producción, se recomienda usar HTTPS
- La API Key debe mantenerse segura y no exponerse públicamente
- **Con Docker**: El contenedor usa `host.docker.internal` para acceder a HCEN en el host
- Si HCEN está en otro servidor, modificar `HCEN_API_URL` en `docker-compose.yml`
- **Importante**: Compilar el proyecto (`mvn clean package`) antes de construir la imagen Docker
