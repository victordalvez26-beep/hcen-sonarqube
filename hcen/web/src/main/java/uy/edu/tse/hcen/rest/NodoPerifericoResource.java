package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.model.Departamento;
import uy.edu.tse.hcen.model.EstadoNodoPeriferico;
import uy.edu.tse.hcen.model.NodoPeriferico;
import uy.edu.tse.hcen.repository.NodoPerifericoRepository;
import uy.edu.tse.hcen.rest.dto.NodoPerifericoConverter;
import uy.edu.tse.hcen.rest.dto.NodoPerifericoDTO;
import uy.edu.tse.hcen.service.NodoService;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// JMS resources removed to simplify to a plain CRUD implementation

@Path("/nodos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NodoPerifericoResource {

    private static final Logger logger = Logger.getLogger(NodoPerifericoResource.class.getName());

    @EJB
    private NodoPerifericoRepository repo;

    @EJB
    private NodoService nodoService;

    // Previously this resource enqueued JMS messages for integration. JMS
    // behavior has been removed to simplify the application to a CRUD-only flow.


    @GET
    public List<NodoPerifericoDTO> list() {
        return repo.findAll().stream().map(NodoPerifericoConverter::toDTO).collect(Collectors.toList());
    }

    @GET
    @Path("{rut}")
    public Response get(@PathParam("rut") String rut) {
        NodoPeriferico n = repo.findByRUT(rut);
        if (n == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(NodoPerifericoConverter.toDTO(n)).build();
    }

    /**
     * Crea una nueva clínica e inicializa automáticamente el tenant en el componente periférico.
     * 
     * Flujo:
     * 1. Valida datos y verifica que no exista RUT duplicado
     * 2. Persiste con estado PENDIENTE
     * 3. Llama al componente periférico vía HTTP (POST /config/init)
     * 4. Actualiza estado a ACTIVO (si exitoso) o ERROR_MENSAJERIA (si falla)
     * 5. Retorna el nodo creado con su estado actualizado
     */
    @POST
    public Response create(@Valid NodoPerifericoDTO nodoDto) {
        try {
            // validate enums
            validateEnumValues(nodoDto);
            // check duplicate RUT
            if (nodoDto.getRUT() != null && repo.findByRUT(nodoDto.getRUT()) != null) {
                return Response.status(Response.Status.CONFLICT).entity("Nodo with same RUT already exists").build();
            }
            
            NodoPeriferico nodo = NodoPerifericoConverter.toEntity(nodoDto);
            
            // Usar createAndNotify para inicializar el tenant automáticamente
            NodoPeriferico created = nodoService.createAndNotify(nodo);

            return Response.status(Response.Status.CREATED).entity(NodoPerifericoConverter.toDTO(created)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ConstraintViolationException e) {
            String msg = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (jakarta.persistence.PersistenceException pe) {
            // Attempt to detect unique constraint violations and return 409
            Throwable cause = pe.getCause();
            if (cause != null && (cause.getMessage() != null && cause.getMessage().toLowerCase().contains("unique") || cause instanceof java.sql.SQLIntegrityConstraintViolationException)) {
                return Response.status(Response.Status.CONFLICT).entity("Nodo with same RUT already exists").build();
            }
            // fallback: rethrow or return server error
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(pe.getMessage()).build();
        }
    }

    /**
     * Create a nodo and publish the "alta.clinica" message automatically.
     * This endpoint will attempt to notify RabbitMQ as part of the create operation.
     * If message publishing fails, the transaction will be rolled back and the
     * endpoint will return 500 to indicate the failure to notify.
     */
    @POST
    @Path("notify")
    public Response createAndNotify(@Valid NodoPerifericoDTO nodoDto) {
        try {
            validateEnumValues(nodoDto);
            NodoPeriferico nodo = NodoPerifericoConverter.toEntity(nodoDto);
            NodoPeriferico created = nodoService.createAndNotify(nodo);
            return Response.status(Response.Status.CREATED).entity(NodoPerifericoConverter.toDTO(created)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ConstraintViolationException e) {
            String msg = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (RuntimeException re) {
            // createAndNotify throws RuntimeException on publish failure (transaction rolled back)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(re.getMessage()).build();
        }
    }

    @PUT
    @Path("{rut}")
    public Response update(@PathParam("rut") String rut, @Valid NodoPerifericoDTO nodoDto) {
        NodoPeriferico existing = repo.findByRUT(rut);
        if (existing == null) return Response.status(Response.Status.NOT_FOUND).build();
        try {
            validateEnumValues(nodoDto);
            // if RUT is changing, ensure new RUT is not duplicate
            if (nodoDto.getRUT() != null && !nodoDto.getRUT().equals(existing.getRUT()) && repo.findByRUT(nodoDto.getRUT()) != null) {
                return Response.status(Response.Status.CONFLICT).entity("Nodo with same RUT already exists").build();
            }
            NodoPeriferico nodo = NodoPerifericoConverter.toEntity(nodoDto);
            // preserve internal id
            nodo.setId(existing.getId());
            NodoPeriferico updated = repo.update(nodo);
            return Response.ok(NodoPerifericoConverter.toDTO(updated)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ConstraintViolationException e) {
            String msg = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @DELETE
    @Path("{identifier}")
    public Response delete(@PathParam("identifier") String identifier) {
        NodoPeriferico existing = null;
        
        // Intentar buscar por ID primero (si es numérico)
        try {
            Long id = Long.parseLong(identifier);
            existing = repo.find(id);
        } catch (NumberFormatException e) {
            // Si no es numérico, buscar por RUT
            existing = repo.findByRUT(identifier);
        }
        
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Nodo no encontrado"))
                .build();
        }
        
        // En vez de eliminar, cambiar el estado a INACTIVO
        try {
            nodoService.updateEstado(existing.getId(), EstadoNodoPeriferico.INACTIVO);
            logger.info(String.format("Nodo %s (ID: %d) inhabilitado exitosamente", existing.getNombre(), existing.getId()));
            return Response.ok(Map.of(
                "message", "Nodo inhabilitado exitosamente",
                "id", existing.getId(),
                "nombre", existing.getNombre(),
                "estado", "INACTIVO"
            )).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al inhabilitar nodo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al inhabilitar nodo: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * PUT /api/nodos/{identifier}/activar
     * 
     * Reactiva una clínica que está inactiva, cambiando su estado a ACTIVO.
     */
    @PUT
    @Path("{identifier}/activar")
    public Response activar(@PathParam("identifier") String identifier) {
        NodoPeriferico existing = null;
        
        // Intentar buscar por ID primero (si es numérico)
        try {
            Long id = Long.parseLong(identifier);
            existing = repo.find(id);
        } catch (NumberFormatException e) {
            // Si no es numérico, buscar por RUT
            existing = repo.findByRUT(identifier);
        }
        
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Nodo no encontrado"))
                .build();
        }
        
        // Cambiar el estado a ACTIVO
        try {
            nodoService.updateEstado(existing.getId(), EstadoNodoPeriferico.ACTIVO);
            logger.info(String.format("Nodo %s (ID: %d) reactivado exitosamente", existing.getNombre(), existing.getId()));
            return Response.ok(Map.of(
                "message", "Nodo reactivado exitosamente",
                "id", existing.getId(),
                "nombre", existing.getNombre(),
                "estado", "ACTIVO"
            )).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al reactivar nodo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al reactivar nodo: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Endpoint para recibir notificación del periférico cuando una clínica completa su registro.
     * El periférico llama a este endpoint después de que la clínica complete el formulario de activación.
     * 
     * @param id ID de la clínica
     * @param completionData Datos completos de la clínica (RUT, dirección, username, etc.)
     * @return 200 OK si se actualizó correctamente
     */
    @POST
    @Path("{id}/complete-registration")
    public Response completeRegistration(@PathParam("id") Long id, Map<String, String> completionData) {
        try {
            NodoPeriferico existing = repo.find(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Clinic not found")
                        .build();
            }
            
            // Actualizar con los datos completos recibidos del periférico
            if (completionData.containsKey("rut")) {
                existing.setRUT(completionData.get("rut"));
            }
            if (completionData.containsKey("departamento")) {
                try {
                    existing.setDepartamento(Departamento.valueOf(completionData.get("departamento")));
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid departamento value: " + completionData.get("departamento"));
                }
            }
            if (completionData.containsKey("localidad")) {
                existing.setLocalidad(completionData.get("localidad"));
            }
            if (completionData.containsKey("direccion")) {
                existing.setDireccion(completionData.get("direccion"));
            }
            if (completionData.containsKey("adminNickname")) {
                existing.setAdminNickname(completionData.get("adminNickname"));
            }
            
            // Cambiar estado a ACTIVO
            existing.setEstado(EstadoNodoPeriferico.ACTIVO);
            
            repo.update(existing);
            
            logger.info(String.format("Clinic %d registration completed. RUT: %s, Admin: %s", 
                                     id, existing.getRUT(), existing.getAdminNickname()));
            
            return Response.ok()
                    .entity(Map.of("message", "Registration completed successfully", "status", "ACTIVO"))
                    .build();
                    
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error completing registration for clinic " + id, e);
            return Response.serverError()
                    .entity(Map.of("error", "Failed to complete registration", "details", e.getMessage()))
                    .build();
        }
    }

    /**
     * Update only the technical configuration (nodoPerifericoUrlBase) of a nodo.
     * This endpoint accepts a minimal JSON payload like { "nodoPerifericoUrlBase": "http://..." }
     * and updates only that field to avoid needing to send a full DTO (which may trigger unique constraint issues).
     */
    @PUT
    @Path("{rut}/config")
    public Response updateConfig(@PathParam("rut") String rut, NodoPerifericoDTO cfg) {
        NodoPeriferico existing = repo.findByRUT(rut);
        if (existing == null) return Response.status(Response.Status.NOT_FOUND).build();
        if (cfg == null || cfg.getNodoPerifericoUrlBase() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("nodoPerifericoUrlBase is required").build();
        }
        // validate length
        if (cfg.getNodoPerifericoUrlBase().length() > 255) {
            return Response.status(Response.Status.BAD_REQUEST).entity("nodoPerifericoUrlBase too long").build();
        }
    existing.setNodoPerifericoUrlBase(cfg.getNodoPerifericoUrlBase());
    NodoPeriferico updated = repo.update(existing);
        // After changing the technical configuration, trigger an update notification to the peripheral node.
        try {
            nodoService.notifyPeripheralNode(existing.getId(), "update");
        } catch (RuntimeException re) {
            // If notification fails, log and return 202 Accepted to indicate update applied but notification failed
            return Response.status(Response.Status.ACCEPTED).entity("Config updated but notification failed: " + re.getMessage()).build();
        }
        return Response.ok(NodoPerifericoConverter.toDTO(updated)).build();
    }

    /**
     * Trigger a notification (publish alta) for an existing nodo by id.
     * Useful to retry notification after correcting configuration.
     */
    @POST
    @Path("{rut}/notify")
    public Response notifyExisting(@PathParam("rut") String rut) {
        NodoPeriferico existing = repo.findByRUT(rut);
        if (existing == null) return Response.status(Response.Status.NOT_FOUND).build();
        try {
            nodoService.notifyPeripheralNode(existing.getId(), "init");
            return Response.ok().build();
        } catch (RuntimeException re) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(re.getMessage()).build();
        }
    }

    private void validateEnumValues(NodoPerifericoDTO dto) {
        if (dto.getDepartamento() != null) {
            try {
                Departamento.valueOf(dto.getDepartamento());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid departamento value: " + dto.getDepartamento());
            }
        }
        if (dto.getEstado() != null) {
            try {
                EstadoNodoPeriferico.valueOf(dto.getEstado());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid estado value: " + dto.getEstado());
            }
        }
    }
}