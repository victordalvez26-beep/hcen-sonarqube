package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que almacena las preferencias de notificaciones y el device token
 * de Firebase para cada usuario.
 */
@Entity
@Table(name = "user_notification_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = "user_uid"))
public class UserNotificationPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_uid", nullable = false, unique = true, length = 255)
    private String userUid;
    
    @Column(name = "device_token", columnDefinition = "TEXT")
    private String deviceToken;
    
    // Preferencias de notificaciones por tipo
    @Column(name = "notify_results", nullable = false)
    private boolean notifyResults = true;
    
    @Column(name = "notify_new_access_request", nullable = false)
    private boolean notifyNewAccessRequest = true;
    
    @Column(name = "notify_medical_history", nullable = false)
    private boolean notifyMedicalHistory = true;
    
    @Column(name = "notify_new_access_history", nullable = false)
    private boolean notifyNewAccessHistory = true;
    
    @Column(name = "notify_maintenance", nullable = false)
    private boolean notifyMaintenance = true;
    
    @Column(name = "notify_new_features", nullable = false)
    private boolean notifyNewFeatures = true;
    
    @Column(name = "all_disabled", nullable = false)
    private boolean allDisabled = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "device_token_updated_at")
    private LocalDateTime deviceTokenUpdatedAt;
    
    public UserNotificationPreferences() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UserNotificationPreferences(String userUid) {
        this();
        this.userUid = userUid;
    }
    
    /**
     * Verifica si un tipo de notificación está habilitado.
     * Si allDisabled es true, retorna false para todos los tipos.
     */
    public boolean isNotificationEnabled(NotificationType type) {
        if (allDisabled) {
            return false;
        }
        
        return switch (type) {
            case RESULTS -> notifyResults;
            case NEW_ACCESS_REQUEST -> notifyNewAccessRequest;
            case MEDICAL_HISTORY -> notifyMedicalHistory;
            case NEW_ACCESS_HISTORY -> notifyNewAccessHistory;
            case MAINTENANCE -> notifyMaintenance;
            case NEW_FEATURES -> notifyNewFeatures;
            case ALL_DISABLED -> false; // Este tipo no se puede habilitar
        };
    }
    
    /**
     * Actualiza el device token y marca la fecha de actualización.
     */
    public void updateDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        this.deviceTokenUpdatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserUid() {
        return userUid;
    }
    
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    
    public String getDeviceToken() {
        return deviceToken;
    }
    
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    
    public boolean isNotifyResults() {
        return notifyResults;
    }
    
    public void setNotifyResults(boolean notifyResults) {
        this.notifyResults = notifyResults;
    }
    
    public boolean isNotifyNewAccessRequest() {
        return notifyNewAccessRequest;
    }
    
    public void setNotifyNewAccessRequest(boolean notifyNewAccessRequest) {
        this.notifyNewAccessRequest = notifyNewAccessRequest;
    }
    
    public boolean isNotifyMedicalHistory() {
        return notifyMedicalHistory;
    }
    
    public void setNotifyMedicalHistory(boolean notifyMedicalHistory) {
        this.notifyMedicalHistory = notifyMedicalHistory;
    }
    
    public boolean isNotifyNewAccessHistory() {
        return notifyNewAccessHistory;
    }
    
    public void setNotifyNewAccessHistory(boolean notifyNewAccessHistory) {
        this.notifyNewAccessHistory = notifyNewAccessHistory;
    }
    
    public boolean isNotifyMaintenance() {
        return notifyMaintenance;
    }
    
    public void setNotifyMaintenance(boolean notifyMaintenance) {
        this.notifyMaintenance = notifyMaintenance;
    }
    
    public boolean isNotifyNewFeatures() {
        return notifyNewFeatures;
    }
    
    public void setNotifyNewFeatures(boolean notifyNewFeatures) {
        this.notifyNewFeatures = notifyNewFeatures;
    }
    
    public boolean isAllDisabled() {
        return allDisabled;
    }
    
    public void setAllDisabled(boolean allDisabled) {
        this.allDisabled = allDisabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getDeviceTokenUpdatedAt() {
        return deviceTokenUpdatedAt;
    }
    
    public void setDeviceTokenUpdatedAt(LocalDateTime deviceTokenUpdatedAt) {
        this.deviceTokenUpdatedAt = deviceTokenUpdatedAt;
    }
}

