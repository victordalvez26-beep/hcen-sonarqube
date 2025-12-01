package uy.edu.tse.hcen.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserSessionDAO;
import uy.edu.tse.hcen.dto.TokenResponse;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.UserSession;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.mockito.MockedStatic;
import uy.edu.tse.hcen.util.JWTUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para GubUyCallbackService.
 */
class GubUyCallbackServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserSessionDAO userSessionDAO;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private HttpServletResponse response;

    private GubUyCallbackService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new GubUyCallbackService();
        
        Field authServiceField = GubUyCallbackService.class.getDeclaredField("authService");
        authServiceField.setAccessible(true);
        authServiceField.set(service, authService);
        
        Field sessionDaoField = GubUyCallbackService.class.getDeclaredField("userSessionDAO");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(service, userSessionDAO);
        
        Field tokenServiceField = GubUyCallbackService.class.getDeclaredField("authTokenService");
        tokenServiceField.setAccessible(true);
        tokenServiceField.set(service, authTokenService);
    }

    @Test
    void processCallback_withError_shouldReturnErrorUrl() throws Exception {
        String result = service.processCallback(null, "access_denied", null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=access_denied"));
        verify(authService, never()).exchangeCodeForTokens(anyString());
    }

    @Test
    void processCallback_nullCode_shouldReturnErrorUrl() throws Exception {
        String result = service.processCallback(null, null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=missing_code"));
        verify(authService, never()).exchangeCodeForTokens(anyString());
    }

    @Test
    void processCallback_emptyCode_shouldReturnErrorUrl() throws Exception {
        String result = service.processCallback("", null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=missing_code"));
        verify(authService, never()).exchangeCodeForTokens(anyString());
    }

    @Test
    void processCallback_tokenExchangeFailed_shouldReturnErrorUrl() throws Exception {
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(null);
        
        String result = service.processCallback("auth-code", null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=token_exchange_failed"));
        verify(authService).exchangeCodeForTokens("auth-code");
    }

    @Test
    void processCallback_tokenResponseNull_shouldReturnErrorUrl() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccess_token(null);
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
        
        String result = service.processCallback("auth-code", null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=token_exchange_failed"));
    }

    @Test
    void processCallback_userInfoFailed_shouldReturnErrorUrl() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccess_token("access-token-123");
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
        when(authService.getUserInfo("access-token-123")).thenReturn(null);
        
        String result = service.processCallback("auth-code", null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=userinfo_failed"));
        verify(authService).getUserInfo("access-token-123");
    }

    @Test
    void processCallback_userWithoutUid_shouldReturnErrorUrl() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccess_token("access-token-123");
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
        
        User user = new User(null, "email@example.com", "Name", null, "Surname", null, "CI", "12345678", null);
        when(authService.getUserInfo("access-token-123")).thenReturn(user);
        
        String result = service.processCallback("auth-code", null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("error=userinfo_failed"));
    }

    @Test
    void processCallback_success_shouldReturnSuccessUrl() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccess_token("access-token-123");
        tokenResponse.setRefresh_token("refresh-token-456");
        tokenResponse.setId_token("id-token-789");
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
        
        User user = new User("user-uid-123", "email@example.com", "Juan", null, "Pérez", null, "CI", "12345678", null);
        user.setRol(Rol.USUARIO_SALUD);
        when(authService.getUserInfo("access-token-123")).thenReturn(user);
        
        when(authTokenService.generateTempToken(anyString(), eq("user-uid-123")))
            .thenReturn("temp-token-123");
        UserSession mockSession = new UserSession("user-uid-123", "temp-token-123", "access-token", "refresh-token", LocalDateTime.now().plusHours(24));
        when(userSessionDAO.save(any(UserSession.class))).thenReturn(mockSession);
        doNothing().when(response).setHeader(anyString(), anyString());
        
        String result = service.processCallback("auth-code", null, null, response);
        
        assertNotNull(result);
        assertTrue(result.contains("login=success"));
        assertTrue(result.contains("token="));
        verify(userSessionDAO).save(any(UserSession.class));
        verify(response).setHeader(eq("Set-Cookie"), anyString());
        verify(authTokenService).generateTempToken(anyString(), eq("user-uid-123"));
    }

    @Test
    void processCallback_withState_shouldIncludeState() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccess_token("access-token-123");
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
        
        User user = new User("user-uid-123", "email@example.com", "Juan", null, "Pérez", null, "CI", "12345678", null);
        when(authService.getUserInfo("access-token-123")).thenReturn(user);
        
        when(authTokenService.generateTempToken(anyString(), anyString()))
            .thenReturn("temp-token-123");
        UserSession mockSession = new UserSession("user-uid-123", "temp-token-123", "access-token", "refresh-token", LocalDateTime.now().plusHours(24));
        when(userSessionDAO.save(any(UserSession.class))).thenReturn(mockSession);
        doNothing().when(response).setHeader(anyString(), anyString());
        
        String result = service.processCallback("auth-code", null, "state-xyz", response);
        
        assertNotNull(result);
        assertTrue(result.contains("state=state-xyz"));
    }

    @Test
    void processCallback_authTokenServiceNull_shouldUseJWTAsFallback() throws Exception {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccess_token("access-token-123");
            when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
            
            User user = new User("user-uid-123", "email@example.com", "Juan", null, "Pérez", null, "CI", "12345678", null);
            when(authService.getUserInfo("access-token-123")).thenReturn(user);
            
            mockedJWTUtil.when(() -> JWTUtil.generateJWT(anyString(), anyLong())).thenReturn("mock-jwt-token");
            
            Field tokenServiceField = GubUyCallbackService.class.getDeclaredField("authTokenService");
            tokenServiceField.setAccessible(true);
            uy.edu.tse.hcen.service.AuthTokenService originalService = (uy.edu.tse.hcen.service.AuthTokenService) tokenServiceField.get(service);
            tokenServiceField.set(service, null);
            
            when(userSessionDAO.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(response).setHeader(anyString(), anyString());
            
            String result = service.processCallback("auth-code", null, null, response);
            
            assertNotNull(result);
            assertTrue(result.contains("login=success"));
            assertTrue(result.contains("token="));
            
            tokenServiceField.set(service, originalService);
        }
    }

    @Test
    void processCallback_authTokenServiceException_shouldUseJWTAsFallback() throws Exception {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccess_token("access-token-123");
            when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
            
            User user = new User("user-uid-123", "email@example.com", "Juan", null, "Pérez", null, "CI", "12345678", null);
            when(authService.getUserInfo("access-token-123")).thenReturn(user);
            
            mockedJWTUtil.when(() -> JWTUtil.generateJWT(anyString(), anyLong())).thenReturn("mock-jwt-token");
            
            reset(authTokenService);
            when(authTokenService.generateTempToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Token service error"));
            
            when(userSessionDAO.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(response).setHeader(anyString(), anyString());
            
            String result = service.processCallback("auth-code", null, null, response);
            
            assertNotNull(result);
            assertTrue(result.contains("login=success"));
            assertTrue(result.contains("token="));
        }
    }

    @Test
    void processCallback_emptyState_shouldNotIncludeState() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccess_token("access-token-123");
        when(authService.exchangeCodeForTokens("auth-code")).thenReturn(tokenResponse);
        
        User user = new User("user-uid-123", "email@example.com", "Juan", null, "Pérez", null, "CI", "12345678", null);
        when(authService.getUserInfo("access-token-123")).thenReturn(user);
        
        when(authTokenService.generateTempToken(anyString(), anyString()))
            .thenReturn("temp-token-123");
        UserSession mockSession = new UserSession("user-uid-123", "temp-token-123", "access-token", "refresh-token", LocalDateTime.now().plusHours(24));
        when(userSessionDAO.save(any(UserSession.class))).thenReturn(mockSession);
        doNothing().when(response).setHeader(anyString(), anyString());
        
        String result = service.processCallback("auth-code", null, "   ", response);
        
        assertNotNull(result);
        assertFalse(result.contains("state="));
    }
}
