package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HcenUserResponseTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        // Act
        HcenUserResponse response = new HcenUserResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getUserId());
        assertNull(response.getMensaje());
    }

    @Test
    void constructor_withParams_shouldSetFields() {
        // Arrange
        Long userId = 1L;
        String mensaje = "Usuario creado exitosamente";

        // Act
        HcenUserResponse response = new HcenUserResponse(userId, mensaje);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        // Arrange
        HcenUserResponse response = new HcenUserResponse();
        Long userId = 2L;
        String mensaje = "Test message";

        // Act
        response.setUserId(userId);
        response.setMensaje(mensaje);

        // Assert
        assertEquals(userId, response.getUserId());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void toString_shouldContainFields() {
        // Arrange
        HcenUserResponse response = new HcenUserResponse(1L, "Test");

        // Act
        String result = response.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Test"));
    }

    @Test
    void setUserId_withNull_shouldSetNull() {
        // Arrange
        HcenUserResponse response = new HcenUserResponse(1L, "Test");

        // Act
        response.setUserId(null);

        // Assert
        assertNull(response.getUserId());
    }
}

