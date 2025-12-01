package uy.edu.tse.hcen.rndc.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.model.NotificationType;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.rndc.repository.MetadataDocumentoRndcRepository;
import uy.edu.tse.hcen.service.NotificationService;
import uy.edu.tse.hcen.rndc.mapper.MetadataDocumentoMapper;
import uy.edu.tse.hcen.rndc.model.MetadataDocumento;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Servicio Jakarta EJB para la lógica de negocio del RNDC.
 * 
 * Responsabilidades:
 * - Búsqueda de documentos clínicos
 * - Gestión de metadatos de documentos
 * - Integración con nodos periféricos
 * - Preparación de descargas de documentos
 */
@Stateless
public class DocumentoRndcService {
    
    private static final Logger LOGGER = Logger.getLogger(DocumentoRndcService.class.getName());
    
    @Inject
    private MetadataDocumentoRndcRepository metadataRepository;
    
    @Inject
    private NotificationService notificationService;
    
    @Inject
    private UserDAO userDAO;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    /**
     * Busca documentos por CI del paciente.
     * Si se proporciona información del profesional, filtra por políticas de acceso.
     */
    public List<MetadataDocumentoDTO> buscarDocumentosPorCI(String codDocumPaciente, String profesionalId) {
        return buscarDocumentosPorCI(codDocumPaciente, profesionalId, null, null);
    }
    
    /**
     * Busca documentos por CI del paciente y filtra por políticas de acceso.
     * 
     * @param codDocumPaciente CI del paciente
     * @param profesionalId ID del profesional que está consultando
     * @param tenantId ID de la clínica del profesional
     * @param especialidad Especialidad del profesional
     * @return Lista de documentos que el profesional tiene permiso para ver
     */
    public List<MetadataDocumentoDTO> buscarDocumentosPorCI(String codDocumPaciente, String profesionalId, 
                                                             String tenantId, String especialidad) {
        LOGGER.info(String.format("Buscando documentos para paciente CI: %s, Profesional: %s, Tenant: %s, Especialidad: %s", 
                codDocumPaciente, profesionalId, tenantId, especialidad));
        
        if (codDocumPaciente == null || codDocumPaciente.trim().isEmpty()) {
            throw new IllegalArgumentException("El CI del paciente es requerido");
        }
        
        // Buscar todos los documentos del paciente
        List<MetadataDocumento> entidades = metadataRepository.buscarPorCodDocum(codDocumPaciente);
        LOGGER.info(String.format("Encontrados %d documentos en total para el paciente CI: %s", 
                entidades.size(), codDocumPaciente));
        
        // Si se proporciona información del profesional, filtrar por políticas de acceso
        if (profesionalId != null && !profesionalId.isBlank()) {
            // Llamar al servicio de políticas vía HTTP (está en un WAR separado)
            List<MetadataDocumento> documentosFiltrados = entidades.stream()
                .filter(doc -> {
                    String tipoDocumento = doc.getTipoDocumento() != null ? 
                            doc.getTipoDocumento().name() : null;
                    
                    // Verificar si el profesional tiene permiso para este documento
                    // Llamar al servicio de políticas vía HTTP
                    boolean tienePermiso = verificarPermisoPoliticas(
                            profesionalId, 
                            codDocumPaciente, 
                            tipoDocumento, 
                            tenantId, 
                            especialidad);
                    
                    if (!tienePermiso) {
                        LOGGER.info(String.format("Documento excluido por falta de permiso - ID: %d, Tipo: %s, Profesional: %s", 
                                doc.getId(), tipoDocumento, profesionalId));
                    }
                    
                    return tienePermiso;
                })
                .collect(Collectors.toList());
            
            LOGGER.info(String.format("Retornando %d documentos autorizados (de %d totales) para el profesional %s", 
                    documentosFiltrados.size(), entidades.size(), profesionalId));
            
            return documentosFiltrados.stream()
                .map(MetadataDocumentoMapper::toDTO)
                .collect(Collectors.toList());
        } else {
            // Si no hay información del profesional, retornar todos los documentos
            // (para compatibilidad con llamadas sin autenticación)
            LOGGER.info("No se proporcionó información del profesional, retornando todos los documentos");
            return entidades.stream()
                .map(MetadataDocumentoMapper::toDTO)
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Busca documentos por nombre de paciente (búsqueda parcial).
     */
    public List<MetadataDocumentoDTO> buscarDocumentosPorNombre(String nombrePaciente, String profesionalId) {
        LOGGER.info(String.format("Buscando documentos para paciente con nombre: %s", nombrePaciente));
        
        if (nombrePaciente == null || nombrePaciente.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente es requerido");
        }
        
        List<MetadataDocumento> entidades = metadataRepository.buscarPorNombre(nombrePaciente);
        
        LOGGER.info(String.format("Encontrados %d documentos", entidades.size()));
        
        return entidades.stream()
            .map(MetadataDocumentoMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un documento específico por ID.
     */
    public MetadataDocumentoDTO obtenerDocumentoPorId(Long id) {
        LOGGER.info(String.format("Obteniendo documento con ID: %d", id));
        
        MetadataDocumento entidad = metadataRepository.findById(id);
        
        if (entidad == null) {
            LOGGER.warning(String.format("Documento no encontrado: ID %d", id));
            return null;
        }
        
        return MetadataDocumentoMapper.toDTO(entidad);
    }
    
    /**
     * Crea un nuevo metadata de documento usando parámetros primitivos.
     * Esta versión evita problemas de classloader al no usar DTOs compartidos.
     */
    public Long crearDocumentoDesdeParametros(
            String codDocum,
            String nombrePaciente,
            String apellidoPaciente,
            String tipoDocumento,
            String fechaCreacion,
            String formatoDocumento,
            String uriDocumento,
            String clinicaOrigen,
            Long tenantId,
            String profesionalSalud,
            String descripcion,
            boolean accesoPermitido) {
        LOGGER.info("Creando nuevo documento en RNDC desde parámetros");
        
        // Validar datos requeridos
        if (codDocum == null || codDocum.trim().isEmpty()) {
            throw new IllegalArgumentException("El CI del paciente es requerido");
        }
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento es requerido");
        }
        
        // Crear entidad directamente desde parámetros
        MetadataDocumento entidad = new MetadataDocumento();
        entidad.setCodDocum(codDocum);
        entidad.setNombrePaciente(nombrePaciente != null ? nombrePaciente : "");
        entidad.setApellidoPaciente(apellidoPaciente);
        
        // Parsear tipo de documento
        // Mapear valores del componente periférico a valores del enum
        String tipoDocumentoNormalizado = tipoDocumento;
        if ("EVALUACION".equalsIgnoreCase(tipoDocumento)) {
            tipoDocumentoNormalizado = "CONSULTA_MEDICA"; // Mapear EVALUACION a CONSULTA_MEDICA
        }
        
        try {
            entidad.setTipoDocumento(uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico.valueOf(tipoDocumentoNormalizado));
        } catch (IllegalArgumentException e) {
            // Si el tipo no es válido, usar OTROS como fallback
            LOGGER.warning(String.format("Tipo de documento inválido: %s, usando OTROS como fallback", tipoDocumento));
            entidad.setTipoDocumento(uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico.OTROS);
        }
        
        // Parsear fecha
        if (fechaCreacion != null && !fechaCreacion.trim().isEmpty()) {
            try {
                entidad.setFechaCreacion(java.time.LocalDateTime.parse(fechaCreacion, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                // Fallback: intentar parsear solo fecha
                try {
                   entidad.setFechaCreacion(java.time.LocalDate.parse(fechaCreacion, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
                } catch (Exception e2) {
                   entidad.setFechaCreacion(java.time.LocalDateTime.now(java.time.ZoneId.of("America/Montevideo")));
                }
            }
        } else {
            entidad.setFechaCreacion(java.time.LocalDateTime.now(java.time.ZoneId.of("America/Montevideo")));
        }
        
        // Parsear formato
        try {
            if (formatoDocumento != null && !formatoDocumento.trim().isEmpty()) {
                entidad.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.valueOf(formatoDocumento));
            } else {
                entidad.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            entidad.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        }
        
        entidad.setUriDocumento(uriDocumento);
        entidad.setClinicaOrigen(clinicaOrigen);
        entidad.setTenantId(tenantId);
        entidad.setProfesionalSalud(profesionalSalud);
        entidad.setDescripcion(descripcion);
        entidad.setRestringido(!accesoPermitido);
        
        MetadataDocumento entidadGuardada = metadataRepository.save(entidad);
        
        LOGGER.info(String.format("Documento creado exitosamente con ID: %d", entidadGuardada.getId()));
        
        // Enviar notificación al paciente si está registrado en el sistema
        try {
            notificationService.sendNotification(
                userDAO.findByDocumento(codDocum).getUid(),
                NotificationType.MEDICAL_HISTORY,
                "Nuevo documento en tu historial médico",
                "Se ha agregado un nuevo documento a tu historial clínico. Puedes revisarlo en tus documentos"
            );
        } catch (Exception e) {
            // No fallar la creación del documento si la notificación falla
            LOGGER.warning(String.format("Error al enviar notificación por documento creado (ID: %d): %s", 
                    entidadGuardada.getId(), e.getMessage()));
        }
        
        return entidadGuardada.getId();
    }
    
    /**
     * Prepara la descarga de un documento desde el nodo periférico.
     */
    public DocumentoDescarga prepararDescargaDocumento(Long id) throws Exception {
        LOGGER.info(String.format("Preparando descarga para documento ID: %d", id));
        
        MetadataDocumento metadata = metadataRepository.findById(id);
        
        if (metadata == null) {
            throw new Exception("Documento no encontrado");
        }
        
        // Obtener tenantId: primero del campo, si es null intentar extraerlo de clinicaOrigen
        Long tenantId = metadata.getTenantId();
        if (tenantId == null && metadata.getClinicaOrigen() != null) {
            // Intentar extraer el número de "Clínica X"
            String clinicaOrigen = metadata.getClinicaOrigen();
            LOGGER.warning(String.format("⚠️ [BACKEND] TenantId es null, intentando extraer de clinicaOrigen: %s", clinicaOrigen));
            try {
                // Buscar el número después de "Clínica "
                if (clinicaOrigen.startsWith("Clínica ")) {
                    String numeroStr = clinicaOrigen.substring("Clínica ".length()).trim();
                    tenantId = Long.parseLong(numeroStr);
                    LOGGER.info(String.format("✅ [BACKEND] TenantId extraído de clinicaOrigen: %d", tenantId));
                }
            } catch (Exception e) {
                LOGGER.warning(String.format("⚠️ [BACKEND] No se pudo extraer tenantId de clinicaOrigen: %s", e.getMessage()));
            }
        }
        
        LOGGER.info(String.format("📋 [BACKEND] Usando tenantId: %s para descargar documento", tenantId));
        
        // Obtener documento desde nodo periférico, pasando el tenantId
        InputStream stream = obtenerDocumentoDesdeNodo(metadata.getUriDocumento(), tenantId);
        
        String formato = metadata.getFormatoDocumento() != null ? metadata.getFormatoDocumento().name().toLowerCase() : "pdf";
        String contentType = getContentType(formato);
        String fileName = sanitizeFileName(metadata.getTipoDocumento().name()) + "." + formato.toLowerCase();
        
        return new DocumentoDescarga(stream, contentType, fileName);
    }
    
    private InputStream obtenerDocumentoDesdeNodo(String uri, Long tenantId) throws Exception {
        LOGGER.info(String.format("🔄 [BACKEND→PERIFERICO] Obteniendo documento desde nodo periférico: %s, TenantId: %s", uri, tenantId));
        
        // Obtener URL base del componente periférico desde variable de entorno
        String peripheralBaseUrl = System.getenv().getOrDefault("PERIPHERAL_NODE_URL", "http://localhost:8081");
        
        // Si la URI contiene localhost pero PERIPHERAL_NODE_URL apunta a una URL externa,
        // reemplazar localhost por la URL externa configurada
        if (uri != null && uri.contains("localhost:8081") && !peripheralBaseUrl.contains("localhost")) {
            LOGGER.info(String.format("🔄 [BACKEND→PERIFERICO] Reemplazando localhost:8081 en URI por URL configurada: %s", peripheralBaseUrl));
            uri = uri.replace("http://localhost:8081", peripheralBaseUrl);
            LOGGER.info(String.format("🔄 [BACKEND→PERIFERICO] URI actualizada: %s", uri));
        }
        
        // Extraer el host de la URL base para validación (después del reemplazo)
        String peripheralHost = peripheralBaseUrl.replace("http://", "").replace("https://", "");
        
        // Validar que la URI tenga un formato válido después del reemplazo
        // Permitir URIs que contengan el host configurado, localhost (si es el valor por defecto), o el nombre del servicio Docker
        if (uri != null && 
            !uri.contains(peripheralHost) && 
            !uri.contains("hcen-wildfly-app") &&
            !(uri.contains("localhost:8081") && peripheralBaseUrl.contains("localhost"))) {
            LOGGER.warning(String.format("⚠️ [BACKEND→PERIFERICO] URI no coincide con configuración: %s", uri));
            LOGGER.warning(String.format("⚠️ [BACKEND→PERIFERICO] PERIPHERAL_NODE_URL configurada: %s", peripheralBaseUrl));
            // No lanzar excepción, solo registrar warning y continuar
            // La URI puede ser válida aunque no coincida exactamente (ej: URLs de ejemplo en desarrollo)
            LOGGER.info("ℹ️ [BACKEND→PERIFERICO] Continuando con la URI tal como está");
        }
        
        // Convertir URI externa (localhost:8081) a URI interna (nombre del servicio Docker)
        // cuando se ejecuta dentro de un contenedor Docker
        String uriInterna = convertirUriParaDocker(uri);
        
        // Agregar tenantId como query parameter si está disponible
        if (tenantId != null) {
            String separator = uriInterna.contains("?") ? "&" : "?";
            uriInterna = uriInterna + separator + "tenantId=" + tenantId;
            LOGGER.info(String.format("🔄 [BACKEND→PERIFERICO] TenantId agregado como query parameter: %s", tenantId));
        }
        
        LOGGER.info(String.format("🔄 [BACKEND→PERIFERICO] URI convertida: %s → %s", uri, uriInterna));
        
        // Generar token de servicio para autenticación entre servicios
        String serviceToken = generarTokenServicio();
        
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uriInterna))
                .GET();
            
            // Agregar header de autorización con token de servicio
            if (serviceToken != null) {
                requestBuilder.header("Authorization", "Bearer " + serviceToken);
                LOGGER.info("🔑 [BACKEND→PERIFERICO] Token de servicio agregado a la petición");
            } else {
                LOGGER.warning("⚠️ [BACKEND→PERIFERICO] No se pudo generar token de servicio");
            }
            
            HttpRequest request = requestBuilder.build();
            LOGGER.info(String.format("📡 [BACKEND→PERIFERICO] Enviando petición HTTP GET a: %s", uriInterna));
            
            HttpResponse<InputStream> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofInputStream());
            
            LOGGER.info(String.format("📥 [PERIFERICO→BACKEND] Respuesta recibida - Status: %d", response.statusCode()));
            
            // Verificar headers de respuesta
            if (response.headers().firstValue("Content-Type").isPresent()) {
                LOGGER.info(String.format("📥 [PERIFERICO→BACKEND] Content-Type: %s", 
                        response.headers().firstValue("Content-Type").get()));
            }
            if (response.headers().firstValue("Content-Length").isPresent()) {
                LOGGER.info(String.format("📥 [PERIFERICO→BACKEND] Content-Length: %s bytes", 
                        response.headers().firstValue("Content-Length").get()));
            }
            
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                LOGGER.warning("⚠️ [BACKEND→PERIFERICO] Token de servicio rechazado, intentando sin autenticación");
                // Reintentar sin token (por si el endpoint permite acceso sin autenticación en red interna)
                request = HttpRequest.newBuilder()
                    .uri(URI.create(uriInterna))
                    .GET()
                    .build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                LOGGER.info(String.format("📥 [PERIFERICO→BACKEND] Respuesta sin token - Status: %d", response.statusCode()));
            }
            
            if (response.statusCode() != 200) {
                String errorBody = "";
                try {
                    // Intentar leer el cuerpo de la respuesta de error
                    byte[] errorBytes = response.body().readAllBytes();
                    errorBody = new String(errorBytes);
                    LOGGER.severe(String.format("❌ [PERIFERICO→BACKEND] Error response body: %s", errorBody));
                } catch (Exception ex) {
                    LOGGER.warning("No se pudo leer el cuerpo de la respuesta de error");
                }
                throw new Exception("Error al obtener documento desde nodo periférico. Status: " + response.statusCode() + 
                        (errorBody.isEmpty() ? "" : " - Body: " + errorBody));
            }
            
            LOGGER.info("✅ [PERIFERICO→BACKEND] Stream recibido exitosamente del componente periférico");
            return response.body();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("❌ [BACKEND→PERIFERICO] Error al comunicarse con nodo periférico: %s", uriInterna), e);
            throw new Exception("No se pudo obtener el documento desde el nodo periférico: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera un token JWT de servicio para autenticación entre servicios.
     * Usa el mismo secret y algoritmo que el componente periférico.
     */
    private String generarTokenServicio() {
        try {
            // Usar el mismo secret que el componente periférico (HCEN_SERVICE_SECRET o DEFAULT)
            String secret = System.getenv("HCEN_SERVICE_SECRET");
            if (secret == null || secret.isBlank()) {
                secret = System.getProperty("hcen.service.secret");
            }
            if (secret == null || secret.isBlank()) {
                secret = "TSE_2025_HCEN_SERVICE_SECRET_KEY"; // DEFAULT_SERVICE_SECRET del componente periférico
            }
            
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            long now = java.time.Instant.now().getEpochSecond();
            long exp = now + (24 * 60 * 60); // 24 horas
            
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String encodedHeader = base64UrlEncode(header);
            
            String payload = String.format(
                "{\"sub\":\"hcen-backend\",\"serviceName\":\"HCEN Backend\",\"iat\":%d,\"exp\":%d,\"iss\":\"HCEN-Service\"}",
                now, exp
            );
            String encodedPayload = base64UrlEncode(payload);
            
            String dataToSign = encodedHeader + "." + encodedPayload;
            byte[] signatureBytes = mac.doFinal(dataToSign.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String signature = base64UrlEncodeBytes(signatureBytes);
            
            return dataToSign + "." + signature;
            
        } catch (Exception e) {
            LOGGER.warning("Error generando token de servicio: " + e.getMessage());
            return null; // Continuar sin token si falla
        }
    }
    
    private String base64UrlEncode(String data) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    
    private String base64UrlEncodeBytes(byte[] bytes) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(bytes);
    }
    
    /**
     * Convierte una URI externa a una URI interna de Docker solo si ambos servicios
     * están en la misma red Docker. En producción (servicios separados), no convierte.
     * 
     * La conversión se puede configurar mediante variables de entorno:
     * - PERIPHERAL_SERVICE_NAME: nombre del servicio Docker (default: hcen-wildfly-app)
     * - PERIPHERAL_SERVICE_PORT: puerto interno del servicio (default: 8080)
     * - CONVERT_URI_TO_DOCKER: "true" para habilitar conversión, "false" o no definida para usar URL externa
     */
    private String convertirUriParaDocker(String uri) {
        if (uri == null || uri.isEmpty()) {
            return uri;
        }
        
        // Verificar si debemos convertir a URL Docker interna
        // Solo convertir si ambos servicios están en la misma red Docker
        String convertToDocker = System.getenv().getOrDefault("CONVERT_URI_TO_DOCKER", "false");
        if (!"true".equalsIgnoreCase(convertToDocker)) {
            // No convertir: usar la URL externa directamente (producción)
            LOGGER.fine("Conversión a Docker deshabilitada, usando URL externa: " + uri);
            return uri;
        }
        
        // Obtener configuración desde variables de entorno
        String serviceName = System.getenv().getOrDefault("PERIPHERAL_SERVICE_NAME", "hcen-wildfly-app");
        String servicePort = System.getenv().getOrDefault("PERIPHERAL_SERVICE_PORT", "8080");
        
        // Obtener URL base del componente periférico desde variable de entorno
        String peripheralBaseUrl = System.getenv().getOrDefault("PERIPHERAL_NODE_URL", "http://localhost:8081");
        String peripheralHost = peripheralBaseUrl.replace("http://", "").replace("https://", "");
        
        // Reemplazar la URL externa del componente periférico con nombre del servicio:puerto interno (Docker)
        if (uri.contains(peripheralHost)) {
            String uriInterna = uri.replace(peripheralHost, serviceName + ":" + servicePort);
            LOGGER.info(String.format("URI convertida para acceso interno Docker: %s -> %s", uri, uriInterna));
            return uriInterna;
        }
        
        return uri;
    }
    
    private String getContentType(String formato) {
        return switch (formato.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "dcm" -> "application/dicom";
            case "hl7" -> "application/hl7-v2+er7";
            default -> "application/octet-stream";
        };
    }
    
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "documento";
        }
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    /**
     * Clase interna para encapsular información de descarga.
     */
    public static class DocumentoDescarga {
        private final InputStream stream;
        private final String contentType;
        private final String fileName;
        
        public DocumentoDescarga(InputStream stream, String contentType, String fileName) {
            this.stream = stream;
            this.contentType = contentType;
            this.fileName = fileName;
        }
        
        public InputStream getStream() { return stream; }
        public String getContentType() { return contentType; }
        public String getFileName() { return fileName; }
    }
    
    /**
     * Verifica permisos de acceso llamando al servicio de políticas vía HTTP.
     * El servicio de políticas está en un WAR separado (hcen-politicas-service).
     */
    private boolean verificarPermisoPoliticas(String profesionalId, String codDocumPaciente, 
                                               String tipoDocumento, String tenantId, String especialidad) {
        try {
            // Construir URL del servicio de políticas
            String politicasUrl = uy.edu.tse.hcen.utils.PoliticasServiceUrlUtil.getBaseUrl();
            LOGGER.info("Verificando políticas con URL base: " + politicasUrl);
            
            String url = politicasUrl + "/politicas/verificar" +
                    "?profesionalId=" + java.net.URLEncoder.encode(profesionalId, java.nio.charset.StandardCharsets.UTF_8) +
                    "&pacienteCI=" + java.net.URLEncoder.encode(codDocumPaciente, java.nio.charset.StandardCharsets.UTF_8);
            
            if (tipoDocumento != null && !tipoDocumento.isBlank()) {
                url += "&tipoDoc=" + java.net.URLEncoder.encode(tipoDocumento, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            if (tenantId != null && !tenantId.isBlank()) {
                url += "&tenantId=" + java.net.URLEncoder.encode(tenantId, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            if (especialidad != null && !especialidad.isBlank()) {
                url += "&especialidad=" + java.net.URLEncoder.encode(especialidad, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            Client client = ClientBuilder.newClient();
            try {
                Response response = client.target(url)
                        .request(MediaType.APPLICATION_JSON)
                        .get();
                
                if (response.getStatus() == 200) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> result = response.readEntity(java.util.Map.class);
                    Boolean tienePermiso = (Boolean) result.get("tienePermiso");
                    return tienePermiso != null && tienePermiso;
                } else {
                    LOGGER.warning(String.format("Error al verificar permiso - Status: %d", response.getStatus()));
                    return false; // Fail-secure: denegar acceso en caso de error
                }
            } finally {
                client.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("Error al consultar servicio de políticas: %s", e.getMessage()), e);
            return false; // Fail-secure: denegar acceso en caso de error
        }
    }
}

