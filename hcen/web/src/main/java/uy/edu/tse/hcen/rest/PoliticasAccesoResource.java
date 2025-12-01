package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import uy.edu.tse.hcen.utils.PoliticasServiceUrlUtil;
import uy.edu.tse.hcen.utils.UserUuidUtil;
import uy.edu.tse.hcen.util.CookieUtil;
import uy.edu.tse.hcen.util.JWTUtil;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.User;

/**
 * Recurso REST para gestión de políticas de acceso a documentos clínicos.
 * Este recurso actúa como proxy hacia el servicio de políticas externo.
 */
@Path("/documentos/politicas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PoliticasAccesoResource {

    private static final Logger LOG = Logger.getLogger(PoliticasAccesoResource.class.getName());
    
    @EJB
    private UserDAO userDAO;
    
    private Client createClientWithTimeout() {
        // Usar ClientBuilder estándar de JAX-RS
        // El timeout se manejará a través de las propiedades del sistema o configuración del servidor
        return ClientBuilder.newClient();
    }

    /**
     * POST /api/documentos/politicas
     * 
     * Crea una política de acceso para un profesional específico y un paciente.
     * Proxy hacia el servicio de políticas: POST /api/politicas
     */
    @POST
    public Response crearPolitica(Map<String, Object> body) {
        Client client = null;
        try {
            LOG.info("=== CREAR POLÍTICA - Request recibido ===");
            LOG.info("Body recibido: " + body);
            
            // El servicio de políticas espera el DTO directamente con los nombres correctos
            // No necesitamos mapear, solo pasar el body directamente
            // Pero debemos asegurarnos de que el codDocumPaciente esté presente
            Map<String, Object> politicaRequest = new HashMap<>(body);
            
            if (!politicaRequest.containsKey("codDocumPaciente") || 
                politicaRequest.get("codDocumPaciente") == null || 
                (politicaRequest.get("codDocumPaciente") instanceof String && ((String)politicaRequest.get("codDocumPaciente")).trim().isEmpty())) {
                LOG.warning("codDocumPaciente no viene en el request o está vacío");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El documento del paciente es requerido"))
                        .build();
            }
            
            LOG.info("codDocumPaciente: " + politicaRequest.get("codDocumPaciente"));
            
            // Limpiar campos vacíos que pueden causar problemas de deserialización
            if (politicaRequest.containsKey("fechaVencimiento")) {
                Object fechaVenc = politicaRequest.get("fechaVencimiento");
                if (fechaVenc == null || (fechaVenc instanceof String && ((String)fechaVenc).trim().isEmpty())) {
                    politicaRequest.remove("fechaVencimiento");
                    LOG.info("Removido fechaVencimiento vacío");
                }
            }
            if (politicaRequest.containsKey("tipoDocumento")) {
                Object tipoDoc = politicaRequest.get("tipoDocumento");
                if (tipoDoc == null || (tipoDoc instanceof String && ((String)tipoDoc).trim().isEmpty())) {
                    politicaRequest.remove("tipoDocumento");
                    LOG.info("Removido tipoDocumento vacío");
                }
            }
            if (politicaRequest.containsKey("referencia")) {
                Object ref = politicaRequest.get("referencia");
                if (ref == null || (ref instanceof String && ((String)ref).trim().isEmpty())) {
                    politicaRequest.remove("referencia");
                    LOG.info("Removido referencia vacío");
                }
            }
            
            if (!body.containsKey("clinicaAutorizada") || body.get("clinicaAutorizada") == null || 
                (body.get("clinicaAutorizada") instanceof String && ((String)body.get("clinicaAutorizada")).trim().isEmpty())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Debe seleccionar una clínica"))
                        .build();
            }
            
            politicaRequest.put("clinicaAutorizada", body.get("clinicaAutorizada"));
            politicaRequest.put("profesionalAutorizado", "*"); // Siempre "*" ya que no se usa profesional específico
            
            // Manejar especialidades: si viene especialidadesAutorizadas, usarla; si no, null (todos los profesionales)
            if (body.containsKey("especialidadesAutorizadas")) {
                Object especialidades = body.get("especialidadesAutorizadas");
                if (especialidades != null) {
                    // Si es una lista, convertir a JSON array string
                    if (especialidades instanceof List) {
                        List<?> especialidadesList = (List<?>) especialidades;
                        if (especialidadesList.isEmpty()) {
                            // Lista vacía = todos los profesionales
                            politicaRequest.put("especialidadesAutorizadas", null);
                        } else {
                            // Convertir lista a JSON array string
                            StringBuilder jsonArray = new StringBuilder("[");
                            for (int i = 0; i < especialidadesList.size(); i++) {
                                if (i > 0) jsonArray.append(",");
                                jsonArray.append("\"").append(especialidadesList.get(i)).append("\"");
                            }
                            jsonArray.append("]");
                            politicaRequest.put("especialidadesAutorizadas", jsonArray.toString());
                        }
                    } else if (especialidades instanceof String) {
                        String especialidadesStr = (String) especialidades;
                        if (especialidadesStr.trim().isEmpty()) {
                            politicaRequest.put("especialidadesAutorizadas", null);
                        } else {
                            politicaRequest.put("especialidadesAutorizadas", especialidadesStr);
                        }
                    } else {
                        politicaRequest.put("especialidadesAutorizadas", null);
                    }
                } else {
                    politicaRequest.put("especialidadesAutorizadas", null);
                }
            } else {
                // Si no viene especialidadesAutorizadas, significa todos los profesionales
                politicaRequest.put("especialidadesAutorizadas", null);
            }
            
            // Remover campos que ya no se usan
            politicaRequest.remove("tipoAutorizado");
            // NO remover profesionalAutorizado - debe mantenerse como "*" para cumplir con la restricción NOT NULL
            
            String url = PoliticasServiceUrlUtil.buildUrl("/politicas");
            LOG.info("URL del servicio de políticas: " + url);
            LOG.info("Datos a enviar al servicio: " + politicaRequest);
            
            client = createClientWithTimeout();
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(politicaRequest, MediaType.APPLICATION_JSON));
            
            LOG.info("Respuesta del servicio de políticas - Status: " + response.getStatus());
            
            int status = response.getStatus();
            if (status == Response.Status.CREATED.getStatusCode() || status == Response.Status.OK.getStatusCode()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = response.readEntity(Map.class);
                client.close();
                // Mapear respuesta al formato esperado por el frontend
                if (responseMap != null) {
                    Map<String, Object> frontendResponse = new HashMap<>();
                    frontendResponse.put("politicaId", responseMap.get("id"));
                    frontendResponse.put("mensaje", "Política creada exitosamente");
                    if (responseMap.containsKey("alcance")) {
                        frontendResponse.put("alcance", responseMap.get("alcance"));
                    }
                    if (responseMap.containsKey("duracion")) {
                        frontendResponse.put("duracion", responseMap.get("duracion"));
                    }
                    if (responseMap.containsKey("gestion")) {
                        frontendResponse.put("gestion", responseMap.get("gestion"));
                    }
                    return Response.status(Response.Status.CREATED).entity(frontendResponse).build();
                }
                return Response.status(Response.Status.CREATED).build();
            } else {
                String errorMsg = response.readEntity(String.class);
                client.close();
                LOG.warning("Error creando política: " + errorMsg);
                return Response.status(status).entity(Map.of("error", errorMsg != null ? errorMsg : "Error desconocido")).build();
            }
        } catch (Exception e) {
            LOG.severe("Error creando política: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al crear la política: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/documentos/politicas
     * 
     * Lista las políticas de acceso del usuario autenticado.
     * Extrae el CI del usuario del JWT y filtra las políticas por ese CI.
     * Llama internamente al servicio de políticas: GET /api/politicas/paciente/{ci}
     */
    @GET
    public Response listarPoliticas(@Context HttpServletRequest request) {
        Client client = null;
        try {
            LOG.info("🔵 [POLITICAS] GET /api/documentos/politicas - Iniciando listado de políticas");
            
            // Obtener JWT de la cookie
            String jwtToken = CookieUtil.resolveJwtToken(request);
            
            if (jwtToken == null) {
                LOG.warning("🔴 [POLITICAS] No se encontró JWT en la cookie");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "No autenticado"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] JWT encontrado en cookie");
            
            // Validar JWT y extraer userUid
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                LOG.warning("🔴 [POLITICAS] JWT inválido o expirado");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Token inválido o expirado"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] JWT válido - userUid: " + userUid);
            
            // Buscar el usuario por UID para obtener su CI
            User user = userDAO.findByUid(userUid);
            
            if (user == null) {
                LOG.warning("🔴 [POLITICAS] Usuario no encontrado con UID: " + userUid);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Usuario no encontrado"))
                        .build();
            }
            
            String codDocum = user.getCodDocum();
            LOG.info("🔵 [POLITICAS] Usuario encontrado - UID: " + userUid + ", codDocum inicial: " + codDocum);
            
            // Si el usuario no tiene CI pero el UID contiene el CI (formato: uy-ci-XXXXX)
            if ((codDocum == null || codDocum.trim().isEmpty()) && userUid.startsWith("uy-ci-")) {
                String[] parts = userUid.split("-");
                if (parts.length >= 3) {
                    codDocum = parts[2];
                    LOG.info("🔵 [POLITICAS] CI extraído del UID: " + codDocum);
                }
            }
            
            if (codDocum == null || codDocum.trim().isEmpty()) {
                LOG.warning("🔴 [POLITICAS] No se pudo obtener el CI del usuario. UID: " + userUid);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se pudo identificar el documento del usuario"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] Listando políticas para usuario CI: " + codDocum);
            
            // Llamar internamente al servicio de políticas para obtener solo las políticas del usuario
            String url = PoliticasServiceUrlUtil.buildUrl("/politicas/paciente/" + codDocum);
            LOG.info("🔵 [POLITICAS] URL del servicio de políticas: " + url);
            
            client = createClientWithTimeout();
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                // Verificar el content-type antes de leer
                String contentType = response.getMediaType() != null ? response.getMediaType().toString() : "";
                if (!contentType.contains(MediaType.APPLICATION_JSON)) {
                    String errorBody = response.readEntity(String.class);
                    client.close();
                    LOG.warning("El servicio de políticas devolvió un tipo de contenido inesperado: " + contentType);
                    LOG.warning("Respuesta: " + errorBody);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Map.of("error", "El servicio de políticas no está disponible o devolvió un formato inesperado"))
                            .build();
                }
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> politicas = response.readEntity(List.class);
                client.close();
                
                // El servicio de políticas devuelve DTOs con los nombres correctos
                // Solo necesitamos agregar tipoAutorizado para el frontend
                if (politicas != null) {
                    List<Map<String, Object>> mappedPoliticas = new java.util.ArrayList<>();
                    for (Map<String, Object> politica : politicas) {
                        Map<String, Object> mapped = new HashMap<>(politica);
                        // Ahora siempre es "clinica" (ya no hay profesionales específicos)
                        if (politica.containsKey("clinicaAutorizada") && 
                            politica.get("clinicaAutorizada") != null && 
                            !((String)politica.get("clinicaAutorizada")).isEmpty()) {
                            mapped.put("tipoAutorizado", "clinica");
                            // Parsear especialidades para mostrar en el frontend
                            String especialidades = (String) politica.get("especialidadesAutorizadas");
                            if (especialidades != null && !especialidades.trim().isEmpty()) {
                                mapped.put("especialidadesAutorizadas", especialidades);
                            } else {
                                mapped.put("especialidadesAutorizadas", null); // null = todos los profesionales
                            }
                        } else {
                            mapped.put("tipoAutorizado", "clinica"); // Por defecto
                        }
                        mappedPoliticas.add(mapped);
                    }
                    return Response.ok(mappedPoliticas).build();
                }
                return Response.ok().build();
            } else {
                String errorMsg = "";
                try {
                    errorMsg = response.readEntity(String.class);
                } catch (Exception e) {
                    errorMsg = "Error HTTP " + status;
                }
                if (client != null) {
                    client.close();
                }
                LOG.warning("Error listando políticas: " + errorMsg);
                return Response.status(status).entity(Map.of("error", errorMsg != null && !errorMsg.isEmpty() ? errorMsg : "Error desconocido")).build();
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            if (client != null) {
                client.close();
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("failed to respond") || 
                                         errorMessage.contains("Connection refused") ||
                                         errorMessage.contains("NoHttpResponseException"))) {
                String politicasUrl = PoliticasServiceUrlUtil.getBaseUrl();
                LOG.severe("El servicio de políticas no está disponible en " + politicasUrl + ". Verifique que el servicio esté corriendo.");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(Map.of("error", "El servicio de políticas no está disponible. Por favor, verifique que el servicio esté corriendo en " + politicasUrl))
                        .build();
            }
            LOG.severe("Error listando políticas: " + errorMessage);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al listar políticas: " + errorMessage))
                    .build();
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            LOG.severe("Error listando políticas: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al listar políticas: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/documentos/politicas/paciente/{ci}
     * 
     * Lista las políticas de acceso del usuario autenticado.
     * Extrae el CI del usuario del JWT y filtra las políticas por ese CI.
     * El parámetro {ci} en el path se mantiene por compatibilidad pero se ignora.
     * Proxy hacia el servicio de políticas: GET /api/politicas/paciente/{ci}
     */
    @GET
    @Path("/paciente/{ci}")
    public Response listarPoliticasPorPaciente(@PathParam("ci") String ci, @QueryParam("tenantId") String tenantId, @Context HttpServletRequest request) {
        Client client = null;
        try {
            LOG.info("🔵 [POLITICAS] GET /api/documentos/politicas/paciente/{ci} - Iniciando listado de políticas");
            
            // Obtener JWT de la cookie
            String jwtToken = CookieUtil.resolveJwtToken(request);
            
            if (jwtToken == null) {
                LOG.warning("🔴 [POLITICAS] No se encontró JWT en la cookie");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "No autenticado"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] JWT encontrado en cookie");
            
            // Validar JWT y extraer userUid
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                LOG.warning("🔴 [POLITICAS] JWT inválido o expirado");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Token inválido o expirado"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] JWT válido - userUid: " + userUid);
            
            // Extraer el documento del UID (formato: pais-tipodocumento-numero)
            String codDocum = UserUuidUtil.extractDocumentoFromUid(userUid);
            
            if (codDocum == null || codDocum.trim().isEmpty()) {
                LOG.warning("🔴 [POLITICAS] No se pudo extraer el documento del UID. UID: " + userUid);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se pudo identificar el documento del usuario"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] Documento extraído del UID: " + codDocum + " (UID: " + userUid + ")");
            
            // Validar que el CI del path (si se proporciona) coincida con el CI del usuario autenticado
            // Esto previene que un usuario intente ver políticas de otro paciente
            if (ci != null && !ci.trim().isEmpty() && !ci.equals(codDocum)) {
                LOG.warning("🔴 [POLITICAS] Intento de acceso no autorizado - CI del path: " + ci + ", CI del usuario: " + codDocum);
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No autorizado para ver políticas de otro paciente"))
                        .build();
            }
            
            LOG.info("🔵 [POLITICAS] Listando políticas para usuario CI: " + codDocum);
            
            // Usar el CI del usuario autenticado (ignorar el del path por seguridad)
            String url = PoliticasServiceUrlUtil.buildUrl("/politicas/paciente/" + codDocum);
            if (tenantId != null && !tenantId.isEmpty()) {
                url += "?tenantId=" + tenantId;
            }
            LOG.info("🔵 [POLITICAS] URL del servicio de políticas: " + url);
            
            client = createClientWithTimeout();
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> politicas = response.readEntity(List.class);
                client.close();

                // El servicio de políticas devuelve DTOs con los nombres correctos
                // Solo necesitamos agregar tipoAutorizado para el frontend
                if (politicas != null) {
                    List<Map<String, Object>> mappedPoliticas = new java.util.ArrayList<>();
                    for (Map<String, Object> politica : politicas) {
                        Map<String, Object> mapped = new HashMap<>(politica);
                        // Ahora siempre es "clinica" (ya no hay profesionales específicos)
                        if (politica.containsKey("clinicaAutorizada") && 
                            politica.get("clinicaAutorizada") != null && 
                            !((String)politica.get("clinicaAutorizada")).isEmpty()) {
                            mapped.put("tipoAutorizado", "clinica");
                            // Parsear especialidades para mostrar en el frontend
                            String especialidades = (String) politica.get("especialidadesAutorizadas");
                            if (especialidades != null && !especialidades.trim().isEmpty()) {
                                mapped.put("especialidadesAutorizadas", especialidades);
                            } else {
                                mapped.put("especialidadesAutorizadas", null); // null = todos los profesionales
                            }
                        } else {
                            mapped.put("tipoAutorizado", "clinica"); // Por defecto
                        }
                        mappedPoliticas.add(mapped);
                    }
                    return Response.ok(mappedPoliticas).build();
                }
                return Response.ok().build();
            } else {
                String errorMsg = "";
                try {
                    errorMsg = response.readEntity(String.class);
                } catch (Exception e) {
                    errorMsg = "Error HTTP " + status;
                }
                if (client != null) {
                    client.close();
                }
                LOG.warning("Error listando políticas por paciente: " + errorMsg);
                return Response.status(status).entity(Map.of("error", errorMsg != null && !errorMsg.isEmpty() ? errorMsg : "Error desconocido")).build();
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            if (client != null) {
                client.close();
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("failed to respond") || 
                                         errorMessage.contains("Connection refused") ||
                                         errorMessage.contains("NoHttpResponseException"))) {
                String politicasUrl = PoliticasServiceUrlUtil.getBaseUrl();
                LOG.severe("El servicio de políticas no está disponible en " + politicasUrl + ". Verifique que el servicio esté corriendo.");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(Map.of("error", "El servicio de políticas no está disponible. Por favor, verifique que el servicio esté corriendo en " + politicasUrl))
                        .build();
            }
            LOG.severe("Error listando políticas por paciente: " + errorMessage);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al listar políticas del paciente: " + errorMessage))
                    .build();
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            LOG.severe("Error listando políticas por paciente: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al listar políticas del paciente: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/documentos/politicas/profesional/{id}
     * 
     * Lista las políticas de acceso de un profesional específico.
     * Proxy hacia el servicio de políticas: GET /api/politicas/profesional/{id}
     */
    @GET
    @Path("/profesional/{id}")
    public Response listarPoliticasPorProfesional(@PathParam("id") String id, @Context HttpServletRequest request) {
        LOG.info("🔵 [POLITICAS-ACCESO-RESOURCE] ========== Endpoint /documentos/politicas/profesional/" + id + " llamado ==========");
        LOG.info("🔵 [POLITICAS-ACCESO-RESOURCE] Request URI: " + (request != null ? request.getRequestURI() : "null"));
        LOG.info("🔵 [POLITICAS-ACCESO-RESOURCE] Request URL: " + (request != null ? request.getRequestURL() : "null"));
        try {
            String url = PoliticasServiceUrlUtil.buildUrl("/politicas/profesional/" + id);
            LOG.info("🔵 [POLITICAS-ACCESO-RESOURCE] Consultando políticas para profesional: " + id + " en URL: " + url);
            
            Client client = createClientWithTimeout();
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            LOG.info("Respuesta del servicio de políticas para profesional " + id + ": " + status);
            
            if (status == Response.Status.OK.getStatusCode()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> politicas = response.readEntity(List.class);
                client.close();
                
                // El servicio de políticas devuelve DTOs con los nombres correctos
                // Solo necesitamos agregar tipoAutorizado para el frontend
                if (politicas != null) {
                    List<Map<String, Object>> mappedPoliticas = new java.util.ArrayList<>();
                    for (Map<String, Object> politica : politicas) {
                        Map<String, Object> mapped = new HashMap<>(politica);
                        // Ahora siempre es "clinica" (ya no hay profesionales específicos)
                        if (politica.containsKey("clinicaAutorizada") && 
                            politica.get("clinicaAutorizada") != null && 
                            !((String)politica.get("clinicaAutorizada")).isEmpty()) {
                            mapped.put("tipoAutorizado", "clinica");
                            // Parsear especialidades para mostrar en el frontend
                            String especialidades = (String) politica.get("especialidadesAutorizadas");
                            if (especialidades != null && !especialidades.trim().isEmpty()) {
                                mapped.put("especialidadesAutorizadas", especialidades);
                            } else {
                                mapped.put("especialidadesAutorizadas", null); // null = todos los profesionales
                            }
                        } else {
                            mapped.put("tipoAutorizado", "clinica"); // Por defecto
                        }
                        mappedPoliticas.add(mapped);
                    }
                    LOG.info("Políticas encontradas para profesional " + id + ": " + mappedPoliticas.size());
                    return Response.ok(mappedPoliticas).build();
                }
                return Response.ok().build();
            } else if (status == Response.Status.NOT_FOUND.getStatusCode()) {
                // Si no se encuentran políticas (404), devolver array vacío en lugar de error
                // Esto es un caso válido: el profesional simplemente no tiene políticas
                client.close();
                LOG.info("No se encontraron políticas para el profesional: " + id + " (devuelve array vacío)");
                return Response.ok(new java.util.ArrayList<>()).build();
            } else {
                String errorMsg = response.readEntity(String.class);
                client.close();
                LOG.warning("Error listando políticas por profesional " + id + ": " + errorMsg + " (status: " + status + ")");
                return Response.status(status).entity(Map.of("error", errorMsg != null ? errorMsg : "Error desconocido")).build();
            }
        } catch (Exception e) {
            LOG.severe("Error listando políticas por profesional " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al listar políticas del profesional: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * DELETE /api/documentos/politicas/{id}
     * 
     * Elimina una política de acceso.
     * Proxy hacia el servicio de políticas: DELETE /api/politicas/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarPolitica(@PathParam("id") Long id) {
        try {
            String url = PoliticasServiceUrlUtil.buildUrl("/politicas/" + id);
            
            Client client = createClientWithTimeout();
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .delete();
            
            int status = response.getStatus();
            client.close();
            
            if (status == Response.Status.OK.getStatusCode() || status == Response.Status.NO_CONTENT.getStatusCode()) {
                return Response.ok(Map.of("politicaId", id, "mensaje", "Política eliminada exitosamente")).build();
            } else {
                String errorMsg = response.readEntity(String.class);
                LOG.warning("Error eliminando política: " + errorMsg);
                return Response.status(status).entity(Map.of("error", errorMsg != null ? errorMsg : "Error desconocido")).build();
            }
        } catch (Exception e) {
            LOG.severe("Error eliminando política: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al eliminar la política: " + e.getMessage()))
                    .build();
        }
    }
}

