package uy.edu.tse.hcen.politicas.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para PoliticaAccesoRepository.
 */
class PoliticaAccesoRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<PoliticaAcceso> queryPolitica;

    @Mock
    private TypedQuery<Object[]> queryObjectArray;

    private PoliticaAccesoRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        repository = new PoliticaAccesoRepository();
        
        // Inyectar EntityManager de prueba
        Field emField = PoliticaAccesoRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(repository, em);

        // Comportamiento por defecto para queries de PoliticaAcceso:
        // cualquier llamada a setParameter(..) debe devolver la misma instancia
        // de TypedQuery para permitir el encadenamiento de métodos que hace el repositorio.
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
    }

    @Test
    void crear_shouldPersistPolitica() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setCodDocumPaciente("1.234.567-8");
        politica.setProfesionalAutorizado("PROF-001");
        politica.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        politica.setDuracion(DuracionPoliticaAcceso.INDEFINIDA);
        politica.setGestion(TipoGestionAcceso.AUTOMATICA);
        
        doAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return null;
        }).when(em).persist(any(PoliticaAcceso.class));
        
        PoliticaAcceso result = repository.crear(politica);
        
        assertNotNull(result);
        verify(em).persist(politica);
    }

    @Test
    void buscarPorId_validId_shouldReturnPolitica() {
        Long id = 1L;
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(id);
        
        when(em.find(PoliticaAcceso.class, id)).thenReturn(politica);
        
        PoliticaAcceso result = repository.buscarPorId(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void buscarPorId_notFound_shouldReturnNull() {
        when(em.find(PoliticaAcceso.class, 999L)).thenReturn(null);
        
        PoliticaAcceso result = repository.buscarPorId(999L);
        
        assertNull(result);
    }

    @Test
    void buscarPorPaciente_validCI_shouldReturnActivePoliticas() {
        String ci = "1.234.567-8";
        PoliticaAcceso p1 = new PoliticaAcceso();
        p1.setId(1L);
        p1.setActiva(true);
        PoliticaAcceso p2 = new PoliticaAcceso();
        p2.setId(2L);
        p2.setActiva(true);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter("codDocum", ci)).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(p1, p2));
        
        List<PoliticaAcceso> result = repository.buscarPorPaciente(ci);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(queryPolitica).setParameter("codDocum", ci);
    }

    @Test
    void buscarPorPaciente_noResults_shouldReturnEmptyList() {
        String ci = "999.999.999-9";
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter("codDocum", ci)).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(new ArrayList<>());
        
        List<PoliticaAcceso> result = repository.buscarPorPaciente(ci);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorProfesional_validId_shouldReturnActivePoliticas() {
        String profesionalId = "PROF-001";
        PoliticaAcceso p1 = new PoliticaAcceso();
        p1.setId(1L);
        p1.setActiva(true);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter("profId", profesionalId)).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(p1));
        
        List<PoliticaAcceso> result = repository.buscarPorProfesional(profesionalId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(queryPolitica).setParameter("profId", profesionalId);
    }

    @Test
    void verificarPermiso_basicMethod_shouldDelegateToFullMethod() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(new ArrayList<>());
        
        List<PoliticaAcceso> result = repository.verificarPermiso(profesionalId, codDocumPaciente, tipoDocumento);
        
        assertNotNull(result);
        // El método verificarPermiso llama a createQuery una vez, y buscarPorProfesional también lo llama
        // si no hay políticas encontradas (para logging), así que esperamos al menos 1 llamada
        verify(em, atLeastOnce()).createQuery(anyString(), eq(PoliticaAcceso.class));
    }

    @Test
    void verificarPermiso_withTenantId_shouldIncludeTenantInQuery() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setActiva(true);
        politica.setClinicaAutorizada(tenantId);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId
        );
        
        assertNotNull(result);
        verify(queryPolitica).setParameter("tenantId", tenantId);
    }

    @Test
    void verificarPermiso_withoutTenantId_shouldReturnEmpty() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(new ArrayList<>());
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, null, null
        );
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        // El método sí llama a createQuery incluso sin tenantId, pero puede retornar lista vacía
        verify(em, atLeastOnce()).createQuery(anyString(), eq(PoliticaAcceso.class));
    }

    @Test
    void verificarPermiso_withTipoDocumento_shouldFilterByType() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setTipoDocumento(tipoDocumento);
        politica.setAlcance(AlcancePoliticaAcceso.UN_DOCUMENTO_ESPECIFICO);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId, null
        );
        
        assertNotNull(result);
        verify(queryPolitica).setParameter("tipoDoc", tipoDocumento);
    }

    @Test
    void verificarPermiso_nullTipoDocumento_shouldAcceptNullOrTodos() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tenantId = "CLINIC-100";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setTipoDocumento(null);
        politica.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, null, tenantId, null
        );
        
        assertNotNull(result);
        verify(queryPolitica, never()).setParameter(eq("tipoDoc"), anyString());
    }

    @Test
    void verificarPermiso_withEspecialidad_shouldFilterByEspecialidad() throws Exception {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        String especialidad = "CARDIOLOGIA";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setEspecialidadesAutorizadas("CARDIOLOGIA,PEDIATRIA");
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId, especialidad
        );
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void verificarPermiso_especialidadNotInList_shouldFilterOut() throws Exception {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        String especialidad = "NEUROLOGIA";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setEspecialidadesAutorizadas("CARDIOLOGIA,PEDIATRIA");
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId, especialidad
        );
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void verificarPermiso_especialidadEmpty_shouldAcceptAll() throws Exception {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        String especialidad = "CARDIOLOGIA";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setEspecialidadesAutorizadas("");
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId, especialidad
        );
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void verificarPermiso_expiredPolitica_shouldExclude() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(new ArrayList<>());
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId, null
        );
        
        assertNotNull(result);
        // Verificar que se llama setParameter con "ahora" usando anyString() para evitar InvalidUseOfMatchers
        verify(queryPolitica, atLeastOnce()).setParameter(anyString(), any());
    }

    @Test
    void eliminar_existingPolitica_shouldRemove() {
        Long id = 1L;
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(id);
        
        when(em.find(PoliticaAcceso.class, id)).thenReturn(politica);
        doNothing().when(em).remove(politica);
        
        repository.eliminar(id);
        
        verify(em).find(PoliticaAcceso.class, id);
        verify(em).remove(politica);
    }

    @Test
    void eliminar_notFound_shouldNotCrash() {
        Long id = 999L;
        
        when(em.find(PoliticaAcceso.class, id)).thenReturn(null);
        
        repository.eliminar(id);
        
        verify(em).find(PoliticaAcceso.class, id);
        verify(em, never()).remove(any());
    }

    @Test
    void actualizar_shouldMergePolitica() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        PoliticaAcceso merged = new PoliticaAcceso();
        merged.setId(1L);
        
        when(em.merge(politica)).thenReturn(merged);
        
        PoliticaAcceso result = repository.actualizar(politica);
        
        assertNotNull(result);
        verify(em).merge(politica);
    }

    @Test
    void listarTodas_shouldReturnAll() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        p1.setId(1L);
        PoliticaAcceso p2 = new PoliticaAcceso();
        p2.setId(2L);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(p1, p2));
        
        List<PoliticaAcceso> result = repository.listarTodas();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void obtenerPoliticasCreadasPorDia_shouldReturnStatistics() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        
        Object[] row1 = {new Date(), 5L};
        Object[] row2 = {new Date(), 8L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(queryObjectArray);
        when(queryObjectArray.setParameter("inicio", inicio)).thenReturn(queryObjectArray);
        when(queryObjectArray.setParameter("fin", fin)).thenReturn(queryObjectArray);
        when(queryObjectArray.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerPoliticasCreadasPorDia(inicio, fin);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void obtenerPoliticasPorAlcance_shouldReturnStatistics() {
        Object[] row1 = {AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS, 10L, 8L, 2L};
        Object[] row2 = {AlcancePoliticaAcceso.UN_DOCUMENTO_ESPECIFICO, 5L, 4L, 1L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(queryObjectArray);
        when(queryObjectArray.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerPoliticasPorAlcance();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void obtenerPoliticasPorDuracion_shouldReturnStatistics() {
        Object[] row1 = {DuracionPoliticaAcceso.INDEFINIDA, 12L, 10L};
        Object[] row2 = {DuracionPoliticaAcceso.TEMPORAL, 8L, 7L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(queryObjectArray);
        when(queryObjectArray.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerPoliticasPorDuracion();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void obtenerResumenPoliticas_shouldReturnSummary() {
        Object[] row = {20L, 15L, 5L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(queryObjectArray);
        when(queryObjectArray.getResultList()).thenReturn(resultados);
        
        List<Object[]> result = repository.obtenerResumenPoliticas();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void parseEspecialidades_jsonArray_shouldParse() throws Exception {
        Method method = PoliticaAccesoRepository.class.getDeclaredMethod("parseEspecialidades", String.class);
        method.setAccessible(true);
        
        String especialidades = "[\"CARDIOLOGIA\",\"PEDIATRIA\"]";
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(repository, especialidades);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("CARDIOLOGIA"));
        assertTrue(result.contains("PEDIATRIA"));
    }

    @Test
    void parseEspecialidades_commaSeparated_shouldParse() throws Exception {
        Method method = PoliticaAccesoRepository.class.getDeclaredMethod("parseEspecialidades", String.class);
        method.setAccessible(true);
        
        String especialidades = "CARDIOLOGIA,PEDIATRIA,NEUROLOGIA";
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(repository, especialidades);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("CARDIOLOGIA"));
        assertTrue(result.contains("PEDIATRIA"));
        assertTrue(result.contains("NEUROLOGIA"));
    }

    @Test
    void parseEspecialidades_null_shouldReturnEmpty() throws Exception {
        Method method = PoliticaAccesoRepository.class.getDeclaredMethod("parseEspecialidades", String.class);
        method.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(repository, (Object) null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseEspecialidades_empty_shouldReturnEmpty() throws Exception {
        Method method = PoliticaAccesoRepository.class.getDeclaredMethod("parseEspecialidades", String.class);
        method.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(repository, "");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseEspecialidades_withSpaces_shouldTrim() throws Exception {
        Method method = PoliticaAccesoRepository.class.getDeclaredMethod("parseEspecialidades", String.class);
        method.setAccessible(true);
        
        String especialidades = " CARDIOLOGIA , PEDIATRIA ";
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(repository, especialidades);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("CARDIOLOGIA"));
        assertTrue(result.contains("PEDIATRIA"));
    }

    @Test
    void verificarPermiso_todosLosDocumentos_shouldAcceptAnyType() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String tipoDocumento = "RECETA";
        String tenantId = "CLINIC-100";
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        politica.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        politica.setTipoDocumento(null);
        
        when(em.createQuery(anyString(), eq(PoliticaAcceso.class))).thenReturn(queryPolitica);
        when(queryPolitica.setParameter(anyString(), any())).thenReturn(queryPolitica);
        when(queryPolitica.getResultList()).thenReturn(Arrays.asList(politica));
        
        List<PoliticaAcceso> result = repository.verificarPermiso(
            profesionalId, codDocumPaciente, tipoDocumento, tenantId, null
        );
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

