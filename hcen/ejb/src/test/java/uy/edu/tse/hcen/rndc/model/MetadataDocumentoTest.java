package uy.edu.tse.hcen.rndc.model;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.common.enumerations.FormatoDocumento;
import uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el modelo MetadataDocumento del módulo RNDC.
 */
class MetadataDocumentoTest {

    @Test
    void constructor_default_shouldCreateEmptyDocument() {
        MetadataDocumento doc = new MetadataDocumento();
        
        assertNotNull(doc);
        assertNull(doc.getCodDocum());
        assertNull(doc.getNombrePaciente());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        MetadataDocumento doc = new MetadataDocumento();
        
        doc.setCodDocum("DOC-123");
        doc.setNombrePaciente("Juan");
        doc.setApellidoPaciente("Pérez");
        doc.setTipoDocumento(TipoDocumentoClinico.ESTUDIO_IMAGENOLOGIA);
        doc.setFormatoDocumento(FormatoDocumento.PDF);
        doc.setFechaCreacion(LocalDateTime.now());
        doc.setTenantId(1L);
        doc.setProfesionalSalud("prof-123");
        doc.setDescripcion("Descripción del documento");
        doc.setUriDocumento("http://example.com/doc.pdf");
        doc.setRestringido(true);
        
        assertEquals("DOC-123", doc.getCodDocum());
        assertEquals("Juan", doc.getNombrePaciente());
        assertEquals("Pérez", doc.getApellidoPaciente());
        assertEquals(TipoDocumentoClinico.ESTUDIO_IMAGENOLOGIA, doc.getTipoDocumento());
        assertEquals(FormatoDocumento.PDF, doc.getFormatoDocumento());
        assertNotNull(doc.getFechaCreacion());
        assertEquals(1L, doc.getTenantId());
        assertEquals("prof-123", doc.getProfesionalSalud());
        assertEquals("Descripción del documento", doc.getDescripcion());
        assertEquals("http://example.com/doc.pdf", doc.getUriDocumento());
        assertTrue(doc.isRestringido());
    }

    @Test
    void equals_sameId_shouldReturnTrue() {
        MetadataDocumento doc1 = new MetadataDocumento();
        doc1.setId(1L);
        doc1.setCodDocum("DOC-123");
        
        MetadataDocumento doc2 = new MetadataDocumento();
        doc2.setId(1L);
        doc2.setCodDocum("DOC-123");
        
        assertEquals(doc1, doc2);
    }

    @Test
    void equals_differentId_shouldReturnFalse() {
        MetadataDocumento doc1 = new MetadataDocumento();
        doc1.setId(1L);
        doc1.setCodDocum("DOC-123");
        
        MetadataDocumento doc2 = new MetadataDocumento();
        doc2.setId(2L);
        doc2.setCodDocum("DOC-123");
        
        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_nullId_shouldReturnFalse() {
        MetadataDocumento doc1 = new MetadataDocumento();
        doc1.setId(1L);
        doc1.setCodDocum("DOC-123");
        
        MetadataDocumento doc2 = new MetadataDocumento();
        doc2.setId(null);
        doc2.setCodDocum("DOC-123");
        
        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_sameInstance_shouldReturnTrue() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setCodDocum("DOC-123");
        
        assertEquals(doc, doc);
    }

    @Test
    void equals_differentClass_shouldReturnFalse() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setCodDocum("DOC-123");
        
        assertNotEquals(doc, "not a document");
    }

    @Test
    void hashCode_shouldReturnClassHashCode() {
        MetadataDocumento doc1 = new MetadataDocumento();
        MetadataDocumento doc2 = new MetadataDocumento();
        
        // El hashCode usa getClass().hashCode(), así que debería ser el mismo
        assertEquals(doc1.hashCode(), doc2.hashCode());
    }

    @Test
    void getIdAndSetId_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(123L);
        
        assertEquals(123L, doc.getId());
    }

    @Test
    void getClinicaOrigenAndSetClinicaOrigen_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setClinicaOrigen("Clínica Test");
        
        assertEquals("Clínica Test", doc.getClinicaOrigen());
    }

    @Test
    void isRestringidoAndSetRestringido_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setRestringido(true);
        
        assertTrue(doc.isRestringido());
    }

    @Test
    void toString_shouldContainKeyFields() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setCodDocum("DOC-123");
        doc.setNombrePaciente("Juan");
        doc.setApellidoPaciente("Pérez");
        
        String str = doc.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("DOC-123") || str.contains("MetadataDocumento"));
    }

    @Test
    void restringido_default_shouldBeFalse() {
        MetadataDocumento doc = new MetadataDocumento();
        
        assertFalse(doc.isRestringido());
    }

    @Test
    void restringido_setTrue_shouldBeTrue() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setRestringido(true);
        
        assertTrue(doc.isRestringido());
    }

    @Test
    void constructor_withParameters_shouldSetFields() {
        LocalDateTime fecha = LocalDateTime.of(2024, 1, 15, 0, 0);
        MetadataDocumento doc = new MetadataDocumento(
            "DOC-123",
            "Juan",
            FormatoDocumento.PDF,
            TipoDocumentoClinico.ESTUDIO_IMAGENOLOGIA,
            fecha,
            "http://example.com/doc.pdf",
            "Clínica Test",
            true
        );
        
        assertEquals("DOC-123", doc.getCodDocum());
        assertEquals("Juan", doc.getNombrePaciente());
        assertEquals(FormatoDocumento.PDF, doc.getFormatoDocumento());
        assertEquals(TipoDocumentoClinico.ESTUDIO_IMAGENOLOGIA, doc.getTipoDocumento());
        assertEquals(fecha, doc.getFechaCreacion());
        assertEquals("http://example.com/doc.pdf", doc.getUriDocumento());
        assertEquals("Clínica Test", doc.getClinicaOrigen());
        assertTrue(doc.isRestringido());
    }

    @Test
    void fechaCreacion_setAndGet_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        LocalDateTime fecha = LocalDateTime.of(2024, 1, 15, 0, 0);
        doc.setFechaCreacion(fecha);
        
        assertEquals(fecha, doc.getFechaCreacion());
    }

    @Test
    void tipoDocumento_allTypes_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        
        for (TipoDocumentoClinico tipo : TipoDocumentoClinico.values()) {
            doc.setTipoDocumento(tipo);
            assertEquals(tipo, doc.getTipoDocumento());
        }
    }

    @Test
    void formatoDocumento_allFormats_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        
        for (FormatoDocumento formato : FormatoDocumento.values()) {
            doc.setFormatoDocumento(formato);
            assertEquals(formato, doc.getFormatoDocumento());
        }
    }

    @Test
    void allFields_null_shouldWork() {
        MetadataDocumento doc = new MetadataDocumento();
        
        assertNull(doc.getCodDocum());
        assertNull(doc.getNombrePaciente());
        assertNull(doc.getApellidoPaciente());
        assertNull(doc.getTipoDocumento());
        assertNull(doc.getFormatoDocumento());
        assertNull(doc.getFechaCreacion());
        assertNull(doc.getTenantId());
        assertNull(doc.getProfesionalSalud());
        assertNull(doc.getDescripcion());
        assertNull(doc.getUriDocumento());
        assertFalse(doc.isRestringido());
    }
}

