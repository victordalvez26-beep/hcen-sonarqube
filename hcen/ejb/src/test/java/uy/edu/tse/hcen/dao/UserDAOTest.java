package uy.edu.tse.hcen.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para UserDAO.
 * Nota: Estos tests usan mocks de EntityManager ya que JPA requiere
 * un contenedor de persistencia para funcionar completamente.
 */
class UserDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<User> typedQuery;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userDAO = new UserDAO();
        Field emField = UserDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(userDAO, em);
    }

    @Test
    void findByUid_existingUser_shouldReturnUser() {
        String uid = "test-user-123";
        User user = new User();
        user.setUid(uid);
        user.setEmail("test@example.com");
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(user);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("US");
        
        User result = userDAO.findByUid(uid);
        
        assertNotNull(result);
        assertEquals(uid, result.getUid());
    }

    @Test
    void findByUid_notFound_shouldReturnNull() {
        String uid = "non-existent";
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenThrow(new NoResultException());
        
        User result = userDAO.findByUid(uid);
        
        assertNull(result);
    }

    @Test
    void findByEmail_existingUser_shouldReturnUser() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(user);
        
        User result = userDAO.findByEmail(email);
        
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void findByEmail_notFound_shouldReturnNull() {
        String email = "notfound@example.com";
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        User result = userDAO.findByEmail(email);
        
        assertNull(result);
    }

    @Test
    void saveOrUpdate_newUser_shouldPersist() {
        User newUser = new User();
        newUser.setUid("new-user-123");
        newUser.setEmail("new@example.com");
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        User result = userDAO.saveOrUpdate(newUser);
        
        assertNotNull(result);
        verify(em, times(1)).persist(newUser);
    }

    @Test
    void saveOrUpdate_existingUser_shouldUpdate() {
        String uid = "existing-user-123";
        User existingUser = new User();
        existingUser.setUid(uid);
        existingUser.setRol(Rol.USUARIO_SALUD);
        
        User updatedData = new User();
        updatedData.setUid(uid);
        updatedData.setEmail("updated@example.com");
        updatedData.setPrimerNombre("Updated");
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(existingUser);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("US");
        when(em.merge(any(User.class))).thenReturn(existingUser);
        
        User result = userDAO.saveOrUpdate(updatedData);
        
        assertNotNull(result);
        verify(em, times(1)).merge(any(User.class));
    }

    @Test
    void updateUserRole_validUser_shouldUpdate() {
        String uid = "test-user-123";
        Rol newRole = Rol.ADMIN_HCEN;
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);
        when(nativeQuery.getSingleResult()).thenReturn("AD");
        
        boolean result = userDAO.updateUserRole(uid, newRole);
        
        assertTrue(result);
        verify(em, times(1)).flush();
        verify(em, atLeastOnce()).clear();
    }

    @Test
    void updateUserRole_userNotFound_shouldReturnFalse() {
        String uid = "non-existent";
        Rol newRole = Rol.ADMIN_HCEN;
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(0);
        
        boolean result = userDAO.updateUserRole(uid, newRole);
        
        assertFalse(result);
    }

    @Test
    void findByRol_validRol_shouldReturnList() {
        String rolCodigo = "US";
        User user1 = new User();
        User user2 = new User();
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(user1, user2));
        
        List<User> result = userDAO.findByRol(rolCodigo);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findByRol_invalidRol_shouldReturnEmptyList() {
        String rolCodigo = "INVALID";
        
        List<User> result = userDAO.findByRol(rolCodigo);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        User user1 = new User();
        User user2 = new User();
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(user1, user2));
        
        List<User> result = userDAO.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void countUsuarios_shouldReturnCount() {
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(5L);
        
        long result = userDAO.countUsuarios();
        
        assertEquals(5L, result);
    }

    @Test
    void deleteByUid_existingUser_shouldDelete() {
        String uid = "test-user-123";
        User user = new User();
        user.setUid(uid);
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(user);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("US");
        
        boolean result = userDAO.deleteByUid(uid);
        
        assertTrue(result);
        verify(em, times(1)).remove(user);
    }

    @Test
    void deleteByUid_notFound_shouldReturnFalse() {
        String uid = "non-existent";
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenThrow(new NoResultException());
        
        boolean result = userDAO.deleteByUid(uid);
        
        assertFalse(result);
        verify(em, never()).remove(any(User.class));
    }

    @Test
    void deleteByUid_exception_shouldReturnFalse() {
        String uid = "test-user";
        
        when(em.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));
        
        boolean result = userDAO.deleteByUid(uid);
        
        assertFalse(result);
    }

    @Test
    void findByDocumento_existingUser_shouldReturnUser() {
        String codDocum = "12345678";
        User user = new User();
        user.setCodDocum(codDocum);
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(user);
        
        User result = userDAO.findByDocumento(codDocum);
        
        assertNotNull(result);
        assertEquals(codDocum, result.getCodDocum());
    }

    @Test
    void findByDocumento_notFound_shouldReturnNull() {
        String codDocum = "99999999";
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
        
        User result = userDAO.findByDocumento(codDocum);
        
        assertNull(result);
    }

    @Test
    void persist_shouldPersistUser() {
        User user = new User();
        user.setUid("new-user");
        
        userDAO.persist(user);
        
        verify(em).persist(user);
    }

    @Test
    void merge_shouldMergeUser() {
        User user = new User();
        user.setUid("existing-user");
        User mergedUser = new User();
        mergedUser.setUid("existing-user");
        
        when(em.merge(user)).thenReturn(mergedUser);
        
        User result = userDAO.merge(user);
        
        assertNotNull(result);
        verify(em).merge(user);
    }

    @Test
    void countUsuariosPorRol_shouldReturnMap() {
        @SuppressWarnings("unchecked")
        TypedQuery<Object[]> objectQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(objectQuery);
        when(objectQuery.getResultList()).thenReturn(Arrays.asList(
            new Object[]{Rol.USUARIO_SALUD, 5L},
            new Object[]{Rol.ADMIN_HCEN, 2L}
        ));
        
        Map<String, Long> result = userDAO.countUsuariosPorRol();
        
        assertNotNull(result);
        assertTrue(result.containsKey("US"));
        assertTrue(result.containsKey("AD"));
    }

    @Test
    void countUsuariosPorRol_exception_shouldReturnEmptyMap() {
        when(em.createQuery(anyString(), eq(Object[].class))).thenThrow(new RuntimeException("DB error"));
        
        Map<String, Long> result = userDAO.countUsuariosPorRol();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void countUsuariosConPerfilCompleto_true_shouldReturnCount() {
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), anyBoolean())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(10L);
        
        long result = userDAO.countUsuariosConPerfilCompleto(true);
        
        assertEquals(10L, result);
    }

    @Test
    void countUsuariosConPerfilCompleto_false_shouldReturnCount() {
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), anyBoolean())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(5L);
        
        long result = userDAO.countUsuariosConPerfilCompleto(false);
        
        assertEquals(5L, result);
    }

    @Test
    void countUsuariosConPerfilCompleto_exception_shouldReturnZero() {
        when(em.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("DB error"));
        
        long result = userDAO.countUsuariosConPerfilCompleto(true);
        
        assertEquals(0L, result);
    }

    @Test
    void getRolFromDatabase_existingUser_shouldReturnRol() {
        String uid = "test-user";
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("US");
        
        Rol result = userDAO.getRolFromDatabase(uid);
        
        assertNotNull(result);
        assertEquals(Rol.USUARIO_SALUD, result);
        verify(em).flush();
    }

    @Test
    void getRolFromDatabase_notFound_shouldReturnNull() {
        String uid = "non-existent";
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenThrow(new NoResultException());
        
        Rol result = userDAO.getRolFromDatabase(uid);
        
        assertNull(result);
    }

    @Test
    void getRolFromDatabase_invalidRol_shouldReturnNull() {
        String uid = "test-user";
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("INVALID");
        
        Rol result = userDAO.getRolFromDatabase(uid);
        
        assertNull(result);
    }

    @Test
    void getRolFromDatabase_emptyRol_shouldReturnNull() {
        String uid = "test-user";
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("");
        
        Rol result = userDAO.getRolFromDatabase(uid);
        
        assertNull(result);
    }

    @Test
    void saveOrUpdate_existingUser_withNullFields_shouldUpdate() {
        String uid = "existing-user";
        User existingUser = new User();
        existingUser.setUid(uid);
        existingUser.setRol(Rol.USUARIO_SALUD);
        
        User updatedData = new User();
        updatedData.setUid(uid);
        updatedData.setEmail(null);
        updatedData.setPrimerNombre(null);
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(existingUser);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("US");
        when(em.merge(any(User.class))).thenReturn(existingUser);
        
        User result = userDAO.saveOrUpdate(updatedData);
        
        assertNotNull(result);
        verify(em).merge(any(User.class));
    }

    @Test
    void saveOrUpdate_existingUser_withProfileCompleted_shouldUpdate() {
        String uid = "existing-user";
        User existingUser = new User();
        existingUser.setUid(uid);
        existingUser.setRol(Rol.USUARIO_SALUD);
        existingUser.setProfileCompleted(false);
        
        User updatedData = new User();
        updatedData.setUid(uid);
        updatedData.setProfileCompleted(true);
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(existingUser);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("US");
        when(em.merge(any(User.class))).thenReturn(existingUser);
        
        User result = userDAO.saveOrUpdate(updatedData);
        
        assertNotNull(result);
        verify(em).merge(any(User.class));
    }

    @Test
    void findByUid_withRolInconsistency_shouldCorrectRol() {
        String uid = "test-user";
        User user = new User();
        user.setUid(uid);
        user.setRol(Rol.USUARIO_SALUD);
        
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(user);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn("AD"); // Rol diferente en BD
        
        User result = userDAO.findByUid(uid);
        
        assertNotNull(result);
        verify(em, atLeastOnce()).refresh(any(User.class));
    }

    @Test
    void findAll_exception_shouldReturnEmptyList() {
        when(em.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));
        
        List<User> result = userDAO.findAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void countUsuarios_exception_shouldReturnZero() {
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenThrow(new RuntimeException("DB error"));
        
        long result = userDAO.countUsuarios();
        
        assertEquals(0L, result);
    }

    @Test
    void countUsuarios_nullResult_shouldReturnZero() {
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(null);
        
        long result = userDAO.countUsuarios();
        
        assertEquals(0L, result);
    }

    @Test
    void updateUserRole_withRolMismatch_shouldLogWarning() {
        String uid = "test-user";
        Rol newRole = Rol.ADMIN_HCEN;
        
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);
        when(nativeQuery.getSingleResult()).thenReturn("US"); // Rol diferente al esperado
        
        boolean result = userDAO.updateUserRole(uid, newRole);
        
        assertTrue(result);
        verify(em).flush();
    }
}

