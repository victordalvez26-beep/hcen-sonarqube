package uy.edu.tse.hcen.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para ValidationException.
 */
class ValidationExceptionTest {

    @Test
    void constructor_withFieldAndMessage_shouldCreateException() {
        ValidationException exception = new ValidationException("email", "no es válido");
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("no es válido"));
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
    }

    @Test
    void constructor_withMessageOnly_shouldCreateException() {
        ValidationException exception = new ValidationException("Error de validación general");
        
        assertNotNull(exception);
        assertEquals("Error de validación general", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
    }

    @Test
    void isInstanceOfHcenBusinessException() {
        ValidationException exception = new ValidationException("test", "message");
        assertTrue(exception instanceof HcenBusinessException);
    }

    @Test
    void getErrorCode_shouldReturnValidationError() {
        ValidationException exception = new ValidationException("field", "message");
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
    }
}


