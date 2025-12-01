package uy.edu.tse.hcen.common.enumerations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para FormatoDocumento.
 */
class FormatoDocumentoTest {

    @Test
    void valueOf_allValues_shouldReturnCorrectEnum() {
        assertEquals(FormatoDocumento.PDF, FormatoDocumento.valueOf("PDF"));
        assertEquals(FormatoDocumento.DICOM, FormatoDocumento.valueOf("DICOM"));
        assertEquals(FormatoDocumento.JPG, FormatoDocumento.valueOf("JPG"));
        assertEquals(FormatoDocumento.PNG, FormatoDocumento.valueOf("PNG"));
        assertEquals(FormatoDocumento.XML, FormatoDocumento.valueOf("XML"));
        assertEquals(FormatoDocumento.HL7, FormatoDocumento.valueOf("HL7"));
        assertEquals(FormatoDocumento.JSON, FormatoDocumento.valueOf("JSON"));
        assertEquals(FormatoDocumento.TXT, FormatoDocumento.valueOf("TXT"));
    }

    @Test
    void values_shouldReturnAllEnums() {
        FormatoDocumento[] values = FormatoDocumento.values();
        assertEquals(8, values.length);
    }
}

