package uy.edu.tse.hcen.common.exception;

/**
 * Excepción lanzada cuando se intenta acceder a un recurso sin la autorización adecuada.
 */
public class AccessDeniedException extends HcenBusinessException {
    
    public AccessDeniedException(String message) {
        super("ACCESS_DENIED", message);
    }
    
    public AccessDeniedException(String profesionalId, String recurso) {
        super("ACCESS_DENIED", 
              String.format("El profesional %s no tiene permiso para acceder a: %s", 
                          profesionalId, recurso));
    }
}
