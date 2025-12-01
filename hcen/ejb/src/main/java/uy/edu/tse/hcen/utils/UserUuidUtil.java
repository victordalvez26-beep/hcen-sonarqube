package uy.edu.tse.hcen.utils;

import java.util.logging.Logger;

public class UserUuidUtil {
    
    private static final Logger LOG = Logger.getLogger(UserUuidUtil.class.getName());
    
    /**
     * Genera un UUID estandarizado para usuarios.
     * Formato actual: uy-ci-{documento}
     * 
     * @param documento Número de documento
     * @return UUID formateado
     */
    public static String generateUuid(String documento) {
        if (documento == null) return null;
        return "uy-ci-" + documento.trim();
    }
    
    /**
     * Extrae el número de documento del UID.
     * El formato del UID es: pais-tipodocumento-numero
     * Ejemplo: uy-ci-12345678 -> 12345678
     * 
     * @param uid UID del usuario en formato pais-tipodocumento-numero
     * @return Número de documento, o null si el formato es inválido
     */
    public static String extractDocumentoFromUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return null;
        }
        
        // El formato es: nacionalidad-tipoDocum-codigoDocum
        // Ejemplo: uy-ci-12345678
        String[] parts = uid.split("-", 3);
        if (parts.length >= 3) {
            String documento = parts[2];
            LOG.fine("Documento extraído del UID: " + documento + " (UID: " + uid + ")");
            return documento;
        }
        
        LOG.warning("Formato de UID inválido. Esperado: pais-tipodocumento-numero. Recibido: " + uid);
        return null;
    }
}
