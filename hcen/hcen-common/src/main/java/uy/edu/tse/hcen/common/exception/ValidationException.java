package uy.edu.tse.hcen.common.exception;

/**
 * Excepción lanzada cuando los datos de entrada no son válidos.
 */
public class ValidationException extends HcenBusinessException {
    
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", 
              String.format("Error de validación en campo '%s': %s", field, message));
    }
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
