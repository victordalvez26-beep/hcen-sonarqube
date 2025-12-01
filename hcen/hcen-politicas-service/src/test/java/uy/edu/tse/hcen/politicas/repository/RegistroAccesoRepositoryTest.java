package uy.edu.tse.hcen.politicas.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para RegistroAccesoRepository.
 */
class RegistroAccesoRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<RegistroAcceso> typedQuery;

    @Mock
    private Query nativeQuery;

    private RegistroAccesoRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        repository = new RegistroAccesoRepository();
        Field emField = RegistroAccesoRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(repository, em);
    }

    @Test
    void crear_shouldPersistRegistro() {
        RegistroAcceso registro = new RegistroAcceso();
        registro.setProfesionalId("PROF-001");
        registro.setCodDocumPaciente("1.234.567-8");
        
        doAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            return null;
        }).when(em).persist(any(RegistroAcceso.class));
        
        RegistroAcceso result = repository.crear(registro);
        
        assertNotNull(result);
        verify(em, times(1)).persist(registro);
    }

    @Test
    void buscarPorId_validId_shouldReturnRegistro() {
        Long id = 1L;
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(id);
        
        when(em.find(RegistroAcceso.class, id)).thenReturn(registro);
        
        RegistroAcceso result = repository.buscarPorId(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void buscarPorId_notFound_shouldReturnNull() {
        when(em.find(RegistroAcceso.class, 999L)).thenReturn(null);
        
        RegistroAcceso result = repository.buscarPorId(999L);
        
        assertNull(result);
    }

    @Test
    void buscarPorPaciente_shouldReturnList() {
        String ci = "1.234.567-8";
        RegistroAcceso r1 = new RegistroAcceso();
        RegistroAcceso r2 = new RegistroAcceso();
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(r1, r2));
        
        List<RegistroAcceso> result = repository.buscarPorPaciente(ci);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void buscarPorProfesional_shouldReturnList() {
        String profesionalId = "PROF-001";
        RegistroAcceso r1 = new RegistroAcceso();
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(r1));
        
        List<RegistroAcceso> result = repository.buscarPorProfesional(profesionalId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void buscarPorRangoFechas_shouldReturnList() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000); // Ayer
        Date fin = new Date(); // Hoy
        RegistroAcceso r1 = new RegistroAcceso();
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(r1));
        
        List<RegistroAcceso> result = repository.buscarPorRangoFechas(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void contarAccesosPorPaciente_shouldReturnCount() {
        String ci = "1.234.567-8";
        
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(5L);
        
        Long result = repository.contarAccesosPorPaciente(ci);
        
        assertNotNull(result);
        assertEquals(5L, result);
    }

    @Test
    void buscarPorDocumento_shouldReturnList() {
        String documentoId = "DOC-123";
        RegistroAcceso r1 = new RegistroAcceso();
        RegistroAcceso r2 = new RegistroAcceso();
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("docId", documentoId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(r1, r2));
        
        List<RegistroAcceso> result = repository.buscarPorDocumento(documentoId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(typedQuery).setParameter("docId", documentoId);
    }

    @Test
    void buscarPorDocumento_noResults_shouldReturnEmptyList() {
        String documentoId = "DOC-999";
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("docId", documentoId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        List<RegistroAcceso> result = repository.buscarPorDocumento(documentoId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorPaciente_noResults_shouldReturnEmptyList() {
        String ci = "999.999.999-9";
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("paciente", ci)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        List<RegistroAcceso> result = repository.buscarPorPaciente(ci);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorProfesional_noResults_shouldReturnEmptyList() {
        String profesionalId = "PROF-999";
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("prof", profesionalId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        List<RegistroAcceso> result = repository.buscarPorProfesional(profesionalId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorRangoFechas_noResults_shouldReturnEmptyList() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        when(em.createQuery(anyString(), eq(RegistroAcceso.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("inicio", inicio)).thenReturn(typedQuery);
        when(typedQuery.setParameter("fin", fin)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        List<RegistroAcceso> result = repository.buscarPorRangoFechas(inicio, fin);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void contarAccesosPorPaciente_noAccesses_shouldReturnZero() {
        String ci = "999.999.999-9";
        
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("paciente", ci)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        
        Long result = repository.contarAccesosPorPaciente(ci);
        
        assertEquals(0L, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void obtenerAccesosPorDia_shouldReturnStatistics() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        Object[] row1 = {new Date(), 10L, 8L, 2L};
        Object[] row2 = {new Date(), 15L, 12L, 3L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter("inicio", inicio)).thenReturn(query);
        when(query.setParameter("fin", fin)).thenReturn(query);
        when(query.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerAccesosPorDia(inicio, fin);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void obtenerAccesosPorProfesional_shouldReturnStatistics() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        Object[] row1 = {"PROF-001", 20L, 18L, 2L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter("inicio", inicio)).thenReturn(query);
        when(query.setParameter("fin", fin)).thenReturn(query);
        when(query.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerAccesosPorProfesional(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void obtenerAccesosPorPaciente_shouldReturnStatistics() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        Object[] row1 = {"1.234.567-8", 25L, 23L, 2L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter("inicio", inicio)).thenReturn(query);
        when(query.setParameter("fin", fin)).thenReturn(query);
        when(query.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerAccesosPorPaciente(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void obtenerAccesosPorTipoDocumento_shouldReturnStatistics() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        Object[] row1 = {"RECETA", 30L, 28L, 2L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter("inicio", inicio)).thenReturn(query);
        when(query.setParameter("fin", fin)).thenReturn(query);
        when(query.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerAccesosPorTipoDocumento(inicio, fin);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void obtenerAccesosPorDia_noResults_shouldReturnEmptyList() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter("inicio", inicio)).thenReturn(query);
        when(query.setParameter("fin", fin)).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());
        
        List<Object[]> result = repository.obtenerAccesosPorDia(inicio, fin);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

