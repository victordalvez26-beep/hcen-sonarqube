package uy.edu.tse.hcen.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.UserNotificationPreferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para UserNotificationPreferencesRepository.
 */
class UserNotificationPreferencesRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<UserNotificationPreferences> query;

    private UserNotificationPreferencesRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new UserNotificationPreferencesRepository();
        
        try {
            java.lang.reflect.Field emField = UserNotificationPreferencesRepository.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(repository, em);
        } catch (Exception e) {
            fail("Error inyectando EntityManager: " + e.getMessage());
        }
    }

    @Test
    void findByUserUid_existingPreferences_shouldReturnPreferences() {
        String userUid = "user123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setId(1L);
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(preferences);
        
        UserNotificationPreferences result = repository.findByUserUid(userUid);
        
        assertNotNull(result);
        assertEquals(userUid, result.getUserUid());
    }

    @Test
    void findByUserUid_notFound_shouldReturnNull() {
        String userUid = "nonexistent";
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        
        UserNotificationPreferences result = repository.findByUserUid(userUid);
        
        assertNull(result);
    }

    @Test
    void saveOrUpdate_newPreferences_shouldPersist() {
        String userUid = "newuser";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        doNothing().when(em).persist(preferences);
        
        UserNotificationPreferences result = repository.saveOrUpdate(preferences);
        
        assertNotNull(result);
        verify(em).persist(preferences);
        verify(em, never()).merge(any());
    }

    @Test
    void saveOrUpdate_existingPreferences_shouldUpdate() {
        String userUid = "existinguser";
        UserNotificationPreferences existing = new UserNotificationPreferences(userUid);
        existing.setId(1L);
        existing.setNotifyResults(true);
        
        UserNotificationPreferences newPrefs = new UserNotificationPreferences(userUid);
        newPrefs.setNotifyResults(false);
        newPrefs.setNotifyNewAccessRequest(false);
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(existing);
        when(em.merge(existing)).thenReturn(existing);
        
        UserNotificationPreferences result = repository.saveOrUpdate(newPrefs);
        
        assertNotNull(result);
        verify(em).merge(existing);
        verify(em, never()).persist(any());
        assertFalse(existing.isNotifyResults());
    }

    @Test
    void saveOrUpdate_withDeviceToken_shouldUpdateToken() {
        String userUid = "user123";
        String deviceToken = "new-device-token";
        UserNotificationPreferences existing = new UserNotificationPreferences(userUid);
        existing.setId(1L);
        
        UserNotificationPreferences newPrefs = new UserNotificationPreferences(userUid);
        newPrefs.setDeviceToken(deviceToken);
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(existing);
        when(em.merge(existing)).thenReturn(existing);
        
        UserNotificationPreferences result = repository.saveOrUpdate(newPrefs);
        
        assertNotNull(result);
        // Verificar que el token fue actualizado verificando el merge
        verify(em).merge(existing);
        // Verificar que el método updateDeviceToken fue llamado (indirectamente)
        assertNotNull(existing.getDeviceToken());
    }

    @Test
    void updateDeviceToken_existingPreferences_shouldUpdate() {
        String userUid = "user123";
        String deviceToken = "device-token-123";
        UserNotificationPreferences preferences = new UserNotificationPreferences(userUid);
        preferences.setId(1L);
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(preferences);
        when(em.merge(preferences)).thenReturn(preferences);
        
        UserNotificationPreferences result = repository.updateDeviceToken(userUid, deviceToken);
        
        assertNotNull(result);
        // Verificar que el token fue actualizado
        assertEquals(deviceToken, preferences.getDeviceToken());
        verify(em).merge(preferences);
    }

    @Test
    void updateDeviceToken_notFound_shouldCreateNew() {
        String userUid = "newuser";
        String deviceToken = "device-token-123";
        
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", userUid)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        doNothing().when(em).persist(any(UserNotificationPreferences.class));
        
        UserNotificationPreferences result = repository.updateDeviceToken(userUid, deviceToken);
        
        assertNotNull(result);
        assertEquals(userUid, result.getUserUid());
        verify(em).persist(any(UserNotificationPreferences.class));
    }

    @Test
    void persist_validPreferences_shouldPersist() {
        UserNotificationPreferences preferences = new UserNotificationPreferences("user123");
        
        doNothing().when(em).persist(preferences);
        
        UserNotificationPreferences result = repository.persist(preferences);
        
        assertNotNull(result);
        verify(em).persist(preferences);
    }

    @Test
    void merge_validPreferences_shouldMerge() {
        UserNotificationPreferences preferences = new UserNotificationPreferences("user123");
        preferences.setId(1L);
        UserNotificationPreferences merged = new UserNotificationPreferences("user123");
        merged.setId(1L);
        
        when(em.merge(preferences)).thenReturn(merged);
        
        UserNotificationPreferences result = repository.merge(preferences);
        
        assertNotNull(result);
        verify(em).merge(preferences);
    }

    @Test
    void findByUserUid_nullUid_shouldReturnNull() {
        when(em.createQuery(anyString(), eq(UserNotificationPreferences.class))).thenReturn(query);
        when(query.setParameter("userUid", null)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        
        UserNotificationPreferences result = repository.findByUserUid(null);
        
        assertNull(result);
    }
}

