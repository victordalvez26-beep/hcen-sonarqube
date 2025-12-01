package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceTokenDTOTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        // Act
        DeviceTokenDTO dto = new DeviceTokenDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getDeviceToken());
    }

    @Test
    void constructor_withToken_shouldSetToken() {
        // Arrange
        String token = "test-device-token-123";

        // Act
        DeviceTokenDTO dto = new DeviceTokenDTO(token);

        // Assert
        assertNotNull(dto);
        assertEquals(token, dto.getDeviceToken());
    }

    @Test
    void getterAndSetter_shouldWork() {
        // Arrange
        DeviceTokenDTO dto = new DeviceTokenDTO();
        String token = "new-device-token-456";

        // Act
        dto.setDeviceToken(token);

        // Assert
        assertEquals(token, dto.getDeviceToken());
    }

    @Test
    void setDeviceToken_withNull_shouldSetNull() {
        // Arrange
        DeviceTokenDTO dto = new DeviceTokenDTO("initial-token");

        // Act
        dto.setDeviceToken(null);

        // Assert
        assertNull(dto.getDeviceToken());
    }

    @Test
    void setDeviceToken_withEmptyString_shouldSetEmpty() {
        // Arrange
        DeviceTokenDTO dto = new DeviceTokenDTO();

        // Act
        dto.setDeviceToken("");

        // Assert
        assertEquals("", dto.getDeviceToken());
    }
}

