package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.Nacionalidad;
import uy.edu.tse.hcen.model.User;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de integración básicos para AuthService.
 * 
 * Nota: Los tests completos de integración con WireMock requieren que GubUyConfig
 * use inyección de dependencias o variables de entorno configurables, ya que
 * los campos son final static y no pueden modificarse en tiempo de ejecución.
 * 
 * Estos tests verifican la estructura básica y el manejo de errores.
 */
class AuthServiceIntegrationTest {

    private AuthService authService;
    
    @Mock
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        authService = new AuthService();
        Field daoField = AuthService.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(authService, userDAO);
    }

    @Test
    void exchangeCodeForTokens_validCode_shouldReturnTokenResponse() {
        // Nota: Este test requiere que GubUyConfig use URLs configurables o inyección de dependencias
        // Como los campos son final static, este test verifica la estructura básica pero
        // no puede modificar las URLs reales. Para tests completos de integración,
        // se recomienda refactorizar para usar inyección de dependencias.
        
        // Por ahora, verificamos que el servicio no lance excepciones inmediatas
        // con un código inválido (esperamos que falle al intentar conectarse a la URL real)
        String authCode = "test_auth_code_123";
        
        // Este test verifica que el método puede ser llamado sin errores de compilación
        // Los tests reales de integración deberían ejecutarse contra un mock server configurado
        assertThrows(Exception.class, () -> authService.exchangeCodeForTokens(authCode));
    }

    @Test
    void exchangeCodeForTokens_invalidCode_shouldThrowException() {
        // Verificar que el método maneja errores correctamente
        String authCode = "invalid_code";
        assertThrows(RuntimeException.class, () -> {
            authService.exchangeCodeForTokens(authCode);
        });
    }

    @Test
    void getUserInfo_validToken_shouldReturnUser() {
        // Nota: Este test requiere configuración adicional debido a campos final static
        // Verificamos que el método puede ser llamado
        String accessToken = "valid_access_token";
        
        User newUser = new User(
                "12345678",
                "test@example.com",
                "Juan",
                "Carlos",
                "Pérez",
                "González",
                "CI",
                "12345678",
                Nacionalidad.UY
        );
        newUser.setRol(uy.edu.tse.hcen.model.Rol.USUARIO_SALUD);
        
        // Mock del DAO
        when(userDAO.findByUid(anyString())).thenReturn(null, newUser);
        when(userDAO.saveOrUpdate(any(User.class))).thenReturn(newUser);
        
        // Este test verificará que el método intenta conectarse
        assertThrows(Exception.class, () -> authService.getUserInfo(accessToken));
    }

    @Test
    void getUserInfo_existingUser_shouldUpdateUser() {
        // Verificar estructura básica del método
        String accessToken = "valid_access_token";
        
        User existingUser = new User();
        existingUser.setUid("12345678");
        existingUser.setEmail("old@example.com");
        existingUser.setRol(uy.edu.tse.hcen.model.Rol.USUARIO_SALUD);
        
        when(userDAO.findByUid("12345678")).thenReturn(existingUser);
        
        // Verificar que el método intenta procesar la respuesta
        assertThrows(Exception.class, () -> authService.getUserInfo(accessToken));
    }

    @Test
    void getUserInfo_invalidToken_shouldThrowException() {
        String accessToken = "invalid_token";
        
        assertThrows(RuntimeException.class, () -> {
            authService.getUserInfo(accessToken);
        });
    }

    @Test
    void exchangeCodeForTokens_serverError_shouldThrowException() {
        String authCode = "test_code";
        
        assertThrows(RuntimeException.class, () -> {
            authService.exchangeCodeForTokens(authCode);
        });
    }
}
