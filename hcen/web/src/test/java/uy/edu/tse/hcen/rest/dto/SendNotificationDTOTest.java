package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SendNotificationDTOTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        // Act
        SendNotificationDTO dto = new SendNotificationDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getUserUid());
        assertNull(dto.getNotificationType());
        assertNull(dto.getTitle());
        assertNull(dto.getBody());
    }

    @Test
    void constructor_withAllParams_shouldSetAllFields() {
        // Arrange
        String userUid = "user123";
        String notificationType = "RESULT";
        String title = "Test Title";
        String body = "Test Body";

        // Act
        SendNotificationDTO dto = new SendNotificationDTO(userUid, notificationType, title, body);

        // Assert
        assertNotNull(dto);
        assertEquals(userUid, dto.getUserUid());
        assertEquals(notificationType, dto.getNotificationType());
        assertEquals(title, dto.getTitle());
        assertEquals(body, dto.getBody());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO();
        String userUid = "user456";
        String notificationType = "ACCESS_REQUEST";
        String title = "New Title";
        String body = "New Body";

        // Act
        dto.setUserUid(userUid);
        dto.setNotificationType(notificationType);
        dto.setTitle(title);
        dto.setBody(body);

        // Assert
        assertEquals(userUid, dto.getUserUid());
        assertEquals(notificationType, dto.getNotificationType());
        assertEquals(title, dto.getTitle());
        assertEquals(body, dto.getBody());
    }

    @Test
    void setUserUid_withNull_shouldSetNull() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO("user", "type", "title", "body");

        // Act
        dto.setUserUid(null);

        // Assert
        assertNull(dto.getUserUid());
    }

    @Test
    void setNotificationType_withNull_shouldSetNull() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO("user", "type", "title", "body");

        // Act
        dto.setNotificationType(null);

        // Assert
        assertNull(dto.getNotificationType());
    }
}

