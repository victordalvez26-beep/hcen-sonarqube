# Configuración de Variables de Entorno - Gub.uy

Este documento explica cómo configurar las URLs de integración con ID Uruguay (Gub.uy) para diferentes ambientes.

## Variables de Entorno Disponibles

| Variable | Descripción | Default (Producción - Elastic Cloud) | Desarrollo Local |
|----------|-------------|--------------------------------------|------------------|
| `GUBUY_REDIRECT_URI` | URL de callback para Gub.uy | `https://env-6105410.web.elasticloud.uy/hcen/api/auth/login/callback` | `http://localhost:8080` |
| `FRONTEND_URL` | URL del frontend de HCEN | `https://env-6105410.web.elasticloud.uy` | `http://localhost:3000` |
| `POST_LOGOUT_REDIRECT_URI` | URL post-logout | `https://env-6105410.web.elasticloud.uy/logout` | `http://localhost:8080/logout` |

---

## 🖥️ Ambiente Local (Desarrollo)

**IMPORTANTE:** Por defecto, la aplicación usa Elastic Cloud. Para desarrollo local, configurar:

**Opción A: Docker Compose** (Recomendado)

Editar `docker-compose.yml`:
```yaml
services:
  hcen-backend:
    environment:
      - GUBUY_REDIRECT_URI=http://localhost:8080
      - FRONTEND_URL=http://localhost:3000
      - POST_LOGOUT_REDIRECT_URI=http://localhost:8080/logout
```

**Opción B: Variables de entorno del sistema**

```bash
export GUBUY_REDIRECT_URI=http://localhost:8080
export FRONTEND_URL=http://localhost:3000
export POST_LOGOUT_REDIRECT_URI=http://localhost:8080/logout
```

---

## 🚀 Ambiente de Producción

### Opción 1: Docker Compose (Recomendado)

Editar `docker-compose.yml`:

```yaml
services:
  hcen-backend:
    image: wildfly:latest
    environment:
      - GUBUY_REDIRECT_URI=https://servidor.com
      - FRONTEND_URL=https://servidor.com
      - POST_LOGOUT_REDIRECT_URI=https://servidor.com/logout
    ports:
      - "8080:8080"
```

### Opción 2: Variables de Entorno del Sistema

**En Linux/Mac:**
```bash
export GUBUY_REDIRECT_URI=https://servidor.com
export FRONTEND_URL=https://servidor.com
export POST_LOGOUT_REDIRECT_URI=https://servidor.com/logout

# Iniciar WildFly
./standalone.sh
```

**En Windows:**
```cmd
set GUBUY_REDIRECT_URI=https://servidor.com
set FRONTEND_URL=https://servidor.com
set POST_LOGOUT_REDIRECT_URI=https://servidor.com/logout

standalone.bat
```

### Opción 3: Configuración en `standalone.xml` de WildFly

Editar `standalone.xml` y agregar en la sección `<system-properties>`:

```xml
<system-properties>
    <property name="GUBUY_REDIRECT_URI" value="https://servidor.com"/>
    <property name="FRONTEND_URL" value="https://servidor.com"/>
    <property name="POST_LOGOUT_REDIRECT_URI" value="https://servidor.com/logout"/>
</system-properties>
```

---

## ✅ Verificación

Para verificar qué valores está usando la aplicación, agregar logs en `GubUyCallbackServlet.java`:

```java
LOGGER.info("GUBUY_REDIRECT_URI: " + GubUyConfig.REDIRECT_URI);
LOGGER.info("FRONTEND_URL: " + GubUyConfig.FRONTEND_URL);
```

---

## 📋 Checklist de Despliegue

Antes de desplegar a producción, asegúrate de:

- [ ] Configurar `GUBUY_REDIRECT_URI` con la URL habilitada en Gub.uy
- [ ] Configurar `FRONTEND_URL` con la URL pública del frontend
- [ ] Configurar `POST_LOGOUT_REDIRECT_URI` correctamente
- [ ] Verificar que Gub.uy tenga la URL de producción en su whitelist
- [ ] Probar el flujo completo de login/logout en producción

---

## 🔧 Troubleshooting

### Error: "redirect_uri_mismatch"
- **Causa**: La URL de callback no coincide con la registrada en Gub.uy
- **Solución**: Verificar que `GUBUY_REDIRECT_URI` sea exactamente la URL habilitada en Gub.uy

### Error: "CORS policy"
- **Causa**: La URL del frontend no está permitida
- **Solución**: Verificar que `FRONTEND_URL` esté correctamente configurada

### Las variables no se aplican
- **Causa**: Variables no exportadas o aplicación no reiniciada
- **Solución**: Reiniciar WildFly después de configurar las variables

