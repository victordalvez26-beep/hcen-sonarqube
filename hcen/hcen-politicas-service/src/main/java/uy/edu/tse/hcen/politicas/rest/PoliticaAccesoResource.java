package uy.edu.tse.hcen.politicas.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;
import uy.edu.tse.hcen.politicas.service.PoliticaAccesoService;
import uy.edu.tse.hcen.politicas.mapper.PoliticaAccesoMapper;
import uy.edu.tse.hcen.dto.PoliticaAccesoDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/politicas")
@Produces(MediaType.APPLICATION_JSON)
public class PoliticaAccesoResource {

    @Inject
    private PoliticaAccesoService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearPolitica(PoliticaAccesoDTO dto) {
        try {
            PoliticaAcceso politica = PoliticaAccesoMapper.toEntity(dto);
            PoliticaAcceso creada = service.crearPolitica(politica);
            return Response.status(Response.Status.CREATED)
                    .entity(PoliticaAccesoMapper.toDTO(creada))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id) {
        PoliticaAcceso politica = service.obtenerPorId(id);
        if (politica == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Política no encontrada"))
                    .build();
        }
        return Response.ok(PoliticaAccesoMapper.toDTO(politica)).build();
    }

    @GET
    @Path("/paciente/{ci}")
    public Response listarPorPaciente(@PathParam("ci") String ci, @QueryParam("tenantId") String tenantId) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PoliticaAccesoResource.class.getName());
        logger.info("[POLITICAS-REST] GET /politicas/paciente/{ci} - CI recibido: '" + ci + "', tenantId: " + tenantId);
        
        List<PoliticaAcceso> politicas = service.listarPorPaciente(ci);
        logger.info("[POLITICAS-REST] Políticas encontradas por servicio: " + politicas.size());
        
        if (!politicas.isEmpty()) {
            for (PoliticaAcceso p : politicas) {
                logger.info("  Política encontrada - ID: " + p.getId() + 
                    ", Paciente CI: " + p.getCodDocumPaciente() + 
                    ", Clínica: " + p.getClinicaAutorizada() +
                    ", Activa: " + p.getActiva());
            }
        }
        
        if (tenantId != null && !tenantId.isEmpty()) {
            logger.info("[POLITICAS-REST] Filtrando por tenantId: " + tenantId);
            int antes = politicas.size();
            politicas = politicas.stream()
                .filter(p -> tenantId.equals(p.getClinicaAutorizada()))
                .collect(Collectors.toList());
            logger.info("[POLITICAS-REST] Políticas después del filtro por tenantId: " + politicas.size() + " (antes: " + antes + ")");
        }
        
        List<PoliticaAccesoDTO> dtos = politicas.stream()
                .map(PoliticaAccesoMapper::toDTO)
                .collect(Collectors.toList());
        
        logger.info("[POLITICAS-REST] DTOs creados: " + dtos.size());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/profesional/{profesionalId}")
    public Response listarPorProfesional(@PathParam("profesionalId") String profesionalId) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PoliticaAccesoResource.class.getName());
        logger.info("[REST] Endpoint /politicas/profesional/" + profesionalId + " llamado");
        logger.info("[REST] ProfesionalId recibido: '" + profesionalId + "'");
        
        List<PoliticaAcceso> politicas = service.listarPorProfesional(profesionalId);
        logger.info("[REST] Políticas encontradas por servicio: " + politicas.size());
        
        List<PoliticaAccesoDTO> dtos = politicas.stream()
                .map(PoliticaAccesoMapper::toDTO)
                .collect(Collectors.toList());
        
        logger.info("[REST] DTOs creados: " + dtos.size());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/verificar")
    public Response verificarPermiso(
            @QueryParam("profesionalId") String profesionalId,
            @QueryParam("pacienteCI") String pacienteCI,
            @QueryParam("tipoDoc") String tipoDoc,
            @QueryParam("tenantId") String tenantId,
            @QueryParam("especialidad") String especialidad) {
        
        java.util.logging.Logger.getLogger(PoliticaAccesoResource.class.getName())
            .info(String.format("[POLITICAS] Verificando Permiso - Profesional: '%s', Paciente: '%s', TipoDoc: '%s', Tenant: '%s', Especialidad: '%s'", 
                profesionalId, pacienteCI, tipoDoc, tenantId, especialidad));
                
        if (profesionalId == null || pacienteCI == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "profesionalId y pacienteCI son requeridos"))
                    .build();
        }
        boolean tienePermiso = service.verificarPermiso(profesionalId, pacienteCI, tipoDoc, tenantId, especialidad);
        return Response.ok(Map.of(
                "tienePermiso", tienePermiso,
                "profesionalId", profesionalId,
                "pacienteCI", pacienteCI,
                "tipoDocumento", tipoDoc != null ? tipoDoc : "todos"
        )).build();
    }

    @OPTIONS
    @Path("/verificar")
    public Response verificarPermisoOptions() {
        // Manejar preflight CORS para el endpoint de verificación
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminarPolitica(@PathParam("id") Long id) {
        try {
            service.eliminarPolitica(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/listar")
    public Response listarTodas() {
        List<PoliticaAcceso> politicas = service.listarTodas();
        List<PoliticaAccesoDTO> dtos = politicas.stream()
                .map(PoliticaAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }
}

