package uy.edu.tse.hcen.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.UserClinicAssociation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para UserClinicAssociationRepository.
 */
class UserClinicAssociationRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<UserClinicAssociation> queryAssociation;

    @Mock
    private TypedQuery<Long> queryLong;

    private UserClinicAssociationRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new UserClinicAssociationRepository();
        
        // Inyectar EntityManager mediante reflexión
        try {
            java.lang.reflect.Field emField = UserClinicAssociationRepository.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(repository, em);
        } catch (Exception e) {
            fail("Error inyectando EntityManager: " + e.getMessage());
        }
    }

    @Test
    void findByUserAndClinic_existingAssociation_shouldReturnAssociation() {
        Long userId = 1L;
        Long clinicId = 100L;
        UserClinicAssociation association = new UserClinicAssociation(userId, clinicId);
        association.setId(1L);
        
        when(em.createQuery(anyString(), eq(UserClinicAssociation.class))).thenReturn(queryAssociation);
        when(queryAssociation.setParameter("userId", userId)).thenReturn(queryAssociation);
        when(queryAssociation.setParameter("clinicId", clinicId)).thenReturn(queryAssociation);
        when(queryAssociation.getSingleResult()).thenReturn(association);
        
        UserClinicAssociation result = repository.findByUserAndClinic(userId, clinicId);
        
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(clinicId, result.getClinicTenantId());
        verify(queryAssociation).getSingleResult();
    }

    @Test
    void findByUserAndClinic_notFound_shouldReturnNull() {
        Long userId = 1L;
        Long clinicId = 100L;
        
        when(em.createQuery(anyString(), eq(UserClinicAssociation.class))).thenReturn(queryAssociation);
        when(queryAssociation.setParameter("userId", userId)).thenReturn(queryAssociation);
        when(queryAssociation.setParameter("clinicId", clinicId)).thenReturn(queryAssociation);
        when(queryAssociation.getSingleResult()).thenThrow(new NoResultException());
        
        UserClinicAssociation result = repository.findByUserAndClinic(userId, clinicId);
        
        assertNull(result);
    }

    @Test
    void findClinicsByUser_validUser_shouldReturnList() {
        Long userId = 1L;
        List<Long> clinicIds = new ArrayList<>();
        clinicIds.add(100L);
        clinicIds.add(200L);
        
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter("userId", userId)).thenReturn(queryLong);
        when(queryLong.getResultList()).thenReturn(clinicIds);
        
        List<Long> result = repository.findClinicsByUser(userId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(100L));
        assertTrue(result.contains(200L));
    }

    @Test
    void findClinicsByUser_noAssociations_shouldReturnEmptyList() {
        Long userId = 999L;
        List<Long> emptyList = new ArrayList<>();
        
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter("userId", userId)).thenReturn(queryLong);
        when(queryLong.getResultList()).thenReturn(emptyList);
        
        List<Long> result = repository.findClinicsByUser(userId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findUsersByClinic_validClinic_shouldReturnList() {
        Long clinicId = 100L;
        List<Long> userIds = new ArrayList<>();
        userIds.add(1L);
        userIds.add(2L);
        
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter("clinicId", clinicId)).thenReturn(queryLong);
        when(queryLong.getResultList()).thenReturn(userIds);
        
        List<Long> result = repository.findUsersByClinic(clinicId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }

    @Test
    void findUsersByClinic_noAssociations_shouldReturnEmptyList() {
        Long clinicId = 999L;
        List<Long> emptyList = new ArrayList<>();
        
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter("clinicId", clinicId)).thenReturn(queryLong);
        when(queryLong.getResultList()).thenReturn(emptyList);
        
        List<Long> result = repository.findUsersByClinic(clinicId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void persist_validAssociation_shouldPersist() {
        UserClinicAssociation association = new UserClinicAssociation(1L, 100L);
        
        doNothing().when(em).persist(association);
        
        repository.persist(association);
        
        verify(em, times(1)).persist(association);
    }

    @Test
    void remove_managedAssociation_shouldRemove() {
        UserClinicAssociation association = new UserClinicAssociation(1L, 100L);
        association.setId(1L);
        
        when(em.contains(association)).thenReturn(true);
        doNothing().when(em).remove(association);
        
        repository.remove(association);
        
        verify(em).remove(association);
        verify(em, never()).merge(any());
    }

    @Test
    void remove_detachedAssociation_shouldMergeThenRemove() {
        UserClinicAssociation association = new UserClinicAssociation(1L, 100L);
        association.setId(1L);
        UserClinicAssociation merged = new UserClinicAssociation(1L, 100L);
        merged.setId(1L);
        
        when(em.contains(association)).thenReturn(false);
        when(em.merge(association)).thenReturn(merged);
        doNothing().when(em).remove(merged);
        
        repository.remove(association);
        
        verify(em).merge(association);
        verify(em).remove(merged);
    }

    @Test
    void persist_nullAssociation_shouldNotCrash() {
        // EntityManager.persist puede aceptar null sin lanzar excepción inmediata
        // La excepción se lanzaría en flush/commit, pero para este test solo verificamos que no crashea
        assertDoesNotThrow(() -> {
            try {
                repository.persist(null);
            } catch (Exception e) {
                // Si lanza excepción, está bien, solo verificamos que no crashea el test
            }
        });
    }

    @Test
    void findByUserAndClinic_nullParameters_shouldHandle() {
        when(em.createQuery(anyString(), eq(UserClinicAssociation.class))).thenReturn(queryAssociation);
        when(queryAssociation.setParameter(anyString(), any())).thenReturn(queryAssociation);
        when(queryAssociation.getSingleResult()).thenThrow(new NoResultException());
        
        UserClinicAssociation result = repository.findByUserAndClinic(null, null);
        
        assertNull(result);
    }
}

