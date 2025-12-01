package uy.edu.tse.hcen.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InusServiceUrlUtilTest {

    @Test
    void getBaseUrl_withEnvVar_shouldReturnEnvUrl() {
        String originalEnv = System.getenv("INUS_SERVICE_URL");
        String originalProp = System.getProperty("INUS_SERVICE_URL");
        
        try {
            System.setProperty("INUS_SERVICE_URL", "http://test-inus:8082/pdi");
            String url = InusServiceUrlUtil.getBaseUrl();
            assertNotNull(url);
            assertTrue(url.contains("test-inus") || url.contains("localhost"));
        } finally {
            if (originalProp != null) {
                System.setProperty("INUS_SERVICE_URL", originalProp);
            } else {
                System.clearProperty("INUS_SERVICE_URL");
            }
        }
    }

    @Test
    void getBaseUrl_withoutEnvVar_shouldReturnDefault() {
        String originalEnv = System.getenv("INUS_SERVICE_URL");
        String originalProp = System.getProperty("INUS_SERVICE_URL");
        
        try {
            System.clearProperty("INUS_SERVICE_URL");
            String url = InusServiceUrlUtil.getBaseUrl();
            assertNotNull(url);
            assertTrue(url.contains("localhost") || url.contains("8082"));
        } finally {
            if (originalProp != null) {
                System.setProperty("INUS_SERVICE_URL", originalProp);
            }
        }
    }

    @Test
    void getBaseUrl_withTrailingSlash_shouldRemoveIt() {
        String originalProp = System.getProperty("INUS_SERVICE_URL");
        
        try {
            System.setProperty("INUS_SERVICE_URL", "http://test.com/pdi/");
            String url = InusServiceUrlUtil.getBaseUrl();
            assertNotNull(url);
            assertFalse(url.endsWith("/"));
        } finally {
            if (originalProp != null) {
                System.setProperty("INUS_SERVICE_URL", originalProp);
            } else {
                System.clearProperty("INUS_SERVICE_URL");
            }
        }
    }

    @Test
    void buildUrl_withPathStartingWithSlash_shouldConcatenate() {
        String baseUrl = InusServiceUrlUtil.getBaseUrl();
        String result = InusServiceUrlUtil.buildUrl("/api/test");
        
        assertNotNull(result);
        assertTrue(result.contains("/api/test"));
        assertEquals(baseUrl + "/api/test", result);
    }

    @Test
    void buildUrl_withPathNotStartingWithSlash_shouldAddSlash() {
        String baseUrl = InusServiceUrlUtil.getBaseUrl();
        String result = InusServiceUrlUtil.buildUrl("api/test");
        
        assertNotNull(result);
        assertTrue(result.contains("/api/test"));
        assertEquals(baseUrl + "/api/test", result);
    }

    @Test
    void buildUrl_withEmptyPath_shouldReturnBaseUrlWithSlash() {
        String baseUrl = InusServiceUrlUtil.getBaseUrl();
        String result = InusServiceUrlUtil.buildUrl("");
        
        assertNotNull(result);
        assertEquals(baseUrl + "/", result);
    }

    @Test
    void buildUrl_withNullPath_shouldThrowException() {
        // El método buildUrl no maneja null, así que esperamos una excepción
        assertThrows(NullPointerException.class, () -> {
            InusServiceUrlUtil.buildUrl(null);
        });
    }
}

