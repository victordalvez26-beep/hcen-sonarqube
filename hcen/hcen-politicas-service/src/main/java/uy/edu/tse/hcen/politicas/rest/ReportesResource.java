package uy.edu.tse.hcen.politicas.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;
import java.util.logging.Level;
import uy.edu.tse.hcen.politicas.service.ReportesService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Recurso REST para reportes y análisis agregados de políticas de acceso y registros de acceso.
 */
@Path("/reportes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportesResource {

    private static final Logger LOG = Logger.getLogger(ReportesResource.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @EJB
    private ReportesService reportesService;

    /**
     * GET /api/reportes/accesos/evolucion
     * 
     * Obtiene la evolución de accesos por día en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin (formato: yyyy-MM-dd)
     * @return Lista de estadísticas por día
     */
    @GET
    @Path("/accesos/evolucion")
    public Response obtenerEvolucionAccesos(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = parseDate(fechaInicio);
            Date fin = parseDate(fechaFin);
            
            if (inicio == null || fin == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "fechaInicio y fechaFin son requeridos (formato: yyyy-MM-dd)"))
                        .build();
            }
            
            List<Map<String, Object>> resultado = reportesService.obtenerEvolucionAccesos(inicio, fin);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo evolución de accesos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener evolución de accesos: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/accesos/profesional
     * 
     * Obtiene estadísticas de accesos agrupadas por profesional.
     */
    @GET
    @Path("/accesos/profesional")
    public Response obtenerAccesosPorProfesional(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = parseDate(fechaInicio);
            Date fin = parseDate(fechaFin);
            
            if (inicio == null || fin == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "fechaInicio y fechaFin son requeridos (formato: yyyy-MM-dd)"))
                        .build();
            }
            
            List<Map<String, Object>> resultado = reportesService.obtenerAccesosPorProfesional(inicio, fin);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo accesos por profesional", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener accesos por profesional: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/accesos/paciente
     * 
     * Obtiene estadísticas de accesos agrupadas por paciente.
     */
    @GET
    @Path("/accesos/paciente")
    public Response obtenerAccesosPorPaciente(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = parseDate(fechaInicio);
            Date fin = parseDate(fechaFin);
            
            if (inicio == null || fin == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "fechaInicio y fechaFin son requeridos (formato: yyyy-MM-dd)"))
                        .build();
            }
            
            List<Map<String, Object>> resultado = reportesService.obtenerAccesosPorPaciente(inicio, fin);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo accesos por paciente", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener accesos por paciente: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/accesos/tipo-documento
     * 
     * Obtiene estadísticas de accesos agrupadas por tipo de documento.
     */
    @GET
    @Path("/accesos/tipo-documento")
    public Response obtenerAccesosPorTipoDocumento(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = parseDate(fechaInicio);
            Date fin = parseDate(fechaFin);
            
            if (inicio == null || fin == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "fechaInicio y fechaFin son requeridos (formato: yyyy-MM-dd)"))
                        .build();
            }
            
            List<Map<String, Object>> resultado = reportesService.obtenerAccesosPorTipoDocumento(inicio, fin);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo accesos por tipo de documento", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener accesos por tipo de documento: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/politicas/evolucion
     * 
     * Obtiene la evolución de políticas creadas por día.
     */
    @GET
    @Path("/politicas/evolucion")
    public Response obtenerEvolucionPoliticas(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = parseDate(fechaInicio);
            Date fin = parseDate(fechaFin);
            
            if (inicio == null || fin == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "fechaInicio y fechaFin son requeridos (formato: yyyy-MM-dd)"))
                        .build();
            }
            
            List<Map<String, Object>> resultado = reportesService.obtenerEvolucionPoliticas(inicio, fin);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo evolución de políticas", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener evolución de políticas: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/politicas/alcance
     * 
     * Obtiene estadísticas de políticas agrupadas por alcance.
     */
    @GET
    @Path("/politicas/alcance")
    public Response obtenerPoliticasPorAlcance() {
        try {
            List<Map<String, Object>> resultado = reportesService.obtenerPoliticasPorAlcance();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo políticas por alcance", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener políticas por alcance: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/politicas/duracion
     * 
     * Obtiene estadísticas de políticas agrupadas por duración.
     */
    @GET
    @Path("/politicas/duracion")
    public Response obtenerPoliticasPorDuracion() {
        try {
            List<Map<String, Object>> resultado = reportesService.obtenerPoliticasPorDuracion();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo políticas por duración", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener políticas por duración: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/reportes/politicas/resumen
     * 
     * Obtiene un resumen general de políticas.
     */
    @GET
    @Path("/politicas/resumen")
    public Response obtenerResumenPoliticas() {
        try {
            Map<String, Object> resultado = reportesService.obtenerResumenPoliticas();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error obteniendo resumen de políticas", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener resumen de políticas: " + e.getMessage()))
                    .build();
        }
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            LOG.log(Level.WARNING, "Error parseando fecha: " + dateStr, e);
            return null;
        }
    }
}

