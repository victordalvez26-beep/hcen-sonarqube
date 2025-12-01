# 🔐 Guía de Login - Sistema HCEN

## 📋 Cómo Funciona el Login

El sistema usa **autenticación OAuth 2.0** con **ID Uruguay (gub.uy)**.

### Flujo de Autenticación:

1. **Usuario hace clic en "Iniciar Sesión con gub.uy"**
   - Se redirige a: `https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize`
   - Client ID: `890192`
   - Redirect URI: `http://localhost:8080`

2. **Usuario se autentica en gub.uy**
   - Ingresa sus credenciales de ID Uruguay
   - Autoriza la aplicación

3. **Callback al backend**
   - gub.uy redirige a: `http://localhost:8080?code=XXX&state=YYY`
   - El servlet `GubUyCallbackServlet` procesa el código
   - Se intercambia el código por tokens
   - Se crea una sesión y se guarda en la base de datos
   - Se establece una cookie `hcen_session` con un JWT

4. **Redirección al frontend**
   - El backend redirige a: `http://localhost:3000?login=success`
   - El frontend verifica la sesión con `/api/auth/session`
   - Si hay sesión válida, el usuario queda logueado

---

## ✅ Requisitos para que Funcione

### 1. Backend corriendo en `http://localhost:8080`
```cmd
# Verificar que el backend esté corriendo
docker ps | findstr hcen-backend

# Ver logs del backend
docker logs hcen-backend -f
```

### 2. Frontend corriendo en `http://localhost:3000`
```cmd
# Verificar que el frontend esté corriendo
docker ps | findstr frontend

# O si usas npm directamente
cd hcen-frontend
npm start
```

### 3. Base de datos inicializada
```cmd
# Verificar que las tablas existan
docker exec hcen-postgres psql -U hcen_user -d hcen -c "\dt"
```

### 4. Servlets configurados correctamente
El servlet `GubUyCallbackServlet` debe estar mapeado a `/` (raíz) para recibir el callback.

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: "No puedo hacer login" / "No pasa nada al hacer clic"

**Síntomas:**
- Haces clic en "Iniciar Sesión con gub.uy" y no pasa nada
- O te redirige pero luego no vuelve

**Soluciones:**

1. **Verificar que el backend esté corriendo:**
```cmd
curl http://localhost:8080/api/auth/session
```
Debería devolver `{"authenticated":false}`

2. **Verificar que el frontend esté corriendo:**
```cmd
curl http://localhost:3000
```
Debería devolver el HTML de la página

3. **Ver logs del backend:**
```cmd
docker logs hcen-backend -f
```
Busca errores relacionados con:
- `GubUyCallbackServlet`
- `AuthService`
- `TokenResponse`

4. **Verificar la consola del navegador:**
- Abre DevTools (F12)
- Ve a la pestaña "Console"
- Busca errores de red o JavaScript

### Problema 2: "Error en autenticación" / "missing_code"

**Síntomas:**
- Te redirige a gub.uy
- Después de autenticarte, vuelves con un error
- URL tiene `?error=missing_code` o similar

**Soluciones:**

1. **Verificar que el callback esté configurado correctamente:**
   - El redirect_uri debe ser exactamente: `http://localhost:8080`
   - No debe tener barra final: `http://localhost:8080/` ❌

2. **Verificar logs del backend:**
```cmd
docker logs hcen-backend | findstr "Callback\|error\|Error"
```

3. **Verificar que el servlet esté registrado:**
   - Busca en `web.xml` o anotaciones `@WebServlet`
   - Debe estar mapeado a `/` o `/*`

### Problema 3: "Sesión no válida" / "authenticated: false"

**Síntomas:**
- Haces login exitosamente
- Pero al recargar la página, no estás logueado

**Soluciones:**

1. **Verificar cookies:**
   - Abre DevTools (F12)
   - Ve a "Application" → "Cookies" → `http://localhost:3000`
   - Debe haber una cookie `hcen_session`

2. **Verificar que la cookie se esté enviando:**
   - En DevTools → "Network"
   - Busca la petición a `/api/auth/session`
   - Verifica que tenga la cookie en "Request Headers"

3. **Verificar CORS:**
   - El backend debe permitir cookies desde `http://localhost:3000`
   - Verifica headers `Access-Control-Allow-Credentials: true`

### Problema 4: "Error al obtener información del usuario"

**Síntomas:**
- El login funciona pero no se muestra tu información
- Error en consola sobre `getUserInfo`

**Soluciones:**

1. **Verificar que el access token sea válido:**
```cmd
docker logs hcen-backend | findstr "getUserInfo\|Access token"
```

2. **Verificar la configuración de gub.uy:**
   - Client ID: `890192`
   - Client Secret debe estar correcto en `GubUyConfig`

---

## 🔍 Comandos de Diagnóstico

### Verificar estado del sistema:
```cmd
# Backend corriendo
docker ps | findstr hcen-backend

# Frontend corriendo  
docker ps | findstr frontend

# PostgreSQL corriendo
docker ps | findstr hcen-postgres

# Ver logs del backend
docker logs hcen-backend --tail 50

# Verificar endpoint de sesión
curl http://localhost:8080/api/auth/session
```

### Probar el flujo completo:

1. **Abre el navegador en modo incógnito** (para evitar cookies viejas)
2. **Ve a:** `http://localhost:3000`
3. **Haz clic en "Iniciar Sesión con gub.uy"**
4. **Autentícate con ID Uruguay**
5. **Deberías ser redirigido de vuelta a:** `http://localhost:3000?login=success`
6. **La página debería mostrar tu información**

---

## 🧪 Modo de Prueba (Sin gub.uy)

Si necesitas probar sin autenticación real, puedes:

1. **Crear un usuario manualmente en la base de datos:**
```sql
INSERT INTO users (uid, email, primer_nombre, primer_apellido, rol, profile_completed)
VALUES ('test-user-123', 'test@example.com', 'Test', 'Usuario', 'US', true);
```

2. **Crear una sesión manual (requiere generar JWT):**
   - Esto es más complejo, mejor usar el login real

---

## 📞 URLs Importantes

- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080/api
- **Verificar sesión:** http://localhost:8080/api/auth/session
- **Logout:** http://localhost:8080/api/auth/logout
- **ID Uruguay Testing:** https://auth-testing.iduruguay.gub.uy

---

## ⚠️ Notas Importantes

1. **El redirect_uri debe coincidir exactamente** con el configurado en gub.uy
2. **Las cookies deben estar habilitadas** en tu navegador
3. **El backend debe estar accesible** desde `http://localhost:8080`
4. **El frontend debe estar accesible** desde `http://localhost:3000`
5. **Si cambias los puertos**, debes actualizar:
   - `GubUyConfig.java` en el backend
   - Las URLs en el frontend
   - La configuración en gub.uy (si tienes acceso)

---

## 🆘 Si Nada Funciona

1. **Reinicia todo:**
```cmd
cd hcen
docker-compose down -v
docker-compose up -d

cd ../hcen-frontend
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d
```

2. **Limpia cookies y caché del navegador**
3. **Prueba en modo incógnito**
4. **Verifica los logs completos:**
```cmd
docker logs hcen-backend > backend.log
docker logs hcen-frontend-frontend-dev-1 > frontend.log
```

5. **Revisa la consola del navegador** (F12) para errores JavaScript


