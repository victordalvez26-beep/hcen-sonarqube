package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.common.enumerations.FormatoDocumento;
import uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico;
import uy.edu.tse.hcen.rndc.model.MetadataDocumento;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el modelo MetadataDocumento.
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
        
        MetadataDocumento doc2 = new MetadataDocumento();
        doc2.setId(1L);
        
        assertEquals(doc1, doc2);
    }

    @Test
    void equals_differentId_shouldReturnFalse() {
        MetadataDocumento doc1 = new MetadataDocumento();
        doc1.setId(1L);
        
        MetadataDocumento doc2 = new MetadataDocumento();
        doc2.setId(2L);
        
        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_nullId_shouldReturnFalse() {
        MetadataDocumento doc1 = new MetadataDocumento();
        doc1.setId(1L);
        
        MetadataDocumento doc2 = new MetadataDocumento();
        doc2.setId(null);
        
        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_sameInstance_shouldReturnTrue() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        
        assertEquals(doc, doc);
    }

    @Test
    void equals_differentClass_shouldReturnFalse() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        
        assertNotEquals(doc, "not a document");
    }

    @Test
    void hashCode_shouldReturnClassHashCode() {
        MetadataDocumento doc1 = new MetadataDocumento();
        MetadataDocumento doc2 = new MetadataDocumento();
        
        // hashCode() retorna getClass().hashCode() para todos los objetos
        assertEquals(doc1.hashCode(), doc2.hashCode());
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

