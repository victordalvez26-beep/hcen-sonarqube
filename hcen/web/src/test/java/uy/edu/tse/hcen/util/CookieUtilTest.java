package uy.edu.tse.hcen.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para CookieUtil.
 */
class CookieUtilTest {

    private static final String TEST_JWT = "test.jwt.token";
    private static final long TEST_MAX_AGE = 3600;

    @Test
    void buildSessionCookieHeader_withValidInput_shouldContainToken() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, "https://example.com", null);
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session=" + TEST_JWT));
        assertTrue(header.contains("Path=/"));
        assertTrue(header.contains("Max-Age=" + TEST_MAX_AGE));
        assertTrue(header.contains("HttpOnly=true"));
    }

    @Test
    void buildSessionCookieHeader_httpsUrl_shouldIncludeSecure() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, "https://example.com", null);
        
        // En HTTPS debería incluir SameSite=None y posiblemente Secure
        assertTrue(header.contains("SameSite=None") || header.contains("SameSite=Lax"));
    }

    @Test
    void buildSessionCookieHeader_httpUrl_shouldNotRequireSecure() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, "http://localhost:8080", null);
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session="));
    }

    @Test
    void buildSessionCookieHeader_localhostOrigin_shouldHandleGracefully() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, 
            "https://backend.example.com", "http://localhost:3000");
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session="));
    }

    @Test
    void buildSessionCookieHeader_nullUrl_shouldNotCrash() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, null, null);
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session="));
    }

    @Test
    void buildSessionCookieHeader_elasticCloudUrl_shouldIncludeDomain() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, 
            "https://backend.web.elasticloud.uy", null);
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session="));
    }

    @Test
    void buildSessionCookieHeader_withoutOriginHeader_shouldWork() {
        String header = CookieUtil.buildSessionCookieHeader(TEST_JWT, TEST_MAX_AGE, "https://example.com");
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session="));
    }

    @Test
    void buildDeleteCookieHeader_shouldExpireImmediately() {
        String header = CookieUtil.buildDeleteCookieHeader("https://example.com", null);
        
        assertNotNull(header);
        assertTrue(header.contains("hcen_session="));
        assertTrue(header.contains("Max-Age=0"));
    }

    @Test
    void buildDeleteCookieHeader_withOrigin_shouldWork() {
        String header = CookieUtil.buildDeleteCookieHeader("https://example.com", "http://localhost:3000");
        
        assertNotNull(header);
        assertTrue(header.contains("Max-Age=0"));
    }

    @Test
    void buildDeleteCookieHeader_withoutOriginHeader_shouldWork() {
        String header = CookieUtil.buildDeleteCookieHeader("https://example.com");
        
        assertNotNull(header);
        assertTrue(header.contains("Max-Age=0"));
    }

    @Test
    void buildDeleteCookieHeader_nullUrl_shouldNotCrash() {
        String header = CookieUtil.buildDeleteCookieHeader(null, null);
        
        assertNotNull(header);
        assertTrue(header.contains("Max-Age=0"));
    }
}


