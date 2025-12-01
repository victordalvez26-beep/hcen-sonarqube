package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.dao.UserSessionDAO;
import uy.edu.tse.hcen.model.Departamento;
import uy.edu.tse.hcen.model.Nacionalidad;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.UserSession;
import uy.edu.tse.hcen.util.JWTUtil;
import uy.edu.tse.hcen.util.CookieUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());
    
    @EJB
    private UserDAO userDAO;
    
    @EJB
    private UserSessionDAO userSessionDAO;
    
    @EJB
    private uy.edu.tse.hcen.service.InusIntegrationService inusService;

    @EJB
    private uy.edu.tse.hcen.service.NodoService nodoService;
    
    @GET
    @Path("/profile")
    public Response getProfile(@Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            
            if (jwtToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"No autenticado\"}")
                        .build();
            }
            
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Token inválido\"}")
                        .build();
            }
            
            User user = userDAO.findByUid(userUid);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Usuario no encontrado\"}")
                        .build();
            }
            
            // Verificación de consistencia con INUS (Shadow Read) y obtención de datos
            User inusUser = null;
            try {
                inusUser = inusService.obtenerUsuarioPorUid(user.getUid());
                if (inusUser != null) {
                    boolean consistente = true;
                    StringBuilder discrepancias = new StringBuilder();
                    
                    if (!equalsNullSafe(user.getEmail(), inusUser.getEmail())) {
                        consistente = false;
                        discrepancias.append("Email (Local: ").append(user.getEmail()).append(", INUS: ").append(inusUser.getEmail()).append(") ");
                    }
                    if (!equalsNullSafe(user.getPrimerNombre(), inusUser.getPrimerNombre())) {
                        consistente = false;
                        discrepancias.append("Nombre (Local: ").append(user.getPrimerNombre()).append(", INUS: ").append(inusUser.getPrimerNombre()).append(") ");
                    }
                    if (!consistente) {
                        LOGGER.warning("[CONSISTENCIA INUS] Discrepancia detectada para UID " + user.getUid() + ": " + discrepancias.toString());
                    } else {
                        LOGGER.info("[CONSISTENCIA INUS] Datos consistentes para UID " + user.getUid());
                    }
                } else {
                    LOGGER.warning("[CONSISTENCIA INUS] Usuario local " + user.getUid() + " no encontrado en INUS");
                }
            } catch (Exception ex) {
                LOGGER.severe("Error verificando consistencia con INUS: " + ex.getMessage());
            }
            
            // HOT SWAP: Usar datos de INUS como fuente de verdad si están disponibles
            User displayUser = (inusUser != null) ? inusUser : user;
            
            // Crear respuesta con información del usuario
            StringBuilder response = new StringBuilder();
            response.append("{");
            response.append("\"uid\":\"").append(user.getUid()).append("\",");
            response.append("\"email\":\"").append(displayUser.getEmail() != null ? displayUser.getEmail() : "").append("\",");
            response.append("\"primerNombre\":\"").append(displayUser.getPrimerNombre() != null ? displayUser.getPrimerNombre() : "").append("\",");
            response.append("\"segundoNombre\":\"").append(displayUser.getSegundoNombre() != null ? displayUser.getSegundoNombre() : "").append("\",");
            response.append("\"primerApellido\":\"").append(displayUser.getPrimerApellido() != null ? displayUser.getPrimerApellido() : "").append("\",");
            response.append("\"segundoApellido\":\"").append(displayUser.getSegundoApellido() != null ? displayUser.getSegundoApellido() : "").append("\",");
            response.append("\"tipDocum\":\"").append(displayUser.getTipDocum() != null ? displayUser.getTipDocum() : "").append("\",");
            response.append("\"codDocum\":\"").append(displayUser.getCodDocum() != null ? displayUser.getCodDocum() : "").append("\",");
            response.append("\"nacionalidad\":\"").append(displayUser.getNacionalidad() != null ? displayUser.getNacionalidad().getCodigo() : "").append("\",");
            response.append("\"telefono\":\"").append(displayUser.getTelefono() != null ? displayUser.getTelefono() : "").append("\",");
            response.append("\"direccion\":\"").append(escapeJson(displayUser.getDireccion())).append("\",");
            response.append("\"localidad\":\"").append(escapeJson(displayUser.getLocalidad())).append("\",");
            response.append("\"departamento\":\"").append(displayUser.getDepartamento() != null ? displayUser.getDepartamento().name() : "").append("\",");
            response.append("\"rol\":\"").append(user.getRol() != null ? user.getRol().getCodigo() : "").append("\",");
            response.append("\"rolDescripcion\":\"").append(user.getRol() != null ? user.getRol().getDescripcion() : "").append("\",");
            response.append("\"profileCompleted\":").append(user.isProfileCompleted());
            response.append("}");
            
            return Response.ok(response.toString()).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo perfil del usuario: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error interno del servidor\"}")
                    .build();
        }
    }
    
    private boolean equalsNullSafe(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    @POST
    @Path("/complete-profile")
    public Response completeProfile(@Context HttpServletRequest request, String jsonPayload) {
        return updateUserData(request, jsonToMap(jsonPayload));
    }
    
    @GET
    @Path("/profesionales")
    public Response getProfesionales(@Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"No autenticado\"}").build();
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Token inválido\"}").build();
            
            List<User> users = userDAO.findByRol("US");
            StringBuilder jsonResponse = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(user.getId()).append(",");
                jsonResponse.append("\"uid\":\"").append(escapeJson(user.getUid())).append("\",");
                jsonResponse.append("\"email\":\"").append(escapeJson(user.getEmail())).append("\",");
                jsonResponse.append("\"nombre\":\"").append(escapeJson((user.getPrimerNombre() != null ? user.getPrimerNombre() : "") + " " + (user.getSegundoNombre() != null ? user.getSegundoNombre() : "") + " " + (user.getPrimerApellido() != null ? user.getPrimerApellido() : "") + " " + (user.getSegundoApellido() != null ? user.getSegundoApellido() : "")).trim()).append("\",");
                jsonResponse.append("\"documento\":\"").append(escapeJson(user.getCodDocum())).append("\"");
                jsonResponse.append("}");
                if (i < users.size() - 1) jsonResponse.append(",");
            }
            jsonResponse.append("]");
            return Response.ok(jsonResponse.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllUsers(@Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"No autenticado\"}").build();
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Token inválido\"}").build();
            
            User currentUser = userDAO.findByUid(userUid);
            if (currentUser == null || currentUser.getRol() == null || !currentUser.getRol().getCodigo().equals("AD")) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Acceso denegado\"}").build();
            }
            
            List<User> users = userDAO.findAll();
            StringBuilder jsonResponse = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(user.getId()).append(",");
                jsonResponse.append("\"uid\":\"").append(escapeJson(user.getUid())).append("\",");
                jsonResponse.append("\"email\":\"").append(escapeJson(user.getEmail())).append("\",");
                jsonResponse.append("\"nombre\":\"").append(escapeJson((user.getPrimerNombre() != null ? user.getPrimerNombre() : "") + " " + (user.getSegundoNombre() != null ? user.getSegundoNombre() : "") + " " + (user.getPrimerApellido() != null ? user.getPrimerApellido() : "") + " " + (user.getSegundoApellido() != null ? user.getSegundoApellido() : "")).trim()).append("\",");
                jsonResponse.append("\"documento\":\"").append(escapeJson(user.getCodDocum())).append("\",");
                jsonResponse.append("\"rol\":\"").append(user.getRol() != null ? user.getRol().getCodigo() : "US").append("\",");
                jsonResponse.append("\"rolDescripcion\":\"").append(user.getRol() != null ? user.getRol().getDescripcion() : "Usuario de la Salud").append("\",");
                jsonResponse.append("\"profileCompleted\":").append(user.isProfileCompleted());
                jsonResponse.append("}");
                if (i < users.size() - 1) jsonResponse.append(",");
            }
            jsonResponse.append("]");
            return Response.ok(jsonResponse.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }

    @GET
    @Path("/{uid}")
    public Response getUserDetails(@PathParam("uid") String targetUid, @Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"No autenticado\"}").build();
            
            String currentUserUid = JWTUtil.validateJWT(jwtToken);
            if (currentUserUid == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Token inválido\"}").build();
            
            User currentUser = userDAO.findByUid(currentUserUid);
            if (currentUser == null || currentUser.getRol() == null || !currentUser.getRol().getCodigo().equals("AD")) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Acceso denegado\"}").build();
            }
            
            User targetUser = userDAO.findByUid(targetUid);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();
            }
            
            // Obtener datos de INUS
            User displayUser = targetUser;
            try {
                User inusUser = inusService.obtenerUsuarioPorUid(targetUid);
                if (inusUser != null) {
                    displayUser = inusUser;
                }
            } catch (Exception e) {
                LOGGER.warning("Error obteniendo datos de INUS para detalle de usuario: " + e.getMessage());
            }
            
            // Construir respuesta
            StringBuilder response = new StringBuilder();
            response.append("{");
            response.append("\"uid\":\"").append(targetUser.getUid()).append("\",");
            response.append("\"email\":\"").append(escapeJson(displayUser.getEmail())).append("\",");
            response.append("\"primerNombre\":\"").append(escapeJson(displayUser.getPrimerNombre())).append("\",");
            response.append("\"segundoNombre\":\"").append(escapeJson(displayUser.getSegundoNombre())).append("\",");
            response.append("\"primerApellido\":\"").append(escapeJson(displayUser.getPrimerApellido())).append("\",");
            response.append("\"segundoApellido\":\"").append(escapeJson(displayUser.getSegundoApellido())).append("\",");
            response.append("\"tipDocum\":\"").append(displayUser.getTipDocum() != null ? displayUser.getTipDocum() : "").append("\",");
            response.append("\"codDocum\":\"").append(escapeJson(displayUser.getCodDocum())).append("\",");
            response.append("\"nacionalidad\":\"").append(displayUser.getNacionalidad() != null ? displayUser.getNacionalidad().getCodigo() : "").append("\",");
            response.append("\"telefono\":\"").append(escapeJson(displayUser.getTelefono())).append("\",");
            response.append("\"direccion\":\"").append(escapeJson(displayUser.getDireccion())).append("\",");
            response.append("\"departamento\":\"").append(displayUser.getDepartamento() != null ? displayUser.getDepartamento().getNombre() : "").append("\",");
            response.append("\"localidad\":\"").append(escapeJson(displayUser.getLocalidad())).append("\",");
            
            // Datos locales
            response.append("\"rol\":\"").append(targetUser.getRol() != null ? targetUser.getRol().getCodigo() : "").append("\",");
            response.append("\"rolDescripcion\":\"").append(targetUser.getRol() != null ? targetUser.getRol().getDescripcion() : "").append("\",");
            response.append("\"profileCompleted\":").append(targetUser.isProfileCompleted());
            response.append("}");
            
            return Response.ok(response.toString()).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo detalles de usuario: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }

    @PUT
    @Path("/role")
    public Response updateUserRole(@Context HttpServletRequest request, String jsonPayload) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"No autenticado\"}").build();
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Token inválido\"}").build();
            User currentUser = userDAO.findByUid(userUid);
            if (currentUser == null || currentUser.getRol() == null || !currentUser.getRol().getCodigo().equals("AD")) return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Acceso denegado\"}").build();
            
            String targetUid = extractJsonValue(jsonPayload, "uid");
            String newRoleCode = extractJsonValue(jsonPayload, "rol");
            if (targetUid == null || newRoleCode == null) return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"UID y rol requeridos\"}").build();
            Rol newRole = Rol.fromCodigo(newRoleCode);
            if (newRole == null) return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Rol inválido\"}").build();
            
            boolean success = userDAO.updateUserRole(targetUid, newRole);
            if (success) return Response.ok("{\"success\":true}").build();
            else return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();
        } catch (Exception e) {
            LOGGER.severe("Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }

    @GET
    @Path("/{uid}/role/{rol}")
    public Response updateUserRoleSimple(@PathParam("uid") String uid, @PathParam("rol") String rolCodigo) {
        try {
            Rol nuevoRol = Rol.fromCodigo(rolCodigo);
            if (nuevoRol == null) return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Rol inválido\"}").build();
            boolean actualizado = userDAO.updateUserRole(uid, nuevoRol);
            if (actualizado) return Response.ok("{\"mensaje\":\"Rol actualizado exitosamente\"}").build();
            else return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }

    @POST
    @Path("/{uid}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserRole(@PathParam("uid") String uid, java.util.Map<String, String> body) {
        try {
            String rolCodigo = body != null ? body.get("rol") : null;
            if (rolCodigo == null) return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Rol requerido\"}").build();
            Rol nuevoRol = Rol.fromCodigo(rolCodigo);
            if (nuevoRol == null) return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Rol inválido\"}").build();
            boolean actualizado = userDAO.updateUserRole(uid, nuevoRol);
            if (actualizado) return Response.ok("{\"mensaje\":\"Rol actualizado exitosamente\"}").build();
            else return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }

    @GET
    @Path("/data")
    public Response getUserData(@Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);

            if (jwtToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"No autenticado\"}")
                        .build();
            }

            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Token invalido\"}")
                        .build();
            }

            User user = userDAO.findByUid(userUid);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Usuario no encontrado\"}")
                        .build();
            }
            
            // HOT SWAP: Usar datos de INUS como fuente de verdad
            User displayUser = user;
            try {
                User inusUser = inusService.obtenerUsuarioPorUid(user.getUid());
                if (inusUser != null) {
                    displayUser = inusUser;
                }
            } catch (Exception e) {
                LOGGER.warning("Error obteniendo datos de INUS para getUserData: " + e.getMessage());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaNacimiento = displayUser.getFechaNacimiento() != null ? sdf.format(displayUser.getFechaNacimiento()) : "";
            String nacionalidad = displayUser.getNacionalidad() != null ? displayUser.getNacionalidad().getCodigo() : "";
            String departamento = displayUser.getDepartamento() != null ? displayUser.getDepartamento().getNombre() : "";

            StringBuilder response = new StringBuilder();
            response.append("{");
            response.append("\"email\":\"").append(escapeJson(displayUser.getEmail())).append("\",");
            response.append("\"primer_nombre\":\"").append(escapeJson(displayUser.getPrimerNombre())).append("\",");
            response.append("\"segundo_nombre\":\"").append(escapeJson(displayUser.getSegundoNombre())).append("\",");
            response.append("\"primer_apellido\":\"").append(escapeJson(displayUser.getPrimerApellido())).append("\",");
            response.append("\"segundo_apellido\":\"").append(escapeJson(displayUser.getSegundoApellido())).append("\",");
            response.append("\"tipo_documento\":\"").append(escapeJson(displayUser.getTipDocum())).append("\",");
            response.append("\"codigo_documento\":\"").append(escapeJson(displayUser.getCodDocum())).append("\",");
            response.append("\"nacionalidad\":\"").append(escapeJson(nacionalidad)).append("\",");
            response.append("\"fecha_nacimiento\":\"").append(fechaNacimiento).append("\",");
            response.append("\"departamento\":\"").append(escapeJson(departamento)).append("\",");
            response.append("\"localidad\":\"").append(escapeJson(displayUser.getLocalidad())).append("\",");
            response.append("\"direccion\":\"").append(escapeJson(displayUser.getDireccion())).append("\"");
            response.append("}");

            return Response.ok(response.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo datos completos del usuario: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error interno del servidor\"}")
                    .build();
        }
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserData(@Context HttpServletRequest request, java.util.Map<String, String> body) {
        // Implementación FASE 2: LEAN WRITE (INUS como SSOT)
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"No autenticado\"}").build();
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Token inválido\"}").build();
            User localUser = userDAO.findByUid(userUid);
            if (localUser == null) return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();

            // 1. Obtener estado actual de INUS (Fuente de Verdad)
            User userToUpdate = inusService.obtenerUsuarioPorUid(userUid);
            if (userToUpdate == null) {
                LOGGER.warning("Usuario no encontrado en INUS, usando datos locales para migración: " + userUid);
                userToUpdate = localUser;
            }

            // 2. Aplicar cambios sobre el objeto (sin persistir localmente aún)
            if (body.containsKey("email")) userToUpdate.setEmail(body.get("email"));
            if (body.containsKey("primerNombre")) userToUpdate.setPrimerNombre(body.get("primerNombre"));
            if (body.containsKey("segundoNombre")) userToUpdate.setSegundoNombre(body.get("segundoNombre"));
            if (body.containsKey("primerApellido")) userToUpdate.setPrimerApellido(body.get("primerApellido"));
            if (body.containsKey("segundoApellido")) userToUpdate.setSegundoApellido(body.get("segundoApellido"));
            if (body.containsKey("telefono")) userToUpdate.setTelefono(body.get("telefono"));
            if (body.containsKey("direccion")) userToUpdate.setDireccion(body.get("direccion"));
            if (body.containsKey("localidad")) userToUpdate.setLocalidad(body.get("localidad"));
            
            if (body.containsKey("departamento")) {
                try {
                    Departamento dept = Departamento.valueOf(body.get("departamento").toUpperCase().replace(" ", "_"));
                    userToUpdate.setDepartamento(dept);
                } catch (IllegalArgumentException e) {
                    // Ignorar o manejar error
                }
            }

            // 3. Guardar en INUS (Escritura Maestra)
            try {
                LOGGER.info("Actualizando usuario en INUS (SSOT): " + userUid);
                boolean inusSuccess = inusService.actualizarUsuarioEnInus(userToUpdate);
                if (!inusSuccess) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Error al actualizar en INUS\"}").build();
                }
            } catch (Exception ex) {
                LOGGER.severe("Excepción al actualizar en INUS: " + ex.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error de comunicación con INUS\"}").build();
            }

            // 4. Persistencia Local Selectiva (Solo campos críticos para auth/lógica local)
            // En este caso, solo sincronizamos el email si cambió, ya que puede usarse para login/notificaciones.
            // Los datos demográficos NO se guardan en la tabla 'users' local.
            boolean localUpdateNeeded = false;
            
            if (body.containsKey("email") && !body.get("email").equals(localUser.getEmail())) {
                localUser.setEmail(body.get("email"));
                localUpdateNeeded = true;
            }
            
            // Si hubiera otros campos críticos locales (ej: password), se actualizarían aquí.
            
            if (localUpdateNeeded) {
                userDAO.saveOrUpdate(localUser);
                LOGGER.info("Sincronización local parcial realizada para usuario: " + userUid);
            }

            return Response.ok("{\"success\":true,\"message\":\"Datos actualizados correctamente\"}").build();
        } catch (Exception e) {
            LOGGER.severe("Error actualizando datos de usuario: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno del servidor\"}").build();
        }
    }

    @GET
    @Path("/clinics")
    public Response getUserClinics(@Context HttpServletRequest request) {
        // Implementación original
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"No autenticado\"}").build();
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Token inválido\"}").build();
            User user = userDAO.findByUid(userUid);
            if (user == null) return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();
            
            java.util.List<uy.edu.tse.hcen.model.UserClinicAssociation> associations = nodoService.findAssociationsByUser(user.getId());
            StringBuilder json = new StringBuilder("{\"clinics\":[");
            if (associations != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                for (int i = 0; i < associations.size(); i++) {
                    uy.edu.tse.hcen.model.UserClinicAssociation assoc = associations.get(i);
                    String nombreClinica = "Clínica #" + assoc.getClinicTenantId();
                    try {
                        uy.edu.tse.hcen.model.NodoPeriferico nodo = nodoService.find(assoc.getClinicTenantId());
                        if (nodo != null && nodo.getNombre() != null) nombreClinica = nodo.getNombre();
                    } catch (Exception e) {}
                    json.append("{");
                    json.append("\"id\":").append(assoc.getClinicTenantId()).append(",");
                    json.append("\"nombre\":\"").append(nombreClinica).append("\",");
                    json.append("\"fechaAlta\":\"").append(assoc.getFechaAlta() != null ? assoc.getFechaAlta().format(formatter) : "").append("\"");
                    json.append("}");
                    if (i < associations.size() - 1) json.append(",");
                }
            }
            json.append("]}");
            return Response.ok(json.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo clínicas del usuario: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno\"}").build();
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return null;
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;
            int valueStart = colonIndex + 1;
            while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t' || json.charAt(valueStart) == '\n')) valueStart++;
            if (valueStart >= json.length()) return null;
            if (json.charAt(valueStart) == '"') {
                valueStart++;
                int valueEnd = json.indexOf('"', valueStart);
                if (valueEnd == -1) return null;
                return json.substring(valueStart, valueEnd);
            } else {
                int valueEnd = valueStart;
                while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') valueEnd++;
                return json.substring(valueStart, valueEnd).trim();
            }
        } catch (Exception e) {
            LOGGER.severe("Error extrayendo valor JSON para key: " + key + " - " + e.getMessage());
            return null;
        }
    }
    
    private java.util.Map<String, String> jsonToMap(String json) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        String[] keys = {"fechaNacimiento", "departamento", "localidad", "direccion", "telefono", "codigoPostal", "nacionalidad"};
        for (String key : keys) {
            String val = extractJsonValue(json, key);
            if (val != null) map.put(key, val);
        }
        return map;
    }
}