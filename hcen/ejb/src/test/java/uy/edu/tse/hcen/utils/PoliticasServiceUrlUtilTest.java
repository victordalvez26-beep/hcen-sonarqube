package uy.edu.tse.hcen.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para {@link PoliticasServiceUrlUtil}.
 * Se apoyan en propiedades de sistema para no depender de variables de entorno
 * reales del ambiente donde corren los tests.
 */
class PoliticasServiceUrlUtilTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("POLITICAS_SERVICE_URL");
        System.clearProperty("HCEN_BACKEND_BASE_URL");
        System.clearProperty("GUBUY_REDIRECT_URI");
    }

    @Test
    void getBaseUrl_whenExplicitPropertySet_shouldReturnIt() {
        System.setProperty("POLITICAS_SERVICE_URL", "http://custom-host/politicas/api");

        String baseUrl = PoliticasServiceUrlUtil.getBaseUrl();

        assertEquals("http://custom-host/politicas/api", baseUrl);
    }

    @Test
    void getBaseUrl_whenBackendBaseUrlSet_shouldBuildFromIt() {
        System.setProperty("HCEN_BACKEND_BASE_URL", "https://env-123.web.elasticloud.uy/hcen");

        String baseUrl = PoliticasServiceUrlUtil.getBaseUrl();

        // HTTPS debe convertirse a HTTP y mantener el contexto /hcen/hcen-politicas-service/api
        assertEquals("http://env-123.web.elasticloud.uy/hcen/hcen-politicas-service/api", baseUrl);
    }

    @Test
    void getBaseUrl_whenOnlyGubuyRedirectSet_shouldExtractServerBase() {
        System.setProperty(
                "GUBUY_REDIRECT_URI",
                "https://env-6105410.web.elasticloud.uy/hcen/api/auth/login/callback"
        );

        String baseUrl = PoliticasServiceUrlUtil.getBaseUrl();

        // Extrae https://env-6105410.web.elasticloud.uy, lo convierte a http y agrega /hcen/hcen-politicas-service/api
        assertEquals("http://env-6105410.web.elasticloud.uy/hcen/hcen-politicas-service/api", baseUrl);
    }

    @Test
    void getBaseUrl_whenNothingConfigured_shouldFallbackToDefaultLocalhost() {
        String baseUrl = PoliticasServiceUrlUtil.getBaseUrl();

        assertEquals("http://localhost:8080/hcen-politicas-service/api", baseUrl);
    }

    @Test
    void buildUrl_whenBaseUrlAlreadyEndsWithApi_shouldNotDuplicateSegment() {
        System.setProperty("POLITICAS_SERVICE_URL", "http://localhost:8080/hcen-politicas-service/api");

        String url = PoliticasServiceUrlUtil.buildUrl("/politicas");

        assertEquals("http://localhost:8080/hcen-politicas-service/api/politicas", url);
    }

    @Test
    void buildUrl_whenBaseUrlLacksApi_shouldAppendIt() {
        System.setProperty("POLITICAS_SERVICE_URL", "http://localhost:8080/hcen-politicas-service");

        String url = PoliticasServiceUrlUtil.buildUrl("politicas");

        assertEquals("http://localhost:8080/hcen-politicas-service/api/politicas", url);
    }
}


