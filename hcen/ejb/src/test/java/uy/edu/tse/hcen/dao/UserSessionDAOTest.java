package uy.edu.tse.hcen.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.UserSession;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para UserSessionDAO.
 */
class UserSessionDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<UserSession> typedQuery;

    private UserSessionDAO userSessionDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userSessionDAO = new UserSessionDAO();
        Field emField = UserSessionDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(userSessionDAO, em);
    }

    @Test
    void save_shouldPersistSession() {
        UserSession session = new UserSession();
        session.setUserUid("test-user");
        session.setJwtToken("jwt-token");
        
        userSessionDAO.save(session);
        
        verify(em, times(1)).persist(session);
    }

    @Test
    void findByJwtToken_validToken_shouldReturnSession() {
        String jwtToken = "valid-jwt-token";
        UserSession session = new UserSession();
        session.setJwtToken(jwtToken);
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(session);
        
        UserSession result = userSessionDAO.findByJwtToken(jwtToken);
        
        assertNotNull(result);
        assertEquals(jwtToken, result.getJwtToken());
    }

    @Test
    void findByJwtToken_notFound_shouldReturnNull() {
        String jwtToken = "invalid-token";
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        UserSession result = userSessionDAO.findByJwtToken(jwtToken);
        
        assertNull(result);
    }

    @Test
    void findActiveByUserUid_shouldReturnActiveSessions() {
        String userUid = "test-user-123";
        UserSession session1 = new UserSession();
        session1.setUserUid(userUid);
        session1.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        UserSession session2 = new UserSession();
        session2.setUserUid(userUid);
        session2.setExpiresAt(LocalDateTime.now().plusHours(2));
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(session1, session2));
        
        List<UserSession> result = userSessionDAO.findActiveByUserUid(userUid);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findActiveByUserUid_noActiveSessions_shouldReturnEmptyList() {
        String userUid = "test-user-123";
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());
        
        List<UserSession> result = userSessionDAO.findActiveByUserUid(userUid);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByJwtToken_existingSession_shouldReturnTrue() {
        String jwtToken = "valid-jwt-token";
        UserSession session = new UserSession();
        session.setUserUid("test-user");
        session.setJwtToken(jwtToken);
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(session);
        
        boolean result = userSessionDAO.deleteByJwtToken(jwtToken);
        
        assertTrue(result);
        verify(em).remove(session);
    }

    @Test
    void deleteByJwtToken_notFound_shouldReturnFalse() {
        String jwtToken = "invalid-token";
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        boolean result = userSessionDAO.deleteByJwtToken(jwtToken);
        
        assertFalse(result);
        verify(em, never()).remove(any());
    }

    @Test
    void deleteByJwtToken_exception_shouldReturnFalse() {
        String jwtToken = "valid-token";
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenThrow(new RuntimeException("DB error"));
        
        boolean result = userSessionDAO.deleteByJwtToken(jwtToken);
        
        assertFalse(result);
    }

    @Test
    void deleteAllByUserUid_shouldExecuteUpdate() {
        String userUid = "test-user-123";
        
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(2);
        
        int result = userSessionDAO.deleteAllByUserUid(userUid);
        
        assertEquals(2, result);
        verify(query).executeUpdate();
    }

    @Test
    void cleanExpiredSessions_shouldExecuteUpdate() {
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(5);
        
        int result = userSessionDAO.cleanExpiredSessions();
        
        assertEquals(5, result);
        verify(query).executeUpdate();
    }

    @Test
    void updateAccessToken_existingSession_shouldUpdate() {
        String jwtToken = "valid-jwt-token";
        String newAccessToken = "new-access-token";
        UserSession session = new UserSession();
        session.setJwtToken(jwtToken);
        session.setAccessToken("old-access-token");
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(session);
        
        userSessionDAO.updateAccessToken(jwtToken, newAccessToken);
        
        verify(em).merge(session);
        assertEquals(newAccessToken, session.getAccessToken());
    }

    @Test
    void updateAccessToken_notFound_shouldNotUpdate() {
        String jwtToken = "invalid-token";
        String newAccessToken = "new-access-token";
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        userSessionDAO.updateAccessToken(jwtToken, newAccessToken);
        
        verify(em, never()).merge(any());
    }

    @Test
    void save_shouldReturnSession() {
        UserSession session = new UserSession();
        session.setUserUid("test-user");
        session.setJwtToken("jwt-token");
        
        UserSession result = userSessionDAO.save(session);
        
        assertNotNull(result);
        assertEquals(session, result);
        verify(em).persist(session);
    }

    @Test
    void findByJwtToken_withNullToken_shouldReturnNull() {
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        UserSession result = userSessionDAO.findByJwtToken(null);
        
        assertNull(result);
    }

    @Test
    void findActiveByUserUid_withNullUid_shouldReturnEmptyList() {
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());
        
        List<UserSession> result = userSessionDAO.findActiveByUserUid(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteAllByUserUid_withNullUid_shouldExecuteUpdate() {
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), isNull())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);
        
        int result = userSessionDAO.deleteAllByUserUid(null);
        
        assertEquals(0, result);
    }

    @Test
    void cleanExpiredSessions_withNoExpired_shouldReturnZero() {
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);
        
        int result = userSessionDAO.cleanExpiredSessions();
        
        assertEquals(0, result);
    }

    @Test
    void updateAccessToken_withNullJwtToken_shouldNotUpdate() {
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        userSessionDAO.updateAccessToken(null, "new-token");
        
        verify(em, never()).merge(any());
    }

    @Test
    void updateAccessToken_withNullNewToken_shouldStillUpdate() {
        String jwtToken = "valid-jwt-token";
        UserSession session = new UserSession();
        session.setJwtToken(jwtToken);
        session.setAccessToken("old-access-token");
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(session);
        
        userSessionDAO.updateAccessToken(jwtToken, null);
        
        verify(em).merge(session);
        assertNull(session.getAccessToken());
    }

    @Test
    void deleteByJwtToken_withNullToken_shouldReturnFalse() {
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        boolean result = userSessionDAO.deleteByJwtToken(null);
        
        assertFalse(result);
    }

    @Test
    void findActiveByUserUid_withExpiredSessions_shouldFilterCorrectly() {
        String userUid = "test-user-123";
        UserSession activeSession = new UserSession();
        activeSession.setUserUid(userUid);
        activeSession.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(em.createQuery(anyString(), eq(UserSession.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(activeSession));
        
        List<UserSession> result = userSessionDAO.findActiveByUserUid(userUid);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}


