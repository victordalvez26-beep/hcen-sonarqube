# HCEN Frontend - Ambiente de Desarrollo

## Scripts de Desarrollo Disponibles

### 1. `npm run dev` (Recomendado)
```bash
npm run dev
```
- **Descripción**: Inicia el servidor de desarrollo con nodemon
- **Características**: 
  - Reinicio automático al cambiar archivos
  - Monitorea archivos `.js`, `.jsx`, `.css`, `.scss`, `.json`
  - Configuración optimizada en `nodemon.json`
  - No abre el navegador automáticamente

### 2. `npm run dev:clean`
```bash
npm run dev:clean
```
- **Descripción**: Inicia el servidor limpiando caché primero
- **Cuándo usar**: Cuando tengas problemas de caché o cambios no se reflejen
- **Características**:
  - Limpia el caché de node_modules
  - Inicia el servidor limpio
  - Manejo mejorado de señales

### 3. `npm run dev:simple`
```bash
npm run dev:simple
```
- **Descripción**: Versión simple de nodemon
- **Cuándo usar**: Si tienes problemas con la configuración de nodemon.json

### 4. `npm start` (Tradicional)
```bash
npm start
```
- **Descripción**: Inicia el servidor sin nodemon
- **Cuándo usar**: Para desarrollo básico sin reinicio automático

## 📁 Archivos de Configuración

### `nodemon.json`
Configuración de nodemon que define:
- **Archivos a monitorear**: Solo la carpeta `src/`
- **Extensiones**: `.js`, `.jsx`, `.css`, `.scss`, `.json`
- **Archivos a ignorar**: `node_modules/`, `build/`, archivos de test
- **Delay**: 1 segundo para evitar reinicios múltiples
- **Variables de entorno**: `BROWSER=none` para no abrir navegador

### `dev-start.js`
Script personalizado que:
- Limpia caché si es necesario
- Inicia el servidor con configuración optimizada
- Maneja señales de cierre limpiamente
- Proporciona feedback visual

## 🔧 Configuración del Ambiente

### Variables de Entorno
El ambiente de desarrollo está configurado con:
- `BROWSER=none`: No abre el navegador automáticamente
- `FAST_REFRESH=true`: Habilita React Fast Refresh
- `NODE_ENV=development`: Modo de desarrollo

### Hot Reload
- **React Fast Refresh**: Habilitado para cambios instantáneos en componentes
- **CSS Hot Reload**: Los cambios en CSS se reflejan sin recargar la página
- **JavaScript**: Los cambios en JS/JSX reinician el servidor automáticamente

## 🛠️ Solución de Problemas

### El servidor no se reinicia
```bash
# Usar versión con limpieza de caché
npm run dev:clean
```

### Cambios no se reflejan
```bash
# Limpiar caché manualmente
rm -rf node_modules/.cache
npm run dev:clean
```

### Puerto ocupado
```bash
# El servidor automáticamente encuentra un puerto libre
# Si necesitas un puerto específico, usa:
PORT=3001 npm run dev
```

### Problemas con nodemon
```bash
# Usar versión simple
npm run dev:simple
```

## Notas Importantes

1. **Primera vez**: Usa `npm run dev:clean` para asegurar un inicio limpio
2. **Desarrollo diario**: Usa `npm run dev` para máxima eficiencia
3. **Problemas de caché**: Siempre usa `npm run dev:clean`
4. **Cierre limpio**: Usa `Ctrl+C` para cerrar el servidor correctamente

## 🌐 URLs de Desarrollo

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Backend Admin**: http://localhost:9990

## Integración con Backend

El frontend está configurado para comunicarse con:
- **API Base URL**: `http://localhost:8080/api`
- **CORS**: Configurado en el backend para `http://localhost:3000`
- **Cookies**: Habilitadas para autenticación
