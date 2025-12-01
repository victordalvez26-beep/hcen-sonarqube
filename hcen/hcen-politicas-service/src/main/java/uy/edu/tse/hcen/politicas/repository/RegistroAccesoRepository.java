package uy.edu.tse.hcen.politicas.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;

import java.util.Date;
import java.util.List;

@Stateless
public class RegistroAccesoRepository {

    @PersistenceContext(unitName = "politicasPersistenceUnit")
    private EntityManager em;

    public RegistroAcceso crear(RegistroAcceso registro) {
        em.persist(registro);
        return registro;
    }

    public RegistroAcceso buscarPorId(Long id) {
        return em.find(RegistroAcceso.class, id);
    }

    public List<RegistroAcceso> buscarPorPaciente(String codDocumPaciente) {
        return em.createQuery(
            "SELECT r FROM RegistroAcceso r WHERE r.codDocumPaciente = :paciente ORDER BY r.fecha DESC",
            RegistroAcceso.class
        )
        .setParameter("paciente", codDocumPaciente)
        .getResultList();
    }

    public List<RegistroAcceso> buscarPorProfesional(String profesionalId) {
        return em.createQuery(
            "SELECT r FROM RegistroAcceso r WHERE r.profesionalId = :prof ORDER BY r.fecha DESC",
            RegistroAcceso.class
        )
        .setParameter("prof", profesionalId)
        .getResultList();
    }

    public List<RegistroAcceso> buscarPorDocumento(String documentoId) {
        return em.createQuery(
            "SELECT r FROM RegistroAcceso r WHERE r.documentoId = :docId ORDER BY r.fecha DESC",
            RegistroAcceso.class
        )
        .setParameter("docId", documentoId)
        .getResultList();
    }

    public List<RegistroAcceso> buscarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        return em.createQuery(
            "SELECT r FROM RegistroAcceso r WHERE r.fecha BETWEEN :inicio AND :fin ORDER BY r.fecha DESC",
            RegistroAcceso.class
        )
        .setParameter("inicio", fechaInicio)
        .setParameter("fin", fechaFin)
        .getResultList();
    }

    public Long contarAccesosPorPaciente(String codDocumPaciente) {
        return em.createQuery(
            "SELECT COUNT(r) FROM RegistroAcceso r WHERE r.codDocumPaciente = :paciente",
            Long.class
        )
        .setParameter("paciente", codDocumPaciente)
        .getSingleResult();
    }

    /**
     * Obtiene estadísticas agregadas de accesos agrupadas por día en un rango de fechas.
     * Útil para reportes de evolución temporal.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> obtenerAccesosPorDia(Date fechaInicio, Date fechaFin) {
        return em.createQuery(
            "SELECT CAST(r.fecha AS DATE), COUNT(r), " +
            "SUM(CASE WHEN r.exito = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.exito = false THEN 1 ELSE 0 END) " +
            "FROM RegistroAcceso r " +
            "WHERE r.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY CAST(r.fecha AS DATE) " +
            "ORDER BY CAST(r.fecha AS DATE) ASC",
            Object[].class
        )
        .setParameter("inicio", fechaInicio)
        .setParameter("fin", fechaFin)
        .getResultList();
    }

    /**
     * Obtiene estadísticas de accesos agrupadas por profesional en un rango de fechas.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> obtenerAccesosPorProfesional(Date fechaInicio, Date fechaFin) {
        return em.createQuery(
            "SELECT r.profesionalId, COUNT(r), " +
            "SUM(CASE WHEN r.exito = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.exito = false THEN 1 ELSE 0 END) " +
            "FROM RegistroAcceso r " +
            "WHERE r.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY r.profesionalId " +
            "ORDER BY COUNT(r) DESC",
            Object[].class
        )
        .setParameter("inicio", fechaInicio)
        .setParameter("fin", fechaFin)
        .getResultList();
    }

    /**
     * Obtiene estadísticas de accesos agrupadas por paciente en un rango de fechas.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> obtenerAccesosPorPaciente(Date fechaInicio, Date fechaFin) {
        return em.createQuery(
            "SELECT r.codDocumPaciente, COUNT(r), " +
            "SUM(CASE WHEN r.exito = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.exito = false THEN 1 ELSE 0 END) " +
            "FROM RegistroAcceso r " +
            "WHERE r.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY r.codDocumPaciente " +
            "ORDER BY COUNT(r) DESC",
            Object[].class
        )
        .setParameter("inicio", fechaInicio)
        .setParameter("fin", fechaFin)
        .getResultList();
    }

    /**
     * Obtiene estadísticas de accesos agrupadas por tipo de documento en un rango de fechas.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> obtenerAccesosPorTipoDocumento(Date fechaInicio, Date fechaFin) {
        return em.createQuery(
            "SELECT r.tipoDocumento, COUNT(r), " +
            "SUM(CASE WHEN r.exito = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.exito = false THEN 1 ELSE 0 END) " +
            "FROM RegistroAcceso r " +
            "WHERE r.fecha BETWEEN :inicio AND :fin AND r.tipoDocumento IS NOT NULL " +
            "GROUP BY r.tipoDocumento " +
            "ORDER BY COUNT(r) DESC",
            Object[].class
        )
        .setParameter("inicio", fechaInicio)
        .setParameter("fin", fechaFin)
        .getResultList();
    }
}






