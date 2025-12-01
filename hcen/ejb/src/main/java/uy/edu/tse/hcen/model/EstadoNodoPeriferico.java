package uy.edu.tse.hcen.model;

/**
 * Estados posibles del nodo periférico para la integración con HCEN central.
 */
public enum EstadoNodoPeriferico {
    ACTIVO,              // Clínica completamente activa y operativa
    INACTIVO,            // Clínica desactivada temporalmente
    ERROR_MENSAJERIA,    // Error en la comunicación con el periférico
    MANTENIMIENTO,       // En mantenimiento programado
    PENDIENTE            // Email enviado, esperando que la clínica complete el formulario de activación
}
