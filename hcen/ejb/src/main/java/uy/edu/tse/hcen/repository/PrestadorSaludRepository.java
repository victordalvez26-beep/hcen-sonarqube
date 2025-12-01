package uy.edu.tse.hcen.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.model.PrestadorSalud;

import java.util.List;

/**
 * Repositorio para gestionar Prestadores de Salud.
 */
@Stateless
public class PrestadorSaludRepository {
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    /**
     * Persiste un nuevo prestador.
     */
    public void persist(PrestadorSalud prestador) {
        em.persist(prestador);
    }
    
    /**
     * Actualiza un prestador existente.
     */
    public PrestadorSalud merge(PrestadorSalud prestador) {
        return em.merge(prestador);
    }
    
    /**
     * Busca un prestador por ID.
     */
    public PrestadorSalud findById(Long id) {
        return em.find(PrestadorSalud.class, id);
    }
    
    /**
     * Busca un prestador por RUT.
     */
    public PrestadorSalud findByRut(String rut) {
        try {
            return em.createQuery(
                "SELECT p FROM PrestadorSalud p WHERE p.rut = :rut",
                PrestadorSalud.class)
                .setParameter("rut", rut)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Busca un prestador por token de invitación.
     */
    public PrestadorSalud findByInvitationToken(String token) {
        try {
            return em.createQuery(
                "SELECT p FROM PrestadorSalud p WHERE p.invitationToken = :token",
                PrestadorSalud.class)
                .setParameter("token", token)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Busca un prestador por API Key.
     */
    public PrestadorSalud findByApiKey(String apiKey) {
        try {
            return em.createQuery(
                "SELECT p FROM PrestadorSalud p WHERE p.apiKey = :apiKey",
                PrestadorSalud.class)
                .setParameter("apiKey", apiKey)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Obtiene todos los prestadores.
     */
    public List<PrestadorSalud> findAll() {
        return em.createQuery(
            "SELECT p FROM PrestadorSalud p ORDER BY p.nombre",
            PrestadorSalud.class)
            .getResultList();
    }
    
    /**
     * Elimina un prestador.
     */
    public void remove(PrestadorSalud prestador) {
        if (!em.contains(prestador)) {
            prestador = em.merge(prestador);
        }
        em.remove(prestador);
    }
}

