package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.service.NodoPerifericoHttpClient.NodoInitPayload;
import uy.edu.tse.hcen.service.NodoPerifericoHttpClient.InitResponse;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para NodoPerifericoHttpClient en el paquete service.
 */
class NodoPerifericoHttpClientServiceTest {

    private NodoPerifericoHttpClient client;

    @BeforeEach
    void setUp() {
        client = new NodoPerifericoHttpClient();
    }

    @Test
    void initializeTenant_withNullBaseUrl_shouldReturnFailure() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        InitResponse result = client.initializeTenant(null, payload);
        
        assertNotNull(result);
        assertFalse(result.success);
        assertNotNull(result.errorMessage);
    }

    @Test
    void initializeTenant_withEmptyBaseUrl_shouldReturnFailure() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        InitResponse result = client.initializeTenant("", payload);
        
        assertNotNull(result);
        assertFalse(result.success);
        assertNotNull(result.errorMessage);
    }

    @Test
    void initializeTenant_withBlankBaseUrl_shouldReturnFailure() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        InitResponse result = client.initializeTenant("   ", payload);
        
        assertNotNull(result);
        assertFalse(result.success);
        assertNotNull(result.errorMessage);
    }

    @Test
    void initializeTenant_withInvalidUrl_shouldReturnFailure() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        InitResponse result = client.initializeTenant("invalid-url-that-does-not-exist-12345", payload);
        
        assertNotNull(result);
        assertFalse(result.success);
    }

    @Test
    void updateTenant_withNullBaseUrl_shouldReturnFalse() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        boolean result = client.updateTenant(null, payload);
        
        assertFalse(result);
    }

    @Test
    void updateTenant_withEmptyBaseUrl_shouldReturnFalse() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        boolean result = client.updateTenant("", payload);
        
        assertFalse(result);
    }

    @Test
    void updateTenant_withInvalidUrl_shouldReturnFalse() {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        
        boolean result = client.updateTenant("invalid-url-that-does-not-exist-12345", payload);
        
        assertFalse(result);
    }

    @Test
    void deleteTenant_withNullBaseUrl_shouldReturnFalse() {
        boolean result = client.deleteTenant(null, 1L);
        
        assertFalse(result);
    }

    @Test
    void deleteTenant_withEmptyBaseUrl_shouldReturnFalse() {
        boolean result = client.deleteTenant("", 1L);
        
        assertFalse(result);
    }

    @Test
    void deleteTenant_withNullClinicId_shouldReturnFalse() {
        boolean result = client.deleteTenant("http://example.com", null);
        
        assertFalse(result);
    }

    @Test
    void deleteTenant_withInvalidUrl_shouldReturnFalse() {
        boolean result = client.deleteTenant("invalid-url-that-does-not-exist-12345", 1L);
        
        assertFalse(result);
    }

    @Test
    void normalizeBaseUrl_withTrailingSlash_shouldRemove() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("normalizeBaseUrl", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, "http://example.com/");
        
        assertEquals("http://example.com", result);
    }

    @Test
    void normalizeBaseUrl_withoutTrailingSlash_shouldReturnSame() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("normalizeBaseUrl", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, "http://example.com");
        
        assertEquals("http://example.com", result);
    }

    @Test
    void normalizeBaseUrl_withNull_shouldReturnEmpty() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("normalizeBaseUrl", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, (String) null);
        
        assertEquals("", result);
    }

    @Test
    void payloadToJson_withAllFields_shouldCreateValidJson() throws Exception {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.rut = "12345678";
        payload.nombre = "Test Clinic";
        payload.departamento = "MONTEVIDEO";
        payload.localidad = "Centro";
        payload.direccion = "Av. 18 de Julio 1234";
        payload.nodoPerifericoUrlBase = "http://example.com";
        payload.nodoPerifericoUsuario = "admin";
        payload.nodoPerifericoPassword = "password";
        payload.contacto = "admin@test.com";
        payload.url = "http://test.com";
        
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("payloadToJson", NodoInitPayload.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, payload);
        
        assertNotNull(result);
        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"nombre\":\"Test Clinic\""));
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void payloadToJson_withNullFields_shouldSkipNulls() throws Exception {
        NodoInitPayload payload = new NodoInitPayload();
        payload.id = 1L;
        payload.nombre = "Test Clinic";
        // Otros campos son null
        
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("payloadToJson", NodoInitPayload.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, payload);
        
        assertNotNull(result);
        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"nombre\":\"Test Clinic\""));
        assertFalse(result.contains("\"rut\":"));
    }

    @Test
    void escapeJson_withSpecialCharacters_shouldEscape() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("escapeJson", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, "Test \"quoted\" string");
        
        assertNotNull(result);
        assertTrue(result.contains("\\\""));
    }

    @Test
    void escapeJson_withBackslash_shouldEscape() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("escapeJson", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, "Test\\backslash");
        
        assertNotNull(result);
        assertTrue(result.contains("\\\\"));
    }

    @Test
    void escapeJson_withNull_shouldReturnEmpty() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("escapeJson", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, (String) null);
        
        assertEquals("", result);
    }

    @Test
    void extractJsonField_withValidField_shouldExtract() throws Exception {
        String json = "{\"adminNickname\":\"admin123\",\"activationUrl\":\"http://test.com\"}";
        
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, json, "adminNickname");
        
        assertEquals("admin123", result);
    }

    @Test
    void extractJsonField_withMissingField_shouldReturnNull() throws Exception {
        String json = "{\"otherField\":\"value\"}";
        
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, json, "missingField");
        
        assertNull(result);
    }

    @Test
    void extractJsonField_withNullJson_shouldReturnNull() throws Exception {
        Method method = NodoPerifericoHttpClient.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(client, null, "field");
        
        assertNull(result);
    }
}

