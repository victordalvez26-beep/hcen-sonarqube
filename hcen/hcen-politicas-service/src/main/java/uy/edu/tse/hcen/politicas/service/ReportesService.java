package uy.edu.tse.hcen.politicas.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.edu.tse.hcen.politicas.repository.RegistroAccesoRepository;
import uy.edu.tse.hcen.politicas.repository.PoliticaAccesoRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes y análisis agregados de políticas de acceso y registros de acceso.
 */
@Stateless
public class ReportesService {

    @Inject
    private RegistroAccesoRepository registroAccesoRepository;

    @Inject
    private PoliticaAccesoRepository politicaAccesoRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Obtiene la evolución de accesos por día en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de mapas con fecha, total, exitosos y denegados
     */
    public List<Map<String, Object>> obtenerEvolucionAccesos(Date fechaInicio, Date fechaFin) {
        List<Object[]> resultados = registroAccesoRepository.obtenerAccesosPorDia(fechaInicio, fechaFin);
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            Date fecha = row[0] instanceof Date ? (Date) row[0] : null;
            Long total = row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            Long exitosos = row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L;
            Long denegados = row[3] != null && row[3] instanceof Number ? ((Number) row[3]).longValue() : 0L;
            
            item.put("fecha", fecha != null ? DATE_FORMAT.format(fecha) : "N/A");
            item.put("total", total);
            item.put("exitosos", exitosos);
            item.put("denegados", denegados);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de accesos por profesional.
     */
    public List<Map<String, Object>> obtenerAccesosPorProfesional(Date fechaInicio, Date fechaFin) {
        List<Object[]> resultados = registroAccesoRepository.obtenerAccesosPorProfesional(fechaInicio, fechaFin);
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("profesionalId", row[0] != null ? row[0].toString() : "N/A");
            item.put("total", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            item.put("exitosos", row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L);
            item.put("denegados", row[3] != null && row[3] instanceof Number ? ((Number) row[3]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de accesos por paciente.
     */
    public List<Map<String, Object>> obtenerAccesosPorPaciente(Date fechaInicio, Date fechaFin) {
        List<Object[]> resultados = registroAccesoRepository.obtenerAccesosPorPaciente(fechaInicio, fechaFin);
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("codDocumPaciente", row[0] != null ? row[0].toString() : "N/A");
            item.put("total", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            item.put("exitosos", row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L);
            item.put("denegados", row[3] != null && row[3] instanceof Number ? ((Number) row[3]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de accesos por tipo de documento.
     */
    public List<Map<String, Object>> obtenerAccesosPorTipoDocumento(Date fechaInicio, Date fechaFin) {
        List<Object[]> resultados = registroAccesoRepository.obtenerAccesosPorTipoDocumento(fechaInicio, fechaFin);
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("tipoDocumento", row[0] != null ? row[0].toString() : "N/A");
            item.put("total", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            item.put("exitosos", row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L);
            item.put("denegados", row[3] != null && row[3] instanceof Number ? ((Number) row[3]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene la evolución de políticas creadas por día.
     */
    public List<Map<String, Object>> obtenerEvolucionPoliticas(Date fechaInicio, Date fechaFin) {
        List<Object[]> resultados = politicaAccesoRepository.obtenerPoliticasCreadasPorDia(fechaInicio, fechaFin);
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            Date fecha = row[0] instanceof Date ? (Date) row[0] : null;
            Long total = row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            
            item.put("fecha", fecha != null ? DATE_FORMAT.format(fecha) : "N/A");
            item.put("total", total);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de políticas por alcance.
     */
    public List<Map<String, Object>> obtenerPoliticasPorAlcance() {
        List<Object[]> resultados = politicaAccesoRepository.obtenerPoliticasPorAlcance();
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("alcance", row[0] != null ? row[0].toString() : "N/A");
            item.put("total", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            item.put("activas", row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L);
            item.put("inactivas", row[3] != null && row[3] instanceof Number ? ((Number) row[3]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de políticas por duración.
     */
    public List<Map<String, Object>> obtenerPoliticasPorDuracion() {
        List<Object[]> resultados = politicaAccesoRepository.obtenerPoliticasPorDuracion();
        
        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("duracion", row[0] != null ? row[0].toString() : "N/A");
            item.put("total", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            item.put("activas", row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene un resumen general de políticas.
     */
    public Map<String, Object> obtenerResumenPoliticas() {
        List<Object[]> resultados = politicaAccesoRepository.obtenerResumenPoliticas();
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("total", 0L);
        resumen.put("activas", 0L);
        resumen.put("inactivas", 0L);
        
        if (resultados.isEmpty() || resultados.get(0) == null) {
            return resumen;
        }
        
        Object[] row = resultados.get(0);
        if (row != null) {
            resumen.put("total", row[0] != null && row[0] instanceof Number ? ((Number) row[0]).longValue() : 0L);
            resumen.put("activas", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            resumen.put("inactivas", row[2] != null && row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L);
        }
        return resumen;
    }
}





