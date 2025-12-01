package uy.edu.tse.hcen.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uy.edu.tse.hcen.common.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para ValidationUtil.
 * Verifica validaciones, casos límite y casos de error.
 */
class ValidationUtilTest {

    @Test
    void requireNonEmpty_validString_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.requireNonEmpty("valid", "fieldName"));
    }

    @Test
    void requireNonEmpty_null_shouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.requireNonEmpty(null, "testField"));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("testField"));
    }

    @Test
    void requireNonEmpty_emptyString_shouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.requireNonEmpty("", "testField"));
        assertNotNull(exception);
    }

    @Test
    void requireNonEmpty_whitespaceOnly_shouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.requireNonEmpty("   ", "testField"));
        assertNotNull(exception);
    }

    @Test
    void validateCI_validFormat_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.validateCI("1.234.567-8"));
        assertDoesNotThrow(() -> ValidationUtil.validateCI("9.876.543-2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "12345678-9",
        "1.234.567-89",
        "1.234.567-",
        ".234.567-8",
        "1.234.567",
        "1.2.3.4.5.6.7-8",
        "1234567-8",
        "1.234567-8",
        "abcd.efg.h-i"
    })
    void validateCI_invalidFormats_shouldThrowValidationException(String invalidCI) {
        assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateCI(invalidCI));
    }

    @Test
    void validateCI_null_shouldThrowValidationException() {
        assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateCI(null));
    }

    @Test
    void isValidCI_validFormat_shouldReturnTrue() {
        assertTrue(ValidationUtil.isValidCI("1.234.567-8"));
        assertTrue(ValidationUtil.isValidCI("9.876.543-2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "12345678-9",
        "invalid"
    })
    void isValidCI_invalidFormats_shouldReturnFalse(String invalidCI) {
        assertFalse(ValidationUtil.isValidCI(invalidCI));
    }

    @Test
    void isValidCI_null_shouldReturnFalse() {
        assertFalse(ValidationUtil.isValidCI(null));
    }

    @Test
    void validateNombre_validName_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.validateNombre("Juan", "nombre"));
        assertDoesNotThrow(() -> ValidationUtil.validateNombre("María José", "nombre"));
        assertDoesNotThrow(() -> ValidationUtil.validateNombre("José María", "nombre"));
    }

    @Test
    void validateNombre_tooShort_shouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.validateNombre("A", "nombre"));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("al menos"));
    }

    @Test
    void validateNombre_tooLong_shouldThrowValidationException() {
        String longName = "A".repeat(101);
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.validateNombre(longName, "nombre"));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("exceder"));
    }

    @Test
    void validateNombre_invalidCharacters_shouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.validateNombre("Juan<script>", "nombre"));
        assertNotNull(exception);
    }

    @Test
    void validateNombre_null_shouldThrowValidationException() {
        assertThrows(ValidationException.class,
            () -> ValidationUtil.validateNombre(null, "nombre"));
    }

    @Test
    void sanitize_null_shouldReturnNull() {
        assertNull(ValidationUtil.sanitize(null));
    }

    @Test
    void sanitize_validString_shouldReturnSameString() {
        String input = "Valid String";
        assertEquals("Valid String", ValidationUtil.sanitize(input));
    }

    @Test
    void sanitize_withDangerousCharacters_shouldRemoveThem() {
        String input = "Test<script>alert('xss')</script>";
        String result = ValidationUtil.sanitize(input);
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
    }

    @Test
    void sanitize_withMultipleSpaces_shouldNormalize() {
        String input = "Test    with   multiple    spaces";
        String result = ValidationUtil.sanitize(input);
        assertFalse(result.contains("    "));
    }

    @Test
    void sanitize_withSpecialCharacters_shouldRemoveThem() {
        String input = "Test\"with'quotes;backslash\\";
        String result = ValidationUtil.sanitize(input);
        assertFalse(result.contains("\""));
        assertFalse(result.contains("'"));
        assertFalse(result.contains(";"));
        assertFalse(result.contains("\\"));
    }

    @Test
    void sanitize_withWhitespace_shouldTrim() {
        String input = "   Test   ";
        String result = ValidationUtil.sanitize(input);
        assertFalse(result.startsWith(" "));
        assertFalse(result.endsWith(" "));
    }

    @Test
    void requireNonNull_validObject_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.requireNonNull("test", "fieldName"));
        assertDoesNotThrow(() -> ValidationUtil.requireNonNull(new Object(), "fieldName"));
    }

    @Test
    void requireNonNull_null_shouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.requireNonNull(null, "testField"));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("testField"));
    }

    @Test
    void validateMaxLength_withinLimit_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.validateMaxLength("test", "field", 10));
    }

    @Test
    void validateMaxLength_atLimit_shouldNotThrow() {
        String atLimit = "A".repeat(10);
        assertDoesNotThrow(() -> ValidationUtil.validateMaxLength(atLimit, "field", 10));
    }

    @Test
    void validateMaxLength_exceedsLimit_shouldThrowValidationException() {
        String tooLong = "A".repeat(11);
        ValidationException exception = assertThrows(ValidationException.class,
            () -> ValidationUtil.validateMaxLength(tooLong, "field", 10));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("exceder"));
        assertTrue(exception.getMessage().contains("10"));
    }

    @Test
    void validateMaxLength_null_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.validateMaxLength(null, "field", 10));
    }
}

