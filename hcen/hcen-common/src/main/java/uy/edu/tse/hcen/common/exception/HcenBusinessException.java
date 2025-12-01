package uy.edu.tse.hcen.common.exception;

/**
 * Excepción base para todas las excepciones de negocio de la aplicación.
 * Las excepciones de negocio representan errores esperados en la lógica de la aplicación.
 */
public class HcenBusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public HcenBusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public HcenBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public HcenBusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
