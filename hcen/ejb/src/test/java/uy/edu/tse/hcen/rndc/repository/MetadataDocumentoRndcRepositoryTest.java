package uy.edu.tse.hcen.rndc.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.rndc.model.MetadataDocumento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para MetadataDocumentoRndcRepository.
 */
class MetadataDocumentoRndcRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<MetadataDocumento> queryDocumento;

    @Mock
    private TypedQuery<Long> queryLong;

    private MetadataDocumentoRndcRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new MetadataDocumentoRndcRepository();
        
        try {
            java.lang.reflect.Field emField = MetadataDocumentoRndcRepository.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(repository, em);
        } catch (Exception e) {
            fail("Error inyectando EntityManager: " + e.getMessage());
        }
    }

    @Test
    void buscarPorCodDocum_validCI_shouldReturnList() {
        String codDocum = "12345678";
        List<MetadataDocumento> documentos = new ArrayList<>();
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(codDocum);
        documentos.add(doc);
        
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.setParameter("codDocum", codDocum)).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(documentos);
        
        List<MetadataDocumento> result = repository.buscarPorCodDocum(codDocum);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(codDocum, result.get(0).getCodDocum());
    }

    @Test
    void buscarPorCodDocum_noResults_shouldReturnEmptyList() {
        String codDocum = "99999999";
        
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.setParameter("codDocum", codDocum)).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(new ArrayList<>());
        
        List<MetadataDocumento> result = repository.buscarPorCodDocum(codDocum);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorNombre_validName_shouldReturnList() {
        String nombre = "Juan Pérez";
        List<MetadataDocumento> documentos = new ArrayList<>();
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setNombrePaciente("Juan Pérez González");
        documentos.add(doc);
        
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.setParameter(eq("nombre"), anyString())).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(documentos);
        
        List<MetadataDocumento> result = repository.buscarPorNombre(nombre);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void buscarPorNombre_caseInsensitive_shouldMatch() {
        String nombre = "juan";
        
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.setParameter(eq("nombre"), anyString())).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(new ArrayList<>());
        
        List<MetadataDocumento> result = repository.buscarPorNombre(nombre);
        
        assertNotNull(result);
        verify(queryDocumento).setParameter(eq("nombre"), contains("%juan%"));
    }

    @Test
    void findById_existingId_shouldReturnDocument() {
        Long id = 1L;
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(id);
        
        when(em.find(MetadataDocumento.class, id)).thenReturn(doc);
        
        MetadataDocumento result = repository.findById(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void findById_notFound_shouldReturnNull() {
        Long id = 999L;
        
        when(em.find(MetadataDocumento.class, id)).thenReturn(null);
        
        MetadataDocumento result = repository.findById(id);
        
        assertNull(result);
    }

    @Test
    void save_newDocument_shouldPersist() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setCodDocum("12345678");
        doc.setNombrePaciente("Test");
        
        doNothing().when(em).persist(doc);
        
        MetadataDocumento result = repository.save(doc);
        
        assertNotNull(result);
        verify(em).persist(doc);
        verify(em, never()).merge(any());
    }

    @Test
    void save_existingDocument_shouldMerge() {
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum("12345678");
        MetadataDocumento merged = new MetadataDocumento();
        merged.setId(1L);
        
        when(em.merge(doc)).thenReturn(merged);
        
        MetadataDocumento result = repository.save(doc);
        
        assertNotNull(result);
        verify(em).merge(doc);
        verify(em, never()).persist(any());
    }

    @Test
    void deleteById_existingDocument_shouldRemove() {
        Long id = 1L;
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(id);
        
        when(em.find(MetadataDocumento.class, id)).thenReturn(doc);
        doNothing().when(em).remove(doc);
        
        repository.deleteById(id);
        
        verify(em).find(MetadataDocumento.class, id);
        verify(em).remove(doc);
    }

    @Test
    void deleteById_notFound_shouldNotCrash() {
        Long id = 999L;
        
        when(em.find(MetadataDocumento.class, id)).thenReturn(null);
        
        repository.deleteById(id);
        
        verify(em).find(MetadataDocumento.class, id);
        verify(em, never()).remove(any());
    }

    @Test
    void findAll_shouldReturnAllDocuments() {
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(new MetadataDocumento());
        documentos.add(new MetadataDocumento());
        
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(documentos);
        
        List<MetadataDocumento> result = repository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void count_shouldReturnTotalCount() {
        Long count = 10L;
        
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(count);
        
        long result = repository.count();
        
        assertEquals(10L, result);
    }

    @Test
    void buscarPorCodDocum_nullCI_shouldHandle() {
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.setParameter("codDocum", null)).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(new ArrayList<>());
        
        List<MetadataDocumento> result = repository.buscarPorCodDocum(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorNombre_nullName_shouldHandle() {
        when(em.createQuery(anyString(), eq(MetadataDocumento.class))).thenReturn(queryDocumento);
        when(queryDocumento.setParameter(eq("nombre"), anyString())).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(new ArrayList<>());
        
        List<MetadataDocumento> result = repository.buscarPorNombre(null);
        
        assertNotNull(result);
    }
}


