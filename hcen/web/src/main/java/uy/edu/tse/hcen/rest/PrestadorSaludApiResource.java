package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.jboss.logging.Logger;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.dto.DTMetadatos;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.rest.dto.UsuarioSaludDTO;
import uy.edu.tse.hcen.filter.PrestadorSaludApiSecured;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.UserClinicAssociation;
import uy.edu.tse.hcen.repository.UserClinicAssociationRepository;
import uy.edu.tse.hcen.rest.dto.HcenUserResponse;
import uy.edu.tse.hcen.rndc.service.DocumentoRndcService;
import uy.edu.tse.hcen.service.PrestadorSaludService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * API REST para prestadores de salud activos.
 * 
 * Este recurso expone servicios protegidos que permiten a los prestadores:
 * 1. Dar de alta usuarios en el INUS
 * 2. Registrar metadatos de documentos clínicos en el RNDC
 * 3. Recuperar lista de documentos clínicos asociados a un usuario
 * 4. Recuperar documentos clínicos específicos alojados en otros prestadores
 * 
 * Todas las peticiones requieren:
 * - Header X-API-Key con la API key del prestador
 * - Origen de la petición debe coincidir con la URL registrada del prestador
 */
@Path("/prestador-salud/services")
@PrestadorSaludApiSecured
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PrestadorSaludApiResource {
    
    private static final Logger LOGGER = Logger.getLogger(PrestadorSaludApiResource.class);
    
    @EJB
    private PrestadorSaludService prestadorService;
    
    @EJB
    private DocumentoRndcService documentoRndcService;
    
    @EJB
    private UserDAO userDAO;
    
    @EJB
    private UserClinicAssociationRepository associationRepo;

    @EJB
    private uy.edu.tse.hcen.service.InusIntegrationService inusService;
    
    /**
     * Obtiene el ID del prestador autenticado desde el contexto de la petición.
     * El filtro PrestadorSaludApiFilter establece este valor como atributo en HttpServletRequest.
     */
    private Long getPrestadorId(HttpServletRequest request) {
        Object idObj = request.getAttribute("prestadorId");
        if (idObj instanceof Long) {
            return (Long) idObj;
        }
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        LOGGER.warn("No se encontró prestadorId en HttpServletRequest. El filtro puede no haberse ejecutado correctamente.");
        return null;
    }
    
    /**
     * Obtiene el nombre del prestador autenticado desde el contexto de la petición.
     * El filtro PrestadorSaludApiFilter establece este valor como atributo en HttpServletRequest.
     */
    private String getPrestadorNombre(HttpServletRequest request) {
        Object nombreObj = request.getAttribute("prestadorNombre");
        if (nombreObj instanceof String) {
            return (String) nombreObj;
        }
        LOGGER.warn("No se encontró prestadorNombre en HttpServletRequest. El filtro puede no haberse ejecutado correctamente.");
        return null;
    }
    
    /**
     * POST /api/prestador-salud/services/usuarios-salud
     * 
     * Da de alta un usuario en el INUS (Índice Nacional de Usuarios de Salud).
     * Reutiliza el endpoint existente de UsuarioSaludResource.
     */
    @POST
    @Path("/usuarios-salud")
    public Response crearUsuarioSalud(UsuarioSaludDTO dto, @Context HttpServletRequest request) {
        Long prestadorId = getPrestadorId(request);
        String prestadorNombre = getPrestadorNombre(request);
        
        LOGGER.info(String.format(
            "Prestador %s (ID: %d) solicitando alta de usuario - CI: %s",
            prestadorNombre, prestadorId, dto != null ? dto.getCi() : "null"
        ));
        
        try {
            // Validaciones básicas
            if (dto == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Datos del usuario son requeridos"))
                    .build();
            }
            
            if (dto.getCi() == null || dto.getCi().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "CI es requerido"))
                    .build();
            }
            
            // Establecer el prestador como tenantId (para asociación)
            if (dto.getTenantId() == null) {
                dto.setTenantId(prestadorId);
            }
            
            // -----------------------------------------------------
            // 1. INTEGRACIÓN CON INUS (Master Data)
            // -----------------------------------------------------
            
            // Preparar datos recibidos para enviar a INUS
            User datosEntrada = new User();
            datosEntrada.setTipDocum("CI");
            datosEntrada.setCodDocum(dto.getCi());
            datosEntrada.setPrimerNombre(dto.getNombre());
            datosEntrada.setPrimerApellido(dto.getApellido());
            if (dto.getFechaNacimiento() != null) {
                 datosEntrada.setFechaNacimiento(Date.from(dto.getFechaNacimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            datosEntrada.setDireccion(dto.getDireccion());
            datosEntrada.setTelefono(dto.getTelefono());
            datosEntrada.setEmail(dto.getEmail());
            datosEntrada.setDepartamento(dto.getDepartamento());
            datosEntrada.setLocalidad(dto.getLocalidad());
            datosEntrada.setNacionalidad(uy.edu.tse.hcen.model.Nacionalidad.UY); 
            datosEntrada.setRol(Rol.USUARIO_SALUD);

            // Buscar en INUS
            String generatedUid = uy.edu.tse.hcen.utils.UserUuidUtil.generateUuid(dto.getCi());
            User inusUser = inusService.obtenerUsuarioPorUid(generatedUid);
            
            if (inusUser == null) {
                inusUser = inusService.obtenerUsuarioPorDocumento("CI", dto.getCi());
            }
            
            String finalUid;

            if (inusUser == null) {
                // Crear en INUS
                finalUid = generatedUid;
                datosEntrada.setUid(finalUid);
                boolean creado = inusService.crearUsuarioEnInus(datosEntrada);
                if (!creado) {
                    LOGGER.warn("No se pudo crear el usuario en INUS.");
                }
            } else {
                // Actualizar en INUS con datos del prestador
                finalUid = inusUser.getUid();
                datosEntrada.setUid(finalUid);
                // Aquí podríamos hacer un merge más inteligente, pero por ahora actualizamos
                inusService.actualizarUsuarioEnInus(datosEntrada);
            }

            // -----------------------------------------------------
            // 2. ACTUALIZACIÓN LOCAL (Réplica/Caché)
            // -----------------------------------------------------
            
            // Buscar usuario local por documento (CI)
            User user = userDAO.findByDocumento(dto.getCi());
            boolean isNewUser = (user == null);
            
            if (isNewUser) {
                // Crear nuevo User local
                user = new User();
                user.setUid(finalUid);
                user.setRol(Rol.USUARIO_SALUD);
                user.setCodDocum(dto.getCi());
                user.setTipDocum("CI");
                user.setPrimerNombre(dto.getNombre());
                user.setPrimerApellido(dto.getApellido());
                if (dto.getFechaNacimiento() != null) {
                    user.setFechaNacimiento(Date.from(dto.getFechaNacimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                user.setDireccion(dto.getDireccion());
                user.setTelefono(dto.getTelefono());
                user.setEmail(dto.getEmail());
                user.setDepartamento(dto.getDepartamento());
                user.setLocalidad(dto.getLocalidad());
                user.setProfileCompleted(true);
                
                userDAO.persist(user);
            } else {
                // Actualizar localmente
                if (!finalUid.equals(user.getUid())) user.setUid(finalUid);
                
                if (dto.getNombre() != null) user.setPrimerNombre(dto.getNombre());
                if (dto.getApellido() != null) user.setPrimerApellido(dto.getApellido());
                if (dto.getFechaNacimiento() != null) user.setFechaNacimiento(Date.from(dto.getFechaNacimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                if (dto.getDireccion() != null) user.setDireccion(dto.getDireccion());
                if (dto.getTelefono() != null) user.setTelefono(dto.getTelefono());
                if (dto.getEmail() != null) user.setEmail(dto.getEmail());
                if (dto.getDepartamento() != null) user.setDepartamento(dto.getDepartamento());
                if (dto.getLocalidad() != null) user.setLocalidad(dto.getLocalidad());
                
                userDAO.merge(user);
            }
            
            // Replicar asociación en INUS
            inusService.asociarUsuarioConPrestador(finalUid, prestadorId, null);
            
            // Verificar asociación User-Prestador
            UserClinicAssociation association = associationRepo.findByUserAndClinic(
                user.getId(), 
                prestadorId
            );
            
            if (association == null) {
                association = new UserClinicAssociation();
                association.setUserId(user.getId());
                association.setClinicTenantId(prestadorId);
                association.setFechaAlta(LocalDateTime.now());
                associationRepo.persist(association);
            }
            
            String mensaje = isNewUser ? 
                "Usuario creado y asociado correctamente" : 
                "Usuario actualizado y asociado correctamente";
            
            return Response.ok(new HcenUserResponse(user.getId(), mensaje)).build();
            
        } catch (Exception e) {
            LOGGER.error("Error al crear usuario de salud: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * POST /api/prestador-salud/services/metadatos-documento
     * 
     * Registra metadatos de un documento clínico en el RNDC.
     * Reutiliza el endpoint existente de MetadatosDocumentoResource.
     */
    @POST
    @Path("/metadatos-documento")
    public Response registrarMetadatosDocumento(DTMetadatos dtoMetadata, @Context HttpServletRequest request) {
        Long prestadorId = getPrestadorId(request);
        String prestadorNombre = getPrestadorNombre(request);
        
        LOGGER.info(String.format(
            "Prestador %s (ID: %d) registrando metadatos de documento - CI: %s",
            prestadorNombre, prestadorId, 
            dtoMetadata != null ? dtoMetadata.getDocumentoIdPaciente() : "null"
        ));
        
        try {
            // Validaciones básicas
            if (dtoMetadata == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Metadata es requerida"))
                    .build();
            }
            
            if (dtoMetadata.getDocumentoIdPaciente() == null || dtoMetadata.getDocumentoIdPaciente().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "CI del paciente es requerido"))
                    .build();
            }
            
            // Establecer el prestador como origen del documento
            // Esto puede ser útil para auditoría
            if (dtoMetadata.getTenantId() == null) {
                dtoMetadata.setTenantId(String.valueOf(prestadorId));
            }
            
            // Usar el servicio RNDC existente
            // Convertir DTMetadatos a parámetros para el servicio
            String codDocum = dtoMetadata.getDocumentoIdPaciente();
            String nombrePaciente = null;
            String apellidoPaciente = null;
            
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
                fechaCreacion = dtoMetadata.getFechaCreacion().toLocalDate().toString();
            } else if (dtoMetadata.getFechaRegistro() != null) {
                fechaCreacion = dtoMetadata.getFechaRegistro().toLocalDate().toString();
            }
            String formatoDocumento = dtoMetadata.getFormato() != null ? dtoMetadata.getFormato().replace("application/", "").toUpperCase() : "PDF";
            String uriDocumento = dtoMetadata.getUrlAcceso();
            String clinicaOrigen = prestadorNombre;
            
            Long documentoId = documentoRndcService.crearDocumentoDesdeParametros(
                codDocum,
                nombrePaciente,
                apellidoPaciente,
                tipoDocumento,
                fechaCreacion,
                formatoDocumento,
                uriDocumento,
                clinicaOrigen,
                Long.valueOf(prestadorId),
                null, // profesionalSalud
                null, // descripcion
                true  // accesoPermitido (por defecto permitido para prestadores)
            );
            
            MetadataDocumentoDTO metadata = documentoRndcService.obtenerDocumentoPorId(documentoId);
            
            return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                    "id", documentoId,
                    "message", "Metadatos registrados exitosamente",
                    "metadata", metadata
                ))
                .build();
            
        } catch (Exception e) {
            LOGGER.error("Error al registrar metadatos: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * GET /api/prestador-salud/services/documentos/paciente/{ci}
     * 
     * Recupera la lista de documentos clínicos asociados a un usuario (por CI).
     */
    @GET
    @Path("/documentos/paciente/{ci}")
    public Response listarDocumentosPorPaciente(@PathParam("ci") String ci, @Context HttpServletRequest request) {
        Long prestadorId = getPrestadorId(request);
        String prestadorNombre = getPrestadorNombre(request);
        
        LOGGER.info(String.format(
            "Prestador %s (ID: %d) consultando documentos de paciente - CI: %s",
            prestadorNombre, prestadorId, ci
        ));
        
        try {
            if (ci == null || ci.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "CI del paciente es requerido"))
                    .build();
            }
            
            // Usar el servicio RNDC existente
            List<MetadataDocumentoDTO> documentos = documentoRndcService.buscarDocumentosPorCI(
                ci,
                null, // profesionalId (no aplica para prestadores)
                String.valueOf(prestadorId), // tenantId como String
                null  // especialidad
            );
            
            return Response.ok(Map.of(
                "ci", ci,
                "total", documentos.size(),
                "documentos", documentos
            )).build();
            
        } catch (Exception e) {
            LOGGER.error("Error al listar documentos: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * GET /api/prestador-salud/services/documentos/{id}
     * 
     * Recupera los metadatos de un documento clínico específico.
     */
    @GET
    @Path("/documentos/{id}")
    public Response obtenerDocumento(@PathParam("id") Long id, @Context HttpServletRequest request) {
        Long prestadorId = getPrestadorId(request);
        String prestadorNombre = getPrestadorNombre(request);
        
        LOGGER.info(String.format(
            "Prestador %s (ID: %d) consultando documento - ID: %d",
            prestadorNombre, prestadorId, id
        ));
        
        try {
            if (id == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "ID del documento es requerido"))
                    .build();
            }
            
            MetadataDocumentoDTO metadata = documentoRndcService.obtenerDocumentoPorId(id);
            
            if (metadata == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Documento no encontrado"))
                    .build();
            }
            
            return Response.ok(metadata).build();
            
        } catch (Exception e) {
            LOGGER.error("Error al obtener documento: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * GET /api/prestador-salud/services/documentos/{id}/descargar
     * 
     * Recupera un documento clínico específico alojado en otro prestador.
     * El documento se descarga desde la URI almacenada en los metadatos.
     */
    @GET
    @Path("/documentos/{id}/descargar")
    public Response descargarDocumento(@PathParam("id") Long id, @Context HttpServletRequest request) {
        Long prestadorId = getPrestadorId(request);
        String prestadorNombre = getPrestadorNombre(request);
        
        LOGGER.info(String.format(
            "Prestador %s (ID: %d) solicitando descarga de documento - ID: %d",
            prestadorNombre, prestadorId, id
        ));
        
        try {
            if (id == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "ID del documento es requerido"))
                    .build();
            }
            
            // Obtener metadatos
            MetadataDocumentoDTO metadata = documentoRndcService.obtenerDocumentoPorId(id);
            
            if (metadata == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Documento no encontrado"))
                    .build();
            }
            
            // Preparar descarga usando el servicio existente
            DocumentoRndcService.DocumentoDescarga descarga = 
                documentoRndcService.prepararDescargaDocumento(id);
            
            if (descarga == null || descarga.getStream() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Documento no disponible para descarga"))
                    .build();
            }
            
            // Retornar el documento como stream
            return Response.ok(descarga.getStream(), descarga.getContentType())
                .header("Content-Disposition", "attachment; filename=\"" + descarga.getFileName() + "\"")
                .build();
            
        } catch (Exception e) {
            LOGGER.error("Error al descargar documento: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno: " + e.getMessage()))
                .build();
        }
    }
}

