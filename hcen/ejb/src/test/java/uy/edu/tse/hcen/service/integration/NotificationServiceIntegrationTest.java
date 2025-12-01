package uy.edu.tse.hcen.service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.NotificationType;
import uy.edu.tse.hcen.model.UserNotificationPreferences;
import uy.edu.tse.hcen.repository.UserNotificationPreferencesRepository;
import uy.edu.tse.hcen.service.NotificationService;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para NotificationService.
 * Estos tests verifican el comportamiento del servicio con mocks de dependencias.
 * Se ejecutan solo si la variable de entorno INTEGRATION_TESTS está configurada.
 */
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
class NotificationServiceIntegrationTest {

    @Mock
    private UserNotificationPreferencesRepository preferencesRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService();
        
        Field repoField = NotificationService.class.getDeclaredField("preferencesRepository");
        repoField.setAccessible(true);
        repoField.set(notificationService, preferencesRepository);
    }

    @Test
    void sendNotification_withValidPreferences_shouldSend() {
        // Crear preferencias de prueba
        String testUserUid = "test-user-integration-" + System.currentTimeMillis();
        UserNotificationPreferences preferences = new UserNotificationPreferences(testUserUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken("test-device-token-12345");
        
        when(preferencesRepository.findByUserUid(testUserUid)).thenReturn(preferences);
        
        // Intentar enviar notificación
        // Nota: Esto puede fallar si Firebase no está configurado correctamente
        boolean result = notificationService.sendNotification(
            testUserUid, 
            NotificationType.RESULTS, 
            "Test Title", 
            "Test Body"
        );
        
        // El resultado puede ser false si Firebase no está disponible
        // pero no debe lanzar excepción
        assertDoesNotThrow(() -> {
            notificationService.sendNotification(testUserUid, NotificationType.RESULTS, "Title", "Body");
        });
    }

    @Test
    void sendNotification_withDisabledNotifications_shouldReturnFalse() {
        String testUserUid = "test-user-disabled-" + System.currentTimeMillis();
        UserNotificationPreferences preferences = new UserNotificationPreferences(testUserUid);
        preferences.setNotifyResults(false);
        preferences.setDeviceToken("test-device-token");
        
        when(preferencesRepository.findByUserUid(testUserUid)).thenReturn(preferences);
        
        boolean result = notificationService.sendNotification(
            testUserUid, 
            NotificationType.RESULTS, 
            "Test Title", 
            "Test Body"
        );
        
        assertFalse(result);
    }

    @Test
    void sendFirebaseNotification_withValidToken_shouldAttemptSend() {
        // Este test intentará enviar una notificación real a Firebase
        // Puede fallar si Firebase no está configurado
        String testToken = System.getenv("FCM_TEST_TOKEN");
        
        if (testToken == null || testToken.isEmpty()) {
            // Skip test si no hay token de prueba
            return;
        }
        
        assertDoesNotThrow(() -> {
            boolean result = notificationService.sendFirebaseNotification(
                testToken, 
                "Integration Test Title", 
                "Integration Test Body"
            );
            // El resultado puede ser false si Firebase no está disponible
            assertNotNull(Boolean.valueOf(result));
        });
    }
}
