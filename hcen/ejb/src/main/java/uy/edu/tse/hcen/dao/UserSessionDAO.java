package uy.edu.tse.hcen.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.model.UserSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class UserSessionDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserSessionDAO.class.getName());
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    // Guarda una nueva sesión
    public UserSession save(UserSession session) {
        em.persist(session);
        LOGGER.info("Sesión creada para usuario: " + session.getUserUid());
        return session;
    }
    
    // Busca una sesión por token JWT
    public UserSession findByJwtToken(String jwtToken) {
        try {
            return em.createQuery(
                    "SELECT s FROM UserSession s WHERE s.jwtToken = :jwtToken", 
                    UserSession.class)
                    .setParameter("jwtToken", jwtToken)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Busca todas las sesiones activas de un usuario
    public List<UserSession> findActiveByUserUid(String userUid) {
        return em.createQuery(
                "SELECT s FROM UserSession s WHERE s.userUid = :userUid AND s.expiresAt > :now", 
                UserSession.class)
                .setParameter("userUid", userUid)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }
    
    // Elimina una sesión por token JWT
    public boolean deleteByJwtToken(String jwtToken) {
        try {
            UserSession session = findByJwtToken(jwtToken);
            if (session != null) {
                em.remove(session);
                LOGGER.info("Sesión eliminada para usuario: " + session.getUserUid());
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.severe("Error eliminando sesión: " + e.getMessage());
            return false;
        }
    }
    

    // Elimina todas las sesiones de un usuario
    public int deleteAllByUserUid(String userUid) {
        return em.createQuery("DELETE FROM UserSession s WHERE s.userUid = :userUid")
                .setParameter("userUid", userUid)
                .executeUpdate();
    }
    

    // Limpia sesiones expiradas (para ejecutar periódicamente)
    public int cleanExpiredSessions() {
        int deleted = em.createQuery("DELETE FROM UserSession s WHERE s.expiresAt < :now")
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
        LOGGER.info("Sesiones expiradas eliminadas: " + deleted);
        return deleted;
    }
    

    // Actualiza el access token de una sesión (cuando se renueva con refresh token)
    public void updateAccessToken(String jwtToken, String newAccessToken) {
        UserSession session = findByJwtToken(jwtToken);
        if (session != null) {
            session.setAccessToken(newAccessToken);
            em.merge(session);
            LOGGER.info("Access token actualizado para sesión");
        }
    }
}

