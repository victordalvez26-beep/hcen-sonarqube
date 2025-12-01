package uy.edu.tse.hcen.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.model.UserClinicAssociation;

import java.util.List;

/**
 * Repositorio para gestionar las asociaciones entre usuarios y clínicas.
 */
@Stateless
public class UserClinicAssociationRepository {
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    /**
     * Busca una asociación específica entre un usuario y una clínica.
     * 
     * @param userId ID del usuario
     * @param clinicTenantId ID del tenant de la clínica
     * @return La asociación si existe, null en caso contrario
     */
    public UserClinicAssociation findByUserAndClinic(Long userId, Long clinicTenantId) {
        try {
            return em.createQuery(
                "SELECT u FROM UserClinicAssociation u " +
                "WHERE u.userId = :userId AND u.clinicTenantId = :clinicId",
                UserClinicAssociation.class)
                .setParameter("userId", userId)
                .setParameter("clinicId", clinicTenantId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Obtiene todas las clínicas asociadas a un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de IDs de clínicas asociadas
     */
    public List<Long> findClinicsByUser(Long userId) {
        return em.createQuery(
            "SELECT u.clinicTenantId FROM UserClinicAssociation u WHERE u.userId = :userId",
            Long.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    /**
     * Obtiene todas las asociaciones de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de asociaciones
     */
    public List<UserClinicAssociation> findByUser(Long userId) {
        return em.createQuery(
            "SELECT u FROM UserClinicAssociation u WHERE u.userId = :userId",
            UserClinicAssociation.class)
            .setParameter("userId", userId)
            .getResultList();
    }
    
    /**
     * Obtiene todos los usuarios asociados a una clínica.
     * 
     * @param clinicTenantId ID del tenant de la clínica
     * @return Lista de IDs de usuarios asociados
     */
    public List<Long> findUsersByClinic(Long clinicTenantId) {
        return em.createQuery(
            "SELECT u.userId FROM UserClinicAssociation u WHERE u.clinicTenantId = :clinicId",
            Long.class)
            .setParameter("clinicId", clinicTenantId)
            .getResultList();
    }
    
    /**
     * Persiste una nueva asociación.
     * 
     * @param association La asociación a persistir
     */
    public void persist(UserClinicAssociation association) {
        em.persist(association);
    }
    
    /**
     * Elimina una asociación.
     * 
     * @param association La asociación a eliminar
     */
    public void remove(UserClinicAssociation association) {
        if (!em.contains(association)) {
            association = em.merge(association);
        }
        em.remove(association);
    }
}

