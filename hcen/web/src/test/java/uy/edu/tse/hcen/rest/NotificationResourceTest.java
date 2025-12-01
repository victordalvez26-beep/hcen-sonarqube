package uy.edu.tse.hcen.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.NotificationType;
import uy.edu.tse.hcen.model.UserNotificationPreferences;
import uy.edu.tse.hcen.repository.UserNotificationPreferencesRepository;
import uy.edu.tse.hcen.rest.dto.DeviceTokenDTO;
import uy.edu.tse.hcen.rest.dto.NotificationPreferencesDTO;
import uy.edu.tse.hcen.rest.dto.SendNotificationDTO;
import uy.edu.tse.hcen.service.NotificationService;
import uy.edu.tse.hcen.util.JWTUtil;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para NotificationResource.
 */
class NotificationResourceTest {

    @Mock
    private UserNotificationPreferencesRepository preferencesRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpServletRequest request;

    private NotificationResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new NotificationResource();
        
        Field repoField = NotificationResource.class.getDeclaredField("preferencesRepository");
        repoField.setAccessible(true);
        repoField.set(resource, preferencesRepository);
        
        Field serviceField = NotificationResource.class.getDeclaredField("notificationService");
        serviceField.setAccessible(true);
        serviceField.set(resource, notificationService);
    }

    private void setupAuthenticatedUser(String userUid, MockedStatic<JWTUtil> mockedJWTUtil) {
        String jwt = "valid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(userUid);
    }

    @Test
    void getPreferences_existingPreferences_shouldReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            UserNotificationPreferences preferences = new UserNotificationPreferences("user-uid");
            preferences.setNotifyResults(true);
            
            when(preferencesRepository.findByUserUid("user-uid")).thenReturn(preferences);
            
            Response response = resource.getPreferences(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNotNull(response.getEntity());
        }
    }

    @Test
    void getPreferences_noPreferences_shouldCreateAndReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            when(preferencesRepository.findByUserUid("user-uid")).thenReturn(null);
            
            Response response = resource.getPreferences(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(preferencesRepository).persist(any(UserNotificationPreferences.class));
        }
    }

    @Test
    void getPreferences_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.getPreferences(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void getPreferences_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("jwt_token", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.getPreferences(request);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void getPreferences_exception_shouldReturnInternalError() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            when(preferencesRepository.findByUserUid("user-uid"))
                .thenThrow(new RuntimeException("Database error"));
            
            Response response = resource.getPreferences(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void updatePreferences_existingPreferences_shouldReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            UserNotificationPreferences preferences = new UserNotificationPreferences("user-uid");
            when(preferencesRepository.findByUserUid("user-uid")).thenReturn(preferences);
            
            NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
            dto.setNotifyResults(true);
            dto.setNotifyNewAccessRequest(false);
            
            Response response = resource.updatePreferences(request, dto);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(preferencesRepository).saveOrUpdate(any(UserNotificationPreferences.class));
        }
    }

    @Test
    void updatePreferences_noPreferences_shouldCreateAndReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            when(preferencesRepository.findByUserUid("user-uid")).thenReturn(null);
            
            NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
            dto.setNotifyResults(true);
            
            Response response = resource.updatePreferences(request, dto);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(preferencesRepository).saveOrUpdate(any(UserNotificationPreferences.class));
        }
    }

    @Test
    void updatePreferences_unauthorized_shouldReturnUnauthorized() {
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.updatePreferences(request, dto);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void updatePreferences_exception_shouldReturnInternalError() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            when(preferencesRepository.findByUserUid("user-uid"))
                .thenThrow(new RuntimeException("Database error"));
            
            NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
            Response response = resource.updatePreferences(request, dto);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void registerDeviceToken_validToken_shouldReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            DeviceTokenDTO dto = new DeviceTokenDTO();
            dto.setDeviceToken("firebase-token-123");
            
            Response response = resource.registerDeviceToken(request, dto);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(preferencesRepository).updateDeviceToken(eq("user-uid"), eq("firebase-token-123"));
        }
    }

    @Test
    void registerDeviceToken_nullToken_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            DeviceTokenDTO dto = new DeviceTokenDTO();
            dto.setDeviceToken(null);
            
            Response response = resource.registerDeviceToken(request, dto);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void registerDeviceToken_emptyToken_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            DeviceTokenDTO dto = new DeviceTokenDTO();
            dto.setDeviceToken("   ");
            
            Response response = resource.registerDeviceToken(request, dto);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void registerDeviceToken_unauthorized_shouldReturnUnauthorized() {
        DeviceTokenDTO dto = new DeviceTokenDTO();
        dto.setDeviceToken("token");
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.registerDeviceToken(request, dto);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void registerDeviceToken_exception_shouldReturnInternalError() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            DeviceTokenDTO dto = new DeviceTokenDTO();
            dto.setDeviceToken("token");
            doThrow(new RuntimeException("Database error"))
                .when(preferencesRepository).updateDeviceToken(anyString(), anyString());
            
            Response response = resource.registerDeviceToken(request, dto);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void removeDeviceToken_existingPreferences_shouldReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            UserNotificationPreferences preferences = new UserNotificationPreferences("user-uid");
            preferences.setDeviceToken("existing-token");
            when(preferencesRepository.findByUserUid("user-uid")).thenReturn(preferences);
            
            Response response = resource.removeDeviceToken(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(preferencesRepository).saveOrUpdate(any(UserNotificationPreferences.class));
        }
    }

    @Test
    void removeDeviceToken_noPreferences_shouldReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            when(preferencesRepository.findByUserUid("user-uid")).thenReturn(null);
            
            Response response = resource.removeDeviceToken(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(preferencesRepository, never()).saveOrUpdate(any());
        }
    }

    @Test
    void removeDeviceToken_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.removeDeviceToken(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void removeDeviceToken_exception_shouldReturnInternalError() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUser("user-uid", mockedJWTUtil);
            
            when(preferencesRepository.findByUserUid("user-uid"))
                .thenThrow(new RuntimeException("Database error"));
            
            Response response = resource.removeDeviceToken(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void sendNotification_validData_shouldReturnOk() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("results");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        when(notificationService.sendNotification(
            eq("user-uid"), eq(NotificationType.RESULTS), eq("Test Title"), eq("Test Body")))
            .thenReturn(true);
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_serviceReturnsFalse_shouldReturnInternalError() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("results");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        when(notificationService.sendNotification(anyString(), any(), anyString(), anyString()))
            .thenReturn(false);
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_nullUserUid_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid(null);
        dto.setNotificationType("RESULTS");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_emptyUserUid_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("   ");
        dto.setNotificationType("RESULTS");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_nullNotificationType_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType(null);
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_emptyNotificationType_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("   ");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_nullTitle_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("RESULTS");
        dto.setTitle(null);
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_emptyTitle_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("RESULTS");
        dto.setTitle("   ");
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_nullBody_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("RESULTS");
        dto.setTitle("Test Title");
        dto.setBody(null);
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_emptyBody_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("RESULTS");
        dto.setTitle("Test Title");
        dto.setBody("   ");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_invalidNotificationType_shouldReturnBadRequest() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("INVALID_TYPE");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void sendNotification_exception_shouldReturnInternalError() {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserUid("user-uid");
        dto.setNotificationType("results");
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        
        when(notificationService.sendNotification(anyString(), any(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.sendNotification(dto);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}

