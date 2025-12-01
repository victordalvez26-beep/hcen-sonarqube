package uy.edu.tse.hcen.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.service.EmailService;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para EmailTestResource.
 */
class EmailTestResourceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailTestResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new EmailTestResource();
        // Inyectar mock mediante reflexión
        Field field = EmailTestResource.class.getDeclaredField("emailService");
        field.setAccessible(true);
        field.set(resource, emailService);
    }

    @Test
    void testEmail_success_shouldReturnOkResponse() {
        when(emailService.sendTestEmail(anyString())).thenReturn(true);

        Response response = resource.testEmail();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertTrue((Boolean) entity.get("success"));
        assertTrue(entity.get("message").toString().contains("enviado correctamente"));
        
        verify(emailService, times(1)).sendTestEmail(anyString());
    }

    @Test
    void testEmail_serviceReturnsFalse_shouldReturnInternalServerError() {
        when(emailService.sendTestEmail(anyString())).thenReturn(false);

        Response response = resource.testEmail();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertFalse((Boolean) entity.get("success"));
        assertTrue(entity.get("message").toString().contains("Error al enviar email"));
        
        verify(emailService, times(1)).sendTestEmail(anyString());
    }

    @Test
    void testEmail_serviceThrowsException_shouldReturnInternalServerError() {
        when(emailService.sendTestEmail(anyString())).thenThrow(new RuntimeException("Error de conexión SMTP"));

        Response response = resource.testEmail();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertFalse((Boolean) entity.get("success"));
        assertTrue(entity.get("message").toString().contains("Exception"));
        assertNotNull(entity.get("class"));
        
        verify(emailService, times(1)).sendTestEmail(anyString());
    }

    @Test
    void testEmail_serviceThrowsNullPointerException_shouldHandleGracefully() {
        when(emailService.sendTestEmail(anyString())).thenThrow(new NullPointerException("Null value"));

        Response response = resource.testEmail();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertFalse((Boolean) entity.get("success"));
    }
}


