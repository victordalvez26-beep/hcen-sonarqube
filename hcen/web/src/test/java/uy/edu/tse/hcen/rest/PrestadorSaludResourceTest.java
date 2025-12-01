package uy.edu.tse.hcen.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.EstadoPrestador;
import uy.edu.tse.hcen.model.PrestadorSalud;
import uy.edu.tse.hcen.service.PrestadorSaludService;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para PrestadorSaludResource.
 */
class PrestadorSaludResourceTest {

    @Mock
    private PrestadorSaludService service;

    @InjectMocks
    private PrestadorSaludResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new PrestadorSaludResource();
        Field field = PrestadorSaludResource.class.getDeclaredField("service");
        field.setAccessible(true);
        field.set(resource, service);
    }

    @Test
    void invitarPrestador_validData_shouldReturnCreated() {
        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", "Clínica Test");
        datos.put("contacto", "test@example.com");
        
        PrestadorSalud prestador = new PrestadorSalud("Clínica Test", "test@example.com");
        prestador.setId(1L);
        prestador.setInvitationUrl("http://example.com/register?token=123");
        prestador.setEstado(EstadoPrestador.PENDIENTE_REGISTRO);
        
        when(service.crearInvitacion(anyString(), anyString())).thenReturn(prestador);
        
        Response response = resource.invitarPrestador(datos);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, times(1)).crearInvitacion("Clínica Test", "test@example.com");
    }

    @Test
    void invitarPrestador_missingNombre_shouldReturnBadRequest() {
        Map<String, String> datos = new HashMap<>();
        datos.put("contacto", "test@example.com");
        
        Response response = resource.invitarPrestador(datos);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(service, never()).crearInvitacion(anyString(), anyString());
    }

    @Test
    void invitarPrestador_missingContacto_shouldReturnBadRequest() {
        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", "Clínica Test");
        
        Response response = resource.invitarPrestador(datos);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void completarRegistro_validData_shouldReturnOk() {
        Map<String, String> datos = new HashMap<>();
        datos.put("token", "valid-token");
        datos.put("rut", "12345678-9");
        datos.put("url", "http://example.com");
        datos.put("departamento", "MONTEVIDEO");
        datos.put("localidad", "Ciudad");
        datos.put("direccion", "Calle 123");
        datos.put("telefono", "123456789");
        
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        prestador.setRut("12345678-9");
        prestador.setEstado(EstadoPrestador.ACTIVO);
        
        when(service.completarRegistro(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString())).thenReturn(prestador);
        
        Response response = resource.completarRegistro(datos);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void completarRegistro_invalidToken_shouldReturnBadRequest() {
        Map<String, String> datos = new HashMap<>();
        datos.put("token", "invalid");
        datos.put("rut", "12345678-9");
        datos.put("url", "http://example.com");
        datos.put("departamento", "MONTEVIDEO");
        datos.put("localidad", "Centro");
        datos.put("direccion", "Calle 123");
        datos.put("telefono", "123456789");
        
        when(service.completarRegistro(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Token inválido"));
        
        Response response = resource.completarRegistro(datos);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPrestadores_shouldReturnList() {
        PrestadorSalud p1 = new PrestadorSalud("Clinic 1", "c1@example.com");
        PrestadorSalud p2 = new PrestadorSalud("Clinic 2", "c2@example.com");
        
        when(service.listarTodos()).thenReturn(List.of(p1, p2));
        
        Response response = resource.listarPrestadores();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, times(1)).listarTodos();
    }

    @Test
    void obtenerPrestador_validId_shouldReturnOk() {
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        
        when(service.obtenerPorId(1L)).thenReturn(prestador);
        
        Response response = resource.obtenerPrestador(1L);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerPrestador_notFound_shouldReturnNotFound() {
        when(service.obtenerPorId(999L)).thenReturn(null);
        
        Response response = resource.obtenerPrestador(999L);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void invitarPrestador_emptyNombre_shouldReturnBadRequest() {
        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", "");
        datos.put("contacto", "test@example.com");
        
        Response response = resource.invitarPrestador(datos);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void invitarPrestador_emptyContacto_shouldReturnBadRequest() {
        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", "Clínica Test");
        datos.put("contacto", "");
        
        Response response = resource.invitarPrestador(datos);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void invitarPrestador_serviceException_shouldReturnInternalServerError() {
        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", "Clínica Test");
        datos.put("contacto", "test@example.com");
        
        when(service.crearInvitacion(anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.invitarPrestador(datos);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void completarRegistro_missingToken_shouldHandleGracefully() {
        Map<String, String> datos = new HashMap<>();
        datos.put("rut", "12345678-9");
        
        Response response = resource.completarRegistro(datos);
        
        // El servicio puede lanzar excepción o retornar null, verificar que no crashea
        assertNotNull(response);
    }

    @Test
    void completarRegistro_serviceException_shouldReturnInternalServerError() {
        Map<String, String> datos = new HashMap<>();
        datos.put("token", "valid-token");
        datos.put("rut", "12345678-9");
        datos.put("url", "http://example.com");
        datos.put("departamento", "MONTEVIDEO");
        datos.put("localidad", "Centro");
        datos.put("direccion", "Calle 123");
        datos.put("telefono", "123456789");
        
        when(service.completarRegistro(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        Response response = resource.completarRegistro(datos);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPrestadores_emptyList_shouldReturnEmptyList() {
        when(service.listarTodos()).thenReturn(List.of());
        
        Response response = resource.listarPrestadores();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void listarPrestadores_serviceException_shouldReturnInternalServerError() {
        when(service.listarTodos()).thenThrow(new RuntimeException("Database error"));
        
        Response response = resource.listarPrestadores();
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPrestador_serviceException_shouldReturnInternalServerError() {
        when(service.obtenerPorId(1L)).thenThrow(new RuntimeException("Database error"));
        
        Response response = resource.obtenerPrestador(1L);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}

