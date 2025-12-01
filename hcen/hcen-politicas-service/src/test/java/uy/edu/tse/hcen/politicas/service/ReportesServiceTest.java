package uy.edu.tse.hcen.politicas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.repository.PoliticaAccesoRepository;
import uy.edu.tse.hcen.politicas.repository.RegistroAccesoRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ReportesService.
 */
class ReportesServiceTest {

    @Mock
    private RegistroAccesoRepository registroAccesoRepository;

    @Mock
    private PoliticaAccesoRepository politicaAccesoRepository;

    @InjectMocks
    private ReportesService reportesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerEvolucionAccesos_validDates_shouldReturnList() {
        Date fechaInicio = new Date(System.currentTimeMillis() - 86400000); // Ayer
        Date fechaFin = new Date();
        
        Object[] row1 = {new Date(), 10L, 8L, 2L};
        Object[] row2 = {new Date(), 15L, 12L, 3L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(registroAccesoRepository.obtenerAccesosPorDia(fechaInicio, fechaFin)).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerEvolucionAccesos(fechaInicio, fechaFin);
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(registroAccesoRepository).obtenerAccesosPorDia(fechaInicio, fechaFin);
    }

    @Test
    void obtenerAccesosPorProfesional_validDates_shouldReturnList() {
        Date fechaInicio = new Date(System.currentTimeMillis() - 86400000);
        Date fechaFin = new Date();
        
        Object[] row1 = {"prof1", 20L, 18L, 2L};
        Object[] row2 = {"prof2", 15L, 14L, 1L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(registroAccesoRepository.obtenerAccesosPorProfesional(fechaInicio, fechaFin)).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerAccesosPorProfesional(fechaInicio, fechaFin);
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("prof1", resultado.get(0).get("profesionalId"));
        assertEquals(20L, resultado.get(0).get("total"));
    }

    @Test
    void obtenerAccesosPorPaciente_validDates_shouldReturnList() {
        Date fechaInicio = new Date(System.currentTimeMillis() - 86400000);
        Date fechaFin = new Date();
        
        Object[] row1 = {"12345678", 25L, 23L, 2L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        
        when(registroAccesoRepository.obtenerAccesosPorPaciente(fechaInicio, fechaFin)).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerAccesosPorPaciente(fechaInicio, fechaFin);
        
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("12345678", resultado.get(0).get("codDocumPaciente"));
    }

    @Test
    void obtenerAccesosPorTipoDocumento_validDates_shouldReturnList() {
        Date fechaInicio = new Date(System.currentTimeMillis() - 86400000);
        Date fechaFin = new Date();
        
        Object[] row1 = {"RECETA", 30L, 28L, 2L};
        Object[] row2 = {"LABORATORIO", 20L, 19L, 1L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(registroAccesoRepository.obtenerAccesosPorTipoDocumento(fechaInicio, fechaFin)).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerAccesosPorTipoDocumento(fechaInicio, fechaFin);
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void obtenerEvolucionPoliticas_validDates_shouldReturnList() {
        Date fechaInicio = new Date(System.currentTimeMillis() - 86400000);
        Date fechaFin = new Date();
        
        Object[] row1 = {new Date(), 5L};
        Object[] row2 = {new Date(), 8L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(politicaAccesoRepository.obtenerPoliticasCreadasPorDia(fechaInicio, fechaFin)).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerEvolucionPoliticas(fechaInicio, fechaFin);
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(5L, resultado.get(0).get("total"));
    }

    @Test
    void obtenerPoliticasPorAlcance_shouldReturnList() {
        Object[] row1 = {"TODOS_LOS_DOCUMENTOS", 10L, 8L, 2L};
        Object[] row2 = {"DOCUMENTO_ESPECIFICO", 5L, 4L, 1L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(politicaAccesoRepository.obtenerPoliticasPorAlcance()).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerPoliticasPorAlcance();
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("TODOS_LOS_DOCUMENTOS", resultado.get(0).get("alcance"));
    }

    @Test
    void obtenerPoliticasPorDuracion_shouldReturnList() {
        Object[] row1 = {"INDEFINIDA", 12L, 10L};
        Object[] row2 = {"TEMPORAL", 8L, 7L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row1);
        resultados.add(row2);
        
        when(politicaAccesoRepository.obtenerPoliticasPorDuracion()).thenReturn(resultados);
        
        List<Map<String, Object>> resultado = reportesService.obtenerPoliticasPorDuracion();
        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void obtenerResumenPoliticas_shouldReturnMap() {
        Object[] row = {20L, 15L, 5L};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(politicaAccesoRepository.obtenerResumenPoliticas()).thenReturn(resultados);
        
        Map<String, Object> resultado = reportesService.obtenerResumenPoliticas();
        
        assertNotNull(resultado);
        assertEquals(20L, resultado.get("total"));
        assertEquals(15L, resultado.get("activas"));
        assertEquals(5L, resultado.get("inactivas"));
    }

    @Test
    void obtenerResumenPoliticas_emptyResult_shouldReturnZeros() {
        when(politicaAccesoRepository.obtenerResumenPoliticas()).thenReturn(Collections.emptyList());
        
        Map<String, Object> resultado = reportesService.obtenerResumenPoliticas();
        
        assertNotNull(resultado);
        assertEquals(0L, resultado.get("total"));
        assertEquals(0L, resultado.get("activas"));
        assertEquals(0L, resultado.get("inactivas"));
    }

    @Test
    void obtenerResumenPoliticas_nullResult_shouldReturnZeros() {
        Object[] row = {null, null, null};
        List<Object[]> resultados = new ArrayList<>();
        resultados.add(row);
        
        when(politicaAccesoRepository.obtenerResumenPoliticas()).thenReturn(resultados);
        
        Map<String, Object> resultado = reportesService.obtenerResumenPoliticas();
        
        assertNotNull(resultado);
        assertEquals(0L, resultado.get("total"));
        assertEquals(0L, resultado.get("activas"));
        assertEquals(0L, resultado.get("inactivas"));
    }
}
