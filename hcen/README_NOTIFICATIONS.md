# Sistema de Notificaciones HCEN

Este documento describe el sistema de notificaciones push implementado para HCEN, que permite enviar notificaciones a usuarios tanto en el frontend web como en la aplicación móvil a través de Firebase Cloud Messaging (FCM).

## Componentes Creados

### 1. Modelo de Datos

#### `NotificationType.java`
Enum que define los 7 tipos de notificaciones disponibles:

- **`RESULTS`** (código: `results`) - Notificación de resultados de laboratorio o imágenes
- **`NEW_ACCESS_REQUEST`** (código: `new_access_request`) - Nuevo pedido de acceso a información médica
- **`MEDICAL_HISTORY`** (código: `medical_history`) - Actualizaciones en el historial médico
- **`NEW_ACCESS_HISTORY`** (código: `new_access_history`) - Nuevo acceso a historia clínica
- **`MAINTENANCE`** (código: `maintenance`) - Mantenimiento del sistema
- **`NEW_FEATURES`** (código: `new_features`) - Nuevas funciones o mejoras
- **`ALL_DISABLED`** (código: `all_disabled`) - Desactivar todas las notificaciones

**Ubicación:** `ejb/src/main/java/uy/edu/tse/hcen/model/NotificationType.java`

#### `UserNotificationPreferences.java`
Entidad JPA que almacena las preferencias de notificaciones y el device token de cada usuario:

- `userUid` - Identificador único del usuario (clave primaria)
- `deviceToken` - Token de Firebase para notificaciones push móviles
- `notifyResults` - Preferencia para resultados
- `notifyNewAccessRequest` - Preferencia para nuevos pedidos de acceso
- `notifyMedicalHistory` - Preferencia para actualizaciones de historial
- `notifyNewAccessHistory` - Preferencia para nuevos accesos
- `notifyMaintenance` - Preferencia para mantenimiento
- `notifyNewFeatures` - Preferencia para nuevas funciones
- `allDisabled` - Flag para desactivar todas las notificaciones
- Timestamps de creación y actualización

**Ubicación:** `ejb/src/main/java/uy/edu/tse/hcen/model/UserNotificationPreferences.java`

### 2. Repositorio

#### `UserNotificationPreferencesRepository.java`
Repositorio EJB que gestiona las operaciones de base de datos:

- `findByUserUid(String userUid)` - Busca preferencias por UID de usuario
- `saveOrUpdate(UserNotificationPreferences)` - Crea o actualiza preferencias
- `updateDeviceToken(String userUid, String deviceToken)` - Actualiza solo el device token
- `persist(UserNotificationPreferences)` - Persiste nuevas preferencias
- `merge(UserNotificationPreferences)` - Actualiza preferencias existentes

**Ubicación:** `ejb/src/main/java/uy/edu/tse/hcen/repository/UserNotificationPreferencesRepository.java`

### 3. DTOs

#### `NotificationPreferencesDTO.java`
DTO para transferir preferencias de notificaciones entre capas.

**Ubicación:** `web/src/main/java/uy/edu/tse/hcen/rest/dto/NotificationPreferencesDTO.java`

#### `DeviceTokenDTO.java`
DTO para registrar o actualizar el device token de Firebase.

**Ubicación:** `web/src/main/java/uy/edu/tse/hcen/rest/dto/DeviceTokenDTO.java`

#### `SendNotificationDTO.java`
DTO para enviar una notificación a un usuario.

**Ubicación:** `web/src/main/java/uy/edu/tse/hcen/rest/dto/SendNotificationDTO.java`

### 4. Servicio de Notificaciones

#### `NotificationService.java`
Servicio EJB que implementa la lógica de negocio y la integración con Firebase:

**Métodos principales:**
- `sendNotification(String userUid, NotificationType type, String title, String body)` - Envía notificación verificando preferencias
- `sendFirebaseNotification(String deviceToken, String title, String body)` - Envía notificación directamente a Firebase (sin verificar preferencias)

**Características:**
- Integración completa con Firebase Cloud Messaging (FCM)
- Autenticación OAuth2 con JWT firmado (RS256)
- Verificación automática de preferencias antes de enviar
- Manejo de errores y logging detallado
- Construcción manual de JSON (sin dependencias externas)

**Ubicación:** `ejb/src/main/java/uy/edu/tse/hcen/service/NotificationService.java`

### 5. API REST

#### `NotificationResource.java`
Resource REST que expone los endpoints para gestionar notificaciones:

**Endpoints:**

1. **`GET /api/notifications/preferences`**
   - Obtiene las preferencias de notificaciones del usuario autenticado
   - Requiere autenticación JWT
   - Retorna: `NotificationPreferencesDTO`

2. **`PUT /api/notifications/preferences`**
   - Actualiza las preferencias de notificaciones del usuario autenticado
   - Requiere autenticación JWT
   - Body: `NotificationPreferencesDTO`
   - Retorna: `{ "success": true, "message": "..." }`

3. **`POST /api/notifications/device-token`**
   - Registra o actualiza el device token de Firebase del usuario autenticado
   - Requiere autenticación JWT
   - Body: `DeviceTokenDTO`
   - Retorna: `{ "success": true, "message": "..." }`

4. **`DELETE /api/notifications/device-token`**
   - Elimina el device token del usuario autenticado
   - Requiere autenticación JWT
   - Retorna: `{ "success": true, "message": "..." }`

5. **`POST /api/notifications/send`**
   - Endpoint interno para enviar una notificación a un usuario
   - Body: `SendNotificationDTO`
   - Retorna: `{ "success": true, "message": "..." }` o error
   - **Nota:** En producción, este endpoint debería estar protegido o ser interno

**Ubicación:** `web/src/main/java/uy/edu/tse/hcen/rest/NotificationResource.java`

## Características Principales

### ✅ Verificación de Preferencias
Antes de enviar cualquier notificación, el sistema verifica automáticamente si el usuario tiene habilitado ese tipo de notificación. Si `allDisabled` está activo, no se envían notificaciones de ningún tipo.

### ✅ Sincronización Multiplataforma
Las preferencias se pueden actualizar tanto desde el frontend web como desde la aplicación móvil, manteniendo sincronización entre ambas plataformas.

### ✅ Gestión de Device Tokens
El sistema almacena el device token de Firebase cuando la aplicación móvil lo registra, permitiendo enviar notificaciones push a dispositivos específicos.

### ✅ Integración Firebase Completa
Implementación completa de la integración con Firebase Cloud Messaging, incluyendo:
- Autenticación OAuth2 con Service Account
- Generación de JWT firmado (RS256)
- Envío de mensajes a través de la API REST de FCM

### ✅ Seguridad
- Los endpoints de preferencias requieren autenticación JWT
- El device token solo puede ser actualizado por el usuario autenticado
- Validación de tipos de notificación antes de enviar

## Flujo de Uso

### 1. Registro de Device Token (App Móvil)

Cuando un usuario inicia sesión en la app móvil, debe registrar su device token:

```http
POST /hcen-web/api/notifications/device-token
Content-Type: application/json
Cookie: jwt_token=...

{
  "deviceToken": "e2DDD30kROu-Z40bDwcCNr:APA91bEQ-NUpPtAIieTv4U9o6VJuFelVYPA04bJNAcAa95SgEP_3B1F7Gww35AZRGOZCyZq2QqvbtE4k0uuQyPeJ2-8pwe_gdV3uPJrrwdzzQxcb8Xh3hRI"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Device token registrado correctamente"
}
```

### 2. Actualizar Preferencias (Frontend/Móvil)

El usuario puede activar/desactivar tipos de notificaciones:

```http
PUT /hcen-web/api/notifications/preferences
Content-Type: application/json
Cookie: jwt_token=...

{
  "notifyResults": true,
  "notifyNewAccessRequest": true,
  "notifyMedicalHistory": true,
  "notifyNewAccessHistory": false,
  "notifyMaintenance": true,
  "notifyNewFeatures": false,
  "allDisabled": false
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Preferencias actualizadas correctamente"
}
```

### 3. Obtener Preferencias Actuales

```http
GET /hcen-web/api/notifications/preferences
Cookie: jwt_token=...
```

**Respuesta:**
```json
{
  "notifyResults": true,
  "notifyNewAccessRequest": true,
  "notifyMedicalHistory": true,
  "notifyNewAccessHistory": false,
  "notifyMaintenance": true,
  "notifyNewFeatures": false,
  "allDisabled": false
}
```

### 4. Enviar Notificación (Desde Otro Servicio)

Para enviar una notificación desde otro servicio del backend:

```java
@Inject
private NotificationService notificationService;

// Enviar notificación
boolean sent = notificationService.sendNotification(
    "user123",                                    // userUid
    NotificationType.RESULTS,                     // Tipo de notificación
    "Nuevos resultados disponibles",              // Título
    "Sus análisis de laboratorio están listos"   // Mensaje
);
```

O usando el endpoint REST:

```http
POST /hcen-web/api/notifications/send
Content-Type: application/json

{
  "userUid": "user123",
  "notificationType": "results",
  "title": "Nuevos resultados disponibles",
  "body": "Sus análisis de laboratorio están listos"
}
```

## Base de Datos

La tabla `user_notification_preferences` se crea automáticamente gracias a la configuración de Hibernate:

```xml
<property name="hibernate.hbm2ddl.auto" value="update" />
```

**Estructura de la tabla:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `user_uid` (VARCHAR(255), UNIQUE, NOT NULL)
- `device_token` (TEXT)
- `notify_results` (BOOLEAN, default: true)
- `notify_new_access_request` (BOOLEAN, default: true)
- `notify_medical_history` (BOOLEAN, default: true)
- `notify_new_access_history` (BOOLEAN, default: true)
- `notify_maintenance` (BOOLEAN, default: true)
- `notify_new_features` (BOOLEAN, default: true)
- `all_disabled` (BOOLEAN, default: false)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `device_token_updated_at` (TIMESTAMP)

## Configuración Firebase

El servicio utiliza las siguientes credenciales de Firebase (hardcodeadas en `NotificationService.java`):

- **Project ID:** `hcen-tse`
- **Client Email:** `firebase-adminsdk-fbsvc@hcen-tse.iam.gserviceaccount.com`
- **Private Key:** (incluida en el código)

**⚠️ Nota de Seguridad:** En producción, estas credenciales deberían estar en variables de entorno o en un archivo de configuración seguro, no hardcodeadas en el código.

## Próximos Pasos

### 1. Compilar y Desplegar
```bash
mvn clean package
# Desplegar el EAR en WildFly
```

### 2. Probar Endpoints
Usar Postman, curl o cualquier cliente HTTP para probar los endpoints:

```bash
# Obtener preferencias
curl -X GET http://localhost:8080/hcen-web/api/notifications/preferences \
  -H "Cookie: jwt_token=YOUR_JWT_TOKEN"

# Registrar device token
curl -X POST http://localhost:8080/hcen-web/api/notifications/device-token \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt_token=YOUR_JWT_TOKEN" \
  -d '{"deviceToken":"YOUR_DEVICE_TOKEN"}'
```

### 3. Integrar en Servicios Existentes
Agregar llamadas a `NotificationService` en los servicios que generan eventos:

**Ejemplo en `DocumentoService`:**
```java
@Inject
private NotificationService notificationService;

public void procesarResultadoLaboratorio(String userUid, String resultado) {
    // ... lógica de procesamiento ...
    
    // Enviar notificación
    notificationService.sendNotification(
        userUid,
        NotificationType.RESULTS,
        "Nuevos resultados disponibles",
        "Sus análisis de laboratorio están listos para revisar"
    );
}
```

### 4. Frontend/Móvil
Implementar en el frontend y la app móvil:

- **Pantalla de preferencias:** Lista de switches para cada tipo de notificación
- **Registro de device token:** Enviar el token al iniciar sesión
- **Sincronización:** Actualizar preferencias cuando el usuario cambie los switches

## Ejemplos de Integración

### Ejemplo 1: Notificación cuando hay nuevos resultados

```java
@Stateless
public class ResultadoLaboratorioService {
    
    @Inject
    private NotificationService notificationService;
    
    public void notificarResultadoDisponible(String userUid, String tipoExamen) {
        notificationService.sendNotification(
            userUid,
            NotificationType.RESULTS,
            "Resultados disponibles",
            "Sus resultados de " + tipoExamen + " están listos"
        );
    }
}
```

### Ejemplo 2: Notificación de nuevo acceso

```java
@Stateless
public class AccesoService {
    
    @Inject
    private NotificationService notificationService;
    
    public void notificarNuevoAcceso(String userUid, String profesionalNombre) {
        notificationService.sendNotification(
            userUid,
            NotificationType.NEW_ACCESS_HISTORY,
            "Nuevo acceso a su historia clínica",
            profesionalNombre + " ha accedido a su historia clínica"
        );
    }
}
```

## Troubleshooting

### El device token no se registra
- Verificar que el usuario esté autenticado (JWT válido)
- Verificar que el endpoint esté correctamente mapeado
- Revisar logs de WildFly para errores

### Las notificaciones no se envían
- Verificar que el usuario tenga el device token registrado
- Verificar que el tipo de notificación esté habilitado en las preferencias
- Verificar que `allDisabled` no esté activo
- Revisar logs de Firebase y del servicio
- Verificar credenciales de Firebase

### Error de autenticación con Firebase
- Verificar que la private key esté correctamente formateada
- Verificar que el service account tenga permisos de FCM
- Revisar logs para ver el error específico de OAuth2

## Archivos Modificados/Creados

### Nuevos Archivos
- `ejb/src/main/java/uy/edu/tse/hcen/model/NotificationType.java`
- `ejb/src/main/java/uy/edu/tse/hcen/model/UserNotificationPreferences.java`
- `ejb/src/main/java/uy/edu/tse/hcen/repository/UserNotificationPreferencesRepository.java`
- `ejb/src/main/java/uy/edu/tse/hcen/service/NotificationService.java`
- `web/src/main/java/uy/edu/tse/hcen/rest/NotificationResource.java`
- `web/src/main/java/uy/edu/tse/hcen/rest/dto/NotificationPreferencesDTO.java`
- `web/src/main/java/uy/edu/tse/hcen/rest/dto/DeviceTokenDTO.java`
- `web/src/main/java/uy/edu/tse/hcen/rest/dto/SendNotificationDTO.java`

### Archivos Modificados
- `ejb/src/main/resources/META-INF/persistence.xml` - Agregada entidad `UserNotificationPreferences`

## Referencias

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [FCM HTTP v1 API](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages)
- [Google OAuth2 Service Account](https://developers.google.com/identity/protocols/oauth2/service-account)

