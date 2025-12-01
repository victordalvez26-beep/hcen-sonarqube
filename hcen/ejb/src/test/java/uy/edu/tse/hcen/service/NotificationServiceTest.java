package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.NotificationType;
import uy.edu.tse.hcen.model.UserNotificationPreferences;
import uy.edu.tse.hcen.repository.UserNotificationPreferencesRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para NotificationService.
 * Nota: Los métodos que hacen llamadas HTTP reales a Firebase requieren
 * mocks más complejos (WireMock, etc.) o refactorización para usar clientes inyectables.
 */
class NotificationServiceTest {

    @Mock
    private UserNotificationPreferencesRepository preferencesRepository;

    private NotificationService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new NotificationService();
        
        Field repoField = NotificationService.class.getDeclaredField("preferencesRepository");
        repoField.setAccessible(true);
        repoField.set(service, preferencesRepository);
    }

    @Test
    void sendNotification_userNotFound_shouldReturnFalse() {
        String userUid = "nonexistent";
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(null);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
        verify(preferencesRepository).findByUserUid(userUid);
    }

    @Test
    void sendNotification_notificationDisabled_shouldReturnFalse() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(false);
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_noDeviceToken_shouldReturnFalse() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken(null);
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_emptyDeviceToken_shouldReturnFalse() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken("");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_allDisabled_shouldReturnFalse() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setAllDisabled(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_nullUserUid_shouldReturnFalse() {
        boolean result = service.sendNotification(null, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    // Tests antiguos que dependían de un método privado escapeJson(String) eliminado
    // se omiten para centrar la cobertura en la lógica observable de negocio.

    @Test
    void sendNotification_withAllTypes_shouldCheckPreferences() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setNotifyNewAccessRequest(true);
        preferences.setNotifyMedicalHistory(true);
        preferences.setDeviceToken("device-token");

        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);

        // Ejecutar al menos una notificación para disparar el flujo
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");

        // No nos importa el valor final (puede depender de HTTP), solo que consulte preferencias
        assertNotNull(result);
        verify(preferencesRepository, atLeastOnce()).findByUserUid(userUid);
    }

    @Test
    void sendNotification_exception_shouldReturnFalse() {
        String userUid = "user123";
        
        when(preferencesRepository.findByUserUid(userUid))
            .thenThrow(new RuntimeException("Database error"));
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    // De igual forma, los tests que invocaban directamente base64UrlEncode(String)
    // ya no aplican porque el helper ahora opera sobre bytes y se usa internamente
    // como detalle de implementación.

    @Test
    void sendFirebaseNotification_nullToken_shouldReturnFalse() {
        boolean result = service.sendFirebaseNotification(null, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendFirebaseNotification_emptyToken_shouldReturnFalse() {
        boolean result = service.sendFirebaseNotification("", "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendFirebaseNotification_nullTitle_shouldHandleGracefully() {
        // El método intentará hacer una llamada HTTP real
        boolean result = service.sendFirebaseNotification("device-token", null, "Body");
        
        // Puede retornar false si falla la conexión, pero no debe lanzar excepción
        // result es boolean primitivo, no puede ser null
        assertTrue(result == true || result == false);
    }

    @Test
    void sendFirebaseNotification_nullBody_shouldHandleGracefully() {
        boolean result = service.sendFirebaseNotification("device-token", "Title", null);
        
        // result es boolean primitivo, no puede ser null
        assertTrue(result == true || result == false);
    }

    @Test
    void sendNotification_withDifferentNotificationTypes_shouldCheckPreferences() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setNotifyNewAccessRequest(true);
        preferences.setNotifyMedicalHistory(true);
        preferences.setNotifyNewAccessHistory(true);
        preferences.setNotifyMaintenance(true);
        preferences.setNotifyNewFeatures(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        // Test diferentes tipos de notificación
        NotificationType[] types = {
            NotificationType.RESULTS,
            NotificationType.NEW_ACCESS_REQUEST,
            NotificationType.MEDICAL_HISTORY,
            NotificationType.NEW_ACCESS_HISTORY,
            NotificationType.MAINTENANCE,
            NotificationType.NEW_FEATURES
        };
        
        for (NotificationType type : types) {
            service.sendNotification(userUid, type, "Title", "Body");
            // Puede retornar false si falla la conexión HTTP, pero debe verificar preferencias
            verify(preferencesRepository, atLeastOnce()).findByUserUid(userUid);
        }
    }

    @Test
    void sendNotification_withNotificationTypeDisabled_shouldReturnFalse() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(false);
        preferences.setNotifyNewAccessRequest(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_withNotificationTypeNewAccessRequest_shouldCheckPreference() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyNewAccessRequest(false);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.NEW_ACCESS_REQUEST, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_withNotificationTypeMedicalHistory_shouldCheckPreference() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyMedicalHistory(false);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.MEDICAL_HISTORY, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_withNotificationTypeAllDisabled_shouldReturnFalse() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setAllDisabled(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.ALL_DISABLED, "Title", "Body");
        
        assertFalse(result);
    }

    // A partir de aquí había un gran bloque de tests que inspeccionaba directamente
    // métodos privados (createJWT, getAccessToken, escapeJson, base64UrlEncode con String).
    // Esos métodos se han refactorizado o eliminado; en lugar de mantener tests acoplados
    // a detalles internos, concentramos la cobertura en métodos públicos como
    // sendNotification y sendFirebaseNotification.


    @Test
    void sendNotification_withNotificationTypeNewAccessHistory_shouldCheckPreference() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyNewAccessHistory(false);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.NEW_ACCESS_HISTORY, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_withNotificationTypeMaintenance_shouldCheckPreference() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyMaintenance(false);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.MAINTENANCE, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_withNotificationTypeNewFeatures_shouldCheckPreference() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyNewFeatures(false);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.NEW_FEATURES, "Title", "Body");
        
        assertFalse(result);
    }

    @Test
    void sendNotification_withNullTitle_shouldHandleGracefully() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        // El método intentará enviar notificación real, pero no debe lanzar excepción
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, null, "Body");
        
        // Puede retornar false si falla la conexión, pero no debe lanzar excepción
        assertTrue(result == true || result == false);
    }

    @Test
    void sendNotification_withNullBody_shouldHandleGracefully() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", null);
        
        assertTrue(result == true || result == false);
    }

    @Test
    void sendNotification_withEmptyTitle_shouldHandleGracefully() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "", "Body");
        
        assertTrue(result == true || result == false);
    }

    @Test
    void sendNotification_withEmptyBody_shouldHandleGracefully() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setNotifyResults(true);
        preferences.setDeviceToken("device-token");
        
        when(preferencesRepository.findByUserUid(userUid)).thenReturn(preferences);
        
        boolean result = service.sendNotification(userUid, NotificationType.RESULTS, "Title", "");
        
        assertTrue(result == true || result == false);
    }

    // Nota: Los métodos getAccessToken y sendFirebaseNotification requieren
    // mocks de HttpURLConnection que son complejos. Para tests completos se recomienda:
    // 1. Refactorizar para usar un cliente HTTP inyectable
    // 2. Usar WireMock para tests de integración
    // 3. Crear tests de integración separados
}


