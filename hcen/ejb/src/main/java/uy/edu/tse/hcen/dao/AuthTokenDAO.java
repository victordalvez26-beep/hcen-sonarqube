package uy.edu.tse.hcen.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import uy.edu.tse.hcen.model.AuthToken;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class AuthTokenDAO {
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    public void save(AuthToken authToken) {
        em.persist(authToken);
    }
    
    public AuthToken findByToken(String token) {
        TypedQuery<AuthToken> query = em.createQuery(
            "SELECT a FROM AuthToken a WHERE a.token = :token",
            AuthToken.class
        );
        query.setParameter("token", token);
        List<AuthToken> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    public void markAsUsed(String token) {
        AuthToken authToken = findByToken(token);
        if (authToken != null) {
            authToken.setUsed(true);
            em.merge(authToken);
        }
    }
    
    public void deleteExpiredTokens() {
        TypedQuery<AuthToken> query = em.createQuery(
            "SELECT a FROM AuthToken a WHERE a.expiresAt < :now",
            AuthToken.class
        );
        query.setParameter("now", LocalDateTime.now());
        List<AuthToken> expired = query.getResultList();
        for (AuthToken token : expired) {
            em.remove(token);
        }
    }
    
    public void delete(AuthToken authToken) {
        if (!em.contains(authToken)) {
            authToken = em.merge(authToken);
        }
        em.remove(authToken);
    }
}

