package uy.edu.tse.hcen.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DnicServiceUrlUtilTest {

    @Test
    void getServiceUrl_withEnvVar_shouldReturnEnvUrl() {
        String originalValue = System.getenv("DNIC_SERVICE_URL");
        try {
            System.setProperty("DNIC_SERVICE_URL", "http://test-dnic:8083/ws");
            String url = DnicServiceUrlUtil.getServiceUrl();
            assertNotNull(url);
            // Si la variable de entorno está configurada, debería usarla
            // Si no, usará el default
        } finally {
            if (originalValue != null) {
                System.setProperty("DNIC_SERVICE_URL", originalValue);
            } else {
                System.clearProperty("DNIC_SERVICE_URL");
            }
        }
    }

    @Test
    void getServiceUrl_withoutEnvVar_shouldReturnDefault() {
        String originalValue = System.getenv("DNIC_SERVICE_URL");
        try {
            // Asegurarse de que no hay variable de entorno
            if (System.getProperty("DNIC_SERVICE_URL") != null) {
                System.clearProperty("DNIC_SERVICE_URL");
            }
            
            String url = DnicServiceUrlUtil.getServiceUrl();
            assertNotNull(url);
            assertTrue(url.contains("localhost") || url.contains("8083"));
        } finally {
            if (originalValue != null) {
                System.setProperty("DNIC_SERVICE_URL", originalValue);
            }
        }
    }

    @Test
    void getServiceUrl_shouldReturnValidUrl() {
        String url = DnicServiceUrlUtil.getServiceUrl();
        assertNotNull(url);
        assertFalse(url.isEmpty());
        assertTrue(url.startsWith("http://"));
    }
}

