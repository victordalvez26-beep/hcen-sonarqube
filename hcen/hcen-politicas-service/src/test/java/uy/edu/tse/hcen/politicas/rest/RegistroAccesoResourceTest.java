package uy.edu.tse.hcen.politicas.rest;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;
import uy.edu.tse.hcen.politicas.service.RegistroAccesoService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

/**
 * Tests unitarios exhaustivos para RegistroAccesoResource.
 */
class RegistroAccesoResourceTest {

    @Mock
    private RegistroAccesoService service;

    @Mock
    private HttpHeaders headers;

    @InjectMocks
    private RegistroAccesoResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new RegistroAccesoResource();
        
        Field serviceField = RegistroAccesoResource.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(resource, service);
        
        Field headersField = RegistroAccesoResource.class.getDeclaredField("headers");
        headersField.setAccessible(true);
        headersField.set(resource, headers);
    }

    @Test
    void registrarAcceso_validJson_shouldReturnCreated() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\"," +
                "\"documentoId\":\"DOC-123\",\"tipoDocumento\":\"RECETA\",\"exito\":true}";
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(true);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any());
    }

    @Test
    void registrarAcceso_missingRequiredFields_shouldReturnBadRequest() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\"}"; // Falta codDocumPaciente
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any());
    }

    @Test
    void registrarAcceso_nullProfesionalId_shouldReturnBadRequest() {
        String jsonBody = "{\"codDocumPaciente\":\"1.234.567-8\"}";
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void registrarAcceso_invalidJson_shouldReturnInternalServerError() {
        String jsonBody = "{invalid json}";
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void registrarAcceso_withAllFields_shouldExtractAllFields() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\"," +
                "\"documentoId\":\"DOC-123\",\"tipoDocumento\":\"RECETA\"," +
                "\"ipAddress\":\"192.168.1.1\",\"userAgent\":\"Mozilla/5.0\"," +
                "\"exito\":false,\"motivoRechazo\":\"Sin permiso\"," +
                "\"referencia\":\"Consulta\",\"clinicaId\":\"CLINIC-100\"," +
                "\"nombreProfesional\":\"Dr. Juan Pérez\",\"especialidad\":\"CARDIOLOGIA\"}";
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(false);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).registrarAcceso(
                eq("PROF-001"), eq("1.234.567-8"), eq("DOC-123"), eq("RECETA"),
                eq("192.168.1.1"), eq("Mozilla/5.0"), eq(false), eq("Sin permiso"),
                eq("Consulta"), eq("CLINIC-100"), eq("Dr. Juan Pérez"), eq("CARDIOLOGIA")
        );
    }

    @Test
    void registrarAcceso_extractIpFromHeaders_shouldUseHeaderValue() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\"}";
        
        when(headers.getHeaderString("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(headers.getHeaderString("User-Agent")).thenReturn("TestAgent");
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(true);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).registrarAcceso(
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                eq("10.0.0.1"), eq("TestAgent"), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any()
        );
    }

    @Test
    void registrarAcceso_extractIpFromXRealIP_shouldUseFallback() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\"}";
        
        when(headers.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(headers.getHeaderString("X-Real-IP")).thenReturn("192.168.1.100");
        when(headers.getHeaderString("User-Agent")).thenReturn("TestAgent");
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(true);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).registrarAcceso(
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                eq("192.168.1.100"), eq("TestAgent"), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any()
        );
    }

    @Test
    void registrarAcceso_exitoDefaultTrue_shouldUseDefault() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\"}";
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(true);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).registrarAcceso(
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), eq(true), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any()
        );
    }

    @Test
    void obtenerPorId_validId_shouldReturnOk() {
        Long id = 1L;
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(id);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(true);
        registro.setFecha(new java.util.Date());
        
        when(service.obtenerPorId(id)).thenReturn(registro);
        
        Response response = resource.obtenerPorId(id);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).obtenerPorId(id);
    }

    @Test
    void obtenerPorId_notFound_shouldReturnNotFound() {
        Long id = 999L;
        
        when(service.obtenerPorId(id)).thenReturn(null);
        
        Response response = resource.obtenerPorId(id);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(service).obtenerPorId(id);
    }

    @Test
    void obtenerPorId_exception_shouldReturnInternalServerError() {
        Long id = 1L;
        
        when(service.obtenerPorId(id)).thenThrow(new RuntimeException("Database error"));
        
        Response response = resource.obtenerPorId(id);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPorPaciente_validCI_shouldReturnOk() {
        String ci = "1.234.567-8";
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setProfesionalId("PROF-001");
        r1.setCodDocumPaciente(ci);
        r1.setExito(true);
        RegistroAcceso r2 = new RegistroAcceso();
        r2.setProfesionalId("PROF-002");
        r2.setCodDocumPaciente(ci);
        r2.setExito(true);
        List<RegistroAcceso> registros = Arrays.asList(r1, r2);
        
        when(service.listarPorPaciente(ci)).thenReturn(registros);
        
        Response response = resource.listarPorPaciente(ci);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).listarPorPaciente(ci);
    }

    @Test
    void listarPorProfesional_validId_shouldReturnOk() {
        String profesionalId = "PROF-001";
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setProfesionalId(profesionalId);
        r1.setCodDocumPaciente("1.234.567-8");
        r1.setExito(true);
        List<RegistroAcceso> registros = Arrays.asList(r1);
        
        when(service.listarPorProfesional(profesionalId)).thenReturn(registros);
        
        Response response = resource.listarPorProfesional(profesionalId);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPorProfesional(profesionalId);
    }

    @Test
    void listarPorDocumento_validId_shouldReturnOk() {
        String documentoId = "DOC-123";
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setProfesionalId("PROF-001");
        r1.setCodDocumPaciente("1.234.567-8");
        r1.setDocumentoId(documentoId);
        r1.setExito(true);
        List<RegistroAcceso> registros = Arrays.asList(r1);
        
        when(service.listarPorDocumento(documentoId)).thenReturn(registros);
        
        Response response = resource.listarPorDocumento(documentoId);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPorDocumento(documentoId);
    }

    @Test
    void listarPorRangoFechas_validDates_shouldReturnOk() {
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setProfesionalId("PROF-001");
        r1.setCodDocumPaciente("1.234.567-8");
        r1.setExito(true);
        List<RegistroAcceso> registros = Arrays.asList(r1);
        
        when(service.listarPorRangoFechas(any(Date.class), any(Date.class))).thenReturn(registros);
        
        Response response = resource.listarPorRangoFechas(fechaInicio, fechaFin);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(service).listarPorRangoFechas(any(Date.class), any(Date.class));
    }

    @Test
    void listarPorRangoFechas_missingDates_shouldReturnBadRequest() {
        Response response = resource.listarPorRangoFechas(null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).listarPorRangoFechas(any(Date.class), any(Date.class));
    }

    @Test
    void listarPorRangoFechas_invalidDateFormat_shouldReturnBadRequest() {
        Response response = resource.listarPorRangoFechas("invalid-date", "2024-01-31");
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void contarPorPaciente_validCI_shouldReturnOk() {
        String ci = "1.234.567-8";
        Long count = 10L;
        
        when(service.contarAccesosPorPaciente(ci)).thenReturn(count);
        
        Response response = resource.contarPorPaciente(ci);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).contarAccesosPorPaciente(ci);
    }

    @Test
    void registrarAcceso_serviceThrowsException_shouldReturnInternalServerError() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\"}";
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void registrarAcceso_nullExito_shouldDefaultToTrue() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\",\"exito\":null}";
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(true);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).registrarAcceso(
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), eq(true), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any()
        );
    }

    @Test
    void registrarAcceso_exitoFalse_shouldPassFalse() {
        String jsonBody = "{\"profesionalId\":\"PROF-001\",\"codDocumPaciente\":\"1.234.567-8\",\"exito\":false}";
        
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(1L);
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        registro.setExito(false);
        registro.setFecha(new java.util.Date());
        
        when(service.registrarAcceso(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), anyBoolean(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(registro);
        
        Response response = resource.registrarAcceso(jsonBody);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(service).registrarAcceso(
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), eq(false), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any()
        );
    }
}

