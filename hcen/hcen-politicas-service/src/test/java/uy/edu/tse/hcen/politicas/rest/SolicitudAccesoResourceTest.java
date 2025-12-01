package uy.edu.tse.hcen.politicas.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;
import uy.edu.tse.hcen.politicas.service.SolicitudAccesoService;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.*;
import static org.mockito.ArgumentMatchers.isNull;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SolicitudAccesoResourceTest {

    @Mock
    private SolicitudAccesoService service;

    @InjectMocks
    private SolicitudAccesoResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // Inyectar manualmente el servicio usando reflexión
        Field serviceField = SolicitudAccesoResource.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(resource, service);
    }

    @Test
    void crearSolicitud_validData_shouldReturnCreated() {
        Map<String, Object> body = new HashMap<>();
        body.put("solicitanteId", "prof-1");
        body.put("codDocumPaciente", "12345678");
        body.put("especialidad", "CARDIOLOGIA");
        body.put("tipoDocumento", "CONSULTA_MEDICA");
        body.put("razonSolicitud", "Consulta médica");
        body.put("tenantId", "100");

        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        when(service.crearSolicitud(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString(), anyString()))
            .thenReturn(solicitud);

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).crearSolicitud(eq("prof-1"), eq("CARDIOLOGIA"), eq("12345678"),
            eq("CONSULTA_MEDICA"), isNull(), eq("Consulta médica"), eq("100"));
    }

    @Test
    void crearSolicitud_missingSolicitanteId_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).crearSolicitud(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void crearSolicitud_missingCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("solicitanteId", "prof-1");

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearSolicitud_serviceThrowsException_shouldReturnBadRequest() {
    Map<String, Object> body = new HashMap<>();
    body.put("solicitanteId", "prof-1");
    body.put("especialidad", "Cardiologia"); 
    body.put("codDocumPaciente", "12345678");
    body.put("tipoDocumento", "CI");
    body.put("documentoId", "1234567-8");
    body.put("razonSolicitud", "Necesidad de acceso");
    body.put("tenantId", "clinica-A");


    when(service.crearSolicitud(any(), any(), any(),
        any(), any(), any(), any()))
        .thenThrow(new RuntimeException("Service error: Fallo de base de datos"));

    Response response = resource.crearSolicitud(body);

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), 
                 "Debe retornar 400 (BAD_REQUEST) cuando el servicio lanza una excepción.");
    
    assertNotNull(response.getEntity(), "El cuerpo de la respuesta no debe ser null.");
    
    verify(service, times(1)).crearSolicitud(any(), any(), any(), any(), any(), any(), any());
}
    @Test
    void obtenerPorId_validId_shouldReturnOk() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        when(service.obtenerPorId(1L)).thenReturn(solicitud);

        Response response = resource.obtenerPorId(1L);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).obtenerPorId(1L);
    }

    @Test
    void obtenerPorId_notFound_shouldReturnNotFound() {
        when(service.obtenerPorId(999L)).thenReturn(null);

        Response response = resource.obtenerPorId(999L);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPendientes_shouldReturnOk() {
        List<SolicitudAcceso> solicitudes = Arrays.asList(new SolicitudAcceso());
        when(service.listarPendientes()).thenReturn(solicitudes);

        Response response = resource.listarPendientes();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPendientes();
    }

    @Test
    void listarPorPaciente_validCI_shouldReturnOk() {
        List<SolicitudAcceso> solicitudes = Arrays.asList(new SolicitudAcceso());
        when(service.listarPorPaciente("12345678")).thenReturn(solicitudes);

        Response response = resource.listarPorPaciente("12345678");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPorPaciente("12345678");
    }

    @Test
    void listarPendientesPorPaciente_validCI_shouldReturnOk() {
        List<SolicitudAcceso> solicitudes = Arrays.asList(new SolicitudAcceso());
        when(service.listarPendientesPorPaciente("12345678")).thenReturn(solicitudes);

        Response response = resource.listarPendientesPorPaciente("12345678");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPendientesPorPaciente("12345678");
    }

    @Test
    void listarPorProfesional_validId_shouldReturnOk() {
        List<SolicitudAcceso> solicitudes = Arrays.asList(new SolicitudAcceso());
        when(service.listarPorProfesional("prof-1")).thenReturn(solicitudes);

        Response response = resource.listarPorProfesional("prof-1");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPorProfesional("prof-1");
    }

    @Test
    void aprobarSolicitud_validId_shouldReturnOk() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");
        body.put("comentario", "Aprobado");

        when(service.aprobarSolicitud(1L, "admin", "Aprobado")).thenReturn(solicitud);

        Response response = resource.aprobarSolicitud(1L, body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).aprobarSolicitud(1L, "admin", "Aprobado");
    }

    @Test
    void aprobarSolicitud_notFound_shouldReturnNotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        when(service.aprobarSolicitud(999L, "admin", null))
            .thenThrow(new IllegalArgumentException("Solicitud no encontrada"));

        Response response = resource.aprobarSolicitud(999L, body);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void aprobarSolicitud_invalidState_shouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        when(service.aprobarSolicitud(1L, "admin", null))
            .thenThrow(new IllegalStateException("Solicitud ya resuelta"));

        Response response = resource.aprobarSolicitud(1L, body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    @Test
    void rechazarSolicitud_validId_shouldReturnOk() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");
        body.put("comentario", "Rechazado");

        when(service.rechazarSolicitud(1L, "admin", "Rechazado")).thenReturn(solicitud);

        Response response = resource.rechazarSolicitud(1L, body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).rechazarSolicitud(1L, "admin", "Rechazado");
    }

    @Test
    void rechazarSolicitud_notFound_shouldReturnNotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        when(service.rechazarSolicitud(999L, "admin", null))
            .thenThrow(new IllegalArgumentException("Solicitud no encontrada"));

        Response response = resource.rechazarSolicitud(999L, body);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void rechazarSolicitud_invalidState_shouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        when(service.rechazarSolicitud(1L, "admin", null))
            .thenThrow(new IllegalStateException("Solicitud ya resuelta"));

        Response response = resource.rechazarSolicitud(1L, body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void rechazarSolicitud_exception_shouldReturnInternalError() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        when(service.rechazarSolicitud(1L, "admin", null))
            .thenThrow(new RuntimeException("Unexpected error"));

        Response response = resource.rechazarSolicitud(1L, body);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void crearSolicitud_withNullOptionalFields_shouldHandleGracefully() {
        Map<String, Object> body = new HashMap<>();
        body.put("solicitanteId", "prof-1");
        body.put("codDocumPaciente", "12345678");

        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        when(service.crearSolicitud(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString()))
            .thenReturn(solicitud);

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void crearSolicitud_withNullBody_shouldReturnBadRequest() {
        Response response = resource.crearSolicitud(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).crearSolicitud(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void crearSolicitud_withEmptySolicitanteId_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("solicitanteId", "");
        body.put("codDocumPaciente", "12345678");

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).crearSolicitud(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void crearSolicitud_withEmptyCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("solicitanteId", "prof-1");
        body.put("codDocumPaciente", "");

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).crearSolicitud(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void listarPorPaciente_withNullCI_shouldReturnBadRequest() {
        Response response = resource.listarPorPaciente(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).listarPorPaciente(anyString());
    }

    @Test
    void listarPorPaciente_withEmptyCI_shouldReturnBadRequest() {
        Response response = resource.listarPorPaciente("");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).listarPorPaciente(anyString());
    }

    @Test
    void listarPendientesPorPaciente_withNullCI_shouldReturnBadRequest() {
        Response response = resource.listarPendientesPorPaciente(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).listarPendientesPorPaciente(anyString());
    }

    @Test
    void listarPendientesPorPaciente_withEmptyCI_shouldReturnBadRequest() {
        Response response = resource.listarPendientesPorPaciente("");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).listarPendientesPorPaciente(anyString());
    }

    @Test
    void listarPorProfesional_withNullId_shouldReturnBadRequest() {
        Response response = resource.listarPorProfesional(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).listarPorProfesional(anyString());
    }

    @Test
    void listarPorProfesional_withEmptyId_shouldReturnBadRequest() {
        Response response = resource.listarPorProfesional("");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).listarPorProfesional(anyString());
    }

    @Test
    void aprobarSolicitud_withNullId_shouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        Response response = resource.aprobarSolicitud(null, body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).aprobarSolicitud(anyLong(), anyString(), anyString());
    }

    @Test
    void aprobarSolicitud_withNullBody_shouldReturnBadRequest() {
        Response response = resource.aprobarSolicitud(1L, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).aprobarSolicitud(anyLong(), anyString(), anyString());
    }

    @Test
    void aprobarSolicitud_withNullResueltoPor_shouldStillCallService() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", null);
        body.put("comentario", "Aprobado");

        when(service.aprobarSolicitud(1L, null, "Aprobado")).thenReturn(solicitud);

        Response response = resource.aprobarSolicitud(1L, body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).aprobarSolicitud(1L, null, "Aprobado");
    }

    @Test
    void aprobarSolicitud_withNullComentario_shouldStillCallService() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");
        body.put("comentario", null);

        when(service.aprobarSolicitud(1L, "admin", null)).thenReturn(solicitud);

        Response response = resource.aprobarSolicitud(1L, body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).aprobarSolicitud(1L, "admin", null);
    }

    @Test
    void aprobarSolicitud_withException_shouldReturnInternalError() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        when(service.aprobarSolicitud(1L, "admin", null))
            .thenThrow(new RuntimeException("Unexpected error"));

        Response response = resource.aprobarSolicitud(1L, body);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void rechazarSolicitud_withNullId_shouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");

        Response response = resource.rechazarSolicitud(null, body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).rechazarSolicitud(anyLong(), anyString(), anyString());
    }

    @Test
    void rechazarSolicitud_withNullBody_shouldReturnBadRequest() {
        Response response = resource.rechazarSolicitud(1L, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, never()).rechazarSolicitud(anyLong(), anyString(), anyString());
    }

    @Test
    void rechazarSolicitud_withNullResueltoPor_shouldStillCallService() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", null);
        body.put("comentario", "Rechazado");

        when(service.rechazarSolicitud(1L, null, "Rechazado")).thenReturn(solicitud);

        Response response = resource.rechazarSolicitud(1L, body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).rechazarSolicitud(1L, null, "Rechazado");
    }

    @Test
    void rechazarSolicitud_withNullComentario_shouldStillCallService() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        Map<String, String> body = new HashMap<>();
        body.put("resueltoPor", "admin");
        body.put("comentario", null);

        when(service.rechazarSolicitud(1L, "admin", null)).thenReturn(solicitud);

        Response response = resource.rechazarSolicitud(1L, body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).rechazarSolicitud(1L, "admin", null);
    }

    @Test
    void crearSolicitud_withEmptySolicitanteIdAndCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("solicitanteId", "");
        body.put("codDocumPaciente", "");

        Response response = resource.crearSolicitud(body);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).crearSolicitud(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void obtenerPorId_withNullId_shouldCallService() {
        when(service.obtenerPorId(null)).thenReturn(null);

        Response response = resource.obtenerPorId(null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(service).obtenerPorId(null);
    }

    @Test
    void listarPendientes_withEmptyList_shouldReturnOk() {
        when(service.listarPendientes()).thenReturn(new ArrayList<>());

        Response response = resource.listarPendientes();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).listarPendientes();
    }
}

