package uy.edu.tse.hcen.service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.service.AuthService;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para AuthService.
 * Estos tests verifican el comportamiento del servicio con mocks de dependencias.
 * Se ejecutan solo si la variable de entorno INTEGRATION_TESTS está configurada.
 */
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
class AuthServiceIntegrationTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService();
        
        Field daoField = AuthService.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(authService, userDAO);
    }

    @Test
    void exchangeCodeForTokens_withValidCode_shouldReturnTokens() {
        // Este test requiere un código de autorización válido de Gub.uy
        // En un entorno de integración real, se usaría un código de prueba
        String testCode = System.getenv("GUBUY_TEST_CODE");
        
        if (testCode == null || testCode.isEmpty()) {
            // Skip test si no hay código de prueba
            return;
        }
        
        assertDoesNotThrow(() -> {
            var response = authService.exchangeCodeForTokens(testCode);
            assertNotNull(response);
            assertNotNull(response.getAccess_token());
        });
    }

    @Test
    void getUserInfo_withValidToken_shouldReturnUser() {
        // Este test requiere un access token válido
        String testToken = System.getenv("GUBUY_TEST_TOKEN");
        
        if (testToken == null || testToken.isEmpty()) {
            // Skip test si no hay token de prueba
            return;
        }
        
        assertDoesNotThrow(() -> {
            User user = authService.getUserInfo(testToken);
            assertNotNull(user);
            assertNotNull(user.getUid());
        });
    }

    @Test
    void getUserInfo_withInvalidToken_shouldThrowException() {
        assertThrows(Exception.class, () -> {
            authService.getUserInfo("invalid-token-12345");
        });
    }

    @Test
    void exchangeCodeForTokens_withInvalidCode_shouldThrowException() {
        assertThrows(Exception.class, () -> {
            authService.exchangeCodeForTokens("invalid-code-12345");
        });
    }
}

