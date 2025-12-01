package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para NotificationType enum.
 */
class NotificationTypeTest {

    @Test
    void getCode_shouldReturnCorrectCode() {
        assertEquals("results", NotificationType.RESULTS.getCode());
        assertEquals("new_access_request", NotificationType.NEW_ACCESS_REQUEST.getCode());
        assertEquals("all_disabled", NotificationType.ALL_DISABLED.getCode());
    }

    @Test
    void getDescription_shouldReturnCorrectDescription() {
        assertEquals("Resultados de Laboratorio", NotificationType.RESULTS.getDescription());
        assertEquals("Nuevo Pedido de Acceso", NotificationType.NEW_ACCESS_REQUEST.getDescription());
        assertNotNull(NotificationType.ALL_DISABLED.getDescription());
    }

    @Test
    void fromCode_validCode_shouldReturnType() {
        assertEquals(NotificationType.RESULTS, NotificationType.fromCode("results"));
        assertEquals(NotificationType.NEW_ACCESS_REQUEST, NotificationType.fromCode("new_access_request"));
        assertEquals(NotificationType.MEDICAL_HISTORY, NotificationType.fromCode("medical_history"));
        assertEquals(NotificationType.ALL_DISABLED, NotificationType.fromCode("all_disabled"));
    }

    @Test
    void fromCode_invalidCode_shouldReturnNull() {
        assertNull(NotificationType.fromCode("invalid_code"));
        assertNull(NotificationType.fromCode(null));
        assertNull(NotificationType.fromCode(""));
    }

    @Test
    void fromCode_caseSensitive_shouldReturnNull() {
        assertNull(NotificationType.fromCode("RESULTS"));
        assertNull(NotificationType.fromCode("Results"));
    }

    @Test
    void values_shouldContainAllTypes() {
        NotificationType[] types = NotificationType.values();
        assertEquals(7, types.length);
    }

    @Test
    void allTypes_shouldHaveCodeAndDescription() {
        for (NotificationType type : NotificationType.values()) {
            assertNotNull(type.getCode());
            assertFalse(type.getCode().isEmpty());
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
        }
    }
}
