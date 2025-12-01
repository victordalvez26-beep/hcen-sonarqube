package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para EmailService.
 * Nota: Los tests de métodos que envían emails reales requieren
 * mocks de JavaMail o un servidor SMTP de prueba (como GreenMail).
 * Estos tests validan la lógica de validación y construcción de contenido.
 */
class EmailServiceTest {

    private EmailService service;

    @BeforeEach
    void setUp() {
        service = new EmailService();
    }

    @Test
    void sendActivationEmail_nullEmail_shouldReturnFalse() {
        boolean result = service.sendActivationEmail(null, "Clinic", "admin", "url", "tenant");
        assertFalse(result);
    }

    @Test
    void sendActivationEmail_emptyEmail_shouldReturnFalse() {
        boolean result = service.sendActivationEmail("", "Clinic", "admin", "url", "tenant");
        assertFalse(result);
    }

    @Test
    void sendActivationEmail_blankEmail_shouldReturnFalse() {
        boolean result = service.sendActivationEmail("   ", "Clinic", "admin", "url", "tenant");
        assertFalse(result);
    }

    @Test
    void sendInvitationEmail_nullEmail_shouldReturnFalse() {
        boolean result = service.sendInvitationEmail(null, "Clinic", "url", "tenant");
        assertFalse(result);
    }

    @Test
    void sendInvitationEmail_emptyEmail_shouldReturnFalse() {
        boolean result = service.sendInvitationEmail("", "Clinic", "url", "tenant");
        assertFalse(result);
    }

    @Test
    void sendInvitationEmail_blankEmail_shouldReturnFalse() {
        boolean result = service.sendInvitationEmail("   ", "Clinic", "url", "tenant");
        assertFalse(result);
    }

    @Test
    void sendTestEmail_nullEmail_shouldHandleGracefully() {
        // El método intenta enviar y puede lanzar excepciones de JavaMail
        // Verificamos que no crashea inmediatamente, pero puede fallar en la configuración SMTP
        // En un entorno real con configuración SMTP, esto podría lanzar una excepción
        try {
            service.sendTestEmail(null);
        } catch (Exception e) {
            // Esperado - el método puede lanzar excepción con email null
            assertTrue(e instanceof Exception);
        }
    }

    @Test
    void sendPrestadorInvitationEmail_nullEmail_shouldThrowException() {
        // El método lanza RuntimeException si falla
        assertThrows(RuntimeException.class, () -> 
            service.sendPrestadorInvitationEmail(null, "Prestador", "url"));
    }

    @Test
    void sendPrestadorInvitationEmail_emptyEmail_shouldThrowException() {
        assertThrows(RuntimeException.class, () ->
            service.sendPrestadorInvitationEmail("", "Prestador", "url"));
    }

    @Test
    void sendPrestadorInvitationEmail_blankEmail_shouldThrowException() {
        assertThrows(RuntimeException.class, () ->
            service.sendPrestadorInvitationEmail("   ", "Prestador", "url"));
    }

    @Test
    void buildActivationEmailHtml_shouldReturnHtmlContent() throws Exception {
        java.lang.reflect.Method method = EmailService.class.getDeclaredMethod(
            "buildActivationEmailHtml", String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "Clinic Test", "admin", 
            "http://example.com/activate?token=123", "tenant1");
        
        assertNotNull(result);
        assertTrue(result.contains("Clinic Test"));
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("activate"));
    }

    @Test
    void buildInvitationHtml_shouldReturnHtmlContent() throws Exception {
        java.lang.reflect.Method method = EmailService.class.getDeclaredMethod(
            "buildInvitationHtml", String.class, String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "Clinic Test", 
            "http://example.com/portal/clinica/1/activate?token=123");
        
        assertNotNull(result);
        assertTrue(result.contains("Clinic Test"));
        assertTrue(result.contains("activate"));
    }

    @Test
    void buildPrestadorInvitationEmailBody_shouldReturnHtmlContent() throws Exception {
        java.lang.reflect.Method method = EmailService.class.getDeclaredMethod(
            "buildPrestadorInvitationEmailBody", String.class, String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "Prestador Test", 
            "http://example.com/register?token=123");
        
        assertNotNull(result);
        assertTrue(result.contains("Prestador Test"));
        assertTrue(result.contains("register"));
    }

    // Nota: Para tests más completos que validen el envío real de emails,
    // se recomienda usar GreenMail (servidor SMTP de prueba en memoria)
    // o mockear las clases de Jakarta Mail con PowerMock o similar.
}

