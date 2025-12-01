# HCEN Frontend

Aplicación frontend para el sistema de Historia Clínica Electrónica Nacional.

## Tecnologías

- React 18
- React Router
- Autenticación con ID Uruguay (OpenID Connect)

## Desarrollo

### Sin Docker

```bash
npm install
npm start
```

La aplicación estará disponible en `http://localhost:3000`

### Con Docker (Desarrollo)

```bash
docker-compose up frontend-dev
```

### Con Docker (Producción)

```bash
docker-compose --profile production up frontend-prod
```

O construir manualmente:

```bash
docker build -t hcen-frontend .
docker run -p 3000:3000 hcen-frontend
```

## Build para producción

```bash
npm run build
```

Los archivos estáticos se generan en `/build`

## Autenticación

La aplicación se integra con ID Uruguay usando OpenID Connect:
- **Login**: Redirige a `auth-testing.iduruguay.gub.uy`
- **Sesión**: Cookie HTTP-Only `hcen_session` con JWT
- **Backend**: `http://localhost:8080`

## Endpoints del Backend

- `GET /api/auth/session` - Verificar sesión activa
- `GET /api/auth/logout` - Cerrar sesión
