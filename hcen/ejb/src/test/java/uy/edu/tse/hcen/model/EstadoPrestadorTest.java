package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para EstadoPrestador enum.
 */
class EstadoPrestadorTest {

    @Test
    void getDescripcion_shouldReturnCorrectDescription() {
        assertEquals("Pendiente de Registro", EstadoPrestador.PENDIENTE_REGISTRO.getDescripcion());
        assertEquals("Activo", EstadoPrestador.ACTIVO.getDescripcion());
        assertEquals("Inactivo", EstadoPrestador.INACTIVO.getDescripcion());
        assertEquals("En Mantenimiento", EstadoPrestador.MANTENIMIENTO.getDescripcion());
        assertEquals("Error de Conexión", EstadoPrestador.ERROR_CONEXION.getDescripcion());
    }

    @Test
    void values_shouldContainAllStates() {
        EstadoPrestador[] estados = EstadoPrestador.values();
        assertEquals(5, estados.length);
        
        assertTrue(java.util.Arrays.asList(estados).contains(EstadoPrestador.PENDIENTE_REGISTRO));
        assertTrue(java.util.Arrays.asList(estados).contains(EstadoPrestador.ACTIVO));
        assertTrue(java.util.Arrays.asList(estados).contains(EstadoPrestador.INACTIVO));
        assertTrue(java.util.Arrays.asList(estados).contains(EstadoPrestador.MANTENIMIENTO));
        assertTrue(java.util.Arrays.asList(estados).contains(EstadoPrestador.ERROR_CONEXION));
    }

    @Test
    void allEstados_shouldHaveDescription() {
        for (EstadoPrestador estado : EstadoPrestador.values()) {
            assertNotNull(estado.getDescripcion());
            assertFalse(estado.getDescripcion().isEmpty());
        }
    }
}
