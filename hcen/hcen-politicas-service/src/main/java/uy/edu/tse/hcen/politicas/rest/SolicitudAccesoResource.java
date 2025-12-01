package uy.edu.tse.hcen.politicas.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;
import uy.edu.tse.hcen.politicas.service.SolicitudAccesoService;
import uy.edu.tse.hcen.politicas.mapper.SolicitudAccesoMapper;
import uy.edu.tse.hcen.dto.SolicitudAccesoDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/solicitudes")
@Produces(MediaType.APPLICATION_JSON)
public class SolicitudAccesoResource {

    @Inject
    private SolicitudAccesoService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearSolicitud(Map<String, Object> body) {
        try {
            if (body == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Body is required"))
                        .build();
            }


            String solicitanteId = (String) body.get("solicitanteId");
            String especialidad = (String) body.get("especialidad");
            String codDocumPaciente = (String) body.get("codDocumPaciente");
            String tipoDocumento = (String) body.get("tipoDocumento");
            String documentoId = (String) body.get("documentoId");
            String razonSolicitud = (String) body.get("razonSolicitud");
            String tenantId = (String) body.get("tenantId"); // ID de la clínica del profesional

            if (solicitanteId == null || codDocumPaciente == null || solicitanteId.isEmpty() || codDocumPaciente.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "solicitanteId y codDocumPaciente son requeridos"))
                        .build();
            }

            SolicitudAcceso solicitud = service.crearSolicitud(
                solicitanteId, especialidad, codDocumPaciente,
                tipoDocumento, documentoId, razonSolicitud, tenantId
            );

            return Response.status(Response.Status.CREATED)
                    .entity(SolicitudAccesoMapper.toDTO(solicitud))
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
        SolicitudAcceso solicitud = service.obtenerPorId(id);
        if (solicitud == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Solicitud no encontrada"))
                    .build();
        }
        return Response.ok(SolicitudAccesoMapper.toDTO(solicitud)).build();
    }

    @GET
    @Path("/pendientes")
    public Response listarPendientes() {
        List<SolicitudAcceso> solicitudes = service.listarPendientes();
        List<SolicitudAccesoDTO> dtos = solicitudes.stream()
                .map(SolicitudAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/paciente/{ci}")
    public Response listarPorPaciente(@PathParam("ci") String ci) {
        if (ci == null || ci.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "ci is required"))
                    .build();
        }
        List<SolicitudAcceso> solicitudes = service.listarPorPaciente(ci);
        List<SolicitudAccesoDTO> dtos = solicitudes.stream()
                .map(SolicitudAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/paciente/{ci}/pendientes")
    public Response listarPendientesPorPaciente(@PathParam("ci") String ci) {
        if (ci == null || ci.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "ci is required"))
                    .build();
        }
        List<SolicitudAcceso> solicitudes = service.listarPendientesPorPaciente(ci);
        List<SolicitudAccesoDTO> dtos = solicitudes.stream()
                .map(SolicitudAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/profesional/{profesionalId}")
    public Response listarPorProfesional(@PathParam("profesionalId") String profesionalId) {
        if (profesionalId == null || profesionalId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "profesionalId is required"))
                    .build();
        }
        List<SolicitudAcceso> solicitudes = service.listarPorProfesional(profesionalId);
        List<SolicitudAccesoDTO> dtos = solicitudes.stream()
                .map(SolicitudAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @POST
    @Path("/{id}/aprobar")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
    public Response aprobarSolicitud(@PathParam("id") Long id, Map<String, String> body) {
        try {
            if (id == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "id is required"))
                        .build();
            }
            if (body == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Body is required"))
                        .build();
            }

            String resueltoPor = body.get("resueltoPor");
            String comentario = body.get("comentario");
            
            SolicitudAcceso solicitud = service.aprobarSolicitud(id, resueltoPor, comentario);
            return Response.ok(SolicitudAccesoMapper.toDTO(solicitud)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/rechazar")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
    public Response rechazarSolicitud(@PathParam("id") Long id, Map<String, String> body) {
        try {
            if (id == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "id is required"))
                        .build();
            }
            if (body == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Body is required"))
                        .build();
            }
            String resueltoPor = body.get("resueltoPor");
            String comentario = body.get("comentario");
            
            SolicitudAcceso solicitud = service.rechazarSolicitud(id, resueltoPor, comentario);
            return Response.ok(SolicitudAccesoMapper.toDTO(solicitud)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}








