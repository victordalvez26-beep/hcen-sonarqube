package uy.edu.tse.hcen.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.model.UserNotificationPreferences;

@Stateless
public class UserNotificationPreferencesRepository {
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    /**
     * Busca las preferencias de notificaciones de un usuario por su UID.
     */
    public UserNotificationPreferences findByUserUid(String userUid) {
        try {
            return em.createQuery(
                "SELECT p FROM UserNotificationPreferences p WHERE p.userUid = :userUid",
                UserNotificationPreferences.class)
                .setParameter("userUid", userUid)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Crea o actualiza las preferencias de notificaciones.
     */
    public UserNotificationPreferences saveOrUpdate(UserNotificationPreferences preferences) {
        UserNotificationPreferences existing = findByUserUid(preferences.getUserUid());
        if (existing != null) {
            // Actualizar campos
            existing.setNotifyResults(preferences.isNotifyResults());
            existing.setNotifyNewAccessRequest(preferences.isNotifyNewAccessRequest());
            existing.setNotifyMedicalHistory(preferences.isNotifyMedicalHistory());
            existing.setNotifyNewAccessHistory(preferences.isNotifyNewAccessHistory());
            existing.setNotifyMaintenance(preferences.isNotifyMaintenance());
            existing.setNotifyNewFeatures(preferences.isNotifyNewFeatures());
            existing.setAllDisabled(preferences.isAllDisabled());
            
            // Si se proporciona un nuevo device token, actualizarlo
            if (preferences.getDeviceToken() != null && !preferences.getDeviceToken().isEmpty()) {
                existing.updateDeviceToken(preferences.getDeviceToken());
            }
            
            return em.merge(existing);
        } else {
            em.persist(preferences);
            return preferences;
        }
    }
    
    /**
     * Actualiza solo el device token de un usuario.
     */
    public UserNotificationPreferences updateDeviceToken(String userUid, String deviceToken) {
        UserNotificationPreferences preferences = findByUserUid(userUid);
        if (preferences == null) {
            // Crear nuevas preferencias si no existen
            preferences = new UserNotificationPreferences(userUid);
            preferences.updateDeviceToken(deviceToken);
            em.persist(preferences);
        } else {
            preferences.updateDeviceToken(deviceToken);
            em.merge(preferences);
        }
        return preferences;
    }
    
    /**
     * Persiste una nueva entidad.
     */
    public UserNotificationPreferences persist(UserNotificationPreferences preferences) {
        em.persist(preferences);
        return preferences;
    }
    
    /**
     * Actualiza una entidad existente.
     */
    public UserNotificationPreferences merge(UserNotificationPreferences preferences) {
        return em.merge(preferences);
    }
}

