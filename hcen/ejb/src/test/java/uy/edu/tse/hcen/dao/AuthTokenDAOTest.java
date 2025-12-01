package uy.edu.tse.hcen.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.AuthToken;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para AuthTokenDAO.
 */
class AuthTokenDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<AuthToken> typedQuery;

    private AuthTokenDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        dao = new AuthTokenDAO();
        
        Field emField = AuthTokenDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(dao, entityManager);
    }

    @Test
    void save_validToken_shouldPersist() {
        AuthToken token = new AuthToken();
        token.setToken("test-token");
        token.setUserUid("user-uid");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        dao.save(token);
        
        verify(entityManager).persist(token);
    }

    @Test
    void findByToken_existingToken_shouldReturnToken() {
        String tokenValue = "test-token";
        AuthToken token = new AuthToken();
        token.setToken(tokenValue);
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(token));
        
        AuthToken result = dao.findByToken(tokenValue);
        
        assertNotNull(result);
        assertEquals(tokenValue, result.getToken());
        verify(typedQuery).setParameter("token", tokenValue);
    }

    @Test
    void findByToken_nonExistingToken_shouldReturnNull() {
        String tokenValue = "non-existing-token";
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        AuthToken result = dao.findByToken(tokenValue);
        
        assertNull(result);
    }

    @Test
    void findByToken_multipleResults_shouldReturnFirst() {
        String tokenValue = "test-token";
        AuthToken token1 = new AuthToken();
        token1.setToken(tokenValue);
        AuthToken token2 = new AuthToken();
        token2.setToken(tokenValue);
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(token1, token2));
        
        AuthToken result = dao.findByToken(tokenValue);
        
        assertNotNull(result);
        assertEquals(token1, result);
    }

    @Test
    void markAsUsed_existingToken_shouldMarkAsUsed() {
        String tokenValue = "test-token";
        AuthToken token = new AuthToken();
        token.setToken(tokenValue);
        token.setUsed(false);
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(token));
        
        dao.markAsUsed(tokenValue);
        
        assertTrue(token.isUsed());
        verify(entityManager).merge(token);
    }

    @Test
    void markAsUsed_nonExistingToken_shouldDoNothing() {
        String tokenValue = "non-existing-token";
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        dao.markAsUsed(tokenValue);
        
        verify(entityManager, never()).merge(any());
    }

    @Test
    void markAsUsed_alreadyUsed_shouldStillMerge() {
        String tokenValue = "test-token";
        AuthToken token = new AuthToken();
        token.setToken(tokenValue);
        token.setUsed(true);
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(token));
        
        dao.markAsUsed(tokenValue);
        
        verify(entityManager).merge(token);
    }

    @Test
    void deleteExpiredTokens_shouldRemoveExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        AuthToken expired1 = new AuthToken();
        expired1.setToken("expired1");
        expired1.setExpiresAt(now.minusHours(1));
        AuthToken expired2 = new AuthToken();
        expired2.setToken("expired2");
        expired2.setExpiresAt(now.minusDays(1));
        
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("now"), any(LocalDateTime.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(expired1, expired2));
        
        dao.deleteExpiredTokens();
        
        verify(entityManager).remove(expired1);
        verify(entityManager).remove(expired2);
    }

    @Test
    void deleteExpiredTokens_noExpiredTokens_shouldDoNothing() {
        when(entityManager.createQuery(anyString(), eq(AuthToken.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("now"), any(LocalDateTime.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        dao.deleteExpiredTokens();
        
        verify(entityManager, never()).remove(any());
    }

    @Test
    void delete_managedEntity_shouldRemove() {
        AuthToken token = new AuthToken();
        token.setToken("test-token");
        when(entityManager.contains(token)).thenReturn(true);
        
        dao.delete(token);
        
        verify(entityManager).remove(token);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void delete_detachedEntity_shouldMergeThenRemove() {
        AuthToken token = new AuthToken();
        token.setToken("test-token");
        AuthToken mergedToken = new AuthToken();
        mergedToken.setToken("test-token");
        
        when(entityManager.contains(token)).thenReturn(false);
        when(entityManager.merge(token)).thenReturn(mergedToken);
        
        dao.delete(token);
        
        verify(entityManager).merge(token);
        verify(entityManager).remove(mergedToken);
    }

    @Test
    void delete_nullToken_shouldThrowException() {
        // El EntityManager.merge(null) o remove(null) lanzará IllegalArgumentException
        // según la especificación JPA
        when(entityManager.contains(null)).thenReturn(false);
        when(entityManager.merge(null)).thenThrow(new IllegalArgumentException("Entity cannot be null"));
        
        assertThrows(IllegalArgumentException.class, () -> dao.delete(null));
    }
}

