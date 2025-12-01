package uy.edu.tse.hcen.politicas.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.service.ReportesService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ReportesResource.
 */
class ReportesResourceTest {

    @Mock
    private ReportesService reportesService;

    private ReportesResource reportesResource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        reportesResource = new ReportesResource();
        
        java.lang.reflect.Field serviceField = ReportesResource.class.getDeclaredField("reportesService");
        serviceField.setAccessible(true);
        serviceField.set(reportesResource, reportesService);
    }

    @Test
    void obtenerEvolucionAccesos_validDates_shouldReturnOk() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        Map<String, Object> item = new HashMap<>();
        item.put("fecha", "2024-01-15");
        item.put("total", 10L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerEvolucionAccesos(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerEvolucionAccesos(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerEvolucionAccesos_missingDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerEvolucionAccesos(null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorProfesional_validDates_shouldReturnOk() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        when(reportesService.obtenerAccesosPorProfesional(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerAccesosPorProfesional(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorPaciente_validDates_shouldReturnOk() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        when(reportesService.obtenerAccesosPorPaciente(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerAccesosPorPaciente(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorTipoDocumento_validDates_shouldReturnOk() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        when(reportesService.obtenerAccesosPorTipoDocumento(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerAccesosPorTipoDocumento(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionPoliticas_validDates_shouldReturnOk() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        when(reportesService.obtenerEvolucionPoliticas(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerEvolucionPoliticas(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorAlcance_shouldReturnOk() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        when(reportesService.obtenerPoliticasPorAlcance()).thenReturn(resultado);
        
        Response response = reportesResource.obtenerPoliticasPorAlcance();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorDuracion_shouldReturnOk() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        when(reportesService.obtenerPoliticasPorDuracion()).thenReturn(resultado);
        
        Response response = reportesResource.obtenerPoliticasPorDuracion();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenPoliticas_shouldReturnOk() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", 20L);
        resultado.put("activas", 15L);
        resultado.put("inactivas", 5L);
        
        when(reportesService.obtenerResumenPoliticas()).thenReturn(resultado);
        
        Response response = reportesResource.obtenerResumenPoliticas();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerEvolucionAccesos_serviceException_shouldReturnInternalError() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        when(reportesService.obtenerEvolucionAccesos(any(), any()))
                .thenThrow(new RuntimeException("Database error"));
        
        Response response = reportesResource.obtenerEvolucionAccesos(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionAccesos_invalidDateFormat_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerEvolucionAccesos("invalid-date", "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionAccesos_oneDateMissing_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerEvolucionAccesos("2024-01-01", null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorProfesional_missingDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerAccesosPorProfesional(null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorProfesional_invalidDateFormat_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerAccesosPorProfesional("invalid", "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorProfesional_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerAccesosPorProfesional(any(), any()))
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerAccesosPorProfesional("2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorPaciente_missingDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerAccesosPorPaciente(null, "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorPaciente_invalidDateFormat_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerAccesosPorPaciente("2024-01-01", "invalid");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorPaciente_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerAccesosPorPaciente(any(), any()))
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerAccesosPorPaciente("2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorTipoDocumento_missingDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerAccesosPorTipoDocumento(null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorTipoDocumento_invalidDateFormat_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerAccesosPorTipoDocumento("2024/01/01", "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorTipoDocumento_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerAccesosPorTipoDocumento(any(), any()))
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerAccesosPorTipoDocumento("2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionPoliticas_missingDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerEvolucionPoliticas(null, "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionPoliticas_invalidDateFormat_shouldReturnBadRequest() {
        // parseDate devuelve null para fechas con formato inválido
        // El código valida si inicio == null || fin == null y devuelve 400
        // Usamos una fecha que definitivamente fallará el parseo (formato completamente inválido)
        when(reportesService.obtenerEvolucionPoliticas(any(), any())).thenReturn(new ArrayList<>());
        
        Response response = reportesResource.obtenerEvolucionPoliticas("invalid-date-format", "2024-01-31");
        
        // Verificamos que devuelva 400 cuando una fecha es inválida
        // Si parseDate devuelve null para "invalid-date-format", entonces inicio == null
        // y el código debería devolver 400
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(reportesService, never()).obtenerEvolucionPoliticas(any(), any());
    }

    @Test
    void obtenerEvolucionPoliticas_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerEvolucionPoliticas(any(), any()))
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerEvolucionPoliticas("2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorAlcance_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerPoliticasPorAlcance())
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerPoliticasPorAlcance();
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorDuracion_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerPoliticasPorDuracion())
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerPoliticasPorDuracion();
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenPoliticas_serviceException_shouldReturnInternalError() {
        when(reportesService.obtenerResumenPoliticas())
                .thenThrow(new RuntimeException("Service error"));
        
        Response response = reportesResource.obtenerResumenPoliticas();
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionAccesos_emptyStringDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerEvolucionAccesos("", "");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionAccesos_blankStringDates_shouldReturnBadRequest() {
        Response response = reportesResource.obtenerEvolucionAccesos("   ", "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerAccesosPorProfesional_withData_shouldReturnData() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        Map<String, Object> item = new HashMap<>();
        item.put("profesionalId", "PROF-001");
        item.put("total", 25L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerAccesosPorProfesional(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerAccesosPorProfesional(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerAccesosPorPaciente_withData_shouldReturnData() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        Map<String, Object> item = new HashMap<>();
        item.put("codDocumPaciente", "1.234.567-8");
        item.put("total", 30L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerAccesosPorPaciente(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerAccesosPorPaciente(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerAccesosPorTipoDocumento_withData_shouldReturnData() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        Map<String, Object> item = new HashMap<>();
        item.put("tipoDocumento", "RECETA");
        item.put("total", 15L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerAccesosPorTipoDocumento(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerAccesosPorTipoDocumento(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerEvolucionPoliticas_withData_shouldReturnData() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        Map<String, Object> item = new HashMap<>();
        item.put("fecha", "2024-01-15");
        item.put("total", 5L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerEvolucionPoliticas(any(), any())).thenReturn(resultado);
        
        Response response = reportesResource.obtenerEvolucionPoliticas(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerPoliticasPorAlcance_withData_shouldReturnData() {
        Map<String, Object> item = new HashMap<>();
        item.put("alcance", "TODOS_LOS_DOCUMENTOS");
        item.put("total", 10L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerPoliticasPorAlcance()).thenReturn(resultado);
        
        Response response = reportesResource.obtenerPoliticasPorAlcance();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerPoliticasPorDuracion_withData_shouldReturnData() {
        Map<String, Object> item = new HashMap<>();
        item.put("duracion", "INDEFINIDA");
        item.put("total", 12L);
        List<Map<String, Object>> resultado = Arrays.asList(item);
        
        when(reportesService.obtenerPoliticasPorDuracion()).thenReturn(resultado);
        
        Response response = reportesResource.obtenerPoliticasPorDuracion();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }
}
