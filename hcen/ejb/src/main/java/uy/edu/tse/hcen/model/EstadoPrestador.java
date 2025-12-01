package uy.edu.tse.hcen.model;

/**
 * Estados posibles de un Prestador de Salud en HCEN.
 */
public enum EstadoPrestador {
    
    /**
     * Invitación enviada, esperando que el prestador complete el formulario de registro.
     */
    PENDIENTE_REGISTRO,
    
    /**
     * Prestador activo y disponible para consultas de documentos clínicos.
     */
    ACTIVO,
    
    /**
     * Prestador temporalmente inactivo.
     */
    INACTIVO,
    
    /**
     * Prestador en mantenimiento.
     */
    MANTENIMIENTO,
    
    /**
     * Error al intentar contactar al prestador.
     */
    ERROR_CONEXION;
    
    public String getDescripcion() {
        switch (this) {
            case PENDIENTE_REGISTRO: return "Pendiente de Registro";
            case ACTIVO: return "Activo";
            case INACTIVO: return "Inactivo";
            case MANTENIMIENTO: return "En Mantenimiento";
            case ERROR_CONEXION: return "Error de Conexión";
            default: return name();
        }
    }
}

