package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UserNotificationPreferences.
 */
class UserNotificationPreferencesTest {

    @Test
    void constructor_default_shouldInitializeCreatedAt() {
        UserNotificationPreferences prefs = new UserNotificationPreferences();
        
        assertNotNull(prefs.getCreatedAt());
        assertTrue(prefs.isNotifyResults());
        assertTrue(prefs.isNotifyNewAccessRequest());
        assertFalse(prefs.isAllDisabled());
    }

    @Test
    void constructor_withUserUid_shouldSetUserUid() {
        UserNotificationPreferences prefs = new UserNotificationPreferences("user-uid-123");
        
        assertEquals("user-uid-123", prefs.getUserUid());
        assertNotNull(prefs.getCreatedAt());
    }

    @Test
    void isNotificationEnabled_results_whenAllDisabled_shouldReturnFalse() {
        UserNotificationPreferences prefs = new UserNotificationPreferences("user-uid");
        prefs.setAllDisabled(true);
        prefs.setNotifyResults(true);
        
        assertFalse(prefs.isNotificationEnabled(NotificationType.RESULTS));
    }

    @Test
    void isNotificationEnabled_results_whenEnabled_shouldReturnTrue() {
        UserNotificationPreferences prefs = new UserNotificationPreferences("user-uid");
        prefs.setAllDisabled(false);
        prefs.setNotifyResults(true);
        
        assertTrue(prefs.isNotificationEnabled(NotificationType.RESULTS));
    }

    @Test
    void isNotificationEnabled_results_whenDisabled_shouldReturnFalse() {
        UserNotificationPreferences prefs = new UserNotificationPreferences("user-uid");
        prefs.setAllDisabled(false);
        prefs.setNotifyResults(false);
        
        assertFalse(prefs.isNotificationEnabled(NotificationType.RESULTS));
    }

    @Test
    void isNotificationEnabled_allTypes_shouldCheckCorrectFields() {
        UserNotificationPreferences prefs = new UserNotificationPreferences("user-uid");
        prefs.setAllDisabled(false);
        prefs.setNotifyResults(true);
        prefs.setNotifyNewAccessRequest(true);
        prefs.setNotifyMedicalHistory(true);
        prefs.setNotifyNewAccessHistory(true);
        prefs.setNotifyMaintenance(true);
        prefs.setNotifyNewFeatures(true);
        
        assertTrue(prefs.isNotificationEnabled(NotificationType.RESULTS));
        assertTrue(prefs.isNotificationEnabled(NotificationType.NEW_ACCESS_REQUEST));
        assertTrue(prefs.isNotificationEnabled(NotificationType.MEDICAL_HISTORY));
        assertTrue(prefs.isNotificationEnabled(NotificationType.NEW_ACCESS_HISTORY));
        assertTrue(prefs.isNotificationEnabled(NotificationType.MAINTENANCE));
        assertTrue(prefs.isNotificationEnabled(NotificationType.NEW_FEATURES));
        assertFalse(prefs.isNotificationEnabled(NotificationType.ALL_DISABLED));
    }

    @Test
    void updateDeviceToken_shouldUpdateTokenAndTimestamp() {
        UserNotificationPreferences prefs = new UserNotificationPreferences("user-uid");
        LocalDateTime before = LocalDateTime.now();
        
        prefs.updateDeviceToken("new-device-token");
        
        assertEquals("new-device-token", prefs.getDeviceToken());
        assertNotNull(prefs.getDeviceTokenUpdatedAt());
        assertNotNull(prefs.getUpdatedAt());
        assertTrue(prefs.getDeviceTokenUpdatedAt().isAfter(before.minusSeconds(1)));
    }

    @Test
    void gettersAndSetters_shouldWork() {
        UserNotificationPreferences prefs = new UserNotificationPreferences();
        prefs.setId(1L);
        prefs.setUserUid("uid-123");
        prefs.setDeviceToken("token-456");
        prefs.setNotifyResults(false);
        prefs.setNotifyNewAccessRequest(false);
        prefs.setNotifyMedicalHistory(false);
        prefs.setNotifyNewAccessHistory(false);
        prefs.setNotifyMaintenance(false);
        prefs.setNotifyNewFeatures(false);
        prefs.setAllDisabled(true);
        LocalDateTime now = LocalDateTime.now();
        prefs.setCreatedAt(now);
        prefs.setUpdatedAt(now);
        prefs.setDeviceTokenUpdatedAt(now);
        
        assertEquals(1L, prefs.getId());
        assertEquals("uid-123", prefs.getUserUid());
        assertEquals("token-456", prefs.getDeviceToken());
        assertFalse(prefs.isNotifyResults());
        assertFalse(prefs.isNotifyNewAccessRequest());
        assertFalse(prefs.isNotifyMedicalHistory());
        assertFalse(prefs.isNotifyNewAccessHistory());
        assertFalse(prefs.isNotifyMaintenance());
        assertFalse(prefs.isNotifyNewFeatures());
        assertTrue(prefs.isAllDisabled());
        assertEquals(now, prefs.getCreatedAt());
        assertEquals(now, prefs.getUpdatedAt());
        assertEquals(now, prefs.getDeviceTokenUpdatedAt());
    }
}


