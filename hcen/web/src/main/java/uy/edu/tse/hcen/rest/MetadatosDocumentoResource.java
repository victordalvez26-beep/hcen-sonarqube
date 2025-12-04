package uy.edu.tse.hcen.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.dto.DTMetadatos;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.rndc.service.DocumentoRndcService;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.util.CookieUtil;
import uy.edu.tse.hcen.util.JWTUtil;
import uy.edu.tse.hcen.utils.PoliticasServiceUrlUtil;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

/**
 * REST Resource para recibir metadata de documentos desde el componente periférico.
 * 
 * Endpoint: POST /api/metadatos-documento
 */
@Path("/metadatos-documento")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MetadatosDocumentoResource {

    private static final Logger logger = Logger.getLogger(MetadatosDocumentoResource.class.getName());

    // El servicio está en el módulo EJB
    @EJB
    private DocumentoRndcService documentoRndcService;
    
    @EJB
    private UserDAO userDAO;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * POST /api/metadatos-documento
     * 
     * Recibe metadata desde el componente periférico y la almacena.
     */
    @POST
    public Response recibirMetadatos(DTMetadatos dtoMetadata) {
        logger.info("Recibiendo metadata desde componente periférico - CI: " + 
                    (dtoMetadata != null ? dtoMetadata.getDocumentoIdPaciente() : "null"));
        
        try {
            if (dtoMetadata == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Metadata es requerida"))
                    .build();
            }
            
            if (dtoMetadata.getDocumentoIdPaciente() == null || dtoMetadata.getDocumentoIdPaciente().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "CI del paciente es requerido"))
                    .build();
            }
            
            String codDocum = dtoMetadata.getDocumentoIdPaciente().trim();
            String nombrePaciente = null;
            String apellidoPaciente = null;
            
            // Separar nombre y apellido de datosPatronimicos
            if (dtoMetadata.getDatosPatronimicos() != null && !dtoMetadata.getDatosPatronimicos().isBlank()) {
                String[] partes = dtoMetadata.getDatosPatronimicos().trim().split("\\s+", 2);
                if (partes.length > 0) {
                    nombrePaciente = partes[0];
                }
                if (partes.length > 1) {
                    apellidoPaciente = partes[1];
                }
            }
            
            String tipoDocumento = dtoMetadata.getTipoDocumento() != null ? dtoMetadata.getTipoDocumento() : "EVALUACION";
            String fechaCreacion = null;
            if (dtoMetadata.getFechaCreacion() != null) {
                fechaCreacion = dtoMetadata.getFechaCreacion().format(DATE_FORMATTER);
            } else if (dtoMetadata.getFechaRegistro() != null) {
                fechaCreacion = dtoMetadata.getFechaRegistro().format(DATE_FORMATTER);
            }
            String formatoDocumento = dtoMetadata.getFormato() != null ? dtoMetadata.getFormato().replace("application/", "").toUpperCase() : "PDF";
            String uriDocumento = dtoMetadata.getUrlAcceso();
            String clinicaOrigen = dtoMetadata.getTenantId() != null ? "Clínica " + dtoMetadata.getTenantId() : dtoMetadata.getAaPrestador();
            
            // Log para debugging
            logger.info(String.format("[BACKEND] Recibiendo metadata - TenantId (String): %s, CI: %s", 
                    dtoMetadata.getTenantId(), dtoMetadata.getDocumentoIdPaciente()));
            
            Long tenantId = null;
            if (dtoMetadata.getTenantId() != null && !dtoMetadata.getTenantId().isBlank()) {
                try {
                    tenantId = Long.parseLong(dtoMetadata.getTenantId());
                    logger.info(String.format("[BACKEND] TenantId parseado correctamente: %d", tenantId));
                } catch (NumberFormatException e) {
                    logger.warning("[BACKEND] No se pudo parsear tenantId: " + dtoMetadata.getTenantId());
                }
            } else {
                logger.warning("[BACKEND] TenantId es null o vacío en la metadata recibida");
            }
            String profesionalSalud = dtoMetadata.getAutor();
            String descripcion = dtoMetadata.getDescripcion();
            boolean accesoPermitido = !dtoMetadata.isBreakingTheGlass();
            
            // Crear el documento en el RNDC usando parámetros primitivos
            Long documentoId = documentoRndcService.crearDocumentoDesdeParametros(
                codDocum, nombrePaciente, apellidoPaciente, tipoDocumento, 
                fechaCreacion, formatoDocumento, uriDocumento, clinicaOrigen, tenantId,
                profesionalSalud, descripcion, accesoPermitido
            );
            
            logger.info("Metadata recibida y almacenada exitosamente - ID: " + documentoId);
            
            // Construir respuesta
            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("id", documentoId);
            respuesta.put("codDocum", codDocum);
            respuesta.put("tipoDocumento", tipoDocumento);
            respuesta.put("fechaCreacion", fechaCreacion);
            respuesta.put("uriDocumento", uriDocumento);
            
            return Response.status(Response.Status.CREATED)
                .entity(respuesta)
                .build();
            
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al recibir metadata", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al recibir metadata desde componente periférico", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al procesar metadata: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * GET /api/metadatos-documento/paciente/{ci}
     * 
     * Obtiene todos los documentos de un paciente por su CI.
     * Filtra por políticas de acceso si se proporciona información del profesional.
     * Registra el acceso del profesional a la historia clínica del paciente.
     * 
     * Query params opcionales:
     * - profesionalId: ID del profesional que está consultando
     * - tenantId: ID de la clínica del profesional
     * - especialidad: Especialidad del profesional
     * - nombreProfesional: Nombre completo del profesional
     */
    @GET
    @Path("/paciente/{ci}")
    public Response obtenerDocumentosPorCI(
            @PathParam("ci") String ci,
            @QueryParam("profesionalId") String profesionalId,
            @QueryParam("tenantId") String tenantId,
            @QueryParam("especialidad") String especialidad,
            @QueryParam("nombreProfesional") String nombreProfesional) {
        logger.info(String.format("Obteniendo documentos para paciente CI: %s, Profesional: %s, Tenant: %s, Especialidad: %s, NombreProfesional: %s", 
                ci, profesionalId, tenantId, especialidad, nombreProfesional));
        
        try {
            if (ci == null || ci.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "CI del paciente es requerido"))
                    .build();
            }
            
            List<uy.edu.tse.hcen.dto.MetadataDocumentoDTO> documentos = 
                documentoRndcService.buscarDocumentosPorCI(ci.trim(), profesionalId, tenantId, especialidad);
            
            logger.info(String.format("Retornando %d documentos (filtrados por políticas) para el paciente CI: %s", 
                    documentos.size(), ci));
            
            // Registrar acceso del profesional a la historia clínica del paciente
            // Solo si hay información del profesional (viene del componente periférico)
            logger.info(String.format("Verificando condiciones para registrar acceso - profesionalId: %s, tenantId: %s", profesionalId, tenantId));
            if (profesionalId != null && !profesionalId.isBlank() && tenantId != null && !tenantId.isBlank()) {
                logger.info(String.format("Condiciones cumplidas, registrando acceso para profesional %s, clínica %s, paciente %s", profesionalId, tenantId, ci));
                registrarAccesoHistoriaClinica(
                    profesionalId,
                    nombreProfesional,
                    especialidad,
                    tenantId,
                    ci,
                    null, // documentoId - null para búsquedas generales
                    null, // tipoDocumento - null para búsquedas generales
                    documentos != null && !documentos.isEmpty() // éxito
                );
            } else {
                logger.warning(String.format("No se registró el acceso - profesionalId: %s, tenantId: %s (uno o ambos son null/blank)", profesionalId, tenantId));
            }
            
            return Response.ok(documentos).build();
            
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al obtener documentos", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener documentos del paciente", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al obtener documentos: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * GET /api/metadatos-documento/usuario
     * 
     * Obtiene todos los documentos del usuario autenticado.
     * El UID se extrae del JWT en la cookie de sesión.
     * El backend busca el CI del usuario y luego busca los documentos por CI.
     */
    @GET
    @Path("/usuario")
    public Response obtenerDocumentosPorUsuario(@Context HttpServletRequest request) {
        logger.info("[ENDPOINT] /metadatos-documento/usuario - Método ejecutado");
        
        try {
            // Obtener JWT de la cookie
            logger.info("[ENDPOINT] Extrayendo JWT de la cookie...");
            String jwtToken = CookieUtil.resolveJwtToken(request);
            
            if (jwtToken == null) {
                logger.warning("[ENDPOINT] No se encontró JWT en la cookie");
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No autenticado"))
                    .build();
            }
            
            logger.info("[ENDPOINT] JWT encontrado en cookie (primeros 50 chars): " + 
                    (jwtToken.length() > 50 ? jwtToken.substring(0, 50) + "..." : jwtToken));
            
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                logger.warning("[ENDPOINT] JWT inválido o expirado");
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token inválido o expirado"))
                    .build();
            }
            
            logger.info("[ENDPOINT] JWT válido - UID extraído: " + userUid);
            logger.info("[ENDPOINT] Obteniendo documentos para usuario UID: " + userUid);
            
            // Buscar el usuario por UID para obtener su CI
            User user = userDAO.findByUid(userUid);
            
            if (user == null) {
                logger.warning("Usuario no encontrado con UID: " + userUid);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Usuario no encontrado"))
                    .build();
            }
            
            logger.info(String.format("Usuario encontrado - UID: %s, CI: %s, Nombre: %s", 
                    userUid, user.getCodDocum(), user.getPrimerNombre()));
            
            String codDocum = user.getCodDocum();
            
            if ((codDocum == null || codDocum.trim().isEmpty()) && userUid.startsWith("uy-ci-")) {
                String ciExtraido = userUid.replace("uy-ci-", "");
                logger.info(String.format("Extrayendo CI del UID - UID: %s, CI extraído: %s", userUid, ciExtraido));
                codDocum = ciExtraido;
            }
            
            if (codDocum == null || codDocum.trim().isEmpty()) {
                logger.warning("Usuario no tiene CI asociado - UID: " + userUid);
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El usuario no tiene CI asociado"))
                    .build();
            }
            
            logger.info(String.format("Usuario encontrado - UID: %s, CI: %s", userUid, codDocum));
            
            String profesionalId = null;
            List<uy.edu.tse.hcen.dto.MetadataDocumentoDTO> documentos = 
                documentoRndcService.buscarDocumentosPorCI(codDocum.trim(), profesionalId);
            
            logger.info(String.format("Encontrados %d documentos para el usuario UID: %s (CI: %s)", 
                    documentos.size(), userUid, codDocum));
            
            return Response.ok(documentos).build();
            
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al obtener documentos", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener documentos del usuario", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al obtener documentos: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * GET /api/metadatos-documento/{id}/descargar
     * 
     * Descarga el PDF del documento desde el componente periférico.
     * El backend hace proxy al componente periférico usando la URI almacenada en la metadata.
     */
    @GET
    @Path("/{id}/descargar")
    @Produces("application/pdf")
    @PermitAll
    public Response descargarDocumento(@PathParam("id") Long id, @Context HttpServletRequest request) {
        logger.info(String.format("🔽 [FRONTEND→BACKEND] Iniciando descarga - Documento ID: %d", id));
        
        try {
            // Obtener la metadata del documento
            logger.info(String.format("[BACKEND] Obteniendo metadata para documento ID: %d", id));
            MetadataDocumentoDTO metadata = documentoRndcService.obtenerDocumentoPorId(id);
            
            if (metadata == null) {
                logger.warning(String.format("[BACKEND] Metadata no encontrada para documento ID: %d", id));
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Documento no encontrado"))
                    .build();
            }
            
            logger.info(String.format("[BACKEND] Metadata obtenida - URI: %s, Clínica: %s", 
                    metadata.getUriDocumento(), metadata.getClinicaOrigen()));
            
            DocumentoRndcService.DocumentoDescarga descarga = 
                documentoRndcService.prepararDescargaDocumento(id);
            
            if (descarga == null || descarga.getStream() == null) {
                logger.severe(String.format("[BACKEND] Descarga nula o stream nulo después de obtener metadata. ID: %d", id));
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "El PDF no está disponible para descarga (stream nulo)"))
                    .build();
            }
            
            logger.info(String.format("[BACKEND] Stream recibido del componente periférico"));
            
            // Leer el stream completo a un byte array
            InputStream stream = descarga.getStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            int totalBytes = 0;
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                totalBytes += nRead;
            }
            byte[] pdfBytes = buffer.toByteArray();
            stream.close();
            
            logger.info(String.format("[BACKEND] PDF leído completamente - Tamaño: %d bytes (leídos: %d)", 
                    pdfBytes.length, totalBytes));
            
            if (pdfBytes.length == 0) {
                logger.severe(String.format("[BACKEND] PDF vacío - Tamaño: 0 bytes"));
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "El PDF recibido está vacío"))
                    .build();
            }
            
            // Verificar que los primeros bytes sean de un PDF válido
            if (pdfBytes.length >= 4) {
                String header = new String(pdfBytes, 0, 4);
                if (!header.startsWith("%PDF")) {
                    logger.warning(String.format("[BACKEND] Los primeros bytes no son de un PDF válido: %s", header));
                    logger.warning(String.format("[BACKEND] Primeros 200 bytes: %s", 
                            new String(pdfBytes, 0, Math.min(200, pdfBytes.length))));
                } else {
                    logger.info(String.format("[BACKEND] PDF válido detectado - Header: %s", header));
                }
            }
            
            logger.info(String.format("[BACKEND→FRONTEND] Enviando PDF al frontend - Tamaño: %d bytes", pdfBytes.length));
            
            // Retornar el PDF
            return Response.ok(pdfBytes)
                .header("Content-Type", descarga.getContentType())
                .header("Content-Disposition", "attachment; filename=\"" + descarga.getFileName() + "\"")
                .build();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("[BACKEND] Error al descargar documento ID: %d", id), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al descargar documento: " + e.getMessage()))
                .build();
        }
    }

    /**
     * POST /api/documentos/solicitar-acceso
     * 
     * Permite a un usuario solicitar acceso a un documento específico.
     * Proxy hacia el servicio de políticas.
     */
    @POST
    @Path("/solicitar-acceso")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response solicitarAcceso(@Context HttpServletRequest request, Map<String, Object> body) {
        logger.info("Solicitud de acceso recibida");
        
        try {
            // Obtener JWT de la cookie o del header Authorization (Bearer)
            String jwtToken = CookieUtil.resolveJwtToken(request);
            if (jwtToken == null) {
                // Intentar obtener del header Authorization (Bearer token)
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwtToken = authHeader.substring(7); // Remover "Bearer "
                    logger.info("Token JWT obtenido del header Authorization (Bearer)");
                }
            } else {
                logger.info("Token JWT obtenido de la cookie");
            }
            
            if (jwtToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No autenticado"))
                    .build();
            }
            
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token inválido"))
                    .build();
            }
            
            // El userUid puede ser un nickname del componente periférico o un UID de HCEN Central
            // No es necesario que el usuario exista en HCEN Central para crear una solicitud de acceso
            // El solicitanteId puede ser cualquier identificador del profesional
            logger.info(String.format("Token validado - solicitanteId: %s (puede ser nickname del componente periférico)", userUid));
            
            if (body == null || body.get("pacienteCI") == null || String.valueOf(body.get("pacienteCI")).trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "pacienteCI es requerido"))
                    .build();
            }
            
            String pacienteCI = body.get("pacienteCI").toString();
            
            // VERIFICAR SI YA EXISTE UNA POLÍTICA ACTIVA PARA ESTE PROFESIONAL Y PACIENTE
            // Esto previene crear solicitudes duplicadas cuando ya hay acceso
            try {
                String politicasUrl = PoliticasServiceUrlUtil.buildUrl("/politicas/profesional/" + java.net.URLEncoder.encode(userUid, "UTF-8"));
                logger.info(String.format("[BACKEND] Verificando políticas existentes para profesional: %s, paciente: %s", userUid, pacienteCI));
                
                jakarta.ws.rs.client.Client checkClient = jakarta.ws.rs.client.ClientBuilder.newClient();
                try {
                    jakarta.ws.rs.core.Response politicasResponse = checkClient.target(politicasUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .get();
                    
                    if (politicasResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> politicas = politicasResponse.readEntity(java.util.List.class);
                        if (politicas != null) {
                            logger.info(String.format("[BACKEND] Políticas encontradas para profesional %s: %d", userUid, politicas.size()));
                            
                            // Verificar si hay alguna política activa para este paciente específico
                            logger.info(String.format("[BACKEND] Verificando %d políticas para paciente %s", politicas.size(), pacienteCI));
                            boolean tienePoliticaActiva = false;
                            
                            for (Map<String, Object> politica : politicas) {
                                // Intentar obtener codDocumPaciente de diferentes campos posibles
                                String codDocumPaciente = null;
                                if (politica.containsKey("codDocumPaciente")) {
                                    codDocumPaciente = politica.get("codDocumPaciente") != null ? 
                                        politica.get("codDocumPaciente").toString() : null;
                                } else if (politica.containsKey("pacienteCI")) {
                                    codDocumPaciente = politica.get("pacienteCI") != null ? 
                                        politica.get("pacienteCI").toString() : null;
                                } else if (politica.containsKey("codDocum")) {
                                    codDocumPaciente = politica.get("codDocum") != null ? 
                                        politica.get("codDocum").toString() : null;
                                }
                                
                                Boolean activa = null;
                                if (politica.get("activa") instanceof Boolean) {
                                    activa = (Boolean) politica.get("activa");
                                } else if (politica.get("activa") != null) {
                                    String activaStr = politica.get("activa").toString();
                                    activa = !activaStr.equals("false") && !activaStr.equals("0");
                                }
                                
                                // Si activa es null pero la política existe, asumir que está activa (por defecto)
                                // Esto maneja el caso donde el DTO no mapea correctamente el campo
                                if (activa == null) {
                                    logger.warning(String.format("[BACKEND] Política ID: %s tiene activa=null, asumiendo activa=true", politica.get("id")));
                                    activa = Boolean.TRUE; // Por defecto, las políticas están activas
                                }
                                
                                Object politicaId = politica.get("id");
                                logger.info(String.format("[BACKEND] Política ID: %s, codDocumPaciente: '%s', pacienteCI buscado: '%s', activa: %s", 
                                    politicaId, codDocumPaciente, pacienteCI, activa));
                                
                                boolean esPacienteCorrecto = codDocumPaciente != null && 
                                    codDocumPaciente.trim().equals(pacienteCI.trim());
                                boolean esActiva = activa != null && activa;
                                
                                logger.info(String.format("[BACKEND] Política ID: %s - esPacienteCorrecto: %s, esActiva: %s", 
                                    politicaId, esPacienteCorrecto, esActiva));
                                
                                if (esPacienteCorrecto && esActiva) {
                                    logger.warning(String.format("[BACKEND] Ya existe una política activa para profesional %s y paciente %s (ID: %s). Bloqueando solicitud.", 
                                        userUid, pacienteCI, politicaId));
                                    tienePoliticaActiva = true;
                                    break;
                                }
                            }
                            
                            if (tienePoliticaActiva) {
                                checkClient.close();
                                return Response.status(Response.Status.CONFLICT)
                                    .entity(Map.of(
                                        "error", "Ya tiene acceso a los documentos de este paciente",
                                        "mensaje", "Ya tiene acceso a los documentos de este paciente. No es necesario solicitar acceso nuevamente.",
                                        "success", false
                                    ))
                                    .build();
                            }
                        }
                    }
                } finally {
                    checkClient.close();
                }
            } catch (Exception e) {
                // Si falla la verificación, continuar con la creación de la solicitud
                // No queremos bloquear la solicitud si hay un problema técnico
                logger.warning(String.format("[BACKEND] Error al verificar políticas existentes (continuando con solicitud): %s", e.getMessage()));
            }
            
            // Construir solicitud para el servicio de políticas
            // El servicio de políticas está desplegado en el mismo WildFly con contexto /hcen-politicas-service
            String solicitudUrl = PoliticasServiceUrlUtil.buildUrl("/solicitudes");
            logger.info(String.format("URL del servicio de políticas: %s", solicitudUrl));
            
            // Preparar payload para el servicio de políticas
            // El servicio espera: solicitanteId, codDocumPaciente, tipoDocumento, documentoId, razonSolicitud, especialidad, tenantId
            // El solicitanteId puede ser el nickname del profesional del componente periférico
            Map<String, Object> solicitudPayload = new java.util.HashMap<>();
            solicitudPayload.put("solicitanteId", userUid); // Usar el userUid extraído del token (nickname o UID)
            solicitudPayload.put("codDocumPaciente", body.get("pacienteCI"));
            
            // tenantId (clinicaAutorizada) - requerido para crear la política correctamente
            if (body.containsKey("tenantId") && body.get("tenantId") != null) {
                solicitudPayload.put("tenantId", body.get("tenantId"));
                logger.info(String.format("Incluyendo tenantId en solicitud: %s", body.get("tenantId")));
            } else {
                logger.warning("No se proporcionó tenantId en la solicitud - la política puede no funcionar correctamente");
            }
            
            // documentoId es opcional - si no se proporciona, es para todos los documentos del paciente
            if (body.containsKey("documentoId") && body.get("documentoId") != null) {
            solicitudPayload.put("documentoId", body.get("documentoId"));
            }
            
            solicitudPayload.put("razonSolicitud", body.getOrDefault("motivo", body.getOrDefault("razonSolicitud", "Acceso necesario para atención médica")));
            
            // Campos opcionales
            if (body.containsKey("tipoDocumento")) {
                solicitudPayload.put("tipoDocumento", body.get("tipoDocumento"));
            }
            if (body.containsKey("especialidad")) {
                solicitudPayload.put("especialidad", body.get("especialidad"));
            }
            
            // Enviar solicitud al servicio de políticas
            jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient();
            try {
                logger.info(String.format("Enviando solicitud a servicio de políticas: %s con payload: %s", solicitudUrl, solicitudPayload));
                
                jakarta.ws.rs.core.Response response = client.target(solicitudUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(jakarta.ws.rs.client.Entity.entity(solicitudPayload, MediaType.APPLICATION_JSON));
                
                int status = response.getStatus();
                logger.info(String.format("Respuesta del servicio de políticas - Status: %d", status));
                
                if (status == Response.Status.CREATED.getStatusCode() || status == Response.Status.OK.getStatusCode()) {
                    Object responseEntity = response.hasEntity() ? response.readEntity(Object.class) : null;
                    logger.info(String.format("Solicitud creada exitosamente. Respuesta: %s", responseEntity));
                    return Response.status(Response.Status.CREATED)
                        .entity(Map.of("success", true, "mensaje", "Solicitud de acceso enviada exitosamente", "data", responseEntity != null ? responseEntity : Map.of()))
                        .build();
                } else {
                    // Intentar leer como Map primero (JSON), luego como String
                    String errorMsg = "Error desconocido";
                    if (response.hasEntity()) {
                        try {
                            Object errorEntity = response.readEntity(Object.class);
                            if (errorEntity instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> errorMap = (Map<String, Object>) errorEntity;
                                errorMsg = (String) errorMap.getOrDefault("error", errorEntity.toString());
                            } else {
                                errorMsg = errorEntity.toString();
                            }
                        } catch (Exception e) {
                            logger.warning(String.format("Error al leer respuesta de error: %s", e.getMessage()));
                            try {
                                errorMsg = response.readEntity(String.class);
                            } catch (Exception e2) {
                                errorMsg = "Error al procesar respuesta del servidor";
                            }
                        }
                    }
                    logger.warning(String.format("Error al crear solicitud - Status: %d, Mensaje: %s", status, errorMsg));
                    return Response.status(status)
                        .entity(Map.of("error", errorMsg))
                        .build();
                }
            } finally {
                client.close();
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al procesar solicitud de acceso", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al procesar la solicitud: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Registra un acceso a la historia clínica del paciente.
     * Este método es llamado cuando un profesional del componente periférico
     * consulta o descarga documentos de un paciente.
     * 
     * @param profesionalId ID del profesional (nickname)
     * @param nombreProfesional Nombre completo del profesional
     * @param especialidad Especialidad del profesional
     * @param tenantId ID de la clínica (tenant) del profesional
     * @param codDocumPaciente CI del paciente
     * @param documentoId ID del documento (opcional, null para búsquedas generales)
     * @param tipoDocumento Tipo de documento (opcional)
     * @param exito Si el acceso fue exitoso
     */
    private void registrarAccesoHistoriaClinica(
            String profesionalId, String nombreProfesional, String especialidad,
            String tenantId, String codDocumPaciente, String documentoId, String tipoDocumento, boolean exito) {
        
        // Registrar de forma asíncrona para no bloquear la respuesta
        try {
            String registroUrl = PoliticasServiceUrlUtil.buildUrl("/registros");
            
            // Construir payload para registrar acceso
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("profesionalId", profesionalId);
            payload.put("codDocumPaciente", codDocumPaciente);
            payload.put("clinicaId", tenantId);
            
            if (nombreProfesional != null && !nombreProfesional.isBlank()) {
                payload.put("nombreProfesional", nombreProfesional);
            }
            if (especialidad != null && !especialidad.isBlank()) {
                payload.put("especialidad", especialidad);
            }
            if (documentoId != null && !documentoId.isBlank()) {
                payload.put("documentoId", documentoId);
            }
            if (tipoDocumento != null && !tipoDocumento.isBlank()) {
                payload.put("tipoDocumento", tipoDocumento);
            } else {
                payload.put("tipoDocumento", "BÚSQUEDA"); // Búsqueda general si no hay tipo específico
            }
            
            payload.put("exito", exito);
            if (!exito) {
                payload.put("motivoRechazo", "No se encontraron documentos con acceso permitido");
            }
            payload.put("referencia", documentoId != null ? "Descarga de documento" : "Consulta de documentos del paciente");
            
            // Obtener IP y User-Agent del request (si están disponibles)
            // Estos pueden ser null si la llamada viene del componente periférico
            payload.put("ipAddress", null);
            payload.put("userAgent", null);
            
            logger.info(String.format("Registrando acceso - Profesional: %s (%s), Paciente: %s, Clínica: %s, Éxito: %s", 
                    profesionalId, nombreProfesional, codDocumPaciente, tenantId, exito));
            
            // Llamar al servicio de políticas de forma asíncrona
            jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient();
            try {
                jakarta.ws.rs.core.Response response = client.target(registroUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .post(jakarta.ws.rs.client.Entity.entity(payload, MediaType.APPLICATION_JSON));
                
                int status = response.getStatus();
                if (status == 201 || status == 200) {
                    logger.info(String.format("Acceso registrado exitosamente - Status: %d", status));
                } else {
                    String errorBody = response.hasEntity() ? response.readEntity(String.class) : "Sin detalles";
                    logger.warning(String.format("Error al registrar acceso - Status: %d, Response: %s", status, errorBody));
                }
            } finally {
                client.close();
            }
            
        } catch (Exception e) {
            // No propagar excepciones para no afectar la operación principal
            logger.warning(String.format("Error al registrar acceso (no crítico): %s", e.getMessage()));
        }
    }

    /**
     * GET /api/metadatos-documento/paciente/historia
     * Descarga todos los PDFs del usuario autenticado, los mergea y los devuelve en un solo PDF.
     */
    @GET
    @Path("/paciente/historia")
    @Produces("application/pdf")
    public Response descargarPdfMergeado(@Context HttpServletRequest request) {
        logger.info("[ENDPOINT] /metadatos-documento/paciente/historia - Método ejecutado");
        List<File> tempFiles = new ArrayList<>();
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
            String codDocum = user.getCodDocum();
            if (codDocum == null || codDocum.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"El usuario no tiene CI asociado\"}")
                        .build();
            }
            
            // Buscar documentos por CI
            List<MetadataDocumentoDTO> documentos = documentoRndcService.buscarDocumentosPorCI(codDocum, null);
            if (documentos == null || documentos.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"No hay documentos para el usuario\"}")
                        .build();
            }

            // Ordenar por fechaCreacion (de más antiguo a más nuevo)
            documentos.sort((a, b) -> {
                try {
                    java.time.LocalDate fa = java.time.LocalDate.parse(a.getFechaCreacion());
                    java.time.LocalDate fb = java.time.LocalDate.parse(b.getFechaCreacion());
                    return fa.compareTo(fb);
                } catch (Exception e) {
                    return 0;
                }
            });

            // Descargar cada PDF y guardarlo temporalmente
            for (MetadataDocumentoDTO doc : documentos) {
                if (!"PDF".equalsIgnoreCase(doc.getFormatoDocumento())) continue;
                try {
                    var descarga = documentoRndcService.prepararDescargaDocumento(doc.getId());
                    File tempFile = File.createTempFile("doc_" + doc.getId() + "_", ".pdf");
                    try (var out = new FileOutputStream(tempFile); var in = descarga.getStream()) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
                    }
                    tempFiles.add(tempFile);
                } catch (Exception e) {
                    logger.warning("No se pudo descargar el PDF del documento ID: " + doc.getId() + " - " + e.getMessage());
                }
            }
            if (tempFiles.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"No hay PDFs para mergear\"}")
                        .build();
            }

            // Merge PDFs usando PDFBox
            File mergedFile = File.createTempFile("merge_", ".pdf");
            PDFMergerUtility merger = new PDFMergerUtility();
            for (File f : tempFiles) merger.addSource(f);
            merger.setDestinationFileName(mergedFile.getAbsolutePath());
            merger.mergeDocuments(null);
            byte[] mergedBytes = Files.readAllBytes(mergedFile.toPath());

            // Borrar archivos temporales
            for (File f : tempFiles) try { f.delete(); } catch (Exception ignore) {}
            mergedFile.delete();
            logger.info("PDF mergeado generado y archivos temporales eliminados");
            return Response.ok(mergedBytes)
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=historia_clinica.pdf")
                    .build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al mergear y descargar PDFs", e);

            // Intentar borrar archivos temporales si hay error
            for (File f : tempFiles) try { f.delete(); } catch (Exception ignore) {}
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al generar el PDF: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}

