package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPreferencesDTOTest {

    @Test
    void constructor_default_shouldSetDefaultValues() {
        // Act
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Assert
        assertNotNull(dto);
        assertTrue(dto.isNotifyResults());
        assertTrue(dto.isNotifyNewAccessRequest());
        assertTrue(dto.isNotifyMedicalHistory());
        assertTrue(dto.isNotifyNewAccessHistory());
        assertTrue(dto.isNotifyMaintenance());
        assertTrue(dto.isNotifyNewFeatures());
        assertFalse(dto.isAllDisabled());
    }

    @Test
    void settersAndGetters_shouldWork() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyResults(false);
        dto.setNotifyNewAccessRequest(false);
        dto.setNotifyMedicalHistory(false);
        dto.setNotifyNewAccessHistory(false);
        dto.setNotifyMaintenance(false);
        dto.setNotifyNewFeatures(false);
        dto.setAllDisabled(true);

        // Assert
        assertFalse(dto.isNotifyResults());
        assertFalse(dto.isNotifyNewAccessRequest());
        assertFalse(dto.isNotifyMedicalHistory());
        assertFalse(dto.isNotifyNewAccessHistory());
        assertFalse(dto.isNotifyMaintenance());
        assertFalse(dto.isNotifyNewFeatures());
        assertTrue(dto.isAllDisabled());
    }

    @Test
    void setNotifyResults_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyResults(false);

        // Assert
        assertFalse(dto.isNotifyResults());
    }

    @Test
    void setNotifyNewAccessRequest_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyNewAccessRequest(false);

        // Assert
        assertFalse(dto.isNotifyNewAccessRequest());
    }

    @Test
    void setNotifyMedicalHistory_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyMedicalHistory(false);

        // Assert
        assertFalse(dto.isNotifyMedicalHistory());
    }

    @Test
    void setNotifyNewAccessHistory_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyNewAccessHistory(false);

        // Assert
        assertFalse(dto.isNotifyNewAccessHistory());
    }

    @Test
    void setNotifyMaintenance_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyMaintenance(false);

        // Assert
        assertFalse(dto.isNotifyMaintenance());
    }

    @Test
    void setNotifyNewFeatures_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setNotifyNewFeatures(false);

        // Assert
        assertFalse(dto.isNotifyNewFeatures());
    }

    @Test
    void setAllDisabled_shouldUpdateValue() {
        // Arrange
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();

        // Act
        dto.setAllDisabled(true);

        // Assert
        assertTrue(dto.isAllDisabled());
    }
}

