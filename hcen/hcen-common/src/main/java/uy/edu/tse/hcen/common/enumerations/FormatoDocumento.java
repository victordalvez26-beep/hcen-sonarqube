package uy.edu.tse.hcen.common.enumerations;

/**
 * Enum que representa los formatos de documentos clínicos soportados por el sistema.
 */
public enum FormatoDocumento {

    PDF, 
    /** Imágenes médicas */
    DICOM,
    JPG,
    PNG, 
    XML, 
    /** Health Level 7 - Mensajería estándar en salud */
    HL7,
    JSON,
    TXT
}
