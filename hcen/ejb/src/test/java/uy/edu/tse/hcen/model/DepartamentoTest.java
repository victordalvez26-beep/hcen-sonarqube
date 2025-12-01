package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Departamento enum.
 */
class DepartamentoTest {

    @Test
    void getNombre_shouldReturnCorrectName() {
        assertEquals("Montevideo", Departamento.MONTEVIDEO.getNombre());
        assertEquals("Canelones", Departamento.CANELONES.getNombre());
        assertEquals("Maldonado", Departamento.MALDONADO.getNombre());
    }

    @Test
    void toString_shouldReturnNombre() {
        assertEquals("Montevideo", Departamento.MONTEVIDEO.toString());
        assertEquals("Canelones", Departamento.CANELONES.toString());
    }

    @Test
    void valueOf_shouldParseCorrectly() {
        assertEquals(Departamento.MONTEVIDEO, Departamento.valueOf("MONTEVIDEO"));
        assertEquals(Departamento.CANELONES, Departamento.valueOf("CANELONES"));
        assertEquals(Departamento.ARTIGAS, Departamento.valueOf("ARTIGAS"));
    }

    @Test
    void values_shouldContainAllDepartments() {
        Departamento[] departamentos = Departamento.values();
        
        assertEquals(19, departamentos.length);
        assertTrue(java.util.Arrays.asList(departamentos).contains(Departamento.MONTEVIDEO));
        assertTrue(java.util.Arrays.asList(departamentos).contains(Departamento.CANELONES));
        assertTrue(java.util.Arrays.asList(departamentos).contains(Departamento.ARTIGAS));
    }

    @Test
    void allDepartments_shouldHaveNames() {
        for (Departamento dept : Departamento.values()) {
            assertNotNull(dept.getNombre());
            assertFalse(dept.getNombre().isEmpty());
        }
    }
}
