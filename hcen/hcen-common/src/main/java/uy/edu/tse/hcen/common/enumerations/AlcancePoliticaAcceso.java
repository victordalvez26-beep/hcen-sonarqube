package uy.edu.tse.hcen.common.enumerations;

/**
 * Define el nivel de acceso que se otorga a un profesional de salud.
 */
public enum AlcancePoliticaAcceso {

    TODOS_LOS_DOCUMENTOS,
    /** Acceso solo a documentos de tipos específicos */
    DOCUMENTOS_POR_TIPO,
    UN_DOCUMENTO_ESPECIFICO
}
