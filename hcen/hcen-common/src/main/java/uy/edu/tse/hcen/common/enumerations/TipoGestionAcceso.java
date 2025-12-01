package uy.edu.tse.hcen.common.enumerations;

/**
 * Enum que representa el tipo de gestión de acceso a documentos clínicos.
 */
public enum TipoGestionAcceso {
    /** Gestión automática: acceso otorgado por reglas del sistema */
    AUTOMATICA,
    
    /** Gestión manual: requiere aprobación explícita del paciente */
    MANUAL
}
