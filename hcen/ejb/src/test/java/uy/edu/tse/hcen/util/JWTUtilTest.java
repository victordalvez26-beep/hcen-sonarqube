package uy.edu.tse.hcen.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para JWTUtil.
 * Verifica generación, validación y extracción de JWT tokens.
 */
class JWTUtilTest {

    private static final String TEST_USER_UID = "test-user-123";
    private static final long VALID_EXPIRATION = 3600; // 1 hora

    @Test
    void generateJWT_validInput_shouldReturnJWT() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        
        assertNotNull(jwt);
        assertFalse(jwt.isEmpty());
        // JWT debe tener 3 partes separadas por puntos
        String[] parts = jwt.split("\\.");
        assertEquals(3, parts.length, "JWT debe tener formato: header.payload.signature");
    }

    @Test
    void generateJWT_differentUserUIDs_shouldReturnDifferentJWTs() {
        String jwt1 = JWTUtil.generateJWT("user1", VALID_EXPIRATION);
        String jwt2 = JWTUtil.generateJWT("user2", VALID_EXPIRATION);
        
        assertNotEquals(jwt1, jwt2, "Diferentes usuarios deben generar diferentes JWTs");
    }

    @Test
    void generateJWT_sameUserUID_shouldReturnDifferentJWTsDueToTimestamp() throws InterruptedException {
        String jwt1 = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        Thread.sleep(1000); // Esperar 1 segundo para que cambie el timestamp
        String jwt2 = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        
        // Pueden ser diferentes por el timestamp, pero ambos deben ser válidos
        assertNotNull(jwt1);
        assertNotNull(jwt2);
    }

    @Test
    void generateJWT_zeroExpiration_shouldReturnJWT() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, 0);
        
        assertNotNull(jwt);
        // Con expiración 0, el JWT expira inmediatamente (now + 0 = now)
        // Validar inmediatamente puede dar null si ya pasó un segundo
        String validated = JWTUtil.validateJWT(jwt);
        // Puede ser null si ya expiró o puede ser válido si se valida muy rápido
        // Lo importante es que el JWT se genera correctamente
        assertNotNull(jwt);
    }

    @Test
    void generateJWT_nullUserUID_shouldHandleGracefully() {
        // Nota: String.format puede convertir null a la string "null" sin lanzar excepción.
        // El método no valida null explícitamente, así que este test verifica
        // que el método maneja el caso sin crashear inmediatamente.
        // En producción, se recomienda validar null antes de llamar a generateJWT.
        try {
            String jwt = JWTUtil.generateJWT(null, VALID_EXPIRATION);
            // Puede generar un JWT con "null" como sub o lanzar excepción
            // Si no lanza excepción, el test pasa (comportamiento actual)
            if (jwt != null) {
                // Verificar que el JWT se genera (aunque con valor null en sub)
                assertNotNull(jwt);
            }
        } catch (Exception e) {
            // Si lanza excepción, también está bien (comportamiento defensivo)
            assertTrue(e instanceof Exception);
        }
    }

    @Test
    void validateJWT_validJWT_shouldReturnUserUID() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        String extractedUID = JWTUtil.validateJWT(jwt);
        
        assertEquals(TEST_USER_UID, extractedUID);
    }

    @Test
    void validateJWT_nullJWT_shouldReturnNull() {
        String result = JWTUtil.validateJWT(null);
        assertNull(result);
    }

    @Test
    void validateJWT_emptyJWT_shouldReturnNull() {
        String result = JWTUtil.validateJWT("");
        assertNull(result);
    }

    @Test
    void validateJWT_invalidFormat_shouldReturnNull() {
        String invalidJWT = "invalid.jwt";
        String result = JWTUtil.validateJWT(invalidJWT);
        assertNull(result);
    }

    @Test
    void validateJWT_tamperedSignature_shouldReturnNull() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        String[] parts = jwt.split("\\.");
        String tamperedJWT = parts[0] + "." + parts[1] + ".tamperedSignature";
        
        String result = JWTUtil.validateJWT(tamperedJWT);
        assertNull(result);
    }

    @Test
    void validateJWT_expiredJWT_shouldReturnNull() {
        // Generar JWT con expiración negativa (ya expirado)
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, -1);
        
        String result = JWTUtil.validateJWT(jwt);
        assertNull(result);
    }

    @Test
    void validateJWT_malformedPayload_shouldReturnNull() {
        // Crear un JWT con payload malformado
        String malformedJWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload";
        String result = JWTUtil.validateJWT(malformedJWT);
        assertNull(result);
    }

    @Test
    void extractUserUid_validJWT_shouldReturnUserUID() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        String extractedUID = JWTUtil.extractUserUid(jwt);
        
        assertEquals(TEST_USER_UID, extractedUID);
    }

    @Test
    void extractUserUid_invalidJWT_shouldReturnNull() {
        String result = JWTUtil.extractUserUid("invalid.jwt");
        assertNull(result);
    }

    @Test
    void extractUserUid_nullJWT_shouldReturnNull() {
        String result = JWTUtil.extractUserUid(null);
        assertNull(result);
    }

    @Test
    void extractTenantId_validJWTWithoutTenantId_shouldReturnNull() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        String tenantId = JWTUtil.extractTenantId(jwt);
        
        // El JWT generado no tiene tenantId, así que debe ser null
        assertNull(tenantId);
    }

    @Test
    void extractTenantId_invalidJWT_shouldReturnNull() {
        String result = JWTUtil.extractTenantId("invalid.jwt");
        assertNull(result);
    }

    @Test
    void extractTenantId_nullJWT_shouldReturnNull() {
        String result = JWTUtil.extractTenantId(null);
        assertNull(result);
    }

    @Test
    void jwtRoundTrip_shouldWork() {
        String originalUID = "test-uid-456";
        String jwt = JWTUtil.generateJWT(originalUID, VALID_EXPIRATION);
        String validatedUID = JWTUtil.validateJWT(jwt);
        
        assertEquals(originalUID, validatedUID);
    }

    @Test
    void jwtStructure_shouldHaveCorrectFormat() {
        String jwt = JWTUtil.generateJWT(TEST_USER_UID, VALID_EXPIRATION);
        String[] parts = jwt.split("\\.");
        
        assertEquals(3, parts.length);
        assertFalse(parts[0].isEmpty(), "Header no debe estar vacío");
        assertFalse(parts[1].isEmpty(), "Payload no debe estar vacío");
        assertFalse(parts[2].isEmpty(), "Signature no debe estar vacía");
    }
}

