package uy.gub.agesic.inus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uy.gub.agesic.inus.model.InusUsuario;
import uy.gub.agesic.inus.model.UsuarioPrestadorAssociation;
import uy.gub.agesic.inus.service.InusService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para INUS (Institución Nacional de Urgencias de Salud)
 * Gestiona los datos patronímicos de los usuarios
 */
@RestController
@RequestMapping("/api/inus")
@CrossOrigin(origins = "*")
public class InusController {

    @Autowired
    private InusService inusService;

    private static final String SUCCESS = "success";
    private static final String MENSAJE = "mensaje";
    private static final String USUARIO = "usuario";
    private static final String USUARIO_NO_ENCONTRADO_UID = "Usuario no encontrado con UID: ";
    private static final String USUARIO_NO_ENCONTRADO_NOMBRE = "Usuario no encontrado con nombre: ";
    private static final String APELLIDO_LABEL = ", apellido: ";
    private static final String TOTAL = "total";
    private static final String USUARIOS = "usuarios";
    private static final String PRESTADOR_ID = "prestadorId";
    private static final String PRESTADOR_RUT = "prestadorRut";


    /**
     * GET /api/inus/usuarios/{uid}
     * Obtener datos patronímicos de un usuario por UID
     */
    @GetMapping("/usuarios/{uid}")
    public ResponseEntity<Map<String, Object>> obtenerUsuario(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<InusUsuario> usuario = inusService.findByUid(uid);
        
        if (usuario.isPresent()) {
            response.put(SUCCESS, true);
            response.put(USUARIO, usuario.get());
            return ResponseEntity.ok(response);
        } else {
            response.put(SUCCESS, false);
            response.put(MENSAJE, USUARIO_NO_ENCONTRADO_UID + uid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * GET /api/inus/usuarios/buscar/nombre-fecha
     * Obtener datos patronímicos de un usuario por nombre, primer apellido y fecha de nacimiento
     * Formato de fecha esperado: yyyy-MM-dd
     */
    @GetMapping("/usuarios/buscar/nombre-fecha")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioPorNombreYFechaNacimiento(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("fechaNacimiento") String fechaNacimientoStr) {

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr);
            Optional<InusUsuario> usuario = inusService.findByNombreApellidoAndFechaNacimiento(nombre, apellido, fechaNacimiento);

            if (usuario.isPresent()) {
                response.put(SUCCESS, true);
                response.put(USUARIO, usuario.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MENSAJE, USUARIO_NO_ENCONTRADO_NOMBRE + nombre +
                        APELLIDO_LABEL + apellido +
                        " y fecha de nacimiento: " + fechaNacimientoStr);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (DateTimeParseException e) {
            response.put(SUCCESS, false);
            response.put(MENSAJE, "Formato de fecha inválido. Use yyyy-MM-dd");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/inus/usuarios/buscar/nombre-telefono
     * Obtener datos patronímicos de un usuario por nombre, primer apellido y teléfono
     */
    @GetMapping("/usuarios/buscar/nombre-telefono")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioPorNombreYTelefono(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("telefono") String telefono) {

        Map<String, Object> response = new HashMap<>();

        Optional<InusUsuario> usuario = inusService.findByNombreApellidoAndTelefono(nombre, apellido, telefono);

        if (usuario.isPresent()) {
            response.put(SUCCESS, true);
            response.put(USUARIO, usuario.get());
            return ResponseEntity.ok(response);
        } else {
            response.put(SUCCESS, false);
            response.put(MENSAJE, USUARIO_NO_ENCONTRADO_NOMBRE + nombre +
                    APELLIDO_LABEL + apellido +
                    " y teléfono: " + telefono);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * GET /api/inus/usuarios/buscar/nombre-email
     * Obtener datos patronímicos de un usuario por nombre, primer apellido y email
     */
    @GetMapping("/usuarios/buscar/nombre-email")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioPorNombreYEmail(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("email") String email) {

        Map<String, Object> response = new HashMap<>();

        Optional<InusUsuario> usuario = inusService.findByNombreApellidoAndEmail(nombre, apellido, email);

        if (usuario.isPresent()) {
            response.put(SUCCESS, true);
            response.put(USUARIO, usuario.get());
            return ResponseEntity.ok(response);
        } else {
            response.put(SUCCESS, false);
            response.put(MENSAJE, USUARIO_NO_ENCONTRADO_NOMBRE + nombre +
                    APELLIDO_LABEL + apellido +
                    " y email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * POST /api/inus/usuarios
     * Crear nuevo usuario con datos patronímicos
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> crearUsuario(@RequestBody InusUsuario usuario) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que no exista ya un usuario con ese UID
            if (usuario.getUid() != null && inusService.existsByUid(usuario.getUid())) {
                response.put(SUCCESS, false);
                response.put(MENSAJE, "Ya existe un usuario con el UID: " + usuario.getUid());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Guardar nuevo usuario
            InusUsuario usuarioGuardado = inusService.save(usuario);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Usuario creado exitosamente");
            response.put(USUARIO, usuarioGuardado);
            response.put("totalUsuarios", inusService.findAll().size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MENSAJE, "Error al crear usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PUT /api/inus/usuarios/{uid}
     * Actualizar datos patronímicos de un usuario existente
     */
    @PutMapping("/usuarios/{uid}")
    public ResponseEntity<Map<String, Object>> actualizarUsuario(
            @PathVariable String uid,
            @RequestBody InusUsuario usuario) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<InusUsuario> usuarioActualizado = inusService.update(uid, usuario);
            
            if (usuarioActualizado.isPresent()) {
                response.put(SUCCESS, true);
                response.put(MENSAJE, "Usuario actualizado exitosamente");
                response.put(USUARIO, usuarioActualizado.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MENSAJE, USUARIO_NO_ENCONTRADO_UID + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MENSAJE, "Error al actualizar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * DELETE /api/inus/usuarios/{uid}
     * Eliminar usuario por UID
     */
    @DeleteMapping("/usuarios/{uid}")
    public ResponseEntity<Map<String, Object>> eliminarUsuario(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean eliminado = inusService.deleteByUid(uid);
            if (eliminado) {
                response.put(SUCCESS, true);
                response.put(MENSAJE, "Usuario eliminado exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MENSAJE, USUARIO_NO_ENCONTRADO_UID + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MENSAJE, "Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/inus/usuarios
     * Listar todos los usuarios (para testing)
     */
    @GetMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> listarUsuarios() {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(TOTAL, inusService.findAll().size());
        response.put(USUARIOS, inusService.findAll());
        return ResponseEntity.ok(response);
    }
    
    // ============================================
    // SERVICIOS DE BÚSQUEDA POR PRESTADOR
    // ============================================
    
    /**
     * GET /api/inus/usuarios/prestador/{prestadorId}
     */
    @GetMapping("/usuarios/prestador/{prestadorId}")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosPorPrestadorId(@PathVariable Long prestadorId) {
        Map<String, Object> response = new HashMap<>();
        
        List<InusUsuario> usuarios = inusService.findUsuariosCompletosByPrestadorId(prestadorId);
        
        response.put(SUCCESS, true);
        response.put(PRESTADOR_ID, prestadorId);
        response.put(TOTAL, usuarios.size());
        response.put(USUARIOS, usuarios);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/inus/usuarios/prestador/rut/{prestadorRut}
     */
    @GetMapping("/usuarios/prestador/rut/{prestadorRut}")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosPorPrestadorRut(@PathVariable String prestadorRut) {
        Map<String, Object> response = new HashMap<>();
        
        List<InusUsuario> usuarios = inusService.findUsuariosCompletosByPrestadorRut(prestadorRut);
        
        response.put(SUCCESS, true);
        response.put(PRESTADOR_RUT, prestadorRut);
        response.put(TOTAL, usuarios.size());
        response.put(USUARIOS, usuarios);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/inus/usuarios/{uid}/prestadores
     */
    @GetMapping("/usuarios/{uid}/prestadores")
    public ResponseEntity<Map<String, Object>> obtenerPrestadoresPorUsuario(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();
        
        // Verificar que el usuario existe
        Optional<InusUsuario> usuarioOpt = inusService.findByUid(uid);
        if (!usuarioOpt.isPresent()) {
            response.put(SUCCESS, false);
            response.put(MENSAJE, USUARIO_NO_ENCONTRADO_UID + uid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        List<Long> prestadoresIds = inusService.findPrestadoresByUsuario(uid);
        List<UsuarioPrestadorAssociation> asociaciones = new java.util.ArrayList<>();
        for (UsuarioPrestadorAssociation asoc : inusService.findAllAsociaciones()) {
            if (asoc.getUsuarioUid().equals(uid)) {
                asociaciones.add(asoc);
            }
        }
        
        response.put(SUCCESS, true);
        response.put("usuarioUid", uid);
        response.put("totalPrestadores", prestadoresIds.size());
        response.put("prestadoresIds", prestadoresIds);
        response.put("asociaciones", asociaciones);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/inus/usuarios/{uid}/prestadores
     */
    @PostMapping("/usuarios/{uid}/prestadores")
    public ResponseEntity<Map<String, Object>> asociarUsuarioConPrestador(
            @PathVariable String uid,
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el usuario existe
            Optional<InusUsuario> usuarioOpt = inusService.findByUid(uid);
            if (!usuarioOpt.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MENSAJE, USUARIO_NO_ENCONTRADO_UID + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Obtener datos del prestador
            Long prestadorId = null;
            String prestadorRut = null;
            
            if (request.get(PRESTADOR_ID) != null) {
                prestadorId = Long.valueOf(request.get(PRESTADOR_ID).toString());
            }
            if (request.get(PRESTADOR_RUT) != null) {
                prestadorRut = request.get(PRESTADOR_RUT).toString();
            }
            
            if (prestadorId == null && prestadorRut == null) {
                response.put(SUCCESS, false);
                response.put(MENSAJE, "Se requiere prestadorId o prestadorRut");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Si solo viene RUT, usar un ID por defecto
            if (prestadorId == null) {
                prestadorId = 999L; 
            }
            if (prestadorRut == null) {
                prestadorRut = "RUT_" + prestadorId; 
            }
            
            // Crear asociación
            UsuarioPrestadorAssociation asociacion = inusService.crearAsociacion(uid, prestadorId, prestadorRut);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Usuario asociado con prestador exitosamente");
            response.put("asociacion", asociacion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MENSAJE, "Error al asociar usuario con prestador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * DELETE /api/inus/usuarios/{uid}/prestadores/{prestadorId}
     */
    @DeleteMapping("/usuarios/{uid}/prestadores/{prestadorId}")
    public ResponseEntity<Map<String, Object>> eliminarAsociacionUsuarioPrestador(
            @PathVariable String uid,
            @PathVariable Long prestadorId) {
        
        Map<String, Object> response = new HashMap<>();
        
        boolean eliminado = inusService.eliminarAsociacion(uid, prestadorId);
        
        if (eliminado) {
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Asociación eliminada exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put(SUCCESS, false);
            response.put(MENSAJE, "Asociación no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * GET /api/inus/asociaciones
     * Listar todas las asociaciones (administración)
     */
    @GetMapping("/asociaciones")
    public ResponseEntity<Map<String, Object>> listarAsociaciones() {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        List<UsuarioPrestadorAssociation> asociaciones = inusService.findAllAsociaciones();
        response.put(TOTAL, asociaciones.size());
        response.put("asociaciones", asociaciones);
        return ResponseEntity.ok(response);
    }
}