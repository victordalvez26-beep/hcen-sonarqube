package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para AuthToken.
 */
class AuthTokenTest {

    @Test
    void constructor_default_shouldInitializeCreatedAt() {
        AuthToken token = new AuthToken();
        
        assertNotNull(token.getCreatedAt());
        assertFalse(token.isUsed());
    }

    @Test
    void constructor_withParams_shouldSetAllFields() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        AuthToken token = new AuthToken("token-123", "jwt-token-456", "user-uid", expiresAt);
        
        assertEquals("token-123", token.getToken());
        assertEquals("jwt-token-456", token.getJwtToken());
        assertEquals("user-uid", token.getUserUid());
        assertEquals(expiresAt, token.getExpiresAt());
        assertNotNull(token.getCreatedAt());
        assertFalse(token.isUsed());
    }

    @Test
    void isExpired_futureExpiration_shouldReturnFalse() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        AuthToken token = new AuthToken("token-123", "jwt", "user", expiresAt);
        
        assertFalse(token.isExpired());
    }

    @Test
    void isExpired_pastExpiration_shouldReturnTrue() {
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);
        AuthToken token = new AuthToken("token-123", "jwt", "user", expiresAt);
        
        assertTrue(token.isExpired());
    }

    @Test
    void isValid_notUsedAndNotExpired_shouldReturnTrue() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        AuthToken token = new AuthToken("token-123", "jwt", "user", expiresAt);
        
        assertTrue(token.isValid());
    }

    @Test
    void isValid_used_shouldReturnFalse() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        AuthToken token = new AuthToken("token-123", "jwt", "user", expiresAt);
        token.setUsed(true);
        
        assertFalse(token.isValid());
    }

    @Test
    void isValid_expired_shouldReturnFalse() {
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);
        AuthToken token = new AuthToken("token-123", "jwt", "user", expiresAt);
        
        assertFalse(token.isValid());
    }

    @Test
    void setUsed_true_shouldMarkAsUsed() {
        AuthToken token = new AuthToken();
        token.setUsed(true);
        
        assertTrue(token.isUsed());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        AuthToken token = new AuthToken();
        token.setId(1L);
        token.setToken("new-token");
        token.setJwtToken("new-jwt");
        token.setUserUid("new-uid");
        LocalDateTime now = LocalDateTime.now();
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusHours(1));
        
        assertEquals(1L, token.getId());
        assertEquals("new-token", token.getToken());
        assertEquals("new-jwt", token.getJwtToken());
        assertEquals("new-uid", token.getUserUid());
        assertEquals(now, token.getCreatedAt());
        assertEquals(now.plusHours(1), token.getExpiresAt());
    }
}
