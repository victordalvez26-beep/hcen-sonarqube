package uy.edu.tse.hcen.common.util;

import uy.edu.tse.hcen.common.constants.AppConstants;
import uy.edu.tse.hcen.common.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Clase de utilidad para validación y sanitización de entradas.
 * Ayuda a prevenir inyección de código y garantizar la integridad de datos.
 */
public final class ValidationUtil {
    
    private static final Pattern CI_PATTERN = Pattern.compile(AppConstants.REGEX_CI_URUGUAY);
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s\\-\\.0-9]+$");
    
    private ValidationUtil() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }
    
    /**
     * Valida que un string no sea null ni esté vacío.
     * @param value Valor a validar
     * @param fieldName Nombre del campo (para mensajes de error)
     * @throws ValidationException si el valor es inválido
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "no puede estar vacío");
        }
    }
    
    /**
     * Valida el formato de una Cédula de Identidad uruguaya.
     * Formato esperado: 1.234.567-8
     * @param ci Cédula de identidad a validar
     * @throws ValidationException si el formato es inválido
     */
    public static void validateCI(String ci) {
        requireNonEmpty(ci, "CI");
        if (!CI_PATTERN.matcher(ci).matches()) {
            throw new ValidationException("CI", 
                "debe tener el formato válido (ejemplo: 1.234.567-8)");
        }

    }
    
    /**
     * Valida que un nombre tenga longitud apropiada y solo contenga caracteres seguros.
     * @param nombre Nombre a validar
     * @param fieldName Nombre del campo
     * @throws ValidationException si el nombre es inválido
     */
    public static void validateNombre(String nombre, String fieldName) {
        requireNonEmpty(nombre, fieldName);
        
        if (nombre.length() < AppConstants.MIN_LONGITUD_NOMBRE) {
            throw new ValidationException(fieldName, 
                "debe tener al menos " + AppConstants.MIN_LONGITUD_NOMBRE + " caracteres");
        }
        
        if (nombre.length() > AppConstants.MAX_LONGITUD_NOMBRE) {
            throw new ValidationException(fieldName, 
                "no puede exceder " + AppConstants.MAX_LONGITUD_NOMBRE + " caracteres");
        }
        
        if (!SAFE_STRING_PATTERN.matcher(nombre).matches()) {
            throw new ValidationException(fieldName, 
                "contiene caracteres no permitidos");
        }
    }
    
    /**
     * Sanitiza un string eliminando caracteres potencialmente peligrosos.
     * Útil para prevenir inyección de código.
     * @param input String a sanitizar
     * @return String sanitizado
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        
        // Eliminar caracteres peligrosos para prevenir inyección
        return input.trim()
                   .replaceAll("[<>\"';\\\\]", "")  // Eliminar caracteres HTML/SQL peligrosos
                   .replaceAll("\\s+", " ");         // Normalizar espacios
    }
    
    /**
     * Valida que un objeto no sea null.
     * @param object Objeto a validar
     * @param fieldName Nombre del campo
     * @throws ValidationException si el objeto es null
     */
    public static void requireNonNull(Object object, String fieldName) {
        if (object == null) {
            throw new ValidationException(fieldName, "no puede ser null");
        }
    }
    
    /**
     * Valida la longitud de un string.
     * @param value String a validar
     * @param fieldName Nombre del campo
     * @param maxLength Longitud máxima permitida
     * @throws ValidationException si excede la longitud máxima
     */
    public static void validateMaxLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(fieldName, 
                "no puede exceder " + maxLength + " caracteres (actual: " + value.length() + ")");
        }
    }
    
    /**
     * Verifica si una CI tiene formato válido sin lanzar excepción.
     * @param ci Cédula de identidad a verificar
     * @return true si el formato es válido, false en caso contrario
     */
    public static boolean isValidCI(String ci) {
        return ci != null && CI_PATTERN.matcher(ci).matches();
    }
}
