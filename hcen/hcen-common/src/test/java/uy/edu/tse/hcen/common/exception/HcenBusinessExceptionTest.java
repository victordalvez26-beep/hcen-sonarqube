package uy.edu.tse.hcen.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para HcenBusinessException.
 */
class HcenBusinessExceptionTest {

    @Test
    void constructor_withMessage_shouldCreateException() {
        HcenBusinessException exception = new HcenBusinessException("Error de negocio");
        
        assertNotNull(exception);
        assertEquals("Error de negocio", exception.getMessage());
        assertEquals("BUSINESS_ERROR", exception.getErrorCode());
    }

    @Test
    void constructor_withErrorCodeAndMessage_shouldCreateException() {
        HcenBusinessException exception = new HcenBusinessException("CUSTOM_ERROR", "Mensaje personalizado");
        
        assertNotNull(exception);
        assertEquals("Mensaje personalizado", exception.getMessage());
        assertEquals("CUSTOM_ERROR", exception.getErrorCode());
    }

    @Test
    void constructor_withErrorCodeMessageAndCause_shouldCreateException() {
        Throwable cause = new RuntimeException("Causa original");
        HcenBusinessException exception = new HcenBusinessException("ERROR_CODE", "Mensaje", cause);
        
        assertNotNull(exception);
        assertEquals("Mensaje", exception.getMessage());
        assertEquals("ERROR_CODE", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isInstanceOfRuntimeException() {
        HcenBusinessException exception = new HcenBusinessException("test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void getErrorCode_shouldReturnCorrectCode() {
        HcenBusinessException exception = new HcenBusinessException("TEST_CODE", "test message");
        assertEquals("TEST_CODE", exception.getErrorCode());
    }

    @Test
    void defaultErrorCode_whenOnlyMessageProvided() {
        HcenBusinessException exception = new HcenBusinessException("test message");
        assertEquals("BUSINESS_ERROR", exception.getErrorCode());
    }
}


