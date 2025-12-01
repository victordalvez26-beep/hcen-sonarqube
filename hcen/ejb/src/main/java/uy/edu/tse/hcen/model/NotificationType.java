package uy.edu.tse.hcen.model;

/**
 * Tipos de notificaciones disponibles en el sistema HCEN.
 */
public enum NotificationType {
    
    /**
     * Notificación de resultados — Cuando los resultados de laboratorio o de imágenes 
     * (radiografías, ecografías, etc.) estén disponibles.
     */
    RESULTS("results", "Resultados de Laboratorio"),
    
    /**
     * Nuevo pedido de acceso — Cuando un profesional o institución solicita permiso 
     * para acceder a tu información médica.
     */
    NEW_ACCESS_REQUEST("new_access_request", "Nuevo Pedido de Acceso"),
    
    /**
     * Actualizaciones en el historial médico — Cuando se añaden nuevos documentos, 
     * diagnósticos o notas en tu historial médico.
     */
    MEDICAL_HISTORY("medical_history", "Actualizaciones en Historial Médico"),
    
    /**
     * Nuevo acceso a historia clínica — Cuando un profesional o institución obtiene 
     * acceso a tu historia clínica.
     */
    NEW_ACCESS_HISTORY("new_access_history", "Nuevo Acceso a Historia Clínica"),
    
    /**
     * Mantenimiento del sistema — Cuando la app o el sistema va a estar fuera de 
     * servicio temporalmente por mantenimiento.
     */
    MAINTENANCE("maintenance", "Mantenimiento del Sistema"),
    
    /**
     * Nuevas funciones — Avisos sobre mejoras, nuevas características o correcciones 
     * de errores en la app.
     */
    NEW_FEATURES("new_features", "Nuevas Funciones"),
    
    /**
     * Desactivar todas — Desactiva todas las notificaciones de la aplicación.
     */
    ALL_DISABLED("all_disabled", "Desactivar Todas");
    
    private final String code;
    private final String description;
    
    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Busca un NotificationType por su código.
     */
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

