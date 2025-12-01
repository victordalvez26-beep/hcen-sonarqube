package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NodoPerifericoHttpClientTest {

    private NodoPerifericoHttpClient client;

    @BeforeEach
    void setUp() {
        client = new NodoPerifericoHttpClient();
    }

    @Test
    void testClientCreation() {
        assertNotNull(client);
    }

    @Test
    void testInitializeTenant_withNullBaseUrl_shouldReturnFailure() {
        NodoPerifericoHttpClient.NodoInitPayload payload = new NodoPerifericoHttpClient.NodoInitPayload();
        payload.id = 1L;
        payload.rut = "12345678-9";
        payload.nombre = "Test Clinic";

        NodoPerifericoHttpClient.InitResponse response = client.initializeTenant(null, payload);

        assertFalse(response.success);
        assertNotNull(response.errorMessage);
        assertTrue(response.errorMessage.contains("baseUrl"));
    }

    @Test
    void testInitializeTenant_withEmptyBaseUrl_shouldReturnFailure() {
        NodoPerifericoHttpClient.NodoInitPayload payload = new NodoPerifericoHttpClient.NodoInitPayload();
        payload.id = 1L;

        NodoPerifericoHttpClient.InitResponse response = client.initializeTenant("   ", payload);

        assertFalse(response.success);
        assertNotNull(response.errorMessage);
    }

    @Test
    void testUpdateTenant_withNullBaseUrl_shouldReturnFalse() {
        NodoPerifericoHttpClient.NodoInitPayload payload = new NodoPerifericoHttpClient.NodoInitPayload();
        payload.id = 1L;

        boolean result = client.updateTenant(null, payload);

        assertFalse(result);
    }

    @Test
    void testUpdateTenant_withEmptyBaseUrl_shouldReturnFalse() {
        NodoPerifericoHttpClient.NodoInitPayload payload = new NodoPerifericoHttpClient.NodoInitPayload();
        payload.id = 1L;

        boolean result = client.updateTenant("", payload);

        assertFalse(result);
    }

    @Test
    void testDeleteTenant_withNullBaseUrl_shouldReturnFalse() {
        boolean result = client.deleteTenant(null, 1L);

        assertFalse(result);
    }

    @Test
    void testDeleteTenant_withEmptyBaseUrl_shouldReturnFalse() {
        boolean result = client.deleteTenant("   ", 1L);

        assertFalse(result);
    }

    @Test
    void testNodoInitPayloadCreation() {
        NodoPerifericoHttpClient.NodoInitPayload payload = new NodoPerifericoHttpClient.NodoInitPayload();
        payload.id = 1L;
        payload.rut = "12345678-9";
        payload.nombre = "Test";
        payload.departamento = "MONTEVIDEO";
        payload.localidad = "Ciudad";
        payload.direccion = "Calle 123";
        payload.nodoPerifericoUrlBase = "http://localhost:8081";
        payload.nodoPerifericoUsuario = "admin";
        payload.nodoPerifericoPassword = "password";
        payload.contacto = "contact@test.com";
        payload.url = "http://test.com";

        assertNotNull(payload);
        assertEquals(1L, payload.id);
        assertEquals("12345678-9", payload.rut);
    }

    @Test
    void testInitResponseCreation() {
        NodoPerifericoHttpClient.InitResponse response = new NodoPerifericoHttpClient.InitResponse();
        response.success = true;
        response.adminNickname = "admin";
        response.activationUrl = "http://test.com/activate";
        response.activationToken = "token123";
        response.tokenExpiresAt = "2024-12-31";
        response.errorMessage = null;

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("admin", response.adminNickname);
    }
}
