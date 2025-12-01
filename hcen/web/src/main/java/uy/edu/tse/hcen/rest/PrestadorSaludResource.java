package uy.edu.tse.hcen.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import uy.edu.tse.hcen.model.PrestadorSalud;
import uy.edu.tse.hcen.rest.dto.PrestadorSaludDTO;
import uy.edu.tse.hcen.service.PrestadorSaludService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoint REST para gestionar Prestadores de Salud.
 */
@Path("/prestadores-salud")
@Stateless
public class PrestadorSaludResource {
    
    private static final Logger LOGGER = Logger.getLogger(PrestadorSaludResource.class);
    
    @Inject
    private PrestadorSaludService service;
    
    /**
     * Crea una invitación para un nuevo prestador de salud.
     * Solo requiere nombre y email. El prestador completará el resto en el formulario de registro.
     */
    @POST
    @Path("/invitar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response invitarPrestador(Map<String, String> datos) {
        
        String nombre = datos.get("nombre");
        String contacto = datos.get("contacto");
        
        LOGGER.info("POST /prestadores-salud/invitar - Nombre: " + nombre + ", Email: " + contacto);
        
        try {
            // Validaciones
            if (nombre == null || nombre.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El nombre es requerido"))
                    .build();
            }
            
            if (contacto == null || contacto.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El email de contacto es requerido"))
                    .build();
            }
            
            // Crear invitación
            PrestadorSalud prestador = service.crearInvitacion(nombre, contacto);
            
            // Respuesta con datos de la invitación
            Map<String, Object> response = new HashMap<>();
            response.put("id", prestador.getId());
            response.put("nombre", prestador.getNombre());
            response.put("contacto", prestador.getContacto());
            response.put("invitationUrl", prestador.getInvitationUrl());
            response.put("estado", prestador.getEstado().name());
            response.put("message", "Invitación enviada exitosamente");
            
            return Response.status(Response.Status.CREATED).entity(response).build();
            
        } catch (Exception e) {
            LOGGER.error("Error al crear invitación: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al crear invitación: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Completa el registro de un prestador con todos sus datos.
     */
    @POST
    @Path("/completar-registro")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response completarRegistro(Map<String, String> datos) {
        
        String token = datos.get("token");
        
        LOGGER.info("POST /prestadores-salud/completar-registro - Token: " + token);
        
        try {
            // Completar registro
            PrestadorSalud prestador = service.completarRegistro(
                token,
                datos.get("rut"),
                datos.get("url"),
                datos.get("departamento"),
                datos.get("localidad"),
                datos.get("direccion"),
                datos.get("telefono")
            );
            
            if (prestador == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "No se pudo completar el registro"))
                    .build();
            }
            
            // Respuesta con API Key (solo se muestra una vez al completar registro)
            Map<String, Object> response = new HashMap<>();
            response.put("id", prestador.getId());
            response.put("nombre", prestador.getNombre());
            response.put("rut", prestador.getRut());
            response.put("url", prestador.getUrl());
            response.put("estado", prestador.getEstado().name());
            response.put("apiKey", prestador.getApiKey());
            response.put("message", "Registro completado exitosamente. Guarde su API Key de forma segura.");
            
            LOGGER.info("Registro completado - Prestador ID: " + prestador.getId() + ", API Key generada");
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error de validación: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Error al completar registro: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al completar registro: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Obtiene todos los prestadores de salud.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarPrestadores() {
        
        LOGGER.info("GET /prestadores-salud - Listar todos");
        
        try {
            List<PrestadorSalud> prestadores = service.listarTodos();
            
            List<PrestadorSaludDTO> dtos = prestadores.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
            
            return Response.ok(dtos).build();
            
        } catch (Exception e) {
            LOGGER.error("Error al listar prestadores: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al listar prestadores"))
                .build();
        }
    }
    
    /**
     * Obtiene un prestador por ID.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPrestador(@PathParam("id") Long id) {
        
        LOGGER.info("GET /prestadores-salud/" + id);
        
        try {
            PrestadorSalud prestador = service.obtenerPorId(id);
            
            if (prestador == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Prestador no encontrado"))
                    .build();
            }
            
            return Response.ok(entityToDto(prestador)).build();
            
        } catch (Exception e) {
            LOGGER.error("Error al obtener prestador: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al obtener prestador"))
                .build();
        }
    }
    
    // Helper para convertir entidad a DTO
    private PrestadorSaludDTO entityToDto(PrestadorSalud entity) {
        PrestadorSaludDTO dto = new PrestadorSaludDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setRut(entity.getRut());
        dto.setContacto(entity.getContacto());
        dto.setUrl(entity.getUrl());
        dto.setDepartamento(entity.getDepartamento() != null ? entity.getDepartamento().name() : null);
        dto.setLocalidad(entity.getLocalidad());
        dto.setDireccion(entity.getDireccion());
        dto.setTelefono(entity.getTelefono());
        dto.setEstado(entity.getEstado().name());
        dto.setInvitationUrl(entity.getInvitationUrl());
        // NO incluir API Key en listados por seguridad
        return dto;
    }
}

