package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UserSession.
 */
class UserSessionTest {

    @Test
    void constructor_default_shouldInitializeCreatedAt() {
        UserSession session = new UserSession();
        
        assertNotNull(session.getCreatedAt());
    }

    @Test
    void constructor_withParams_shouldSetAllFields() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        UserSession session = new UserSession(
            "user-uid-123",
            "jwt-token-456",
            "access-token-789",
            "refresh-token-abc",
            expiresAt
        );
        
        assertEquals("user-uid-123", session.getUserUid());
        assertEquals("jwt-token-456", session.getJwtToken());
        assertEquals("access-token-789", session.getAccessToken());
        assertEquals("refresh-token-abc", session.getRefreshToken());
        assertEquals(expiresAt, session.getExpiresAt());
        assertNotNull(session.getCreatedAt());
    }

    @Test
    void isExpired_futureExpiration_shouldReturnFalse() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        UserSession session = new UserSession("uid", "jwt", "access", "refresh", expiresAt);
        
        assertFalse(session.isExpired());
    }

    @Test
    void isExpired_pastExpiration_shouldReturnTrue() {
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);
        UserSession session = new UserSession("uid", "jwt", "access", "refresh", expiresAt);
        
        assertTrue(session.isExpired());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        UserSession session = new UserSession();
        session.setId(1L);
        session.setUserUid("new-uid");
        session.setJwtToken("new-jwt");
        session.setAccessToken("new-access");
        session.setRefreshToken("new-refresh");
        session.setIdToken("new-id-token");
        LocalDateTime now = LocalDateTime.now();
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusHours(1));
        
        assertEquals(1L, session.getId());
        assertEquals("new-uid", session.getUserUid());
        assertEquals("new-jwt", session.getJwtToken());
        assertEquals("new-access", session.getAccessToken());
        assertEquals("new-refresh", session.getRefreshToken());
        assertEquals("new-id-token", session.getIdToken());
        assertEquals(now, session.getCreatedAt());
        assertEquals(now.plusHours(1), session.getExpiresAt());
    }
}
