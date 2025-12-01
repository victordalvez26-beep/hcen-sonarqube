package uy.edu.tse.hcen.politicas.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;
import uy.edu.tse.hcen.common.enumerations.EstadoSolicitudAcceso;

import java.util.List;

@Stateless
public class SolicitudAccesoRepository {

    @PersistenceContext(unitName = "politicasPersistenceUnit")
    private EntityManager em;

    public SolicitudAcceso crear(SolicitudAcceso solicitud) {
        em.persist(solicitud);
        return solicitud;
    }

    public SolicitudAcceso buscarPorId(Long id) {
        return em.find(SolicitudAcceso.class, id);
    }

    public List<SolicitudAcceso> buscarPendientes() {
        return em.createQuery(
            "SELECT s FROM SolicitudAcceso s WHERE s.estado = :estado ORDER BY s.fechaSolicitud DESC",
            SolicitudAcceso.class
        )
        .setParameter("estado", EstadoSolicitudAcceso.PENDIENTE)
        .getResultList();
    }

    public List<SolicitudAcceso> buscarPorPaciente(String codDocumPaciente) {
        return em.createQuery(
            "SELECT s FROM SolicitudAcceso s WHERE s.codDocumPaciente = :paciente ORDER BY s.fechaSolicitud DESC",
            SolicitudAcceso.class
        )
        .setParameter("paciente", codDocumPaciente)
        .getResultList();
    }

    public List<SolicitudAcceso> buscarPorProfesional(String profesionalId) {
        return em.createQuery(
            "SELECT s FROM SolicitudAcceso s WHERE s.solicitanteId = :prof ORDER BY s.fechaSolicitud DESC",
            SolicitudAcceso.class
        )
        .setParameter("prof", profesionalId)
        .getResultList();
    }

    public SolicitudAcceso actualizar(SolicitudAcceso solicitud) {
        return em.merge(solicitud);
    }

    public List<SolicitudAcceso> buscarPendientesPorPaciente(String codDocumPaciente) {
        return em.createQuery(
            "SELECT s FROM SolicitudAcceso s WHERE s.codDocumPaciente = :paciente AND s.estado = :estado ORDER BY s.fechaSolicitud DESC",
            SolicitudAcceso.class
        )
        .setParameter("paciente", codDocumPaciente)
        .setParameter("estado", EstadoSolicitudAcceso.PENDIENTE)
        .getResultList();
    }
}










