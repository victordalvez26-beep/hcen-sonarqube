package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para el enum EstadoNodoPeriferico.
 */
class EstadoNodoPerifericoTest {

    @Test
    void valueOf_validValues_shouldReturnEnums() {
        assertEquals(EstadoNodoPeriferico.ACTIVO, EstadoNodoPeriferico.valueOf("ACTIVO"));
        assertEquals(EstadoNodoPeriferico.INACTIVO, EstadoNodoPeriferico.valueOf("INACTIVO"));
        assertEquals(EstadoNodoPeriferico.ERROR_MENSAJERIA, EstadoNodoPeriferico.valueOf("ERROR_MENSAJERIA"));
        assertEquals(EstadoNodoPeriferico.MANTENIMIENTO, EstadoNodoPeriferico.valueOf("MANTENIMIENTO"));
        assertEquals(EstadoNodoPeriferico.PENDIENTE, EstadoNodoPeriferico.valueOf("PENDIENTE"));
    }

    @Test
    void valueOf_invalidValue_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, 
            () -> EstadoNodoPeriferico.valueOf("NO_EXISTE"));
    }

    @Test
    void valueOf_caseSensitive_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> EstadoNodoPeriferico.valueOf("activo"));
    }

    @Test
    void values_shouldReturnAllEnums() {
        EstadoNodoPeriferico[] valores = EstadoNodoPeriferico.values();
        assertNotNull(valores);
        assertEquals(5, valores.length); // ACTIVO, INACTIVO, ERROR_MENSAJERIA, MANTENIMIENTO, PENDIENTE
    }

    @Test
    void name_shouldReturnStringName() {
        assertEquals("ACTIVO", EstadoNodoPeriferico.ACTIVO.name());
        assertEquals("PENDIENTE", EstadoNodoPeriferico.PENDIENTE.name());
    }
}

