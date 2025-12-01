package uy.edu.tse.hcen.politicas.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;
import uy.edu.tse.hcen.politicas.service.RegistroAccesoService;
import uy.edu.tse.hcen.politicas.mapper.RegistroAccesoMapper;
import uy.edu.tse.hcen.dto.RegistroAccesoDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Path("/registros")
@Produces(MediaType.APPLICATION_JSON)
public class RegistroAccesoResource {

    @Inject
    private RegistroAccesoService service;

    @Context
    private HttpHeaders headers;

    /**
     * Helper method para obtener String de forma segura desde un JsonObject
     */
    private String getStringSafely(JsonObject obj, String key) {
        if (!obj.containsKey(key)) return null;
        jakarta.json.JsonValue value = obj.get(key);
        if (value == null || value.getValueType() == jakarta.json.JsonValue.ValueType.NULL) return null;
        try {
            if (value.getValueType() == jakarta.json.JsonValue.ValueType.STRING) {
                return ((jakarta.json.JsonString) value).getString();
            }
            // Si no es string, convertir a string y remover comillas si están presentes
            return value.toString().replaceAll("^\"|\"$", "");
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(RegistroAccesoResource.class.getName())
                .warning(String.format("Error obteniendo string para clave %s: %s", key, e.getMessage()));
            return null;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarAcceso(String jsonBody) {
        try {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegistroAccesoResource.class.getName());
            logger.info("RegistroAccesoResource.registrarAcceso - Recibida petición JSON: " + jsonBody);
            
            // Parsear JSON manualmente usando jakarta.json
            JsonObject jsonObject;
            try (JsonReader reader = Json.createReader(new java.io.StringReader(jsonBody))) {
                jsonObject = reader.readObject();
            }
            
            String profesionalId = getStringSafely(jsonObject, "profesionalId");
            String codDocumPaciente = getStringSafely(jsonObject, "codDocumPaciente");
            String documentoId = getStringSafely(jsonObject, "documentoId");
            String tipoDocumento = getStringSafely(jsonObject, "tipoDocumento");
            String ipAddress = getStringSafely(jsonObject, "ipAddress");
            String userAgent = getStringSafely(jsonObject, "userAgent");
            Boolean exito = jsonObject.containsKey("exito") && !jsonObject.isNull("exito") ? jsonObject.getBoolean("exito") : true;
            String motivoRechazo = getStringSafely(jsonObject, "motivoRechazo");
            String referencia = getStringSafely(jsonObject, "referencia");
            String clinicaId = getStringSafely(jsonObject, "clinicaId");
            String nombreProfesional = getStringSafely(jsonObject, "nombreProfesional");
            String especialidad = getStringSafely(jsonObject, "especialidad");

            logger.info(String.format("RegistroAccesoResource.registrarAcceso - Profesional: %s, Paciente: %s, Clínica: %s, Exito: %s", 
                    profesionalId, codDocumPaciente, clinicaId, exito));

            if (profesionalId == null || codDocumPaciente == null) {
                logger.warning("RegistroAccesoResource.registrarAcceso - Campos requeridos faltantes");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "profesionalId y codDocumPaciente son requeridos"))
                        .build();
            }

            // Obtener IP y User-Agent desde headers si no vienen en el body
            if (ipAddress == null) {
                ipAddress = headers.getHeaderString("X-Forwarded-For");
                if (ipAddress == null) {
                    ipAddress = headers.getHeaderString("X-Real-IP");
                }
            }
            if (userAgent == null) {
                userAgent = headers.getHeaderString("User-Agent");
            }

            logger.info("RegistroAccesoResource.registrarAcceso - Llamando a service.registrarAcceso");
            RegistroAcceso registro = service.registrarAcceso(
                profesionalId, codDocumPaciente, documentoId,
                tipoDocumento, ipAddress, userAgent,
                exito, motivoRechazo, referencia,
                clinicaId, nombreProfesional, especialidad
            );

            logger.info(String.format("RegistroAccesoResource.registrarAcceso - Registro creado con ID: %s", registro.getId()));

            return Response.status(Response.Status.CREATED)
                    .entity(RegistroAccesoMapper.toDTO(registro))
                    .build();
        } catch (Exception e) {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegistroAccesoResource.class.getName());
            logger.severe("RegistroAccesoResource.registrarAcceso - Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id) {
        try {
            RegistroAcceso registro = service.obtenerPorId(id);
            if (registro == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Registro no encontrado"))
                        .build();
            }
            return Response.ok(RegistroAccesoMapper.toDTO(registro)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/paciente/{ci}")
    public Response listarPorPaciente(@PathParam("ci") String ci) {
        List<RegistroAcceso> registros = service.listarPorPaciente(ci);
        List<RegistroAccesoDTO> dtos = registros.stream()
                .map(RegistroAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/profesional/{profesionalId}")
    public Response listarPorProfesional(@PathParam("profesionalId") String profesionalId) {
        List<RegistroAcceso> registros = service.listarPorProfesional(profesionalId);
        List<RegistroAccesoDTO> dtos = registros.stream()
                .map(RegistroAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/documento/{documentoId}")
    public Response listarPorDocumento(@PathParam("documentoId") String documentoId) {
        List<RegistroAcceso> registros = service.listarPorDocumento(documentoId);
        List<RegistroAccesoDTO> dtos = registros.stream()
                .map(RegistroAccesoMapper::toDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/rango-fechas")
    public Response listarPorRangoFechas(
            @QueryParam("fechaInicio") String fechaInicioStr,
            @QueryParam("fechaFin") String fechaFinStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaInicio = fechaInicioStr != null ? sdf.parse(fechaInicioStr) : null;
            Date fechaFin = fechaFinStr != null ? sdf.parse(fechaFinStr) : null;

            if (fechaInicio == null || fechaFin == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "fechaInicio y fechaFin son requeridos (formato: yyyy-MM-dd)"))
                        .build();
            }

            List<RegistroAcceso> registros = service.listarPorRangoFechas(fechaInicio, fechaFin);
            List<RegistroAccesoDTO> dtos = registros.stream()
                    .map(RegistroAccesoMapper::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(dtos).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Formato de fecha inválido. Use: yyyy-MM-dd"))
                    .build();
        }
    }

    @GET
    @Path("/paciente/{ci}/contar")
    public Response contarPorPaciente(@PathParam("ci") String ci) {
        Long cantidad = service.contarAccesosPorPaciente(ci);
        return Response.ok(Map.of("paciente", ci, "cantidadAccesos", cantidad)).build();
    }
}

