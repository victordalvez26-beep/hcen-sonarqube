package uy.edu.tse.hcen.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.PrestadorSalud;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para PrestadorSaludRepository.
 */
class PrestadorSaludRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<PrestadorSalud> typedQuery;

    private PrestadorSaludRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        repository = new PrestadorSaludRepository();
        Field emField = PrestadorSaludRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(repository, em);
    }

    @Test
    void persist_shouldPersistPrestador() {
        PrestadorSalud prestador = new PrestadorSalud("Test Clinic", "test@example.com");
        
        repository.persist(prestador);
        
        verify(em, times(1)).persist(prestador);
    }

    @Test
    void merge_shouldUpdatePrestador() {
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        
        when(em.merge(prestador)).thenReturn(prestador);
        
        PrestadorSalud result = repository.merge(prestador);
        
        assertNotNull(result);
        verify(em, times(1)).merge(prestador);
    }

    @Test
    void findById_validId_shouldReturnPrestador() {
        Long id = 1L;
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(id);
        
        when(em.find(PrestadorSalud.class, id)).thenReturn(prestador);
        
        PrestadorSalud result = repository.findById(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void findById_notFound_shouldReturnNull() {
        when(em.find(PrestadorSalud.class, 999L)).thenReturn(null);
        
        PrestadorSalud result = repository.findById(999L);
        
        assertNull(result);
    }

    @Test
    void findByRut_existingRut_shouldReturnPrestador() {
        String rut = "12345678-9";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setRut(rut);
        
        when(em.createQuery(anyString(), eq(PrestadorSalud.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(prestador);
        
        PrestadorSalud result = repository.findByRut(rut);
        
        assertNotNull(result);
        assertEquals(rut, result.getRut());
    }

    @Test
    void findByRut_notFound_shouldReturnNull() {
        String rut = "99999999-9";
        
        when(em.createQuery(anyString(), eq(PrestadorSalud.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        PrestadorSalud result = repository.findByRut(rut);
        
        assertNull(result);
    }

    @Test
    void findByInvitationToken_validToken_shouldReturnPrestador() {
        String token = "valid-token-123";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setInvitationToken(token);
        
        when(em.createQuery(anyString(), eq(PrestadorSalud.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(prestador);
        
        PrestadorSalud result = repository.findByInvitationToken(token);
        
        assertNotNull(result);
        assertEquals(token, result.getInvitationToken());
    }

    @Test
    void findByInvitationToken_notFound_shouldReturnNull() {
        String token = "invalid-token";
        
        when(em.createQuery(anyString(), eq(PrestadorSalud.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        PrestadorSalud result = repository.findByInvitationToken(token);
        
        assertNull(result);
    }

    @Test
    void findAll_shouldReturnAll() {
        PrestadorSalud p1 = new PrestadorSalud("Clinic 1", "c1@example.com");
        PrestadorSalud p2 = new PrestadorSalud("Clinic 2", "c2@example.com");
        
        when(em.createQuery(anyString(), eq(PrestadorSalud.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(p1, p2));
        
        List<PrestadorSalud> result = repository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void remove_shouldDeletePrestador() {
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        
        when(em.contains(prestador)).thenReturn(true);
        doNothing().when(em).remove(prestador);
        
        repository.remove(prestador);
        
        verify(em, times(1)).remove(prestador);
    }

    @Test
    void remove_notManaged_shouldMergeFirst() {
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        PrestadorSalud managed = new PrestadorSalud("Test", "test@example.com");
        managed.setId(1L);
        
        when(em.contains(prestador)).thenReturn(false);
        when(em.merge(prestador)).thenReturn(managed);
        doNothing().when(em).remove(managed);
        
        repository.remove(prestador);
        
        verify(em, times(1)).merge(prestador);
        verify(em, times(1)).remove(managed);
    }
}


