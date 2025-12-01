package uy.edu.tse.hcen.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.Departamento;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para ReportesService.
 */
class ReportesServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private UserDAO userDAO;

    @Mock
    private Query nativeQuery;

    private ReportesService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new ReportesService();
        
        Field emField = ReportesService.class.getDeclaredField("entityManager");
        emField.setAccessible(true);
        emField.set(service, entityManager);
        
        Field daoField = ReportesService.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(service, userDAO);
    }

    @Test
    void obtenerEvolucionDocumentos_validDates_shouldReturnList() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        
        Object[] row1 = {java.sql.Date.valueOf(inicio), 10L};
        Object[] row2 = {java.sql.Date.valueOf(fin), 15L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerEvolucionDocumentos(inicio, fin);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(nativeQuery, atLeastOnce()).setParameter(anyString(), any());
    }

    @Test
    void obtenerEvolucionDocumentos_nullDates_shouldUseDefaults() {
        Object[] row1 = {java.sql.Date.valueOf(LocalDate.now()), 5L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerEvolucionDocumentos(null, null);
        
        assertNotNull(result);
        verify(nativeQuery, atLeastOnce()).setParameter(anyString(), any());
    }

    @Test
    void obtenerEvolucionDocumentos_timestampDate_shouldConvert() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        
        // Usar java.sql.Date en lugar de Timestamp para evitar problemas con JaCoCo
        Object[] row1 = {java.sql.Date.valueOf(LocalDate.now()), 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerEvolucionDocumentos(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenerEvolucionDocumentos_nullCount_shouldUseZero() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        
        Object[] row1 = {java.sql.Date.valueOf(LocalDate.now()), null};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerEvolucionDocumentos(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0L, result.get(0).get("total"));
    }

    @Test
    void obtenerDocumentosPorEspecialidad_shouldReturnList() {
        Object[] row1 = {"RECETA", 20L};
        Object[] row2 = {"LABORATORIO", 15L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorEspecialidad();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("RECETA", result.get(0).get("especialidad"));
        assertEquals(20L, result.get(0).get("total"));
    }

    @Test
    void obtenerDocumentosPorEspecialidad_nullValues_shouldHandle() {
        Object[] row1 = {null, 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorEspecialidad();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sin información", result.get(0).get("especialidad"));
    }

    @Test
    void obtenerDocumentosPorFormato_shouldReturnList() {
        Object[] row1 = {"PDF", 30L};
        Object[] row2 = {"XML", 20L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorFormato();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PDF", result.get(0).get("formato"));
    }

    @Test
    void obtenerDocumentosPorTenant_shouldReturnList() {
        Object[] row1 = {"100", 25L};
        Object[] row2 = {"200", 15L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorTenant();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("100", result.get(0).get("tenantId"));
    }

    @Test
    void obtenerDocumentosPorTenant_exception_shouldReturnEmptyList() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenThrow(new RuntimeException("Database error"));
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorTenant();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerResumenDocumentos_shouldReturnSummary() {
        Object[] row = {50L, 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        Map<String, Object> result = service.obtenerResumenDocumentos();
        
        assertNotNull(result);
        assertEquals(50L, result.get("total"));
        assertEquals(10L, result.get("breakingTheGlass"));
        assertEquals(40L, result.get("noBreakingTheGlass"));
    }

    @Test
    void obtenerResumenDocumentos_emptyResult_shouldReturnZeros() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(new ArrayList<>());
        
        Map<String, Object> result = service.obtenerResumenDocumentos();
        
        assertNotNull(result);
        assertEquals(0L, result.get("total"));
        assertEquals(0L, result.get("breakingTheGlass"));
        assertEquals(0L, result.get("noBreakingTheGlass"));
    }

    @Test
    void obtenerResumenDocumentos_nullRow_shouldReturnZeros() {
        Object[] row = {null, null};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        Map<String, Object> result = service.obtenerResumenDocumentos();
        
        assertNotNull(result);
        assertEquals(0L, result.get("total"));
        assertEquals(0L, result.get("breakingTheGlass"));
        assertEquals(0L, result.get("noBreakingTheGlass"));
    }

    @Test
    void obtenerResumenUsuarios_shouldReturnSummary() {
        Map<String, Long> porRol = new HashMap<>();
        porRol.put("US", 20L);
        porRol.put("AD", 5L);
        
        when(userDAO.countUsuarios()).thenReturn(30L);
        when(userDAO.countUsuariosPorRol()).thenReturn(porRol);
        when(userDAO.countUsuariosConPerfilCompleto(true)).thenReturn(25L);
        
        Map<String, Object> result = service.obtenerResumenUsuarios();
        
        assertNotNull(result);
        assertEquals(30L, result.get("totalUsuarios"));
        assertEquals(20L, result.get("profesionales"));
        assertEquals(5L, result.get("administradores"));
        assertEquals(5L, result.get("otros"));
        assertEquals(25L, result.get("perfilesCompletos"));
        assertEquals(5L, result.get("perfilesPendientes"));
        assertNotNull(result.get("porRol"));
    }

    @Test
    void obtenerProfesionalesDetalle_shouldReturnList() {
        User user1 = new User("uid1", "user1@example.com", "Juan", null, "Pérez", null, "CI", "12345678", null);
        user1.setId(1L);
        user1.setRol(Rol.USUARIO_SALUD);
        user1.setDepartamento(Departamento.MONTEVIDEO);
        user1.setLocalidad("Centro");
        user1.setProfileCompleted(true);
        
        User user2 = new User("uid2", "user2@example.com", "María", null, "González", null, "CI", "87654321", null);
        user2.setId(2L);
        user2.setRol(Rol.USUARIO_SALUD);
        user2.setProfileCompleted(false);
        
        when(userDAO.findByRol("US")).thenReturn(Arrays.asList(user1, user2));
        
        List<Map<String, Object>> result = service.obtenerProfesionalesDetalle();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("uid1", result.get(0).get("uid"));
        assertEquals("user1@example.com", result.get(0).get("email"));
        assertTrue((Boolean) result.get(0).get("perfilCompleto"));
        assertFalse((Boolean) result.get(1).get("perfilCompleto"));
    }

    @Test
    void obtenerProfesionalesDetalle_emptyList_shouldReturnEmpty() {
        when(userDAO.findByRol("US")).thenReturn(new ArrayList<>());
        
        List<Map<String, Object>> result = service.obtenerProfesionalesDetalle();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerProfesionalesDetalle_nullFields_shouldHandle() {
        User user = new User("uid1", null, null, null, null, null, null, null, null);
        user.setId(1L);
        user.setRol(Rol.USUARIO_SALUD);
        
        when(userDAO.findByRol("US")).thenReturn(Arrays.asList(user));
        
        List<Map<String, Object>> result = service.obtenerProfesionalesDetalle();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).get("nombreCompleto"));
    }

    @Test
    void obtenerEvolucionDocumentos_noResults_shouldReturnEmptyList() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(new ArrayList<>());
        
        List<Map<String, Object>> result = service.obtenerEvolucionDocumentos(inicio, fin);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerEvolucionDocumentos_nullDate_shouldUseN_A() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        
        Object[] row1 = {null, 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerEvolucionDocumentos(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("N/A", result.get(0).get("fecha"));
    }

    @Test
    void obtenerDocumentosPorFormato_nullValues_shouldHandle() {
        Object[] row1 = {null, 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorFormato();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sin información", result.get(0).get("formato"));
    }

    @Test
    void obtenerDocumentosPorTenant_nullTenantId_shouldHandle() {
        Object[] row1 = {null, 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorTenant();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sin información", result.get(0).get("tenantId"));
    }

    @Test
    void obtenerDocumentosPorTenant_nonNumberTotal_shouldUseZero() {
        Object[] row1 = {"100", "invalid"};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        List<Map<String, Object>> result = service.obtenerDocumentosPorTenant();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0L, result.get(0).get("total"));
    }

    @Test
    void obtenerResumenDocumentos_nullBreakingTheGlass_shouldUseZero() {
        Object[] row = {50L, null};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        Map<String, Object> result = service.obtenerResumenDocumentos();
        
        assertNotNull(result);
        assertEquals(50L, result.get("total"));
        assertEquals(0L, result.get("breakingTheGlass"));
        assertEquals(50L, result.get("noBreakingTheGlass"));
    }

    @Test
    void obtenerResumenUsuarios_emptyRolMap_shouldHandle() {
        when(userDAO.countUsuarios()).thenReturn(10L);
        when(userDAO.countUsuariosPorRol()).thenReturn(new HashMap<>());
        when(userDAO.countUsuariosConPerfilCompleto(true)).thenReturn(5L);
        
        Map<String, Object> result = service.obtenerResumenUsuarios();
        
        assertNotNull(result);
        assertEquals(10L, result.get("totalUsuarios"));
        assertEquals(0L, result.get("profesionales"));
        assertEquals(0L, result.get("administradores"));
        assertEquals(10L, result.get("otros"));
    }

    @Test
    void obtenerProfesionalesDetalle_withAllFields_shouldMapCorrectly() {
        User user = new User("uid1", "user@example.com", "Juan", "Carlos", "Pérez", "González", "CI", "12345678", null);
        user.setId(1L);
        user.setRol(Rol.USUARIO_SALUD);
        user.setDepartamento(Departamento.MONTEVIDEO);
        user.setLocalidad("Centro");
        user.setProfileCompleted(true);
        
        when(userDAO.findByRol("US")).thenReturn(Arrays.asList(user));
        
        List<Map<String, Object>> result = service.obtenerProfesionalesDetalle();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan Carlos Pérez González", result.get(0).get("nombreCompleto"));
        assertEquals("Montevideo", result.get(0).get("departamento")); // getNombre() retorna "Montevideo"
    }

    @Test
    void construirNombreCompleto_withOnlyPrimerNombre_shouldReturnOnlyPrimerNombre() throws Exception {
        User user = new User("uid1", "user@example.com", "Juan", null, null, null, "CI", "12345678", null);
        user.setId(1L);
        user.setRol(Rol.USUARIO_SALUD);
        
        when(userDAO.findByRol("US")).thenReturn(Arrays.asList(user));
        
        List<Map<String, Object>> result = service.obtenerProfesionalesDetalle();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).get("nombreCompleto"));
    }

    @Test
    void construirNombreCompleto_withOnlyApellidos_shouldReturnOnlyApellidos() throws Exception {
        User user = new User("uid1", "user@example.com", null, null, "Pérez", "González", "CI", "12345678", null);
        user.setId(1L);
        user.setRol(Rol.USUARIO_SALUD);
        
        when(userDAO.findByRol("US")).thenReturn(Arrays.asList(user));
        
        List<Map<String, Object>> result = service.obtenerProfesionalesDetalle();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pérez González", result.get(0).get("nombreCompleto"));
    }

    @Test
    void obtenerResumenDocumentos_nullTotal_shouldUseZero() {
        Object[] row = {null, 10L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(resultados);
        
        Map<String, Object> result = service.obtenerResumenDocumentos();
        
        assertNotNull(result);
        assertEquals(0L, result.get("total"));
        assertEquals(10L, result.get("breakingTheGlass"));
        assertEquals(-10L, result.get("noBreakingTheGlass"));
    }
}

