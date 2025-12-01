package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para el enum Nacionalidad.
 */
class NacionalidadTest {

    @Test
    void fromCodigo_validUpperCase_shouldReturnNacionalidad() {
        Nacionalidad result = Nacionalidad.fromCodigo("UY");
        assertEquals(Nacionalidad.UY, result);
    }

    @Test
    void fromCodigo_validLowerCase_shouldReturnNacionalidad() {
        Nacionalidad result = Nacionalidad.fromCodigo("uy");
        assertEquals(Nacionalidad.UY, result);
    }

    @Test
    void fromCodigo_validMixedCase_shouldReturnNacionalidad() {
        Nacionalidad result = Nacionalidad.fromCodigo("Ar");
        assertEquals(Nacionalidad.AR, result);
    }

    @Test
    void fromCodigo_invalidCode_shouldReturnNull() {
        Nacionalidad result = Nacionalidad.fromCodigo("XX");
        assertNull(result);
    }

    @Test
    void fromCodigo_null_shouldReturnNull() {
        Nacionalidad result = Nacionalidad.fromCodigo(null);
        assertNull(result);
    }

    @Test
    void fromCodigo_emptyString_shouldReturnNull() {
        Nacionalidad result = Nacionalidad.fromCodigo("");
        assertNull(result);
    }

    @Test
    void getCodigo_shouldReturnEnumName() {
        assertEquals("UY", Nacionalidad.UY.getCodigo());
        assertEquals("AR", Nacionalidad.AR.getCodigo());
        assertEquals("OT", Nacionalidad.OT.getCodigo());
    }

    @Test
    void getNombre_shouldReturnDisplayName() {
        assertEquals("Uruguay", Nacionalidad.UY.getNombre());
        assertEquals("Argentina", Nacionalidad.AR.getNombre());
        assertEquals("Otros", Nacionalidad.OT.getNombre());
    }

    @Test
    void toString_shouldReturnNombre() {
        assertEquals("Uruguay", Nacionalidad.UY.toString());
        assertEquals("Argentina", Nacionalidad.AR.toString());
    }

    @Test
    void allValues_shouldHaveNombre() {
        for (Nacionalidad nacionalidad : Nacionalidad.values()) {
            assertNotNull(nacionalidad.getNombre());
            assertFalse(nacionalidad.getNombre().isEmpty());
        }
    }

    @Test
    void commonNationalities_shouldWork() {
        assertEquals(Nacionalidad.UY, Nacionalidad.fromCodigo("UY"));
        assertEquals(Nacionalidad.AR, Nacionalidad.fromCodigo("AR"));
        assertEquals(Nacionalidad.BR, Nacionalidad.fromCodigo("BR"));
        assertEquals(Nacionalidad.CL, Nacionalidad.fromCodigo("CL"));
        assertEquals(Nacionalidad.US, Nacionalidad.fromCodigo("US"));
        assertEquals(Nacionalidad.ES, Nacionalidad.fromCodigo("ES"));
    }
}


