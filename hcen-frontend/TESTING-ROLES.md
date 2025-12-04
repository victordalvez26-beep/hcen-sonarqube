# Guía de Pruebas - Sistema de Roles

## Estado Actual de los Servicios

- **Frontend:** http://localhost:3000 (Dev Server con Hot Reload)
- **Backend:** http://localhost:8080
- **Base de datos:** PostgreSQL (vacía y lista)

---

## Roles Disponibles

### 1. **US** - Usuario de la Salud (Default)
- **Descripción:** Usuario normal del sistema
- **Permisos:**
  - Ver su propia historia clínica
  - Ver detalles de documentos
  - Descargar PDFs
  - NO puede acceder a Gestión de Clínicas

### 2. **AD** - Administrador HCEN
- **Descripción:** Administrador del sistema
- **Permisos:**
  - Ver su propia historia clínica
  - Ver detalles de documentos
  - Descargar PDFs
  - **Acceso a Gestión de Clínicas**
  - **Botón adicional en el header** (morado)

---

## Prueba 1: Usuario Normal (US)

### Paso 1: Hacer Login
1. Abre el navegador en: http://localhost:3000
2. Haz clic en "Iniciar Sesión con gub.uy"
3. Completa el login con ID Uruguay (testing)

### Paso 2: Completar Perfil
1. El sistema te redirigirá automáticamente a `/complete-profile`
2. Completa todos los campos:
   - Fecha de Nacimiento
   - Departamento
   - Localidad
   - Teléfono
   - Dirección
   - Código Postal
   - Nacionalidad
3. Haz clic en "Completar Perfil"

### Paso 3: Verificar Permisos de Usuario Normal
1. **Header:** Deberías ver solo 1 botón azul "Ver Historia Clínica"
2. **Historia Clínica:** Funciona correctamente
3. **Intenta acceder a:** http://localhost:3000/gestion-clinicas
4. **Resultado esperado:** Mensaje "Esta sección es solo para administradores"

### Paso 4: Obtener tu UID
Ejecuta este comando en la terminal:
```bash
docker exec hcen-postgres psql -U hcen_user -d hcen -c "SELECT id, uid, email, primer_nombre, primer_apellido, rol, nacionalidad, profile_completed FROM users ORDER BY id;"
```

Copia tu UID (formato: `uy-ci-XXXXXXXX`)

---

## Prueba 2: Administrador (AD)

### Paso 1: Convertir tu usuario a Administrador
Reemplaza `TU_UID` con el UID que obtuviste arriba:
```bash
docker exec hcen-postgres psql -U hcen_user -d hcen -c "UPDATE users SET rol = 'AD' WHERE uid = 'TU_UID';"
```

### Paso 2: Recargar la Página
1. Recarga http://localhost:3000 (Ctrl+R o Cmd+R)
2. El sistema volverá a verificar tu sesión

### Paso 3: Verificar Permisos de Administrador
1. **Header:** Deberías ver ahora 2 botones:
   - 🟣 **Morado:** "Gestión de Clínicas" (nuevo)
   - 🔵 **Azul:** "Ver Historia Clínica"
   - 🔴 **Rojo:** "Cerrar Sesión"

2. **Accede a Gestión de Clínicas:** http://localhost:3000/gestion-clinicas
3. **Resultado esperado:** Página de administración de clínicas funcionando

---

## 🔧 Comandos Útiles

### Ver todos los usuarios
```bash
docker exec hcen-postgres psql -U hcen_user -d hcen -c "SELECT id, uid, email, primer_nombre, primer_apellido, rol, nacionalidad, profile_completed FROM users ORDER BY id;"
```

### Cambiar usuario a Admin
```bash
docker exec hcen-postgres psql -U hcen_user -d hcen -c "UPDATE users SET rol = 'AD' WHERE uid = 'TU_UID';"
```

### Cambiar usuario a Usuario Normal
```bash
docker exec hcen-postgres psql -U hcen_user -d hcen -c "UPDATE users SET rol = 'US' WHERE uid = 'TU_UID';"
```

### Ver logs del backend
```bash
docker logs hcen-backend -f
```

### Ver logs del frontend
```bash
docker logs hcen-frontend-frontend-dev-1 -f
```

### Reiniciar todo
```bash
# Backend
cd /Users/reiki17/Desktop/TSE-proyecto-final/hcen
docker-compose down && docker-compose up -d

# Frontend
cd /Users/reiki17/Desktop/TSE-proyecto-final/hcen-frontend
docker-compose -f docker-compose.dev.yml down && docker-compose -f docker-compose.dev.yml up -d
```

---

## ✨ Diferencias Visuales por Rol

### Usuario Normal (US):
```
[Inicio] [Historia Clínica] [Acerca de] [Contacto]  [Ver Historia Clínica] [Cerrar Sesión]
                                                           🔵 Azul          🔴 Rojo
```

### Administrador (AD):
```
[Inicio] [Historia Clínica] [Acerca de] [Contacto]  [Gestión de Clínicas] [Ver Historia Clínica] [Cerrar Sesión]
                                                           🟣 Morado            🔵 Azul          🔴 Rojo
```

---

## 🎯 Comportamientos Esperados

| Acción | Usuario Normal (US) | Administrador (AD) |
|--------|---------------------|-------------------|
| Acceder a `/historia-clinica` | Permitido | Permitido |
| Acceder a `/documento/:id` | Permitido | Permitido |
| Acceder a `/gestion-clinicas` | Bloqueado | Permitido |
| Ver botón "Gestión de Clínicas" | No visible | Visible |
| Completar perfil (primer login) | Obligatorio | Obligatorio |

---

## 🐛 Troubleshooting

### El rol no se actualiza en el frontend
1. Cierra sesión
2. Vuelve a hacer login
3. El sistema debería cargar el rol actualizado

### La página de admin muestra "Acceso Restringido"
1. Verifica el rol en la base de datos
2. Asegúrate de que sea 'AD' (mayúsculas)
3. Recarga la página

### El backend no responde
```bash
docker logs hcen-backend --tail 50
```

### El frontend no carga
```bash
docker logs hcen-frontend-frontend-dev-1 --tail 50
```

---

## 🎉 ¡Listo para Probar!

1. Abre http://localhost:3000
2. Haz login con ID Uruguay
3. Completa tu perfil
4. Copia tu UID de la base de datos
5. Conviértete en admin
6. Recarga y disfruta de los permisos de administrador 🚀


