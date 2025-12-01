package uy.edu.tse.hcen.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.EstadoNodoPeriferico;
import uy.edu.tse.hcen.model.NodoPeriferico;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para NodoPerifericoRepository.
 */
class NodoPerifericoRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<NodoPeriferico> typedQuery;

    private NodoPerifericoRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        repository = new NodoPerifericoRepository();
        Field emField = NodoPerifericoRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(repository, em);
    }

    @Test
    void create_withPassword_shouldHashPassword() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("12345678-9");
        nodo.setNombre("Clínica Test");
        nodo.setNodoPerifericoPassword("plain-password");
        
        doAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return null;
        }).when(em).persist(any(NodoPeriferico.class));
        
        NodoPeriferico result = repository.create(nodo);
        
        assertNotNull(result);
        assertNotNull(result.getPasswordHash());
        assertNotNull(result.getPasswordSalt());
        assertNull(result.getNodoPerifericoPassword()); // Debe ser null después de hashear
        assertEquals(EstadoNodoPeriferico.PENDIENTE, result.getEstado());
        assertNotNull(result.getFechaAlta());
    }

    @Test
    void create_withoutPassword_shouldNotHash() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("12345678-9");
        nodo.setNombre("Clínica Test");
        
        doAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return null;
        }).when(em).persist(any(NodoPeriferico.class));
        
        NodoPeriferico result = repository.create(nodo);
        
        assertNotNull(result);
        verify(em, times(1)).persist(any(NodoPeriferico.class));
    }

    @Test
    void update_withPassword_shouldHashPassword() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoPassword("new-password");
        
        when(em.merge(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        NodoPeriferico result = repository.update(nodo);
        
        assertNotNull(result);
        assertNotNull(result.getPasswordHash());
        assertNotNull(result.getPasswordSalt());
        assertNull(result.getNodoPerifericoPassword());
    }

    @Test
    void find_validId_shouldReturnNodo() {
        Long id = 1L;
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(id);
        
        when(em.find(NodoPeriferico.class, id)).thenReturn(nodo);
        
        NodoPeriferico result = repository.find(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void find_invalidId_shouldReturnNull() {
        when(em.find(NodoPeriferico.class, 999L)).thenReturn(null);
        
        NodoPeriferico result = repository.find(999L);
        
        assertNull(result);
    }

    @Test
    void delete_existingId_shouldRemove() {
        Long id = 1L;
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(id);
        
        when(em.find(NodoPeriferico.class, id)).thenReturn(nodo);
        doNothing().when(em).remove(nodo);
        
        repository.delete(id);
        
        verify(em, times(1)).remove(nodo);
    }

    @Test
    void delete_nonExistentId_shouldNotCrash() {
        when(em.find(NodoPeriferico.class, 999L)).thenReturn(null);
        
        repository.delete(999L);
        
        verify(em, never()).remove(any());
    }

    @Test
    void findByRUT_existingRUT_shouldReturnNodo() {
        String rut = "12345678-9";
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT(rut);
        
        when(em.createQuery(anyString(), eq(NodoPeriferico.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(nodo));
        
        NodoPeriferico result = repository.findByRUT(rut);
        
        assertNotNull(result);
        assertEquals(rut, result.getRUT());
    }

    @Test
    void findByRUT_notFound_shouldReturnNull() {
        when(em.createQuery(anyString(), eq(NodoPeriferico.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());
        
        NodoPeriferico result = repository.findByRUT("99999999-9");
        
        assertNull(result);
    }

    @Test
    void findAll_shouldReturnAll() {
        NodoPeriferico nodo1 = new NodoPeriferico();
        NodoPeriferico nodo2 = new NodoPeriferico();
        
        when(em.createQuery(anyString(), eq(NodoPeriferico.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(nodo1, nodo2));
        
        List<NodoPeriferico> result = repository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateEstadoInNewTx_existingId_shouldUpdateEstado() {
        Long id = 1L;
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(id);
        nodo.setEstado(EstadoNodoPeriferico.PENDIENTE);
        
        when(em.find(NodoPeriferico.class, id)).thenReturn(nodo);
        when(em.merge(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        repository.updateEstadoInNewTx(id, EstadoNodoPeriferico.ACTIVO);
        
        verify(em).find(NodoPeriferico.class, id);
        verify(em).merge(nodo);
        assertEquals(EstadoNodoPeriferico.ACTIVO, nodo.getEstado());
    }

    @Test
    void updateEstadoInNewTx_nonExistentId_shouldNotCrash() {
        when(em.find(NodoPeriferico.class, 999L)).thenReturn(null);
        
        repository.updateEstadoInNewTx(999L, EstadoNodoPeriferico.ACTIVO);
        
        verify(em).find(NodoPeriferico.class, 999L);
        verify(em, never()).merge(any());
    }

    @Test
    void create_withExistingEstado_shouldNotOverride() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("12345678-9");
        nodo.setEstado(EstadoNodoPeriferico.ACTIVO);
        
        doAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return null;
        }).when(em).persist(any(NodoPeriferico.class));
        
        NodoPeriferico result = repository.create(nodo);
        
        assertEquals(EstadoNodoPeriferico.ACTIVO, result.getEstado());
    }

    @Test
    void create_withExistingFechaAlta_shouldNotOverride() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("12345678-9");
        java.time.OffsetDateTime existingDate = java.time.OffsetDateTime.now().minusDays(1);
        nodo.setFechaAlta(existingDate);
        
        doAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return null;
        }).when(em).persist(any(NodoPeriferico.class));
        
        NodoPeriferico result = repository.create(nodo);
        
        assertEquals(existingDate, result.getFechaAlta());
    }

    @Test
    void update_withoutPassword_shouldNotHash() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoPassword(null);
        
        when(em.merge(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        NodoPeriferico result = repository.update(nodo);
        
        assertNotNull(result);
        assertNull(result.getPasswordHash());
        assertNull(result.getPasswordSalt());
    }

    @Test
    void update_withBlankPassword_shouldNotHash() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoPassword("   ");
        
        when(em.merge(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        NodoPeriferico result = repository.update(nodo);
        
        assertNotNull(result);
        assertNull(result.getPasswordHash());
        assertNull(result.getPasswordSalt());
    }
}


