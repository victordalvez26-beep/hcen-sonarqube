package uy.edu.tse.hcen.common.constants;

/**
 * Constantes de la aplicación
 */
public final class AppConstants {
    
    private AppConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }
    
    // ==================== MongoDB Collections ====================
    public static final String COLLECTION_METADATOS = "metadatos_hc"; //Solo de pruebas, eliminar
    
    // ==================== Tipos de Documento ====================
    public static final String TIPO_DOC_RESUMEN_ALTA = "Resumen de Alta";
    public static final String TIPO_DOC_LABORATORIO = "Laboratorio (Hemograma)";
    public static final String TIPO_DOC_RADIOGRAFIA = "Radiografía de Tórax";
    public static final String TIPO_DOC_RECETA = "Receta Médica";
    public static final String TIPO_DOC_SENSIBLE = "Informe Sensible";
    
    // ==================== Formatos de Documento ====================
    public static final String FORMATO_PDF = "PDF";
    public static final String FORMATO_DCM = "dcm";
    public static final String FORMATO_JPG = "jpg";
    
    // ==================== Mensajes de Log ====================
    public static final String LOG_BUSQUEDA_CI = " Búsqueda por Codigo Documento de Identidad: %s";
    public static final String LOG_BUSQUEDA_NOMBRE = " Búsqueda por nombre: %s";
    public static final String LOG_DOCUMENTOS_ENCONTRADOS = " Encontrados %d documentos";
    public static final String LOG_ACCESO_PERMITIDO = " Acceso permitido: %s → %s";
    public static final String LOG_ACCESO_DENEGADO = " Acceso denegado: %s → %s";
    public static final String LOG_SOLICITUD_CREADA = " Solicitud de acceso creada: ID %d";
    public static final String LOG_SOLICITUD_APROBADA = " Solicitud APROBADA: ID %d";
    public static final String LOG_SOLICITUD_RECHAZADA = " Solicitud RECHAZADA: ID %d";
    public static final String LOG_POLITICA_CONFIGURADA = " Política configurada: %s → %s";
    public static final String LOG_REGISTRO_ACCESO = " Acceso registrado: %s";

    // ==================== Mensajes de Error ====================
    public static final String ERROR_DOCUMENTO_NO_ENCONTRADO = "Documento no encontrado";
    public static final String ERROR_ACCESO_DENEGADO = "No tiene permisos para acceder a este documento";
    public static final String ERROR_CI_INVALIDO = "El formato del documento de identidad es inválido";
    public static final String ERROR_NOMBRE_VACIO = "El nombre del paciente no puede estar vacío";
    public static final String ERROR_SOLICITUD_NO_ENCONTRADA = "Solicitud de acceso no encontrada";
    
    // ==================== Validaciones ====================
    public static final String REGEX_CI_URUGUAY = "^[1-9]\\.[0-9]{3}\\.[0-9]{3}-[0-9]$"; // Formato: 1.234.567-8
    public static final int MIN_LONGITUD_NOMBRE = 2;
    public static final int MAX_LONGITUD_NOMBRE = 100;
    public static final int MAX_LONGITUD_DESCRIPCION = 500;
    
    // ==================== Usuarios de Prueba ====================
    public static final String CI_USUARIO_PRUEBA = "1.234.567-8";
    public static final String NOMBRE_USUARIO_PRUEBA = "Juan Rodríguez";
    public static final String PROFESIONAL_PRUEBA_ID = "PROF-001";
    public static final String PROFESIONAL_PRUEBA_NOMBRE = "Dr. Juan Pérez";
    
    // ==================== Configuración ====================
    public static final int CANTIDAD_MAXIMA_RESULTADOS_BUSQUEDA = 100;
    public static final int CANTIDAD_DOCUMENTOS_PRUEBA = 5;
}
