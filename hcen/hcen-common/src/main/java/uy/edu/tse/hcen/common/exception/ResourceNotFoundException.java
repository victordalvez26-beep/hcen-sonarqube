package uy.edu.tse.hcen.common.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado.
 */
public class ResourceNotFoundException extends HcenBusinessException {
    
    public ResourceNotFoundException(String resourceType, String identifier) {
        super("NOT_FOUND", 
              String.format("%s no encontrado con identificador: %s", resourceType, identifier));
    }
    
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
