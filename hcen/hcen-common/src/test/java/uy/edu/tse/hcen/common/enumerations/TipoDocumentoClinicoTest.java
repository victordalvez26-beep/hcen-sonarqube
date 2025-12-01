package uy.edu.tse.hcen.common.enumerations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TipoDocumentoClinico.
 */
class TipoDocumentoClinicoTest {

    @Test
    void valueOf_allValues_shouldReturnCorrectEnum() {
        assertEquals(TipoDocumentoClinico.RESUMEN_ALTA, TipoDocumentoClinico.valueOf("RESUMEN_ALTA"));
        assertEquals(TipoDocumentoClinico.INFORME_LABORATORIO, TipoDocumentoClinico.valueOf("INFORME_LABORATORIO"));
        assertEquals(TipoDocumentoClinico.RADIOGRAFIA, TipoDocumentoClinico.valueOf("RADIOGRAFIA"));
        assertEquals(TipoDocumentoClinico.RECETA_MEDICA, TipoDocumentoClinico.valueOf("RECETA_MEDICA"));
        assertEquals(TipoDocumentoClinico.CONSULTA_MEDICA, TipoDocumentoClinico.valueOf("CONSULTA_MEDICA"));
        assertEquals(TipoDocumentoClinico.CIRUGIA, TipoDocumentoClinico.valueOf("CIRUGIA"));
        assertEquals(TipoDocumentoClinico.ESTUDIO_IMAGENOLOGIA, TipoDocumentoClinico.valueOf("ESTUDIO_IMAGENOLOGIA"));
        assertEquals(TipoDocumentoClinico.ELECTROCARDIOGRAMA, TipoDocumentoClinico.valueOf("ELECTROCARDIOGRAMA"));
        assertEquals(TipoDocumentoClinico.INFORME_PATOLOGIA, TipoDocumentoClinico.valueOf("INFORME_PATOLOGIA"));
        assertEquals(TipoDocumentoClinico.VACUNACION, TipoDocumentoClinico.valueOf("VACUNACION"));
        assertEquals(TipoDocumentoClinico.OTROS, TipoDocumentoClinico.valueOf("OTROS"));
    }

    @Test
    void values_shouldReturnAllEnums() {
        TipoDocumentoClinico[] values = TipoDocumentoClinico.values();
        assertEquals(11, values.length);
    }
}

