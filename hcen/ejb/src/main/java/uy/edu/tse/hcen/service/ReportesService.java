package uy.edu.tse.hcen.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.User;

/**
 * Servicio para generar reportes y análisis agregados de documentos y usuarios dentro del HCEN.
 */
@Stateless
public class ReportesService {

    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager entityManager;

    @EJB
    private UserDAO userDAO;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Obtiene la evolución de documentos creados por día en un rango de fechas.
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Lista de mapas con fecha y total de documentos
     */
    public List<Map<String, Object>> obtenerEvolucionDocumentos(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now().minusDays(6);
        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

        java.sql.Date inicioSql = java.sql.Date.valueOf(inicio);
        java.sql.Date finSql = java.sql.Date.valueOf(fin);

        @SuppressWarnings("unchecked")
        List<Object[]> filas = entityManager.createNativeQuery(
                        "SELECT fecha_creacion, COUNT(*) " +
                                "FROM metadata_documento " +
                                "WHERE fecha_creacion BETWEEN :inicio AND :fin " +
                                "GROUP BY fecha_creacion " +
                                "ORDER BY fecha_creacion")
                .setParameter("inicio", inicioSql)
                .setParameter("fin", finSql)
                .getResultList();

        return filas.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            LocalDate fecha = null;
            if (row[0] instanceof java.sql.Date) {
                fecha = ((java.sql.Date) row[0]).toLocalDate();
            } else if (row[0] instanceof java.sql.Timestamp) {
                fecha = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            }
            item.put("fecha", fecha != null ? fecha.format(DATE_FORMATTER) : "N/A");
            item.put("total", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de documentos agrupadas por especialidad.
     */
    public List<Map<String, Object>> obtenerDocumentosPorEspecialidad() {
        @SuppressWarnings("unchecked")
        List<Object[]> resultados = entityManager.createNativeQuery(
                        "SELECT COALESCE(tipo_documento, 'SIN_TIPO') AS categoria, COUNT(*) " +
                                "FROM metadata_documento " +
                                "GROUP BY COALESCE(tipo_documento, 'SIN_TIPO') " +
                                "ORDER BY COUNT(*) DESC")
                .getResultList();

        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("especialidad", row[0] != null ? row[0].toString() : "Sin información");
            item.put("total", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de documentos agrupadas por formato.
     */
    public List<Map<String, Object>> obtenerDocumentosPorFormato() {
        @SuppressWarnings("unchecked")
        List<Object[]> resultados = entityManager.createNativeQuery(
                        "SELECT COALESCE(formato_documento, 'DESCONOCIDO') AS formato, COUNT(*) " +
                                "FROM metadata_documento " +
                                "GROUP BY COALESCE(formato_documento, 'DESCONOCIDO') " +
                                "ORDER BY COUNT(*) DESC")
                .getResultList();

        return resultados.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("formato", row[0] != null ? row[0].toString() : "Sin información");
            item.put("total", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de documentos agrupadas por tenant (clínica de origen).
     */
    public List<Map<String, Object>> obtenerDocumentosPorTenant() {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> resultados = entityManager.createNativeQuery(
                            "SELECT COALESCE(CAST(tenant_id AS VARCHAR), 'SIN_TENANT') AS tenant, COUNT(*) " +
                                    "FROM metadata_documento " +
                                    "GROUP BY COALESCE(tenant_id, 0) " +
                                    "ORDER BY COUNT(*) DESC")
                    .getResultList();

            return resultados.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                item.put("tenantId", row[0] != null ? row[0].toString() : "Sin información");
                item.put("total", row[1] != null && row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            // Si hay un error (por ejemplo, la tabla no existe o no hay datos), devolver lista vacía
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Obtiene un resumen general de los documentos almacenados.
     */
    public Map<String, Object> obtenerResumenDocumentos() {
        @SuppressWarnings("unchecked")
        List<Object[]> resultados = entityManager.createNativeQuery(
                        "SELECT COUNT(*), " +
                                "SUM(CASE WHEN restringido = true THEN 1 ELSE 0 END) " +
                                "FROM metadata_documento")
                .getResultList();

        Map<String, Object> resumen = new HashMap<>();
        if (resultados.isEmpty() || resultados.get(0) == null) {
            resumen.put("total", 0L);
            resumen.put("breakingTheGlass", 0L);
            resumen.put("noBreakingTheGlass", 0L);
            return resumen;
        }

        Object[] row = resultados.get(0);
        Long total = row[0] != null ? ((Number) row[0]).longValue() : 0L;
        Long breakingTheGlass = row[1] != null ? ((Number) row[1]).longValue() : 0L;
        Long noBreakingTheGlass = total - breakingTheGlass;

        resumen.put("total", total);
        resumen.put("breakingTheGlass", breakingTheGlass);
        resumen.put("noBreakingTheGlass", noBreakingTheGlass);
        return resumen;
    }

    /**
     * Obtiene un resumen de usuarios por rol y perfil completado.
     */
    public Map<String, Object> obtenerResumenUsuarios() {
        Map<String, Object> resumen = new HashMap<>();
        long total = userDAO.countUsuarios();
        Map<String, Long> porRol = userDAO.countUsuariosPorRol();
        long profesionales = porRol.getOrDefault("US", 0L);
        long administradores = porRol.getOrDefault("AD", 0L);
        long perfilesCompletos = userDAO.countUsuariosConPerfilCompleto(true);

        resumen.put("totalUsuarios", total);
        resumen.put("porRol", porRol);
        resumen.put("profesionales", profesionales);
        resumen.put("administradores", administradores);
        resumen.put("otros", total - (profesionales + administradores));
        resumen.put("perfilesCompletos", perfilesCompletos);
        resumen.put("perfilesPendientes", total - perfilesCompletos);
        return resumen;
    }

    /**
     * Obtiene el detalle de los profesionales (usuarios de la salud) registrados en el sistema.
     */
    public List<Map<String, Object>> obtenerProfesionalesDetalle() {
        List<User> profesionales = userDAO.findByRol("US");

        return profesionales.stream().map(user -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("uid", user.getUid());
            item.put("email", user.getEmail());
            item.put("documento", user.getCodDocum());
            item.put("nombreCompleto", construirNombreCompleto(user));
            item.put("departamento", user.getDepartamento() != null ? user.getDepartamento().getNombre() : "");
            item.put("localidad", user.getLocalidad() != null ? user.getLocalidad() : "");
            item.put("perfilCompleto", user.isProfileCompleted());
            return item;
        }).collect(Collectors.toList());
    }

    private String construirNombreCompleto(User user) {
        StringBuilder builder = new StringBuilder();
        if (user.getPrimerNombre() != null) {
            builder.append(user.getPrimerNombre()).append(" ");
        }
        if (user.getSegundoNombre() != null) {
            builder.append(user.getSegundoNombre()).append(" ");
        }
        if (user.getPrimerApellido() != null) {
            builder.append(user.getPrimerApellido()).append(" ");
        }
        if (user.getSegundoApellido() != null) {
            builder.append(user.getSegundoApellido());
        }
        return builder.toString().trim();
    }
}


