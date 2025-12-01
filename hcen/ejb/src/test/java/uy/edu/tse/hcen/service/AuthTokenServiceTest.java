package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.AuthTokenDAO;
import uy.edu.tse.hcen.model.AuthToken;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthTokenService.
 */
class AuthTokenServiceTest {

    @Mock
    private AuthTokenDAO authTokenDAO;

    private AuthTokenService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new AuthTokenService();
        
        // Inyectar mock usando reflection
        java.lang.reflect.Field daoField = AuthTokenService.class.getDeclaredField("authTokenDAO");
        daoField.setAccessible(true);
        daoField.set(service, authTokenDAO);
    }

    @Test
    void generateTempToken_validInput_shouldGenerateAndSave() {
        String jwtToken = "test_jwt_token";
        String userUid = "user123";
        
        doAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            assertNotNull(token.getToken());
            assertNotNull(token.getExpiresAt());
            assertFalse(token.isUsed());
            assertFalse(token.isExpired());
            return null;
        }).when(authTokenDAO).save(any(AuthToken.class));
        
        String tempToken = service.generateTempToken(jwtToken, userUid);
        
        assertNotNull(tempToken);
        assertFalse(tempToken.isEmpty());
        verify(authTokenDAO).save(any(AuthToken.class));
    }

    @Test
    void exchangeToken_validToken_shouldReturnJWT() {
        String tempToken = "valid_temp_token";
        String jwtToken = "test_jwt_token";
        String userUid = "user123";
        
        AuthToken authToken = new AuthToken(
            tempToken,
            jwtToken,
            userUid,
            LocalDateTime.now().plusSeconds(60)
        );
        
        when(authTokenDAO.findByToken(tempToken)).thenReturn(authToken);
        
        String result = service.exchangeToken(tempToken);
        
        assertEquals(jwtToken, result);
        verify(authTokenDAO).findByToken(tempToken);
        verify(authTokenDAO).markAsUsed(tempToken);
    }

    @Test
    void exchangeToken_nullToken_shouldReturnNull() {
        String result = service.exchangeToken(null);
        
        assertNull(result);
        verify(authTokenDAO, never()).findByToken(anyString());
    }

    @Test
    void exchangeToken_emptyToken_shouldReturnNull() {
        String result = service.exchangeToken("");
        
        assertNull(result);
        verify(authTokenDAO, never()).findByToken(anyString());
    }

    @Test
    void exchangeToken_tokenNotFound_shouldReturnNull() {
        String tempToken = "non_existent_token";
        
        when(authTokenDAO.findByToken(tempToken)).thenReturn(null);
        
        String result = service.exchangeToken(tempToken);
        
        assertNull(result);
        verify(authTokenDAO).findByToken(tempToken);
        verify(authTokenDAO, never()).markAsUsed(anyString());
    }

    @Test
    void exchangeToken_expiredToken_shouldReturnNull() {
        String tempToken = "expired_token";
        AuthToken authToken = new AuthToken(
            tempToken,
            "jwt_token",
            "user123",
            LocalDateTime.now().minusSeconds(1) // Expirado
        );
        
        when(authTokenDAO.findByToken(tempToken)).thenReturn(authToken);
        
        String result = service.exchangeToken(tempToken);
        
        assertNull(result);
        verify(authTokenDAO, never()).markAsUsed(anyString());
    }

    @Test
    void exchangeToken_usedToken_shouldReturnNull() {
        String tempToken = "used_token";
        AuthToken authToken = new AuthToken(
            tempToken,
            "jwt_token",
            "user123",
            LocalDateTime.now().plusSeconds(60)
        );
        authToken.setUsed(true);
        
        when(authTokenDAO.findByToken(tempToken)).thenReturn(authToken);
        
        String result = service.exchangeToken(tempToken);
        
        assertNull(result);
        verify(authTokenDAO, never()).markAsUsed(anyString());
    }
}

